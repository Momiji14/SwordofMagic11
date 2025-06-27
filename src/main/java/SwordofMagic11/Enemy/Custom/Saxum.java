package SwordofMagic11.Enemy.Custom;

import SwordofMagic11.Component.*;
import SwordofMagic11.Custom.CustomLocation;
import SwordofMagic11.Custom.UnsetLocation;
import SwordofMagic11.DataBase.MobDataLoader;
import SwordofMagic11.Enemy.EnemyData;
import SwordofMagic11.Enemy.MobData;
import SwordofMagic11.Entity.Damage;
import SwordofMagic11.Entity.SearchEntity;
import SwordofMagic11.Entity.SomEffect;
import SwordofMagic11.Entity.SomEntity;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.StatusType;
import SwordofMagic11.TaskOwner;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

public class Saxum extends CustomData implements EnemyParent {

    public Saxum(MobData mobData, int level, Location location) {
        super(mobData, level, location);
        ignoreCrowdControl(true);
        timer("MinecartSpawn", MinecartSpawnCT);
        timer("Crack", CrackCT);
        timer("RandomEvent", RandomEventCT);
        setFixedMaxHealth(15000000);
        setHealth(getStatus(StatusType.MaxHealth));
    }

    @Override
    public void tick() {
        if (!GiantFg) {
            if (healthPercent() < 0.3) SecondPhase();

            if (!hasTimer("MinecartSpawn")) MinecartSpawn();
            if (!hasTimer("Crack")) Crack();
            if (!hasTimer("RandomEvent") && healthPercent() < 0.7) RandomEvent();
        } else {
            BlowWind();
            if (!hasTimer("BombChecker")) BombChecker();

            if (!hasTimer("BombAccident")) BombAccident();
        }
    }

    @Override
    public synchronized void death(SomEntity killer) {
        super.death(killer);
        carts.forEach(EnemyData::delete);
        for (BombLocation location : BombLocation.values()) {
            if (location.hasBomb()) {
                location.getBomb().delete();
            }
        }
        bombs.forEach(EnemyData::delete);
        timeBomb.delete();
    }

    final CustomLocation SaxumFieldCenter = new CustomLocation(getWorld(), -0.5, 64, 8.5);


    final CustomLocation[] MinecartSpawnLocation = {
            new CustomLocation(getWorld(), -0.5, 64, 39.5), //diamond（クリティカル
            new CustomLocation(getWorld(), -27.5, 64, 7.5), //ruby（攻撃力
            new CustomLocation(getWorld(), -0.5, 64, -20.5),//emerald（リジェネ
            new CustomLocation(getWorld(), 27.5, 64, 9.5),  //sapphire（防御
    };

    enum BombLocation {
        Location_1(new UnsetLocation(-27.5, 83, 34.5)),
        Location_2(new UnsetLocation(-36.5, 82, -6.5)),
        Location_3(new UnsetLocation(11.5, 83, -25.5)),
        Location_4(new UnsetLocation(34.5, 82, 5.5)),
        Location_5(new UnsetLocation(21.5, 82, 42.5)),
        ;

        private static final MobData Bomb = MobDataLoader.getMobData("ダイナマイト");
        private final UnsetLocation location;
        private boolean nextSpawn = true;
        private EnemyData bomb;

        BombLocation(UnsetLocation location) {
            this.location = location;
        }

        public CustomLocation get(Saxum saxum) {
            return location.as(saxum.getWorld());
        }

        public void setBomb(EnemyData bomb) {
            this.bomb = bomb;
        }

        public EnemyData getBomb() {
            return bomb;
        }

        public boolean hasBomb() {
            return bomb != null;
        }

        public void spawnBomb(Saxum saxum) {
            SomTask.sync(() -> {
                EnemyData bomb = EnemyData.spawn(Bomb, saxum.getLevel(), get(saxum), saxum);
                bomb.setNoAI();
                bomb.ignoreCrowdControl(true);
                setBomb(bomb);
            });
        }

        BukkitTask task;

        public void nextSpawn() {
            if (task == null) {
                task = SomTask.asyncDelay(() -> {
                    bomb = null;
                    task = null;
                }, BombSpawnCT);
            }
        }
    }

    public Set<EnemyData> carts = new HashSet<>();
    public Set<EnemyData> bombs = new HashSet<>();
    public EnemyData timeBomb;
    final double SecondFloorLine = 81.0;

    final int CrackCT = 300;
    final int RandomEventCT = 1200;
    final int BombAccidentCT = 100;
    final int MinecartSpawnCT = 600;
    static final int BombSpawnCT = 100;

    boolean GiantFg = false;

    @Override
    public void onChildDeath(EnemyData enemyData, SomEntity killer) {
        if (enemyData.getMobData() == Minecart) {
            //トロッコをキルした時の処理
            carts.forEach(EnemyData::delete);
            CustomLocation loc = enemyData.getLocation();
            int cnt = 0;
            for (CustomLocation spawn : MinecartSpawnLocation) {
                if (loc.distance(spawn) < 1.0) {
                    MinecartKillBuff((PlayerData) killer, cnt);
                }
                cnt++;
            }
        } else if (enemyData.getMobData() == Bomb) {
            if (GiantFg) {
                //Phase2でダイナマイトをキルした時の処理
                BombKillBuff((PlayerData) killer);
            }
        }
    }

    private void Crack() {
        timer("Crack", CrackCT);

        SomParticle atkParticle = new SomParticle(Color.OLIVE, this);
        CustomLocation loc = getLocation();
        atkParticle.setLower();

        final int[] i = {0};
        int n = 3;
        SomTask.asyncCount(() -> {
            if (i[0] != 0) atkParticle.circle(loc, i[0]);
            atkParticle.circle(loc, i[0] + n);
            for (SomEntity target : SearchEntity.nearSomEntity(enemies(), loc, i[0], i[0] + n)) {
                if (target.getEntity().isOnGround()) {
                    Damage.makeDamage(this, target, Damage.Type.Physics, 2);
                }
            }
            i[0] += n;
        }, 8, 9, this);
    }

    void RandomEvent() {
        timer("RandomEvent", RandomEventCT);

        switch (Function.randomInt(0, 1)) {
            case 0 -> Subsidence();
            case 1 -> TimeBomb();
        }
    }

    void Subsidence() {
        SomParticle signParticle = new SomParticle(Particle.EXPLOSION_EMITTER, this);

        playSound(SomSound.BossWarn1);
        SomTask.asyncCount(() -> {
            signParticle.random(SaxumFieldCenter, 27, 3);
            playSound(SomSound.Explode);
        }, 10, 5, () -> {
            SomTask.asyncDelay(() -> {
                enemies().forEach((target) -> Damage.makeDamage(this, target, Damage.Type.Physics, 50));
            }, 20);
        }, this);
    }

    void TimeBomb() {
        playSound(SomSound.BossWarn1);
        SomTask.sync(() -> {
            EnemyData timeBomb = EnemyData.spawn(Bomb, getLevel(), SaxumFieldCenter, this);
            timeBomb.setNoAI();
            timeBomb.ignoreCrowdControl(true);
            timeBomb.setFixedMaxHealth(1000000);
            timeBomb.setHealth(timeBomb.getStatus(StatusType.MaxHealth));
        });
        SomTask.asyncCountIf(() -> {
            playSound(SomSound.Lever);
        }, 20, 10, 20, this::timeBombValid, () -> {
            if (timeBombValid()) {
                playSound(SomSound.Explode);
                new SomParticle(Particle.EXPLOSION_EMITTER, this).circle(SaxumFieldCenter, 0);
                enemies().forEach((target) -> Damage.makeDamage(this, target, Damage.Type.Physics, 20000, 0, 1, 1, 10, 1));
            }
        }, this);
    }

    public boolean timeBombValid() {
        return timeBomb != null && timeBomb.isValid();
    }

    void BombAccident() {
        timer("BombAccident", BombAccidentCT);

        SomParticle signParticle = new SomParticle(Color.RED, this);
        SomParticle atkParticle = new SomParticle(Particle.EXPLOSION_EMITTER, this);
        int signWait = 40;
        int random = Function.randomInt(0, 4);
        CustomLocation loc = BombLocation.values()[random].location.as(getWorld());

        SomTask.asyncCount(() -> {
            //予兆
            signParticle.sphere(loc, 5);
        }, 5, signWait / 5, () -> SomTask.asyncCount(() -> {
            //ボムが爆発する演出とダメージ
            atkParticle.circle(loc, 0);
            SomSound.Explode.radius(loc, 8);
            for (SomEntity target : SearchEntity.nearSomEntity(enemies(), loc, 5)) {
                Damage.makeDamage(this, target, Damage.Type.Physics, 2);
            }
        }, 10, 14, this), this);
    }

    MobData Minecart = MobDataLoader.getMobData("トロッコ");

    void MinecartSpawn() {
        timer("MinecartSpawn", MinecartSpawnCT);

        carts.forEach(EnemyData::delete);
        for (CustomLocation loc : MinecartSpawnLocation) {
            SomTask.sync(() -> {
                EnemyData data = EnemyData.spawn(Minecart, getLevel(), loc, this);
                data.setNoAI();
                data.ignoreCrowdControl(true);
                carts.add(data);
            });
        }
    }

    void MinecartKillBuff(PlayerData data, int type) {
        Color color;
        switch (type) {
            case 0 -> color = Color.AQUA;
            case 1 -> color = Color.RED;
            case 2 -> color = Color.GREEN;
            case 3 -> color = Color.BLUE;
            default -> color = Color.WHITE;
        }
        data.sendMessage("§eトロッコを獲得した！§c通常攻撃§eで投げつけよう！");
        SomTask.asyncDelay(() -> data.leftClickOverride(() -> {
            SomParticle signParticle = new SomParticle(color, data);
            double speed = 2;
            CustomLocation loc = data.getHipsLocation();
            Vector velo = loc.getDirection();
            velo.setY(0);
            velo.normalize().multiply(speed);
            loc.setDirection(velo);

            SomTask.asyncCountIf(() -> {
                loc.add(velo);
                signParticle.sphere(loc, 0.5);
            }, 3, 40 / 3, 0, () -> {
                SomRay ray = SomRay.rayLocationBlock(loc, speed, true);
                return !ray.isHitBlock() && !SearchEntity.nearSomEntity(data.enemies(), loc, 3).contains(this);
            }, () -> {
                SomParticle atkParticle = new SomParticle(Particle.EXPLOSION_EMITTER, data);
                SomSound.Explode.radius(this);
                atkParticle.circle(loc, 0);
                if (SearchEntity.nearSomEntity(data.enemies(), loc, 3).contains(this)) {
                    Damage.makeDamage(data, this, Damage.Type.Physics, this.getStatus(StatusType.MaxHealth) * 0.01, 0, 2, 0, 1, 1.0);
                    MinecartBuff(type);
                }
            }, data);

            data.resetLeftClickOverride();
        }), 20);
    }

    void MinecartBuff(int type) {
        SomEffect effect = new SomEffect("MinecartBuff", "トロッコバフ", 400, this);
        String msg = "";
        switch (type) {
            case 0:
                //クリティカル
                effect.setStatus(StatusType.CriticalRate, 50000);
                effect.setStatus(StatusType.CriticalDamage, 50000);
                msg = "§bクリティカルステータスが大幅に上昇した";
                break;
            case 1:
                //攻撃力
                effect.setStatusMultiplyAttack(1);
                effect.setStatusMultiply(StatusType.SPT, 1);
                msg = "§c攻撃ステータスが大幅に上昇した";
                break;
            case 2:
                //リジェネ
                effect.setStatusMultiply(StatusType.MaxHealth, 2);
                effect.setStatusMultiply(StatusType.MaxMana, 2);
                effect.setStatusMultiply(StatusType.HealthRegen, 9);
                effect.setStatusMultiply(StatusType.ManaRegen, 9);
                msg = "§a体力ステータスとマナステータスが大幅に上昇した";
                break;
            case 3:
                //防御
                effect.setStatusMultiplyDefense(1);
                msg = "§9防御ステータスが大幅に上昇した";
                break;
        }
        enemies().forEach((target) -> target.addEffect(effect));
        sendMessage(msg);
    }

    void SecondPhase() {
        GiantFg = true;
        setFixedMaxHealth(30000000);
        setHealth(getStatus(StatusType.MaxHealth));
        setNoAI();
        ignoreCrowdControl(false);
        teleport(SaxumFieldCenter);
        ignoreCrowdControl(true);
        //ディスガイズ
        SomTask.sync(() -> {
            if (hasDisguise()) getDisguise().stopDisguise();
            MobDisguise disguise = new MobDisguise(DisguiseType.GIANT);
            disguise.setEntity(getEntity());
            disguise.startDisguise();
        });
    }

    void BlowWind() {
        for (SomEntity target : enemies()) {
            if (target.getLocation().getY() < SecondFloorLine) {
                Vector vec = target.getLocation().toVector().subtract(SaxumFieldCenter.toVector()).normalize().multiply(2);
                vec.setY(2);
                target.setVelocity(vec);
            }
        }
    }

    MobData Bomb = MobDataLoader.getMobData("ダイナマイト");

    void BombChecker() {
        timer("BombChecker", 10);

        for (BombLocation location : BombLocation.values()) {
            if (!location.hasBomb()) {
                location.spawnBomb(this);
            } else if (location.getBomb().isInvalid()) {
                location.nextSpawn();
            }
        }
        bombs.removeIf(TaskOwner::isInvalid);
    }

    void BombKillBuff(PlayerData data) {
        data.sendMessage("§eダイナマイトを獲得した！§c通常攻撃§eで投げつけよう！");
        SomTask.asyncDelay(() -> data.leftClickOverride(() -> {
            SomParticle signParticle = new SomParticle(Color.WHITE, data);
            double speed = 2.5;
            CustomLocation loc = data.getHipsLocation();
            Vector velo = loc.getDirection();
            velo.setY(0);
            velo.normalize().multiply(speed);
            loc.setDirection(velo);

            SomTask.asyncCountIf(() -> {
                loc.add(velo);
                velo.setY(velo.getY() - 0.3);
                signParticle.sphere(loc, 0.5);
            }, 3, 40 / 3, 0, () -> {
                SomRay ray = SomRay.rayLocationBlock(loc, speed, true);
                return !ray.isHitBlock() && !SearchEntity.nearSomEntity(data.enemies(), loc, 3).contains(this);
            }, () -> {
                SomParticle atkParticle = new SomParticle(Particle.EXPLOSION_EMITTER, data);
                atkParticle.circle(loc, 0);
                playSound(SomSound.Explode);
                if (SearchEntity.nearSomEntity(data.enemies(), loc, 3).contains(this)) {
                    Damage.makeDamage(data, this, Damage.Type.Physics, this.getStatus(StatusType.MaxHealth) * 0.05, 0, 2, 0, 1, 1);
                }
            }, data);

            data.resetLeftClickOverride();
        }), 20);
    }
}
