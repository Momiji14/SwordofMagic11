package SwordofMagic11.Enemy.Custom;

import SwordofMagic11.Component.SomParticle;
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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import static SwordofMagic11.Component.Function.randomDouble;

public class Logna extends CustomData {

    //bossAreaRadius = 45
    private final int SkillDelay = 50;

    public Logna(MobData mobData, int level, Location location) {
        super(mobData, level, location);
    }

    @Override
    public void tick() {
        waterRipples--;
        if (waterRipples <= 0) WaterRipples();
        if (whirlpool && healthPercent() <= 0.8) Whirlpool();
        if (drown && healthPercent() <= 0.5) Drown();
        if (tsunami && healthPercent() <= 0.2) Tsunami();
    }



    private int waterRipples = 20;
    private final int waterRipplesRadius = 15;
    private final int waterRipplesWait = 2;
    public void WaterRipples() {
        waterRipples = 20;

        SomTask.async(() -> {
            //パーティクル指定
            final SomParticle predictCircleParticle = new SomParticle(Color.RED, this);
            final SomParticle damageCircleParticle = new SomParticle(Particle.BUBBLE_POP, this);
            //その場の座標取得
            CustomLocation location = getLocation().addY(0.1);
            //一発ごとに進む半径の距離
            double eachRadius = 1;

            //最初の予測円
            SomTask.asyncCount(count -> {
                //ダメージ円パーティクル
                double radius = count * eachRadius;
                //予測円パーティクル
                for (double i = 0; i < eachRadius; i += 0.2) {
                    predictCircleParticle.circle(location, radius + i, 16);
                }

                //ダメージ処理
                damageCircleParticle.circle(location, radius + eachRadius, 16);
                Collection<SomEntity> entities = SearchEntity.nearSomEntity(enemies(), location, radius + eachRadius);
                entities.removeIf(entity -> entity.getLocation().distance(location) < radius);
                for (SomEntity entity : entities) {
                    Damage.makeDamage(this, entity, Damage.Type.Magic, 2);
                    SomSound.Drown.play(entity);
                }
                return null;
            }, waterRipplesWait, waterRipplesRadius, SkillDelay, this);
        });
    }

    private boolean whirlpool = true;
    private static final String whirlpoolMessage = "§c「渦は時間通りに流れます。流れに逆らってはいけません」";
    private final int WhirlpoolTolerance = 15;
    private final int WhirlpoolDuration = 200; //tick

    public void Whirlpool() {
        whirlpool = false;
        sendMessage(whirlpoolMessage, SomSound.BossWarn1);

        SomTask.asyncDelay(() -> {
            HashMap<PlayerData, Integer> whirlpoolLastYaw = new HashMap<>();
            HashMap<PlayerData, Integer> whirlpoolFirstYaw = new HashMap<>();
            HashMap<PlayerData, Boolean> whirlpoolClear = new HashMap<>();
            SomEffect effect = new SomEffect("Whirlpool", "渦水", WhirlpoolDuration, this);

            for (PlayerData playerData : enemiesPlayer()) {
                playerData.sendMessage("§c§n渦水§aから逃れてください");
                whirlpoolClear.put(playerData, false);
                whirlpoolFirstYaw.put(playerData, playerData.yawInt());

                int nowYaw = Math.floorMod(playerData.yawInt() - whirlpoolFirstYaw.get(playerData), 360);
                whirlpoolLastYaw.put(playerData, nowYaw);
                //デバフ追加
                playerData.addEffect(effect);
            }

            SomParticle particle = new SomParticle(Particle.BUBBLE_POP, this);
            SomTask.asyncCount(count -> {
                for (PlayerData playerData : enemiesPlayer()) {
                    if (!whirlpoolFirstYaw.containsKey(playerData) || !whirlpoolLastYaw.containsKey(playerData)) {
                        whirlpoolClear.put(playerData, false);
                        whirlpoolFirstYaw.put(playerData, playerData.yawInt());
                        int nowYaw = Math.floorMod(playerData.yawInt() - whirlpoolFirstYaw.get(playerData), 360);
                        whirlpoolLastYaw.put(playerData, nowYaw);
                    }
                    if (!playerData.isDeath() && !whirlpoolClear.get(playerData)) {
                        //現在のyaw(オフセット付き)
                        int nowYaw = Math.floorMod(playerData.yawInt() - whirlpoolFirstYaw.get(playerData), 360);
                        //前回のyaw(オフセット付き)
                        int lastYaw = whirlpoolLastYaw.get(playerData);

                        int difYaw = nowYaw - lastYaw;
                        whirlpoolLastYaw.put(playerData, nowYaw);

                        //パーティクル表示
                        particle.circle(playerData.getHipsLocation(),1, 8);

                        if (difYaw < 0 || WhirlpoolTolerance < difYaw) {
                            Damage.makeDamage(this, playerData, Damage.Type.Magic, 1, 0.25);
                            SomSound.Drown.play(playerData);
                        } else if (340 < nowYaw) {
                            playerData.removeEffect("Whirlpool");
                            playerData.sendMessage("§c§n渦水§aから逃れました", SomSound.Tick);
                            whirlpoolClear.put(playerData, true);
                        }

                    }
                }
                return null;
            }, 2, WhirlpoolDuration, () -> {
                for (PlayerData playerData : enemiesPlayer()) {
                    if (!whirlpoolClear.get(playerData) && !playerData.isDeath()) {
                        sendMessage(playerData.getDisplayName() + "§aが§c§n渦水§aにより§4死亡§aしました", SomSound.Tick);
                        playerData.death(this);
                    }
                }
            }, this);
        }, SkillDelay);
    }



    private boolean drown = true;
    private static final String drownMessage = "§c「肺に水が満たされる」"; //"§c「時は流れ、風化し、全て潮になる」"; //{"", "§c「見捨てられたことは水に流しましょう」"};
    private static final int drownRadius = 4;
    private static final int drownTickCount = 10;
    private final int drownPredictWait = 100;
    public void Drown() {
        drown = false;
        sendMessage(drownMessage, SomSound.BossWarn1);
        SomTask.asyncCount(this::DrownTick, drownPredictWait + 20, drownTickCount, SkillDelay, () -> enemiesPlayer().forEach(playerData -> playerData.sendMessage("§c「肺に水が満たされました」", SomSound.Tick)), this);
    }

    public void DrownTick(){
        SomTask.async(() -> {
            //パーティクル指定
            final SomParticle predictCircleParticle = new SomParticle(Color.YELLOW, this);
            final SomParticle damageCircleParticle = new SomParticle(Particle.BUBBLE_POP, this).setVectorUp();

            //ランダムなロケーションを制定
            CustomLocation randomLocation = spawnLocation.clone().addY(0.1).randomRadiusLocation(20);
            sendMessage("§e§n泡§aが見えます", SomSound.Tick);
            //予測円パーティクル
            SomTask.asyncCount(() -> predictCircleParticle.circle(randomLocation, drownRadius) , 2, drownPredictWait/2, () -> {
                damageCircleParticle.circleFill(randomLocation, drownRadius);
                enemiesPlayer().forEach(playerData -> {
                    if (playerData.getLocation().distance(randomLocation) > drownRadius) {
                        Damage.makeDamage(this, playerData, Damage.Type.Magic, 10, 0.25);
                        damageCircleParticle.circle(playerData.getHipsLocation(), 1.5);
                        playerData.sendMessage("§4§n溺水§aしました", SomSound.Drown);
                    } else {
                        playerData.sendMessage("§4§n溺水§aを§b回避§aしました", SomSound.Tick);
                    }
                });
            }, this);
        });
    }



    private boolean tsunami = true;
    private static final String tsunamiMessage = "§c「海の前で人間は無力です」"; //"§c「ここで私が死んでも、地図に載ることはないでしょう」"; //, /*,*/ "§c「あの時、雰囲気に飲まれてなければ」"};
    private static final int tsunamiRadius = 25;
    private static final int tsunamiTickCount = 10;
    private static final int tsunamiSpeedWait = 3;
    public void Tsunami() {
        tsunami = false;
        sendMessage(tsunamiMessage, SomSound.BossWarn1);
        enemies().forEach(entity -> entity.silence(tsunamiRadius * tsunamiTickCount + SkillDelay, this));
        SomTask.asyncCount(this::TsunamiTick, tsunamiRadius, tsunamiTickCount, SkillDelay, this);
    }

    public void TsunamiTick() {
        SomTask.async(() -> {
            //パーティクル指定
            final SomParticle damageCircleParticle = new SomParticle(Color.AQUA, this);
            //一発ごとに進む半径の距離
            double eachRadius = 1.8;

            SomTask.asyncCount(count -> {
                //Location取得
                CustomLocation location = getLocation().addY(0.4);
                //ダメージ円パーティクル
                double radius = count * eachRadius;
                for (double i = 0; i < eachRadius; i += 0.2) {
                    damageCircleParticle.circle(location, radius + i, 16);
                }

                //ダメージ処理
                Collection<SomEntity> entities = SearchEntity.nearSomEntity(enemies(), location, radius + eachRadius);
                entities.removeIf(entity ->  entity.isDeath() || radius > location.distanceXZ(entity.getLocation()) || entity.getLocation().y() > location.y());
                for (SomEntity entity : entities) {
                    sendMessage(entity.getName() + "§aが§c§n津波§aにより§4死亡§aしました", SomSound.Tick);
                    entity.death(this);
                    SomSound.Drown.play(entity);
                }
                return null;
            }, tsunamiSpeedWait, tsunamiRadius, this);
        });
    }
}
