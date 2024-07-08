package SwordofMagic11.Command.Player;

import SwordofMagic11.Command.SomCommand;
import SwordofMagic11.Command.SomTabComplete;
import SwordofMagic11.Component.SomJson;
import SwordofMagic11.Component.SomSQL;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.DataBase.DataBase;
import SwordofMagic11.Player.ClassType;
import SwordofMagic11.Player.Pallet;
import SwordofMagic11.Player.PlayerData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

import static SwordofMagic11.Component.Function.decoText;

public class PalletStorage implements SomCommand, SomTabComplete {
    @Override
    public boolean PlayerCommand(Player player, PlayerData playerData, String[] args) {
        if (args.length >= 2) {
            try {
                int index = Integer.parseUnsignedInt(args[1]);
                if (index > 0) {
                    if (index <= playerData.donation().additionPalletStorageSize()) {
                        switch (args[0].toLowerCase()) {
                            case "load" -> load(playerData, index);
                            case "save" -> save(playerData, index);
                            case "delete" -> delete(playerData, index);
                        }
                    } else if (playerData.donation().additionPalletStorageSize() > 0) {
                        playerData.sendMessage("§c所持していないパレットストレージです [" + index + "/" + playerData.donation().additionPalletStorageSize() + "]", SomSound.Nope);
                    } else {
                        playerData.sendMessage("§cパレットストレージを所持していません", SomSound.Nope);
                    }
                    return true;
                }
            } catch (Exception ignore) {}
        }
        List<String> message = new ArrayList<>();
        message.add(decoText("パレットストレージ"));
        message.add("§7・§e/palletStorage save <index>");
        message.add("§7・§e/palletStorage load <index>");
        message.add("§7・§e/palletStorage delete <index>");
        playerData.sendMessage(message, SomSound.Nope);
        return true;
    }

    @Override
    public boolean Command(CommandSender sender, String[] args) {
        return false;
    }

    @Override
    public List<String> PlayerTabComplete(Player player, PlayerData playerData, Command command, String[] args) {
        return List.of();
    }

    @Override
    public List<String> TabComplete(CommandSender sender, Command command, String[] args) {
        return List.of();
    }

    public void load(PlayerData playerData, int index) {
        String[] key = new String[]{"UUID", "ID"};
        String[] value = new String[]{playerData.getUUID(), String.valueOf(index)};
        Pallet[] pallet = playerData.classes().getPallet();
        if (SomSQL.exists(DataBase.Table.PalletStorage, key, value, "Pallet")) {
            SomJson json = new SomJson(SomSQL.getString(DataBase.Table.PalletStorage, key, value, "Pallet"));
            for (int i = 0; i < pallet.length; i++) {
                pallet[i] = Pallet.fromJson(json.getSomJson("Pallet-" + i), playerData);
            }
            playerData.sendMessage("§eパレット[" + index + "]§aの§e登録情報§aを読み込みました");
        } else {
            playerData.sendMessage("§eパレット[" + index + "]§aの§e登録情報§aがありません");
        }
    }

    public void save(PlayerData playerData, int index) {
        String[] key = new String[]{"UUID", "ID"};
        String[] value = new String[]{playerData.getUUID(), String.valueOf(index)};
        Pallet[] pallet = playerData.classes().getPallet();
        SomJson json = new SomJson();
        for (int i = 0; i < pallet.length; i++) {
            if (pallet[i] != null) {
                json.set("Pallet-" + i, pallet[i].toJson());
            }
        }
        if (!json.isEmpty()) {
            SomSQL.setSql(DataBase.Table.PalletStorage, key, value, "Pallet", json.toString());
            playerData.sendMessage("§eパレット[" + index + "]§aに§e登録情報§aを§b保存§aしました");
        } else {
            playerData.sendMessage("§eパレット§aが§c空§aです");
        }
    }

    public void delete(PlayerData playerData, int index) {
        String[] key = new String[]{"UUID", "ID"};
        String[] value = new String[]{playerData.getUUID(), String.valueOf(index)};
        if (SomSQL.exists(DataBase.Table.PalletStorage, key, value)) {
            SomSQL.delete(DataBase.Table.PalletStorage, key, value);
            playerData.sendMessage("§eパレット[" + index + "]§aの§e登録情報§aを§c削除§aしました");
        } else {
            playerData.sendMessage("§eパレット[" + index + "]§aの§e登録情報§aがありません");
        }
    }
}
