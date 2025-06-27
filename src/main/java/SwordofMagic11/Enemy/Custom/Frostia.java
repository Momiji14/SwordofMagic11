package SwordofMagic11.Enemy.Custom;

import SwordofMagic11.Component.*;
import SwordofMagic11.Custom.CustomLocation;
import SwordofMagic11.DataBase.MobDataLoader;
import SwordofMagic11.Enemy.EnemyData;
import SwordofMagic11.Enemy.MobData;
import SwordofMagic11.Entity.Damage;
import SwordofMagic11.Entity.SearchEntity;
import SwordofMagic11.Entity.SomEffect;
import SwordofMagic11.Entity.SomEntity;
import SwordofMagic11.StatusType;
import SwordofMagic11.TaskOwner;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.potion.PotionEffectType;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Frostia extends CustomData implements EnemyParent {
    public Frostia(MobData mobData, int level, Location location) {
        super(mobData, level, location);
        this.setNoAI();
        this.ignoreCrowdControl(true);
        timer("Avalanche", AvalancheCT);
        timer("OutsideAvalanche", OutsideAvalancheCT);
        timer("FrostBeam", FrostBeamCT / 2);//snowfallとタイミングをずらすための/2
        timer("SnowFall", SnowFallCT);
        timer("ColdWave", ColdWaveCT);
        timer("ColdWater", ColdWaterCT);
        timer("SummonFrostWisp", 60);
    }

    @Override
    public void tick() {
        if (!hasTimer("LittleHeal") && healthPercent() > 0.05) LittleHeal();
        if (!hasTimer("ColdWater")) ColdWater();
        if (!hasTimer("FreezeUpdate")) FreezeUpdate();

        if (!hasTimer("ColdWave") && healthPercent() < 0.5) ColdWave();
        if (!Paused) {
            if (!hasTimer("Avalanche")) Avalanche();
            if (!hasTimer("OutsideAvalanche")) OutsideAvalanche();
            if (!hasTimer("FrostBeam")) FrostBeam(false);
            if (!hasTimer("SnowFall")) SnowFall();

            if (!hasTimer("CheckFrostWisp") && !SummonWispFg) CheckFrostWisp();
            if (!hasTimer("SummonFrostWisp") && SummonWispFg) SummonFrostWisp();
            if (!UltFrostBeamFg && healthPercent() < 0.1) UltFrostBeam();
        }
    }

    final CustomLocation FrostiaCenterPosition = new CustomLocation(getWorld(), 9, 76, 15);
    final CustomLocation FrostiaFieldCenterPosition = new CustomLocation(getWorld(), 9, 63, 15);
    final double ColdWaterSurface = 63.0;

    private final int ColdWaterCT = 30;
    private final int AvalancheCT = 520;
    private final int OutsideAvalancheCT = 400;
    private final int FrostBeamCT = 160;
    private final int SnowFallCT = 200;
    private final int ColdWaveCT = 370;
    private boolean UltFrostBeamFg = false;
    private boolean SummonWispFg = true;

    final String effectId = "FrostiaFreeze";
    final HashMap<SomEntity, CustomLocation> freezingLoc = new HashMap<>();
    public Set<EnemyData> child = new HashSet<>();
    CustomLocation[] WispSpawnLocation = {
            new CustomLocation(getWorld(), 10.5, 67, -4.5),
            new CustomLocation(getWorld(), 25.5, 67, 4.5),
            new CustomLocation(getWorld(), 28.5, 67, 18.5),
            new CustomLocation(getWorld(), 20.5, 67, 32.5),
            new CustomLocation(getWorld(), 8.5, 67, 34.5),
            new CustomLocation(getWorld(), -8.5, 67, 23.5),
            new CustomLocation(getWorld(), -11.5, 67, 17.5),
            new CustomLocation(getWorld(), -3.5, 67, -0.5)
    };

    @Override
    public synchronized void death(SomEntity killer) {
        super.death(killer);
        child.forEach(EnemyData::delete);
        freezingLoc.keySet().forEach((target) -> target.removeEffect(effectId));
    }

    boolean Paused = false;
    HashMap<String, Integer> PausedTime = new HashMap<>();
    String[] SkillIds = {"Avalanche", "OutsideAvalanche", "SnowFall", "FrostBeam"};

    void PauseTimer() {
        Paused = true;
        invincible(true);

        PausedTime.clear();
        for (String str : SkillIds) {
            if (hasTimer(str)) {
                PausedTime.put(str, timer(str));
            }
        }
    }

    void PlayTimer() {
        Paused = false;
        invincible(false);

        for (String str : PausedTime.keySet()) {
            timer(str, PausedTime.get(str));
        }
    }

    void LittleHeal() {
        timer("LittleHeal", 20);
        this.addHealth(this.getStatus(StatusType.MaxHealth) * 0.0005);
    }

    void Freeze(SomEntity target, int add) {
        int dur = 2000; //持続時間

        int oldLevel = 0;   //レベル
        SomEffect effect = new SomEffect(effectId, "氷結", dur, this);
        //すでにエフェクト付与されてたらそっち使う
        if (target.hasEffect(effectId)) {
            effect = target.getEffect(effectId);
            oldLevel = effect.getInt("Level");
            effect.setTime(dur);
        }

        //デバフのレベル上昇
        int level = Math.min(oldLevel + add, 5);

        if (level > oldLevel) {
            //effectの中身設定
            effect.setInt("Level", level);
            effect.setStatusDefense(-10 * level);
            effect.setStatusMultiplyDefense(-0.02 * level);
            if (level >= 3) {
                //5回に分けてダメージ与える
                double dmg = (50 + target.getStatus(StatusType.MaxHealth) * 0.03 * (level - 2)) / 5 / 100;
                SomTask.asyncCount(() -> Damage.makeDamage(this, target, Damage.Type.Magic, dmg), 20, 5, this);
            }
            if (level >= 4) {
                effect.setStatusMultiply(StatusType.ManaRegen, -0.2 * (level - 3));
            }
            //スローとlevel5の沈黙はUpdateの方

            if (!target.hasEffect(effectId)) target.addEffect(effect);

            if (!target.isSilence()) target.silence(1, this);
        }
        target.statusUpdate();

        //Updateで使うリストに追加
        freezingLoc.put(target, target.getLocation());
        SomSound.Glass.play(target);
    }

    void FreezeUpdate() {
        timer("FreezeUpdate", 10);

        //デバフがなかったりとかしたらリストから外す
        freezingLoc.keySet().removeIf(target -> {
            if (target.getWorld() != this.getWorld() || !target.hasEffect(effectId) || !enemies().contains(target)) {
                //デバフが消えた時の処理
                target.removeEffect(effectId);
                target.removePotionEffect(PotionEffectType.SLOWNESS);
                SomSound.FrostiaRemoveDebuff.play(target);
                return true;
            }
            return false;
        });

        for (SomEntity target : freezingLoc.keySet()) {
            //移動距離に応じてデバフの時間を短縮、現在座標の記録
            int dist = (int) freezingLoc.get(target).distanceXZ(target.getLocation()) * -20;
            SomEffect effect = target.getEffect(effectId);
            effect.addTime(-dist);
            freezingLoc.put(target, target.getLocation());

            target.removePotionEffect(PotionEffectType.SLOWNESS);
            target.SLOWNESS((effect.getInt("Level") - 1), 30);
            if (effect.getInt("Level") == 5) {
                target.silence(15, this);
            }
        }
    }

    Collection<SomEntity> FreezeTarget() {
        if (!freezingLoc.isEmpty()) {
            Collection<SomEntity> list = new HashSet<>();
            int maxLevel = 0;
            for (SomEntity target : freezingLoc.keySet()) {
                if (target.hasEffect(effectId)) continue; //エスケープ忘れ @きんとん
                if (maxLevel < target.getEffect(effectId).getInt("Level")) {
                    maxLevel = target.getEffect(effectId).getInt("Level");
                    list.clear();
                    list.add(target);
                } else if (maxLevel == target.getEffect(effectId).getInt("Level")) {
                    list.add(target);
                }
            }
            return list;
        }
        return enemies();
    }

    void ColdWater() {
        timer("ColdWater", ColdWaterCT);

        enemies().forEach((target) -> {
            if (target.getLocation().y() < ColdWaterSurface) {
                Freeze(target, 1);
            }
        });
    }

    void Avalanche() {
        timer("Avalanche", AvalancheCT);

        int signWait = 40;
        SomParticle atkParticle = new SomParticle(Particle.ITEM_SNOWBALL, this);

        SomTask.asyncCount(() -> {
            //予兆パーティクル
            atkParticle.sphere(FrostiaCenterPosition, 2.0);
            SomSound.Lever.radius(FrostiaCenterPosition);
        }, 5, signWait / 5, () -> {
            //雪崩処理
            final double[] radius = {0};
            atkParticle.setLower();
            atkParticle.setOffsetY(0.5);
            SomTask.asyncCount(() -> {
                radius[0] = Math.min(radius[0] + 1, 14);
                atkParticle.circleFill(FrostiaCenterPosition, radius[0], 12 + radius[0] * 3);
                SearchEntity.nearXZSomEntity(enemies(), FrostiaFieldCenterPosition, radius[0]).forEach((target) ->
                {
                    Damage.makeDamage(this, target, Damage.Type.Magic, 1.0);
                    Freeze(target, 5);
                });
            }, 2, 30, () -> FrostBeam(true), this);
        }, this);
    }

    void OutsideAvalanche() {
        timer("OutsideAvalanche", OutsideAvalancheCT);

        int signWait = 40;
        SomParticle atkParticle = new SomParticle(Color.AQUA, this);

        SomTask.asyncCount(() -> {
            //予兆パーティクル
            atkParticle.sphere(FrostiaCenterPosition, 2.0);
            SomSound.Lever.radius(FrostiaCenterPosition);
        }, 5, signWait / 5, () -> {
            //雪崩処理
            final double[] radius = {48};
            atkParticle.setLower();
            atkParticle.setOffsetY(0.5);
            SomTask.asyncCount(() -> {
                radius[0] = Math.max(radius[0] - 1, 26);
                atkParticle.circle(FrostiaCenterPosition, radius[0], 12 + radius[0] * 3);
                for (SomEntity target : enemies()) {
                    if (target.getLocation().distanceXZ(FrostiaCenterPosition) > radius[0]) {
                        Damage.makeDamage(this, target, Damage.Type.Magic, 1.0);
                        Freeze(target, 5);
                    }
                }
            }, 2, 30, () -> FrostBeam(true), this);
        }, this);
    }

    void FrostBeam(boolean big) {
        timer("FrostBeam", FrostBeamCT);

        int signWait = 30;
        double dmg;
        Color color = Color.RED;
        if (big) {
            signWait = 60;
            dmg = 10.0;
            color = Color.YELLOW;
        } else {
            dmg = 2.5;
        }

        SomParticle signParticle = new SomParticle(color, this);
        SomParticle atkParticle = new SomParticle(Particle.SNOWFLAKE, this);
        SomEntity target;
        Collection<SomEntity> list = FreezeTarget();
        if (!list.isEmpty()) target = Function.randomGet(list);
        else target = null;
        if (target == null) return;

        SomTask.asyncCount(() -> {
            SomRay ray = SomRay.rayLocationEntity(FrostiaCenterPosition, target.getLocation(), 0.1, enemies(), false);
            signParticle.line(FrostiaCenterPosition, ray.getHitPosition());
        }, 5, signWait / 5, () -> {
            SomRay ray = SomRay.rayLocationEntity(FrostiaCenterPosition, target.getLocation(), 0.1, enemies(), false);
            atkParticle.line(FrostiaCenterPosition, ray.getHitPosition());
            SomSound.FrostiaAtk1.radius(ray.getHitPosition());
            if (ray.getHitEntity() != null) {
                Damage.makeDamage(this, ray.getHitEntity(), Damage.Type.Magic, dmg);
            }
        }, this);
    }

    void SnowFall() {
        timer("SnowFall", SnowFallCT);

        int signWait = 30;
        double radius = 3.0;
        SomParticle signParticle = new SomParticle(Color.RED, this);
        SomParticle atkParticle = new SomParticle(Particle.ITEM_SNOWBALL, this);

        Collection<CustomLocation> locs = new HashSet<>();
        enemies().forEach((target) -> locs.add(target.getLocation()));
        SomTask.asyncCount(() -> {
            for (CustomLocation loc : locs) {
                signParticle.circle(loc, radius);
                SomSound.Lever.radius(loc);
            }
        }, 5, signWait / 5, () -> {
            for (CustomLocation loc : locs) {
                atkParticle.circleRain(loc, radius, 3);
                for (SomEntity target : SearchEntity.nearSomEntity(enemies(), loc, radius)) {
                    Freeze(target, 1);
                }
            }
        }, this);
    }

    void UltFrostBeam() {
        UltFrostBeamFg = true;
        PauseTimer();
        playSound(SomSound.BossWarn1);

        int signWait = 200;
        double dmg = 10.0;
        Color color = Color.PURPLE;

        SomParticle signParticle = new SomParticle(color, this);
        SomParticle atkParticle = new SomParticle(Particle.SNOWFLAKE, this);

        SomTask.asyncCount(() -> {
            for (SomEntity target : enemies()) {
                SomRay ray = SomRay.rayLocationEntity(FrostiaCenterPosition, target.getLocation(), 0.1, enemies(), false);
                signParticle.line(FrostiaCenterPosition, ray.getHitPosition());
            }
        }, 5, signWait / 5, () -> SomTask.asyncCount(() -> {
            for (SomEntity target : enemies()) {
                SomRay ray = SomRay.rayLocationEntity(FrostiaCenterPosition, target.getLocation(), 0.1, enemies(), false);
                atkParticle.line(FrostiaCenterPosition, ray.getHitPosition());
                SomSound.FrostiaAtk1.radius(ray.getHitPosition());
                SomEntity victim = ray.getHitEntity();
                if (victim != null) {
                    Freeze(victim, 5);
                    Damage.makeDamage(this, victim, Damage.Type.Magic, dmg);
                }
            }
        }, 10, 20, this::PlayTimer, this), this);
    }

    void CheckFrostWisp() {
        timer("CheckFrostWisp", 20);

        child.removeIf(TaskOwner::isInvalid);
        if (child.isEmpty()) {
            ColdSurge();
        }
    }

    void SummonFrostWisp() {
        SummonWispFg = false;
        SomTask.sync(() -> {
            for (CustomLocation loc : WispSpawnLocation) {
                loc.setDirection(loc.toVector().subtract(FrostiaCenterPosition.toVector()));
                EnemyData data = EnemyData.spawn(MobDataLoader.getMobData("フロストウィスプ"), getLevel(), loc, this);
                data.setNoAI();
                data.damageOverride(1);
                data.setFixedMaxHealth(10);
                data.setHealth(10);
                data.statusUpdate();
                child.add(data);
            }
        });
    }

    @Override
    public void onChildDeath(EnemyData enemyData, SomEntity killer) {
        if (killer.hasEffect(effectId)) {
            killer.getEffect(effectId).setTime(0);
        }
        child.remove(enemyData);
    }

    void ColdSurge() {
        timer("SummonFrostWisp", 100);
        SummonWispFg = true;
        PauseTimer();
        playSound(SomSound.BossWarn1);

        int signWait = 100;
        final double[] radius = {5};
        SomParticle signParticle = new SomParticle(Particle.FIREWORK, this);
        SomParticle atkParticle = new SomParticle(Particle.SNOWFLAKE, this);

        SomTask.asyncCount(() -> {
            signParticle.sphere(FrostiaCenterPosition, radius[0]);
            radius[0] = Math.max(radius[0] - 0.3, 0);
        }, 5, signWait / 5, () -> {
            atkParticle.setSpeed(1.0f);
            atkParticle.random(FrostiaCenterPosition, 200);
            playSound(SomSound.Explode);
            for (SomEntity target : enemies()) {
                SomRay ray = SomRay.rayLocationEntity(FrostiaCenterPosition, target.getLocation(), 0.1, enemies(), false);
                SomEntity victim = ray.getHitEntity();
                if (victim != null) {
                    Freeze(victim, 5);
                    Damage.makeDamage(this, victim, Damage.Type.Magic, 30.0);
                }
            }
            PlayTimer();
        }, this);
    }

    void ColdWave() {
        timer("ColdWave", ColdWaveCT);

        playSound(SomSound.FrostiaAtk2);
        enemies().forEach((target) -> Freeze(target, 1));
    }
}