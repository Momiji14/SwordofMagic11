package SwordofMagic11.Command.Developer;

import SwordofMagic11.Command.SomCommand;
import SwordofMagic11.DataBase.DataBase;
import SwordofMagic11.Item.Material.MaterialData;
import SwordofMagic11.Player.PlayerData;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;

public class CreateMaterial implements SomCommand {
    @Override
    public boolean PlayerCommand(Player player, PlayerData playerData, String[] args) {
        if (args.length >= 1) {
            try {
                MaterialData materialData = new MaterialData();
                materialData.setId(args[0]);
                materialData.setDisplay(args[0]);
                materialData.setIcon(player.getInventory().getItemInMainHand().getType());
                File file = new File(DataBase.Path, "MaterialData\\" + materialData.getId() + ".yml");
                FileConfiguration data = YamlConfiguration.loadConfiguration(file);
                data.set("Display", materialData.getDisplay());
                data.set("Icon", materialData.getIcon().toString());
                if (args.length >= 2) data.set("Sell", Integer.parseUnsignedInt(args[1]));
                data.save(file);
                player.sendMessage("§b" + materialData.getDisplay() + "を作成しました");
            } catch (IOException e) {
                player.sendMessage("§cエラーが発生しました");
            }
        } else {
            player.sendMessage("§c/createMaterial <display> <sell>");
        }
        return true;
    }

    @Override
    public boolean Command(CommandSender sender, String[] args) {
        return false;
    }
}
