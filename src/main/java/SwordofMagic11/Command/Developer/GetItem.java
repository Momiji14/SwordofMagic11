package SwordofMagic11.Command.Developer;

import SwordofMagic11.Command.SomCommand;
import SwordofMagic11.Command.SomTabComplete;
import SwordofMagic11.DataBase.ItemDataLoader;
import SwordofMagic11.Item.SomEquip;
import SwordofMagic11.Item.SomItem;
import SwordofMagic11.Item.SyncItem;
import SwordofMagic11.Player.PlayerData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class GetItem implements SomCommand, SomTabComplete {

    @Override
    public boolean PlayerCommand(Player player, PlayerData playerData, String[] args) {
        SomItem item = SyncItem.register(args[0], playerData, SyncItem.State.ItemInventory);
        if (args.length > 1 && item instanceof SomEquip equip) {
            equip.setPlus(Integer.parseInt(args[1]));
        }
        if (args.length > 2 && item instanceof SomEquip equip) {
            equip.setTrance(Integer.parseInt(args[2]));
        }
        playerData.updateInventory();
        return true;
    }

    @Override
    public boolean Command(CommandSender sender, String[] args) {
        return false;
    }

    @Override
    public List<String> PlayerTabComplete(Player player, PlayerData playerData, Command command, String[] args) {
        if (args.length == 1) return ItemDataLoader.getComplete();
        return null;
    }

    @Override
    public List<String> TabComplete(CommandSender sender, Command command, String[] args) {
        return null;
    }
}
