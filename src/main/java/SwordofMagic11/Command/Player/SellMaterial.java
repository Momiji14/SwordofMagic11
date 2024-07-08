package SwordofMagic11.Command.Player;

import SwordofMagic11.Command.SomCommand;
import SwordofMagic11.Command.SomTabComplete;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Component.SomTask;
import SwordofMagic11.DataBase.MaterialDataLoader;
import SwordofMagic11.Item.Material.MaterialData;
import SwordofMagic11.Player.Market.MarketSystemMaterial;
import SwordofMagic11.Player.PlayerData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SellMaterial implements SomCommand, SomTabComplete {
    @Override
    public boolean PlayerCommand(Player player, PlayerData playerData, String[] args) {
        SomTask.async(() -> {
            if (!playerData.getMap().isCity()) {
                playerData.sendReqInCity();
                return;
            }
            if (args.length == 2) {
                String id = args[0];
                if (Lock.check(playerData, id)) return;
                int amount = Integer.parseUnsignedInt(args[1]);
                if (playerData.materialMenu().getStorage().containsKey(id)) {
                    if (playerData.hasMaterial(id, amount)) {
                        MaterialData material = MaterialDataLoader.getMaterialData(id);
                        playerData.removeMaterial(id, amount);
                        playerData.addMel(material.getSell() * amount);
                        MarketSystemMaterial.add(id, amount);
                        playerData.sendMessage(id + "x" + amount + "§aを§b売却§aしました", SomSound.Tick);
                        return;
                    }
                }
                playerData.sendMessage(id + "x" + amount + "§aを所持していません", SomSound.Nope);
                return;
            }
            playerData.sendMessage("§e/sellMaterial <Material> <Amount>");
        });
        return true;
    }

    @Override
    public boolean Command(CommandSender sender, String[] args) {
        return false;
    }

    @Override
    public List<String> PlayerTabComplete(Player player, PlayerData playerData, Command command, String[] args) {
        switch (args.length) {
            case 1 -> {
                return new ArrayList<>(playerData.materialMenu().getStorage().keySet());
            }
            case 2 -> {
                if (playerData.materialMenu().getStorage().containsKey(args[0])) {
                    return Collections.singletonList(String.valueOf(playerData.getMaterial(args[0])));
                } else {
                    return Collections.singletonList("このアイテムを所持していません");
                }
            }
        }
        return null;
    }

    @Override
    public List<String> TabComplete(CommandSender sender, Command command, String[] args) {
        return null;
    }
}

