package SwordofMagic11.Command.Developer;

import SwordofMagic11.Command.SomCommand;
import SwordofMagic11.Command.SomTabComplete;
import SwordofMagic11.Component.SomSQL;
import SwordofMagic11.DataBase.DataBase;
import SwordofMagic11.Player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

import static SwordofMagic11.Component.Function.decoLore;
import static SwordofMagic11.Component.Function.decoText;

public class AddEld implements SomCommand, SomTabComplete {

    @Override
    public boolean PlayerCommand(Player player, PlayerData playerData, String[] args) {
        String username = args[0];
        int addEld = Integer.parseInt(args[1]);
        Player target = Bukkit.getPlayer(username);
        if (target != null && target.isOnline()) {
            PlayerData targetData = PlayerData.get(target);
            double belowEld = targetData.donation().getEld();
            double belowDonation = targetData.donation().getTotalDonation();
            targetData.donation().addEld(addEld);
            List<String> message = new ArrayList<>();
            message.add(decoText(targetData.getName()));
            message.add(decoLore("エルド") + belowEld + " -> " + targetData.donation().getEld());
            message.add(decoLore("合計寄付") + belowDonation + " -> " + targetData.donation().getTotalDonation());
            playerData.sendMessage(message);
        } else if (SomSQL.exists(DataBase.Table.PlayerData, "Username", username)) {
            String uuid = SomSQL.getString(DataBase.Table.PlayerData, "Username", username, "UUID");
            int totalDonation;
            int eld;
            if (SomSQL.exists(DataBase.Table.PlayerDonation, "UUID", uuid, "TotalDonation")) {
                totalDonation = SomSQL.getInt(DataBase.Table.PlayerDonation, "UUID", uuid, "TotalDonation");
            } else {
                totalDonation = 0;
            }
            if (SomSQL.exists(DataBase.Table.PlayerDonation, "UUID", uuid, "Eld")) {
                eld = SomSQL.getInt(DataBase.Table.PlayerDonation, "UUID", uuid, "Eld");
            } else {
                eld = 0;
            }
            int belowDonation = totalDonation;
            int belowEld = eld;
            eld += addEld;
            totalDonation += addEld;
            SomSQL.setSql(DataBase.Table.PlayerDonation, "UUID", uuid, "TotalDonation", totalDonation);
            SomSQL.setSql(DataBase.Table.PlayerDonation, "UUID", uuid, "Eld", eld);
            SomSQL.setSql(DataBase.Table.PlayerDonation, "UUID", uuid, "Username", username);
            List<String> message = new ArrayList<>();
            message.add(decoText(username));
            message.add(decoLore("エルド") + belowEld + " -> " + eld);
            message.add(decoLore("合計寄付") + belowDonation + " -> " + totalDonation);
            playerData.sendMessage(message);
        } else {
            playerData.sendMessage("存在しないプレイヤーです");
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
            case 1 -> {
                return SomTabComplete.getPlayerList();
            }
            case 2 -> {
                return new ArrayList<>() {{ add("Eld"); }};
            }
        }
        return null;
    }

    @Override
    public List<String> TabComplete(CommandSender sender, Command command, String[] args) {
        return null;
    }
}

