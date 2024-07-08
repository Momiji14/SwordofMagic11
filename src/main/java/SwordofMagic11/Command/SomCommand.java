package SwordofMagic11.Command;

import SwordofMagic11.Player.PlayerData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface SomCommand extends CommandExecutor {
    @Override
    default boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (sender instanceof Player player) {
            PlayerData playerData = PlayerData.get(player);
            if (PlayerCommand(player, playerData, args)) {
                return true;
            }
        }
        return Command(sender, args);
    }

    //Playerがコマンド使用した際に簡易化
    boolean PlayerCommand(Player player, PlayerData playerData, String[] args);

    //Player以外がコマンド使用した際に簡易化
    boolean Command(CommandSender sender, String[] args);

}
