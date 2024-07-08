package SwordofMagic11.Skill.Process.Passive;

import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Skill.ParamType;
import SwordofMagic11.Skill.Parameter;
import SwordofMagic11.StatusType;

import static SwordofMagic11.Entity.Damage.PvPMultiply;

public class BattleManaRegen {

    public static void register(PlayerData playerData) {
        playerData.setOnTakeDamageFunction("BattleManaRegen", (attacker, type, multiply, isCritical) -> {
            if (playerData.hasSkill("BattleManaRegen") && !playerData.hasTimer("BattleManaRegen")) {
                Parameter parameter = playerData.getSkillParam("BattleManaRegen");
                double value = parameter.getParam(ParamType.Value);
                double mana = playerData.getStatus(StatusType.MaxMana) * value;
                if (attacker instanceof PlayerData playerData2 && playerData2.isPvPMode()) {
                    mana *= PvPMultiply;
                }
                playerData.addMana(mana);
                playerData.timer("BattleManaRegen", parameter.getCoolTime());
            }
            return null;
        });
    }
}
