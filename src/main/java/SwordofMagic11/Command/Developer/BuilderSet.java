package SwordofMagic11.Command.Developer;

import SwordofMagic11.Command.SomCommand;
import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.Player.PlayerData;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BuilderSet implements SomCommand {


    @Override
    public boolean PlayerCommand(Player player, PlayerData playerData, String[] args) {
        player.getInventory().addItem(new CustomItemStack(Material.WOODEN_AXE));
        player.getInventory().addItem(new CustomItemStack(Material.GUNPOWDER));
        player.getInventory().addItem(new CustomItemStack(Material.ARROW));
        player.getInventory().addItem(new CustomItemStack(Material.WOODEN_SHOVEL));
        return true;
    }

    @Override
    public boolean Command(CommandSender sender, String[] args) {
        return true;
    }

}

