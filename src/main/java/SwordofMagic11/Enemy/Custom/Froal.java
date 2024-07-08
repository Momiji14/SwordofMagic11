package SwordofMagic11.Enemy.Custom;

import SwordofMagic11.Component.SomParticle;
import SwordofMagic11.Component.SomRay;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Component.SomTask;
import SwordofMagic11.Custom.CustomLocation;
import SwordofMagic11.Enemy.MobData;
import SwordofMagic11.Entity.Damage;
import SwordofMagic11.Entity.SearchEntity;
import SwordofMagic11.Entity.SomEffect;
import SwordofMagic11.Entity.SomEntity;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.SomCore;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static SwordofMagic11.Component.Function.randomDouble;

public class Froal extends CustomData {

    public Froal(MobData mobData, int level, Location location) {
        super(mobData, level, location);
        pivot = getLocation();
    }

    //    Radius = 40くらい
    private final CustomLocation pivot;

    @Override
    public void tick() {
        firePillar--;
        if (!lavaGround) lavaGroundCT--;

        if (firePillar <= 0) FirePillar();
        if (!lavaGround && lavaGroundCT <= 0) LavaGround();

        if (fireOfEnthusiasm && healthPercent() <= 0.9) FireOfEnthusiasm();
        if (lavaGround && healthPercent() <= 0.5) LavaGround();
        if (solarFlare && healthPercent() <= 0.3) SolarFlare();
    }



    private int firePillar = 20;
    private final int firePillarRadius = 10;
    private final int firePillarDuration = 10;//秒
    private final int firePillarPredictWait = 20;
    public void FirePillar(){
        firePillar = 20;

        SomTask.async(() -> {
            int skillRadius = firePillarRadius;

            //パーティクル指定
            final SomParticle predictCircleParticle = new SomParticle(Color.RED, this);
            final SomParticle damageCircleParticle = new SomParticle(Particle.LAVA, this);


            final List<CustomLocation> locations = new ArrayList<>();
            //座標取得
            for (PlayerData viewer: PlayerData.getPlayerList(getWorld())){
                if (viewer.isDeath()) continue;
                locations.add(viewer.getLocation());
                //予測円
                predictCircleParticle.circle(viewer.getLocation(), skillRadius, 16);
            }

            SomTask.asyncDelay(() -> {
                //該当座標に
                for (CustomLocation location : locations){
                    location.setY(-59);
                    //魔法陣生成
                    SomTask.skillTaskCount(() -> {
                        damageCircleParticle.circleFill(location, skillRadius, 8);
                        predictCircleParticle.circle(location, skillRadius, 16);
                        SomSound.Fire.radius(location, 64);
                        for (SomEntity entity : SearchEntity.nearSomEntity(enemies(), location, skillRadius)) {
                            Damage.makeDamage(this, entity, Damage.Type.Magic, 0.25);
                        }
                    }, 10, firePillarDuration * 20, this);
                }
            }, firePillarPredictWait);
        });

    }

    private boolean fireOfEnthusiasm = true;
    private final String fireOfEnthusiasmMessage = "§c「信条を恨んだ」"; //{"§c「諦めることはなかった」", "§c「信念を捨てた」", };
    private final int fireOfEnthusiasmDeg = 6;//perTick(Speed)
    private final int fireOfEnthusiasmDuration = 15;//秒
    private int deg = 0;

    public void FireOfEnthusiasm(){
        fireOfEnthusiasm = false;
        sendMessage(fireOfEnthusiasmMessage);

        SomTask.asyncDelay(()->{
            final SomParticle predictParticle = new SomParticle(Color.RED, this);
            final SomParticle fireParticle = new SomParticle(Particle.SOUL_FIRE_FLAME, this);

            deg = (int) (getEyeLocation().getYaw() % 360);

            SomTask.asyncCount(() -> {
                //座標取得
                CustomLocation location = getEyeLocation();
                location.setPitch(0);

                for (int j = 0; j < 3; j++){
                    int degForRay = (deg + (120 * j)) - 180;
                    location.setYaw(degForRay);
                    SomRay ray = SomRay.rayLocationEntity(location, 64, 0.5, enemies(), true);
                    fireParticle.line(location, 64, 1.5, 5);
                    //ダメージ判定
                    for (SomEntity entity : ray.getHitEntities()){
                        Damage.makeDamage(this, entity, Damage.Type.Magic, 1);
                        SomSound.Lava.play(entity);
                    }
                }
                //角度追加
                deg += fireOfEnthusiasmDeg*2;
            }, 5, fireOfEnthusiasmDuration*10, this);
        },30);
    }



    private boolean lavaGround = true;
    private int lavaGroundCT = 20;
    private final int lavaGroundPredictWait = 2;//0,25 * n

    private final String lavaGroundMessage = "§c「熱量を増す地面」"; //{, "§c「熱に殺された人々」", "§c「太陽が文明を滅ぼす」"};

    public void LavaGround(){
        if (lavaGround) sendMessage(lavaGroundMessage);
        lavaGround = false;
        lavaGroundCT = 20;

        SomTask.async(() -> {
            int radius = 32;
            final SomParticle predictParticle = new SomParticle(Particle.LAVA, this);

            SomTask.asyncCount(() -> {
                if (isInvalid()) return;
                predictParticle.circleFill(pivot, radius, 1);
                for (PlayerData playerData : PlayerData.getPlayerList(getWorld())) {
                    SomSound.Fire.play(playerData);
                }
            }, 5, lavaGroundPredictWait, this);

            //ダメージ判定
            for (SomEntity entity : SearchEntity.nearSomEntity(enemies(), pivot, radius)) {
                if (entity.getEntity().isOnGround()) {
                    Damage.makeDamage(this, entity, Damage.Type.Magic, 2.5);
                    SomSound.Lava.play(entity);
                }
            }
        });
    }



    private boolean solarFlare = true;
    private final String solarFlareMessage = "§c「生き残らなければいけない」"; //{, "§c「信念に殺された人々」", "§c「信条が文明を滅ぼす」"};
    private final int solarFlareAmount = 12;
    private final int solarFlareDuration = 15 * 20;//tick
    public void SolarFlare(){
        solarFlare = false;
        sendMessage(solarFlareMessage);

        SomTask.async(()->{
            double radius = 0.5;
            final SomParticle FireParticle = new SomParticle(Particle.LANDING_LAVA, this);
            SomEffect effect = new SomEffect("SolarFlare", "歪な熱意", solarFlareDuration, this);
            HashMap<CustomLocation, Boolean> flaresCondition = new HashMap<>();

            for (PlayerData playerData : PlayerData.getPlayerList(getWorld())){
                playerData.sendMessage("§c§n熱量§aを抑えてください", SomSound.Tick);
                playerData.addEffect(effect);
            }

            //Log(String.valueOf(getTargets().size()));
            for (int i = 0; i < solarFlareAmount * enemies().size(); i++){
                //ランダムなロケーションを制定
                CustomLocation randomLocation = pivot.clone().addY(1);
                //ランダム Θ(radian) + r(distance) 生成
                double randomTheta = randomDouble(0, Math.PI*2);
                double randomR = randomDouble(0, 29);//radius
                randomLocation.addXZ(Math.cos(randomTheta) * randomR, Math.sin(randomTheta) * randomR);

                flaresCondition.put(randomLocation, false);
            }

            final int perTick = 5;
            Froal froal = this;
            new BukkitRunnable(){
                int tick = 0;

                boolean isClear = false;

                @Override
                public void run(){
                    if (isInvalid()) this.cancel();
                    if (tick < solarFlareDuration){

                        flaresCondition.forEach((location, flg)->{
                            if (flg) return;
                            FireParticle.sphere(location, radius, 5);

                            for (SomEntity entity : enemies()) {
                                double distance = location.distance(entity.getLocation());
                                if (distance < radius*4) {
                                    flaresCondition.replace(location, true);
                                }
                            }
                        });

                        //1つでもクリアされてなければ続行
                        isClear = true;
                        for (Boolean flg : flaresCondition.values()){
                            if (!flg){
                                isClear = false;
                                break;
                            }
                        }
                        if (isClear){
                            for (PlayerData playerData : PlayerData.getPlayerList(getWorld())){
                                playerData.sendMessage("§c§n熱量§aを抑えました", SomSound.Tick);
                                playerData.removeEffect("SolarFlare");
                            }
                            this.cancel();
                        }

                        tick+=perTick;
                    }else{
                        //Clearしてなかったら死
                        if (!isClear){
                            for (PlayerData playerData : PlayerData.getPlayerList(getWorld())){
                                playerData.death(froal);
                                this.cancel();
                            }
                        }
                    }
                }

            }.runTaskTimerAsynchronously(SomCore.plugin(),1, perTick);
        });
    }

}
