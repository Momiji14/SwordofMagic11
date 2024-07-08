package SwordofMagic11.Item;

import SwordofMagic11.Component.SomSQL;
import SwordofMagic11.DataBase.DataBase;
import SwordofMagic11.DataBase.ItemDataLoader;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Player.Setting.PlayerSetting;
import com.github.jasync.sql.db.RowData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static SwordofMagic11.SomCore.Log;

public class SyncItem {
    private static final ConcurrentHashMap<String, SomItem> itemDataBase = new ConcurrentHashMap<>();

    public static List<SomItem> getList(PlayerData playerData) {
        List<SomItem> list = new ArrayList<>();
        for (SomItem item : itemDataBase.values()) {
            if (item.hasOwner() && item.getOwner() == playerData) {
                list.add(item);
            }
        }
        return list;
    }

    public static List<SomItem> getList(PlayerData playerData, State state) {
        List<SomItem> list = getList(playerData);
        list.removeIf(item -> item.getState() != state);
        return list;
    }

    public static void load() {
        for (RowData objects : SomSQL.getSqlList(DataBase.Table.ItemStorage, "*")) {
            if (ItemDataLoader.getComplete().contains(objects.getString("ItemID"))) {
                SomItem item = ItemDataLoader.getItemData(objects.getString("ItemID"));
                item.setUUID(objects.getString("UUID"));
                item.setState(State.valueOf(objects.getString("State")));
                item.setSync(true);
                itemDataBase.put(item.getUUID(), item);
            } else {
                delete(objects.getString("UUID"));
            }
        }
    }

    public static void delete(String uuid) {
        PlayerData playerData = null;
        if (itemDataBase.containsKey(uuid)) playerData = itemDataBase.get(uuid).getOwner();
        itemDataBase.remove(uuid);
        SomSQL.delete(DataBase.Table.ItemStorage, "UUID", uuid);
        if (playerData != null) playerData.updateInventory();
    }

    public static SomItem register(String itemId, PlayerData playerData, State state) {
        if (itemId.isEmpty()) {
            Log("ItemID is null", true);
            return null;
        }
        SomItem item = ItemDataLoader.getItemData(itemId);
        item.randomUUID();
        item.setOwner(playerData);
        item.setState(state);
        item.setSync(true);
        SomSQL.setSql(DataBase.Table.ItemStorage, "UUID", item.getUUID(), "ItemID", item.getId());
        itemDataBase.put(item.getUUID(), item);
        if (playerData.setting().is(PlayerSetting.BooleanEnum.ItemLog)) {
            playerData.sendMessage("§b[+]§f" + item.getDisplay());
        }
        return item;
    }

    public static void setState(String uuid, SyncItem.State state) {
        if (itemDataBase.containsKey(uuid)) {
            getSomItem(uuid).setState(state);
        } else SomSQL.setSql(DataBase.Table.ItemStorage, "UUID", uuid, "State", state.toString());
    }

    public static void setOwner(String uuid, PlayerData playerData) {
        SomSQL.setSql(DataBase.Table.ItemStorage, "UUID", uuid, "Owner", playerData.getUUID());
        updateCache(uuid);
    }

    public static boolean hasSomItem(String uuid) {
        return itemDataBase.containsKey(uuid) || SomSQL.exists(DataBase.Table.ItemStorage, "UUID", uuid);
    }

    public static void updateCache(String uuid) {
        if (SomSQL.exists(DataBase.Table.ItemStorage, "UUID", uuid)) {
            RowData objects = SomSQL.getSql(DataBase.Table.ItemStorage, "UUID", uuid, "*");
            String itemId = objects.getString("ItemID");
            assert itemId != null;
            if (itemId.isEmpty()) {
                delete(objects.getString("UUID"));
            } else if (ItemDataLoader.getComplete().contains(itemId)) {
                SomItem item = ItemDataLoader.getItemData(itemId);
                item.setUUID(uuid);
                item.setState(State.valueOf(objects.getString("State")));
                item.setSync(true);
                item.setOwner(PlayerData.get(Bukkit.getPlayer(UUID.fromString(objects.getString("Owner")))));
                itemDataBase.put(uuid, item);
            } else {
                delete(objects.getString("UUID"));
                Log("存在しないItemIDが参照されました " + uuid + ":" + itemId, true);
            }
        } else {
            Log("DataBaseに存在しないアイテムが参照されました " + uuid, true);
        }
    }

    public static SomItem cloneCache(String uuid) {
        RowData objects = SomSQL.getSql(DataBase.Table.ItemStorage, "UUID", uuid, "*");
        SomItem item = ItemDataLoader.getItemData(objects.getString("ItemID"));
        item.setRawState(State.valueOf(objects.getString("State")));
        item.setUUID(uuid);
        return item;
    }

    public static SomItem getSomItem(String uuid) {
        if (!itemDataBase.containsKey(uuid)) {
            updateCache(uuid);
        }
        return itemDataBase.get(uuid);
    }

    public static void clearCache(String uuid) {
        itemDataBase.remove(uuid);
    }

    public enum State {
        ItemInventory,
        ItemStorage,
        Equipped,
        Market,
        Sold,
    }
}
