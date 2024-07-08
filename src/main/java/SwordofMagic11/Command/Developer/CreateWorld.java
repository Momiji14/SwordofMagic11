package SwordofMagic11.Command.Developer;

import SwordofMagic11.Command.SomCommand;
import SwordofMagic11.Component.WorldManager;
import SwordofMagic11.DataBase.DataBase;
import SwordofMagic11.Player.PlayerData;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.WorldCreator;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;

public class CreateWorld implements SomCommand {
    @Override
    public boolean PlayerCommand(Player player, PlayerData playerData, String[] args) {
        if (args.length == 3 && !player.getInventory().getItemInMainHand().isEmpty()) {
            String name = args[0];
            File file = new File("WorldContainer_" + name);
            if (file.exists()) {
                player.sendMessage("§cすでに" + name + "は存在しています");
            } else {
                String newId = "WorldContainer_" + name;
                String baseId = "WorldBaseData";
                try {
                    FileUtils.copyDirectory(new File(baseId), new File(newId), WorldManager::pathIgnore);
                } catch (Exception e) {
                    player.sendMessage("§cエラーが発生しました");
                    throw new RuntimeException(e);
                }
                File config = new File(DataBase.Path, "MapData\\" + args[2] + "\\" + name + ".yml");
                FileConfiguration data = YamlConfiguration.loadConfiguration(config);
                data.set("Display", args[1]);
                data.set("Icon", player.getInventory().getItemInMainHand().getType().toString());
                data.set("MinBalance", 1);
                data.set("MaxBalance", 3);
                data.set("Region", args[2]);
                try {
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    data.save(config);
                } catch (IOException e) {
                    player.sendMessage("§cエラーが発生しました");
                    throw new RuntimeException(e);
                }
                Bukkit.createWorld(new WorldCreator(newId));
                player.sendMessage("§b" + name + "の作成しました");
            }
        } else {
            playerData.sendMessage("§eアイコンにしたいアイテムを手にもって実行してください");
            playerData.sendMessage("§e/createworld <id(アルファベットのみ)> <表示名> <地域>");
            playerData.sendMessage("§e例: /createworld SandRockedVilage 砂漠の村 ソラル砂漠");
        }
        return true;
    }

    @Override
    public boolean Command(CommandSender sender, String[] args) {
        return true;
    }
}

