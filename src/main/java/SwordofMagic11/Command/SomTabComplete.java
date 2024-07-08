package SwordofMagic11.Command;

import SwordofMagic11.Player.PlayerData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public interface SomTabComplete extends TabCompleter {
    @Override
    default List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        List<String> list = new ArrayList<>();
        List<String> base = TabComplete(sender, command, args);
        if (base != null) list.addAll(base);
        if (sender instanceof Player player) {
            List<String> data = PlayerTabComplete(player, PlayerData.get(player), command, args);
            if (data != null) list.addAll(data);
        }
        if (args.length > 0) list.removeIf(tab -> !tab.toLowerCase().contains(args[args.length - 1].toLowerCase()));
        return list;
    }

    List<String> PlayerTabComplete(Player player, PlayerData playerData, Command command, String[] args);

    List<String> TabComplete(CommandSender sender, Command command, String[] args);

    static List<String> getPlayerList() {
        List<String> complete = new ArrayList<>();
        for (PlayerData playerData : PlayerData.getPlayerList()) {
            complete.add(playerData.getPlayer().getName());
        }
        return complete;
    }
}
