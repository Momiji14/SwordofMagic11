package SwordofMagic11.Command.Developer;

import SwordofMagic11.Command.SomCommand;
import SwordofMagic11.Component.WorldManager;
import SwordofMagic11.Player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;

public class LoadWorld implements SomCommand {
    @Override
    public boolean PlayerCommand(Player player, PlayerData playerData, String[] args) {
        String name = args[0];
        File file = new File("WorldContainer_" + name);
        if (file.exists()) {
            World world = Bukkit.createWorld(new WorldCreator(file.getName()));
            playerData.teleport(world.getSpawnLocation());
            player.sendMessage("§b" + name + "をロードしました");
        } else {
            player.sendMessage("§c" + name + "は存在しません");
        }
        return true;
    }

    @Override
    public boolean Command(CommandSender sender, String[] args) {
        return true;
    }
}

