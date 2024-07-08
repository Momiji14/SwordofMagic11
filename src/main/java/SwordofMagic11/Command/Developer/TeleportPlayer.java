package SwordofMagic11.Command.Developer;

import SwordofMagic11.Command.SomCommand;
import SwordofMagic11.Command.SomTabComplete;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.Player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class TeleportPlayer implements SomCommand, SomTabComplete {


    @Override
    public boolean PlayerCommand(Player player, PlayerData playerData, String[] args) {
        switch (args.length) {
            case 1 -> {
                Player target = Bukkit.getPlayer(args[0]);
                if (target != null && target.isOnline()) {
                    playerData.teleport(target.getLocation());
                } else {
                    playerData.sendMessage("§c対象が存在しません", SomSound.Nope);
                }
            }
            case 2 -> {
                if (args[0].equalsIgnoreCase("AllToMe")) {
                    for (PlayerData targetData : PlayerData.getPlayerList()) {
                        if (targetData != playerData) {
                            targetData.teleport(playerData.getLocation());
                        }
                    }
                } else {
                    Player target = Bukkit.getPlayer(args[0]);
                    Player target2 = Bukkit.getPlayer(args[1]);
                    if (target != null && target.isOnline()) {
                        if (target2 != null && target2.isOnline()) {
                            PlayerData.get(target).teleport(target2.getLocation());
                        } else {
                            playerData.sendMessage("§c対象2が存在しません", SomSound.Nope);
                        }
                    } else {
                        playerData.sendMessage("§c対象1が存在しません", SomSound.Nope);
                    }
                }
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
        switch (args.length) {
            case 1, 2 -> {
                return SomTabComplete.getPlayerList();
            }
        }
        return null;
    }

    @Override
    public List<String> TabComplete(CommandSender sender, Command command, String[] args) {
        return null;
    }
}

