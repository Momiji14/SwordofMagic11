package SwordofMagic11.Command.Developer;

import SwordofMagic11.Command.SomCommand;
import SwordofMagic11.Command.SomTabComplete;
import SwordofMagic11.DataBase.MaterialDataLoader;
import SwordofMagic11.Item.Material.MaterialData;
import SwordofMagic11.Player.PlayerData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class GetMaterial implements SomCommand, SomTabComplete {

    @Override
    public boolean PlayerCommand(Player player, PlayerData playerData, String[] args) {
        MaterialData material = MaterialDataLoader.getMaterialData(args[0]);
        playerData.addMaterial(material, (args.length > 1 ? Integer.parseInt(args[1]) : 1));
        return true;
    }

    @Override
    public boolean Command(CommandSender sender, String[] args) {
        return false;
    }

    @Override
    public List<String> PlayerTabComplete(Player player, PlayerData playerData, Command command, String[] args) {
        if (args.length == 1) return MaterialDataLoader.getComplete();
        return null;
    }

    @Override
    public List<String> TabComplete(CommandSender sender, Command command, String[] args) {
        return null;
    }
}
