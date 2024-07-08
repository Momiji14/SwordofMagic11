package SwordofMagic11.Skill.Process;

import SwordofMagic11.Component.SomRay;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Enemy.EnemyData;
import SwordofMagic11.Pet.SyncPet;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Player.Setting.PlayerSetting;
import SwordofMagic11.Skill.ParamType;
import SwordofMagic11.Skill.Parameter;
import SwordofMagic11.Skill.SkillData;
import SwordofMagic11.Skill.SomSkill;
import SwordofMagic11.StatusType;

import static SwordofMagic11.Component.Function.randomDouble;

public class Tame extends SomSkill {
    public Tame(PlayerData owner, SkillData skillData) {
        super(owner, skillData);
    }

    @Override
    public boolean active() {
        Parameter parameter = getParam();
        double percent = parameter.getParam(ParamType.MicroPercent);
        tame(percent);
        return true;
    }

    public boolean tame(double percent) {
        SomRay ray = SomRay.rayLocationEntity(owner, 5, 0.5, owner.enemies(), false);
        if (ray.isHitEntity()) {
            if (ray.getHitEntity() instanceof EnemyData enemyData) {
                if (enemyData.getMobData().hasMemorial()) {
                    if (enemyData.healthPercent() <= 0.1) {
                        if (!enemyData.hasTimer("TimeTrigger")) {
                            enemyData.timer("TimeTrigger", 1200);
                            if (owner.petCageSize() > owner.petMenu().size()) {
                                if (randomDouble() < percent) {
                                    enemyData.delete();
                                    SyncPet.register(enemyData.getMobData(), owner);
                                    owner.sendMessage("§e" + enemyData.getName() + "§aを§eテイム§aしました！", SomSound.Level);
                                    for (PlayerData otherData : PlayerData.getPlayerList()) {
                                        if (otherData != owner && otherData.setting().is(PlayerSetting.BooleanEnum.TameLog)) {
                                            otherData.sendMessage(owner.getDisplayName() + "§aが§e" + enemyData.getName() + "§aを§eテイム§aしました！", SomSound.Tick);
                                        }
                                    }
                                } else {
                                    owner.sendMessage("§c" + enemyData.getName() + "§aに§4警戒§aされました...", SomSound.Tick);
                                }
                                return true;
                            } else {
                                owner.sendMessage("§eケージ§aが一杯です", SomSound.Nope);
                            }
                        } else {
                            owner.sendMessage("§c" + enemyData.getName() + "§aは§4警戒§aしています", SomSound.Nope);
                        }
                    } else {
                        owner.sendMessage("§c" + enemyData.getName() + "§aの§e体力§aが§e10%以下§aである必要があります", SomSound.Nope);
                    }
                } else {
                    owner.sendMessage("§c" + enemyData.getName() + "§aは§eテイム§a出来ません", SomSound.Nope);
                }
            }
        }
        return false;
    }
}
