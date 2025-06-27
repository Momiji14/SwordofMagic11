package SwordofMagic11.Entity;

import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Component.SomTask;
import SwordofMagic11.Enemy.EnemyData;
import SwordofMagic11.Player.HumanData;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Map.PvPRaid;
import SwordofMagic11.Player.Setting.PlayerSetting;
import SwordofMagic11.SomCore;
import SwordofMagic11.StatusType;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static SwordofMagic11.Component.Function.MinMax;
import static SwordofMagic11.Component.Function.scale;

public interface Damage {
    double PvPMultiply = 0.05;
    double HealMultiply = 0.25;
    double BuffMultiply = 0.25;
    Random random = new Random();

    enum Type {
        Physics("§c物理ダメージ", StatusType.ATK),
        Magic("§d魔法ダメージ", StatusType.MAT),
        Shoot("§9射撃ダメージ", StatusType.SAT),
        Holy("§e神聖ダメージ", StatusType.SPT),
        ;

        private final String display;
        private final StatusType refType;

        Type(String display, StatusType refType) {
            this.display = display;
            this.refType = refType;
        }

        public String getDisplay() {
            return display;
        }

        public StatusType getRefType() {
            return refType;
        }
    }

    Set<SomEntity> hitAnimation = new HashSet<>();

    static void run() {
        SomTask.asyncTimer(hitAnimation::clear, 5, SomCore.TaskOwner);
    }

    static double attack(SomEntity entity, Type type) {
        double value;
        switch (type) {
            case Physics -> value = entity.getStatus(StatusType.ATK);
            case Magic -> value = entity.getStatus(StatusType.MAT);
            case Shoot -> value = entity.getStatus(StatusType.SAT);
            case Holy -> value = entity.getStatus(StatusType.SPT);
            default -> value = 0;
        }
        return value;
    }

    static double defense(SomEntity entity, Type type) {
        double value;
        switch (type) {
            case Physics -> value = entity.getStatus(StatusType.DEF);
            case Magic -> value = entity.getStatus(StatusType.MDF);
            case Shoot -> value = entity.getStatus(StatusType.SDF);
            case Holy -> value = (entity.getStatus(StatusType.DEF) + entity.getStatus(StatusType.MDF) + entity.getStatus(StatusType.SDF)) / 3;
            default -> value = 0;
        }
        return value;
    }

    private static boolean invincible(SomEntity attacker, SomEntity victim) {
        if (victim.isInvincible()) {
            if (attacker instanceof HumanData humanData) {
                if (humanData.setting().is(PlayerSetting.BooleanEnum.MakeDamageLog)) {
                    humanData.sendMessage("§a≪§b無敵");
                }
            }
            return true;
        }
        return false;
    }

    /**
     * ダメージを与えます
     *
     * @param attacker 攻撃者
     * @param victim   被弾者
     * @param type     ダメージタイプ
     * @param multiply ダメージ倍率
     */
    static void makeDamage(SomEntity attacker, SomEntity victim, Damage.Type type, double multiply) {
        makeDamage(attacker, victim, type, multiply, attacker.getStatus(StatusType.Penetration) * 0.01);
    }

    /**
     * ダメージを与えます
     *
     * @param attacker 攻撃者
     * @param victim   被弾者
     * @param type     ダメージタイプ
     * @param multiply ダメージ倍率
     */
    static void makeDamage(SomEntity attacker, SomEntity victim, Damage.Type type, double multiply, double penetration) {
        if (invincible(attacker, victim)) return;
        SomTask.async(() -> {
            double finalMultiply = multiply;
            if (victim instanceof PlayerData playerData && !playerData.isPlayMode()) return;
            if (attacker.getWorld() != victim.getWorld()) return;
            if (attacker.isDeath() || victim.isDeath()) return;
            double attack = attack(attacker, type);
            double defense = defense(victim, type);
            double criRate = attacker.getStatus(StatusType.CriticalRate);
            double criDamage = attacker.getStatus(StatusType.CriticalDamage);
            double criResist = victim.getStatus(StatusType.CriticalResist);

            if (victim.hasEffect("Cover")) {
                defense = Math.max(defense, defense(victim.getEffect("Cover").getOwner(), type));
            }

            double distance = attacker.getLocation().distance(victim.getLocation());
            if (attacker instanceof PlayerData && distance > 20) {
                if (distance < 35) {
                    finalMultiply *= Math.max(0.7, 1 - (distance - 20) * 0.02);
                } else {
                    finalMultiply *= 0.7;
                }
            }

            makeDamage(attacker, victim, type, attack, defense, criRate, criDamage, Math.max(1, criResist), finalMultiply, penetration);
        });
    }

    /**
     * ダメージ計算部 (特殊なダメージ用 )
     */
    static void makeDamage(SomEntity attacker, SomEntity victim, Type type, double attack, double defense, double criRate, double criDamage, double criResist, double multiply) {
        makeDamage(attacker, victim, type, attack, defense, criRate, criDamage, Math.max(1, criResist), multiply, attacker.getStatus(StatusType.Penetration) * 0.01);
    }

    /**
     * ダメージ計算部 (特殊なダメージ用 )
     */
    static void makeDamage(SomEntity attacker, SomEntity victim, Type type, double attack, double defense, double criRate, double criDamage, double criResist, double multiply, double penetration) {
        double critical = criRate / (criResist * 2);
        double criticalDamage = Math.min(multiply * 2.5, criDamage / criResist);
        boolean isCritical = random.nextDouble() < critical;
        if (isCritical) {
            multiply += criticalDamage;
        }

        double damage;
        if (!victim.hasDamageOverride()) {
            double baseDamage = MinMax(attack - defense, 0, attack) * (1 - penetration) + (attack * penetration);
            damage = baseDamage * multiply;

            if (damage <= 0 && attacker instanceof PlayerData playerData) {
                if (playerData.setting().is(PlayerSetting.BooleanEnum.MakeDamageLog)) {
                    playerData.sendMessage("§c" + type.getRefType().getDisplay() + "が足りません §7(" + scale(attack, 1) + "/" + scale(defense, 1) + ")", SomSound.Nope);
                }
                return;
            }

            if (attacker instanceof EnemyData enemyData) {
                enemyData.addMakeDamage(victim, damage);
            }
            if (victim instanceof EnemyData enemyData) {
                enemyData.addHate(attacker, damage);
                enemyData.addTakeDamage(attacker, damage);
            } else if (victim instanceof PlayerData victimData && attacker instanceof PlayerData attackerData) {
                attackerData.pvpAttackTime(300);
                victimData.pvpVictimTime(200);
                victimData.pvpAttacker(attackerData);
                victimData.timerEndTask("PvPVictim", victimData::resetPvPAttacker);
                damage *= PvPMultiply;

                if (PvPRaid.isInPvPRaid(attackerData) && PvPRaid.isInPvPRaid(victimData)) {
                    PvPRaid.addPoint(attackerData, damage / PvPRaid.PointPerMakeDamage);
                    PvPRaid.addPoint(victimData, damage / PvPRaid.PointPerTakeDamage);
                }
            }
        } else {
            damage = victim.damageOverride();
        }

        victim.removeHealth(damage, attacker);
        victim.lastAttacker(attacker);
        if (!hitAnimation.contains(victim)) {
            victim.hurt();
            victim.getEntity().playHurtAnimation(attacker.yaw());
            hitAnimation.add(victim);
        }

        attacker.onMakeDamage(victim, type, multiply, isCritical);
        victim.onTakeDamage(attacker, type, multiply, isCritical);

        String logText = (isCritical ? "§b" : "§f") + scale(damage, 1) + " §f[" + scale(multiply * 100) + "%] §b[" + scale(critical * 100) + "%+" + scale(criticalDamage * 100) + "] §e[" + scale(attack, 1) + "/" + scale(defense, 1) + "] §c[" + scale(victim.getHealth()) + "/" + scale(victim.getStatus(StatusType.MaxHealth)) + "] [" + scale(victim.healthPercent() * 100, 2) + "%] §d[" + scale(penetration * 100) + "%]";
        if (attacker instanceof PlayerData playerData) {
            playerData.addDPS(damage);
            if (playerData.setting().is(PlayerSetting.BooleanEnum.MakeDamageLog)) {
                playerData.sendMessage("§a≪" + logText);
            }
        }

        if (victim instanceof PlayerData playerData) {
            if (playerData.setting().is(PlayerSetting.BooleanEnum.TakeDamageLog)) {
                playerData.sendMessage("§c≫" + logText);
            }
        }
    }

    static void makeHeal(PlayerData healer, SomEntity victim, double multiply) {
        double support = healer.getStatus(StatusType.SPT);
        double heal = support * multiply;

        if (victim instanceof PlayerData playerData && (playerData.isPvPMode() || PvPRaid.isInPvPRaid(playerData) || playerData.bossModeMenu().isInBossTimeAttackMode())) {
            heal *= HealMultiply;

            if (PvPRaid.isInPvPRaid(healer) && PvPRaid.isInPvPRaid(playerData)) {
                PvPRaid.addPoint(healer, heal / PvPRaid.PointPerHeal);
            }
        }

        victim.addHealth(heal);

        String logText = "§e" + scale(heal, 1) + " §f[" + scale(multiply * 100) + "%]";
        if (healer.setting().is(PlayerSetting.BooleanEnum.HealLog)) {
            healer.sendMessage("§a≪" + logText + " §e[" + victim.getName() + "]");
        }

        if (healer != victim && victim instanceof PlayerData playerData) {
            if (playerData.setting().is(PlayerSetting.BooleanEnum.HealLog)) {
                playerData.sendMessage("§c≫" + logText + " §e[" + healer.getName() + "]");
            }
        }
    }

    static void makeMana(SomEntity healer, SomEntity victim, double multiply) {
        double support = healer.getStatus(StatusType.SPT);
        double mana = support * multiply;
        victim.addMana(mana);

        String logText = "§b" + scale(mana, 1) + " §f[" + scale(multiply * 100) + "%]";
        if (healer instanceof HumanData humanData) {
            if (humanData.setting().is(PlayerSetting.BooleanEnum.HealLog)) {
                humanData.sendMessage("§a≪" + logText + " §e[" + victim.getName() + "]");
            }
        }

        if (healer != victim && victim instanceof HumanData humanData) {
            if (humanData.setting().is(PlayerSetting.BooleanEnum.HealLog)) {
                humanData.sendMessage("§c≫" + logText + " §e[" + healer.getName() + "]");
            }
        }
    }
}
