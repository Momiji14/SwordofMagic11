package SwordofMagic11.Command.Developer;

import SwordofMagic11.Command.SomCommand;
import SwordofMagic11.Player.PlayerData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Save implements SomCommand {
    @Override
    public boolean PlayerCommand(Player player, PlayerData playerData, String[] args) {
        playerData.save();
        return true;
    }

    @Override
    public boolean Command(CommandSender sender, String[] args) {
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("restart")) {
                for (PlayerData playerData : PlayerData.getPlayerList()) {
                    playerData.saveLocation();
                }
            }
        }
        return true;
    }
}
