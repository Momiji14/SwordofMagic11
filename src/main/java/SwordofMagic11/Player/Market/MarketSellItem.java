package SwordofMagic11.Player.Market;

import SwordofMagic11.Component.SomSQL;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Component.SomText;
import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.DataBase.ItemDataLoader;
import SwordofMagic11.DataBase.MaterialDataLoader;
import SwordofMagic11.Item.Material.MaterialData;
import SwordofMagic11.Item.SomItem;
import SwordofMagic11.Item.SyncItem;
import SwordofMagic11.Player.Menu.GUIManager;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Player.Setting.PlayerSetting;
import SwordofMagic11.SomCore;
import com.github.jasync.sql.db.ResultSet;
import com.github.jasync.sql.db.RowData;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.*;

import static SwordofMagic11.Component.Function.*;
import static SwordofMagic11.DataBase.DataBase.*;

public class MarketSellItem extends GUIManager {

    public CustomItemStack icon() {
        HashMap<String, Integer> map = new LinkedHashMap<>();
        CustomItemStack item = new CustomItemStack(Material.ANVIL);
        item.setDisplay("マーケット販売 [アイテム]");
        ResultSet resultSet = SomSQL.getSqlList(Table.MarketSellItem, "*", "ORDER BY `ItemID` ASC");
        for (RowData objects : resultSet) {
            String itemId = objects.getString("ItemID");
            if (ItemDataLoader.getComplete().contains(itemId)) {
                map.merge(itemId, 1, Integer::sum);
            }
        }
        int register = 0;
        int index = 0;
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            SomItem somItem = ItemDataLoader.getItemData(entry.getKey());
            item.addLore("§7・§f" + somItem.getDisplay() + " §a" + entry.getValue() + "件の登録");
            register += entry.getValue();
            if (playerData.setting().is(PlayerSetting.BooleanEnum.StorageViewShort) && index > ContainerViewSize) {
                item.addLore("§7その他..." + (map.size() - ContainerViewSize) + "種," + (resultSet.size() - register) + "件");
                break;
            }
            index++;
        }
        item.setCustomData("Market", "MarketSellItem");
        return item;
    }

    private int scroll = 0;

    public MarketSellItem(PlayerData playerData) {
        super(playerData, "マーケット販売 [アイテム]", 6);
    }

    private static final String key = "UUID";

    public static void set(MarketPlayer.ItemPack pack) {
        SomSQL.setSql(Table.MarketSellItem, key, pack.getUUID(), "ItemID", pack.getItemID());
        SomSQL.setSql(Table.MarketSellItem, key, pack.getUUID(), "Mel", pack.mel());
    }

    public static void delete(String uuid) {
        SyncItem.setState(uuid, SyncItem.State.ItemInventory);
        SomSQL.delete(Table.MarketSellItem, key, uuid);
    }

    public static MarketPlayer.ItemPack get(String uuid) {
        return new MarketPlayer.ItemPack(uuid);
    }

    public static int getMel(String uuid) {
        if (SomSQL.exists(Table.MarketSellItem, key, uuid, "Mel")) {
            return SomSQL.getInt(Table.MarketSellItem, key, uuid, "Mel");
        } else return 0;
    }

    public static boolean has(String uuid) {
        return SomSQL.exists(Table.MarketSellItem, key, uuid);
    }

    private String itemId = null;

    @Override
    public void updateContainer() {
        if (itemId == null) {
            HashMap<String, Set<MarketPlayer.ItemPack>> map = new LinkedHashMap<>();
            Set<String> keys = new HashSet<>();
            ResultSet resultSet = SomSQL.getSqlList(Table.MarketSellItem, "*", "ORDER BY `ItemID` ASC");
            for (RowData objects : resultSet) {
                String id = objects.getString("ItemID");
                String uuid = objects.getString("UUID");
                keys.add(uuid);
                if (keys.size() > scroll * 8) {
                    if (!map.containsKey(id)) map.put(id, new HashSet<>());
                    MarketPlayer.ItemPack pack = new MarketPlayer.ItemPack(uuid, objects.getInt("Mel"));
                    map.get(id).add(pack);
                    if (map.size() >= 48) break;
                }
            }
            int slot = 0;
            for (Map.Entry<String, Set<MarketPlayer.ItemPack>> entry : map.entrySet()) {
                String id = entry.getKey();
                SomItem somItem = ItemDataLoader.getItemData(id);
                CustomItemStack item = somItem.viewItem();
                item.addSeparator("出品者一覧");
                int indexPlayer = 0;
                for (MarketPlayer.ItemPack pack : entry.getValue()) {
                    item.addLore("§7・§f" + pack.getOwnerName() + " §e" + pack.mel() + "メル");
                    indexPlayer++;
                    if (playerData.setting().is(PlayerSetting.BooleanEnum.StorageViewShort) && indexPlayer > ContainerViewSize) {
                        item.addLore("§7その他..." + (entry.getValue().size() - ContainerViewSize) + "名");
                        break;
                    }
                }
                item.setCustomData("SellItem", id);
                setItem(slot, item);
                slot++;
                if (isInvalidSlot(slot)) slot++;
            }
        } else {
            ResultSet resultSet = SomSQL.getSqlList(Table.MarketSellItem, "ItemID", itemId, "*", "ORDER BY `Mel` ASC");
            int slot = 0;
            for (RowData objects : resultSet) {
                MarketPlayer.ItemPack pack = new MarketPlayer.ItemPack(objects.getString("UUID"));
                SomItem somItem = SyncItem.cloneCache(pack.getUUID());
                if (somItem.getState() != SyncItem.State.Market) {
                    SomSQL.delete(Table.MarketSellItem, "UUID", somItem.getUUID());
                    continue;
                }
                CustomItemStack item = somItem.viewItem();
                item.addSeparator("マーケット情報");
                item.addLore(decoLore("販売価格") + pack.mel() + "メル");
                item.addLore(decoLore("出品者") + pack.getOwnerName());
                item.setCustomData("UUID", pack.getUUID());
                item.setCustomData("SellOwner", pack.getOwnerUUID());
                setItem(slot, item);
                slot++;
                if (isInvalidSlot(slot)) slot++;
                if (slot >= 53) break;
            }
        }
        setItem(8, UpScroll());
        setItem(17, ItemFlame);
        setItem(26, ItemFlame);
        setItem(35, ItemFlame);
        setItem(44, ItemFlame);
        setItem(53, DownScroll());
    }

    @Override
    public void topClick(int slot, ItemStack clickedItem, ClickType clickType) {
        if (CustomItemStack.hasCustomData(clickedItem, "UpScroll")) {
            scroll = scrollUp(scroll);
            update();
        } else if (CustomItemStack.hasCustomData(clickedItem, "DownScroll")) {
            scroll = scrollDown(inventory, scroll);
            update();
        }
        if (CustomItemStack.hasCustomData(clickedItem, "SellItem")) {
            itemId = CustomItemStack.getCustomData(clickedItem, "SellItem");
            SomSound.Tick.play(playerData);
            update();
        } else if (CustomItemStack.hasCustomData(clickedItem, "SellOwner")) {
            String owner = CustomItemStack.getCustomData(clickedItem, "SellOwner");
            if (playerData.getUUID().equals(owner)) {
                playerData.sendMessage("§a自身の§e出品§aです", SomSound.Nope);
                return;
            }
            String uuid = CustomItemStack.getCustomData(clickedItem, "UUID");
            MarketPlayer.ItemPack pack = get(uuid);
            int mel = pack.mel();
            if (playerData.hasMel(mel)) {
                playerData.removeMel(mel);
                MarketPlayer.addMel(pack.getOwnerUUID(), mel);
                SyncItem.setOwner(uuid, playerData);
                delete(uuid);
                SomItem somItem = SyncItem.getSomItem(uuid);
                playerData.sendMessage(SomText.create(somItem.toComponent()).add("§aを§e" + mel + "メル§aで§b購入§aしました"), SomSound.Tick);
                playerData.updateInventory();
                update();
                MarketPlayer.Log(playerData, "BuyItem:" + uuid + ", ID:" + somItem.getId() + ", Mel:" + mel + ", Seller:" + pack.getOwnerName());
            } else {
                playerData.sendNonMel();
            }
        }
    }

    @Override
    public void bottomClick(int slot, ItemStack clickedItem, ClickType clickType) {

    }

    @Override
    public void close() {
        scroll = 0;
        itemId = null;
    }
}
