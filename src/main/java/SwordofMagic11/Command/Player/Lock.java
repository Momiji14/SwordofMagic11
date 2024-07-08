package SwordofMagic11.Command.Player;

import SwordofMagic11.Command.SomCommand;
import SwordofMagic11.Command.SomTabComplete;
import SwordofMagic11.Component.SomSQL;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Component.SomTask;
import SwordofMagic11.DataBase.CapsuleDataLoader;
import SwordofMagic11.DataBase.DataBase;
import SwordofMagic11.DataBase.MaterialDataLoader;
import SwordofMagic11.Item.CapsuleData;
import SwordofMagic11.Item.Material.MaterialData;
import SwordofMagic11.Item.SomItem;
import SwordofMagic11.Item.SyncItem;
import SwordofMagic11.Player.PlayerData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

import static SwordofMagic11.Component.Function.boolText;
import static SwordofMagic11.Component.Function.decoText;

public class Lock implements SomCommand, SomTabComplete {

    private void sendHelp(PlayerData playerData) {
        List<String> message = new ArrayList<>() {{
            add(decoText("Lock Command"));
            add("§7・§e/lock item <itemID:itemUUID>");
            add("§7・§e/lock material <素材ID>");
            add("§7・§e/lock capsule <カプセルID>");
        }};
        playerData.sendMessage(message, SomSound.Nope);
    }

    @Override
    public boolean PlayerCommand(Player player, PlayerData playerData, String[] args) {
        SomTask.async(() -> {
            if (args.length >= 2) {
                String id = args[1];
                switch (args[0].toLowerCase()) {
                    case "item" -> {
                        if (id.equalsIgnoreCase("gui")) {
                            playerData.lockItem().open();
                            return;
                        }
                        String uuid = id.split(":")[0];
                        if (SyncItem.hasSomItem(uuid)) {
                            SomItem item = SyncItem.getSomItem(uuid);
                            boolean next = !isLock(playerData, uuid);
                            setLock(playerData, uuid, next);
                            playerData.sendMessage(item.getDisplay() + "§aの§e売却防止§aを" + boolText(next) + "§aにしました", SomSound.Tick);
                            return;
                        }
                    }
                    case "material" -> {
                        if (MaterialDataLoader.getComplete().contains(id)) {
                            boolean next = !isLock(playerData, id);
                            setLock(playerData, id, next);
                            MaterialData material = MaterialDataLoader.getMaterialData(id);
                            playerData.sendMessage(material.getDisplay() + "§aの§e売却防止§aを" + boolText(next) + "§aにしました", SomSound.Tick);
                            return;
                        }
                    }
                    case "capsule" -> {
                        if (CapsuleDataLoader.getComplete().contains(id)) {
                            boolean next = !isLock(playerData, id);
                            setLock(playerData, id, next);
                            CapsuleData capsule = CapsuleDataLoader.getCapsuleData(id);
                            playerData.sendMessage(capsule.getDisplay() + "§aの§e売却防止§aを" + boolText(next) + "§aにしました", SomSound.Tick);
                            return;
                        }
                    }
                }
            }
            sendHelp(playerData);
        });
        return true;
    }

    @Override
    public boolean Command(CommandSender sender, String[] args) {
        return false;
    }

    @Override
    public List<String> PlayerTabComplete(Player player, PlayerData playerData, Command command, String[] args) {
        List<String> complete = new ArrayList<>();
        switch (args.length) {
            case 1 -> {
                complete.add("item");
                complete.add("material");
                complete.add("capsule");
            }
            case 2 -> {
                switch (args[0].toLowerCase()) {
                    case "item" -> {
                        complete.add("gui");
                        for (SomItem somItem : playerData.itemInventory().getList()) {
                            complete.add(somItem.getUUID() + ":" + somItem.getDisplay());
                        }
                    }
                    case "material" -> {
                        return new ArrayList<>(playerData.materialMenu().getStorage().keySet());
                    }
                    case "capsule" -> {
                        return new ArrayList<>(playerData.capsuleMenu().getStorage().keySet());
                    }
                }
            }
        }
        return complete;
    }

    @Override
    public List<String> TabComplete(CommandSender sender, Command command, String[] args) {
        return null;
    }

    private static final String[] key = new String[]{"UUID", "ID"};

    public static void setLock(PlayerData playerData, String id, boolean lock) {
        String[] value = new String[]{playerData.getUUID(), id};
        if (lock) {
            SomSQL.setSql(DataBase.Table.PlayerItemLock, key, value, "Locked", "true");
        } else {
            delete(playerData, id);
        }
    }

    public static boolean isLock(PlayerData playerData, String id) {
        String[] value = new String[]{playerData.getUUID(), id};
        return SomSQL.exists(DataBase.Table.PlayerItemLock, key, value);
    }

    public static void delete(PlayerData playerData, String id) {
        String[] value = new String[]{playerData.getUUID(), id};
        SomSQL.delete(DataBase.Table.PlayerItemLock, key, value);
    }

    public static boolean check(PlayerData playerData, String id) {
        if (isLock(playerData, id)) {
            playerData.sendMessage("§aこの§eアイテム§aは§e売却防止§aが§b有効§aです", SomSound.Nope);
            return true;
        }
        return false;
    }
}

