package SwordofMagic11.Command.Developer;

import SwordofMagic11.Command.SomCommand;
import SwordofMagic11.Command.SomTabComplete;
import SwordofMagic11.DataBase.MapDataLoader;
import SwordofMagic11.Map.MapData;
import SwordofMagic11.Player.PlayerData;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class CreateInstance implements SomCommand, SomTabComplete {
    @Override
    public boolean PlayerCommand(Player player, PlayerData playerData, String[] args) {
        if (args.length == 2) {
            String id = args[0];
            String suffix = args[1];
            if (id.equals("all")) {
                int i = 0;
                for (MapData mapData : MapDataLoader.getMapDataList()) {
                    switch (suffix) {
                        case "CityOnly" -> {
                            if (!mapData.isCity()) continue;
                        }
                        case "SpawnerOnly" -> {
                            if (mapData.getSpawner().isEmpty()) continue;
                        }
                    }
                    mapData.getInstance(null, UUID.randomUUID().toString());
                    i++;
                }
                player.sendMessage("§b" + i + "個のInstanceを作成しました");
            } else {
                World instance = MapDataLoader.getMapData(id).createInstance(args[1]);
                playerData.teleport(instance.getSpawnLocation());
                player.sendMessage("§b" + id + "のInstanceを作成しました");
            }
        }
        return true;
    }

    @Override
    public boolean Command(CommandSender sender, String[] args) {
        return true;
    }

    @Override
    public List<String> PlayerTabComplete(Player player, PlayerData playerData, Command command, String[] args) {
        return MapDataLoader.getComplete();
    }

    @Override
    public List<String> TabComplete(CommandSender sender, Command command, String[] args) {
        return null;
    }
}

