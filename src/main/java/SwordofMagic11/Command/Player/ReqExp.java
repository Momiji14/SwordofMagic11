package SwordofMagic11.Command.Player;

import SwordofMagic11.Command.SomCommand;
import SwordofMagic11.Command.SomTabComplete;
import SwordofMagic11.Enemy.EnemyData;
import SwordofMagic11.Player.Classes;
import SwordofMagic11.Player.Gathering.GatheringMenu;
import SwordofMagic11.Player.PlayerData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

import static SwordofMagic11.Component.Function.scale;
import static SwordofMagic11.Player.Classes.MaxLevel;

public class ReqExp implements SomCommand, SomTabComplete {
    @Override
    public boolean PlayerCommand(Player player, PlayerData playerData, String[] args) {
        if (args.length >= 1) {
            if (args[0].contains("to")) {
                int tier = args.length >= 2 ? Integer.parseUnsignedInt(args[1]) : 0;
                String[] text = args[0].split("to");
                int minLevel = Integer.parseUnsignedInt(text[0]);
                int maxLevel = Integer.parseUnsignedInt(text[1]);
                double exp = 0;
                for (int level = minLevel; level < maxLevel; level++) {
                    exp += Classes.ReqExp(level, tier);
                }
                playerData.sendMessage("§aClassReqExp: " + scale(exp));
            } else {
                int level = Integer.parseUnsignedInt(args[0]);
                int tier = args.length >= 2 ? Integer.parseUnsignedInt(args[1]) : 0;
                if (1 <= level && level <= MaxLevel) {
                    playerData.sendMessage("§aClassReqExp: " + scale(Classes.ReqExp(level, tier)));
                    playerData.sendMessage("§cMobDropExp: " + scale(EnemyData.BaseExp(level)));
                    playerData.sendMessage("§eGatheringReqExp: " + scale(GatheringMenu.ReqExp(level)));
                    return true;
                }
            }
        }
        playerData.sendMessage("§e/reqExp: <1~" + MaxLevel + "> [<tier>]");
        return true;
    }

    @Override
    public boolean Command(CommandSender sender, String[] args) {
        return false;
    }

    @Override
    public List<String> PlayerTabComplete(Player player, PlayerData playerData, Command command, String[] args) {
        return null;
    }

    @Override
    public List<String> TabComplete(CommandSender sender, Command command, String[] args) {
        return null;
    }
}
