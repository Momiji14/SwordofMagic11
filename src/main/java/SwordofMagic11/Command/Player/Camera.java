package SwordofMagic11.Command.Player;

import SwordofMagic11.Command.SomCommand;
import SwordofMagic11.Command.SomTabComplete;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static SwordofMagic11.Component.Function.decoText;

public class Camera implements SomCommand, SomTabComplete {

    @Override
    public boolean PlayerCommand(Player player, PlayerData playerData, String[] args) {
        if (args[0].equalsIgnoreCase("stop")) {
            playerData.camera().stop();
        } else if (args.length == 1) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target != null && target.isOnline()) {
                if (target != player) {
                    playerData.camera().start(PlayerData.get(target));
                }
            } else {
                playerData.sendMessage("§c存在しないプレイヤーです", SomSound.Nope);
            }
            return true;
        } else {
            List<String> message = new ArrayList<>();
            message.add(decoText("Camera"));
            message.add("§7・§e/camera <player>");
            message.add("§7・§e/camera stop");
            playerData.sendMessage(message, SomSound.Nope);
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
        if (args.length == 1) {
            if (playerData.camera().isCamera()) {
                complete.add("stop");
                return complete;
            } else {
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

