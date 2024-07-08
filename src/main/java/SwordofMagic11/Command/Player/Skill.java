package SwordofMagic11.Command.Player;

import SwordofMagic11.Command.SomCommand;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.DataBase.SkillDataLoader;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Skill.SkillData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Skill implements SomCommand {
    @Override
    public boolean PlayerCommand(Player player, PlayerData playerData, String[] args) {
        if (args.length == 0) {
            playerData.skillMenu().open();
        } else if (args.length == 2) {
            String id = args[0];
            if (SkillDataLoader.getComplete().contains(id)) {
                SkillData skillData = SkillDataLoader.getSkillData(id);
                if (playerData.classes().getMainClass().getSkill().contains(id)) {
                    try {
                        int point = Integer.parseUnsignedInt(args[1]);
                        playerData.skillMenu().usePoint(skillData, point);
                    } catch (Exception ignore) {}
                } else {
                    playerData.sendMessage("§c他クラス§aの§eスキル§aです", SomSound.Nope);
                }
            } else {
                playerData.sendMessage("§a存在しない§eスキル§aです", SomSound.Nope);
            }
        }
        return true;
    }

    @Override
    public boolean Command(CommandSender sender, String[] args) {
        return false;
    }
}

