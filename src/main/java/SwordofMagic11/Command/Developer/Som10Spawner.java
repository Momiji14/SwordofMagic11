package SwordofMagic11.Command.Developer;

import SwordofMagic11.Command.SomCommand;
import SwordofMagic11.Command.SomTabComplete;
import SwordofMagic11.DataBase.DataBase;
import SwordofMagic11.DataBase.SkillDataLoader;
import SwordofMagic11.Player.PlayerData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static SwordofMagic11.SomCore.Log;

public class Som10Spawner implements SomCommand, SomTabComplete {
    @Override
    public boolean PlayerCommand(Player player, PlayerData playerData, String[] args) {
        File file = new File(DataBase.Path, "Som10Spawner\\" + args[0]);
        File oldFile = new File(DataBase.Path, "Som10Dungeon\\" + args[1]);
        FileConfiguration oldData = YamlConfiguration.loadConfiguration(oldFile);
        if (!oldFile.exists()) {
            playerData.sendMessage(oldFile + "が存在しません");
            return true;
        }
        if (!oldData.isSet("StartLocation.x")) {
            playerData.sendMessage("OldDataが存在しません");
            return true;
        }
        int offsetX = oldData.getInt("StartLocation.x") - player.getLocation().getBlockX();
        int offsetY = oldData.getInt("StartLocation.y") - player.getLocation().getBlockY();
        int offsetZ = oldData.getInt("StartLocation.z") - player.getLocation().getBlockZ();
        Log(offsetX + "," + offsetY + "," + offsetZ);
        int level = Integer.parseInt(args[2]);
        for (File tempFile : file.listFiles()) {
            FileConfiguration tempData = YamlConfiguration.loadConfiguration(tempFile);
            File mapFile = new File(DataBase.Path, "MapData\\時歪\\" + tempData.getString("MapData") + ".yml");
            FileConfiguration mapData = YamlConfiguration.loadConfiguration(mapFile);
            List<String> spawners = mapData.getStringList("Spawners");
            String prefix = tempData.getString("MobData");
            if (spawners.contains(prefix)) {
                int index = 2;
                while (spawners.contains(prefix + index)) {
                    index++;
                }
                prefix += index;
            }

            spawners.add(prefix);
            mapData.set("Spawners", spawners);
            mapData.set(prefix + "." + "MobData", tempData.getString("MobData"));
            mapData.set(prefix + "." + "MinLevel", level);
            mapData.set(prefix + "." + "MaxLevel", level+5);
            mapData.set(prefix + "." + "Radius", tempData.getDouble("Radius"));
            mapData.set(prefix + "." + "MaxEnemy", tempData.getInt("MaxEnemy"));
            mapData.set(prefix + "." + "CoolTime", 50);
            mapData.set(prefix + "." + "Location.x", tempData.getInt("Location.x") - offsetX);
            mapData.set(prefix + "." + "Location.y", tempData.getInt("Location.y") - offsetY);
            mapData.set(prefix + "." + "Location.z", tempData.getInt("Location.z") - offsetZ);
            try {
                mapData.save(mapFile);
                playerData.sendMessage("Import " + tempFile.getName());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return true;
    }

    @Override
    public boolean Command(CommandSender sender, String[] args) {
        return false;
    }

    @Override
    public List<String> PlayerTabComplete(Player player, PlayerData playerData, Command command, String[] args) {
        List<String> complete = new ArrayList<>();
        switch (args.length) {
            case 1 -> {
                File file = new File(DataBase.Path, "Som10Spawner");
                Collections.addAll(complete, file.list());
            }
            case 2 -> {
                File file = new File(DataBase.Path, "Som10Dungeon");
                Collections.addAll(complete, file.list());
            }
        }
        return complete;
    }

    @Override
    public List<String> TabComplete(CommandSender sender, Command command, String[] args) {
        return null;
    }
}
