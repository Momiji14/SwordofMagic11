package SwordofMagic11.Command.Developer;

import SwordofMagic11.Command.SomCommand;
import SwordofMagic11.Command.SomTabComplete;
import SwordofMagic11.DataBase.SkillDataLoader;
import SwordofMagic11.Player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

import static SwordofMagic11.Component.Function.decoText;

public class ViewMaterialStorage implements SomCommand, SomTabComplete {
    @Override
    public boolean PlayerCommand(Player player, PlayerData playerData, String[] args) {
        Player target = Bukkit.getPlayer(args[0]);
        if (target != null && target.isOnline()) {
            PlayerData targetData = PlayerData.get(target);
            List<String> message = new ArrayList<>();
            message.add(decoText(targetData.getName() + "'s MaterialStorage"));
            targetData.materialMenu().getStorage().forEach((id, amount) -> message.add("§7・§f" + id + "x" + amount));
            playerData.sendMessage(message);
        } else {
            playerData.sendMessage("存在しないプレイヤーです");
        }
        return true;
    }

    @Override
    public boolean Command(CommandSender sender, String[] args) {
        return false;
    }

    @Override
    public List<String> PlayerTabComplete(Player player, PlayerData playerData, Command command, String[] args) {
        return SkillDataLoader.getComplete();
    }

    @Override
    public List<String> TabComplete(CommandSender sender, Command command, String[] args) {
        return null;
    }
}
