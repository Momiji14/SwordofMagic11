package SwordofMagic11.Command.Developer;

import SwordofMagic11.Command.SomCommand;
import SwordofMagic11.Command.SomTabComplete;
import SwordofMagic11.DataBase.MapDataLoader;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.SomCore;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class UnloadWorld implements SomCommand, SomTabComplete {
    @Override
    public boolean PlayerCommand(Player player, PlayerData playerData, String[] args) {
        String id = args[0];
        for (World world : Bukkit.getWorlds()) {
            if (world.getName().equalsIgnoreCase(id)) {
                for (Player worldPlayer : world.getPlayers()) {
                    PlayerData.get(worldPlayer).teleport(SomCore.getGlobalSpawnLocation());
                }
                Bukkit.unloadWorld(world, true);
                player.sendMessage("§b" + id + "をアンロードしました");
                return true;
            }
        }
        player.sendMessage("§c" + id + "は存在しないまたはロードされていません");
        return true;
    }

    @Override
    public boolean Command(CommandSender sender, String[] args) {
        return true;
    }

    @Override
    public List<String> PlayerTabComplete(Player player, PlayerData playerData, Command command, String[] args) {
        List<String> complete = new ArrayList<>();
        for (World world : Bukkit.getWorlds()) {
            complete.add(world.getName());
        }
        return complete;
    }

    @Override
    public List<String> TabComplete(CommandSender sender, Command command, String[] args) {
        return null;
    }
}

