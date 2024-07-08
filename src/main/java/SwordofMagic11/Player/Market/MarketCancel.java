package SwordofMagic11.Player.Market;

import SwordofMagic11.Command.Player.Market;
import SwordofMagic11.Component.SomSQL;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.DataBase.CapsuleDataLoader;
import SwordofMagic11.DataBase.DataBase;
import SwordofMagic11.DataBase.MaterialDataLoader;
import SwordofMagic11.Item.CapsuleData;
import SwordofMagic11.Item.Material.MaterialData;
import SwordofMagic11.Item.SomItem;
import SwordofMagic11.Item.SyncItem;
import SwordofMagic11.Player.Menu.GUIManager;
import SwordofMagic11.Player.PlayerData;
import com.github.jasync.sql.db.RowData;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import static SwordofMagic11.Component.Function.*;

public class MarketCancel extends GUIManager {

    public CustomItemStack icon() {
        CustomItemStack item = new CustomItemStack(Material.CRAFTING_TABLE);
        item.setDisplay("マーケット取下");
        item.addLore("§a出品・注文を取り下げます");
        item.setCustomData("Market", "MarketCancel");
        return item;
    }

    private int scroll = 0;
    public MarketCancel(PlayerData playerData) {
        super(playerData, "マーケット取下", 6);
    }

    @Override
    public void updateContainer() {
        int index = 0;
        int slot = 0;
        for (RowData objects : SomSQL.getSqlList(DataBase.Table.MarketSellMaterial, "UUID", playerData.getUUID(), "*")) {
            if (index >= scroll * 8) {
                String id = objects.getString("MaterialID");
                MaterialData material = MaterialDataLoader.getMaterialData(id);
                CustomItemStack item = material.viewItem();
                item.addSeparator("マーケット情報");
                item.addLore(decoLore("販売個数") + objects.getInt("Amount") + "個");
                item.addLore(decoLore("販売単価") + objects.getInt("Mel") + "メル");
                item.setCustomData("SellMaterial", id);
                setItem(slot, item);
                slot++;
                if (isInvalidSlot(slot)) slot++;
                if (slot >= 53) break;
            }
            index++;
        }
        if (slot < 53) for (RowData objects : SomSQL.getSqlList(DataBase.Table.MarketSellCapsule, "UUID", playerData.getUUID(), "*")) {
            if (index >= scroll * 8) {
                String id = objects.getString("CapsuleID");
                CapsuleData capsule = CapsuleDataLoader.getCapsuleData(id);
                CustomItemStack item = capsule.viewItem();
                item.addSeparator("マーケット情報");
                item.addLore(decoLore("販売個数") + objects.getInt("Amount") + "個");
                item.addLore(decoLore("販売単価") + objects.getInt("Mel") + "メル");
                item.setCustomData("SellCapsule", id);
                setItem(slot, item);
                slot++;
                if (isInvalidSlot(slot)) slot++;
                if (slot >= 53) break;
            }
            index++;
        }
        if (slot < 53) for (RowData objects : SomSQL.getSqlList(DataBase.Table.MarketOrderMaterial, "UUID", playerData.getUUID(), "*")) {
            if (index >= scroll * 8) {
                String id = objects.getString("MaterialID");
                MaterialData material = MaterialDataLoader.getMaterialData(id);
                CustomItemStack item = material.viewItem();
                item.addSeparator("マーケット情報");
                item.addLore(decoLore("販売個数") + objects.getInt("Amount") + "個");
                item.addLore(decoLore("販売単価") + objects.getInt("Mel") + "メル");
                item.setCustomData("OrderMaterial", id);
                setItem(slot, item);
                slot++;
                if (isInvalidSlot(slot)) slot++;
                if (slot >= 53) break;
            }
            index++;
        }
        if (slot < 53) for (RowData objects : SomSQL.getSqlList(DataBase.Table.MarketOrderCapsule, "UUID", playerData.getUUID(), "*")) {
            if (index >= scroll * 8) {
                String id = objects.getString("CapsuleID");
                CapsuleData capsule = CapsuleDataLoader.getCapsuleData(id);
                CustomItemStack item = capsule.viewItem();
                item.addSeparator("マーケット情報");
                item.addLore(decoLore("販売個数") + objects.getInt("Amount") + "個");
                item.addLore(decoLore("販売単価") + objects.getInt("Mel") + "メル");
                item.setCustomData("OrderCapsule", id);
                setItem(slot, item);
                slot++;
                if (isInvalidSlot(slot)) slot++;
                if (slot >= 53) break;
            }
            index++;
        }
        if (slot < 53) for (SomItem somItem : SyncItem.getList(playerData, SyncItem.State.Market)) {
            if (index >= scroll * 8) {
                String uuid = somItem.getUUID();
                if (MarketSellItem.has(uuid)) {
                    MarketPlayer.ItemPack pack = new MarketPlayer.ItemPack(uuid);
                    if (pack.getOwnerUUID().equals(playerData.getUUID())) {
                        CustomItemStack item = somItem.viewItem();
                        item.addSeparator("マーケット情報");
                        item.addLore(decoLore("販売価格") + MarketSellItem.getMel(uuid) + "メル");
                        item.setCustomData("Display", somItem.getDisplay());
                        item.setCustomData("SellItem", uuid);
                        setItem(slot, item);
                        slot++;
                        if (isInvalidSlot(slot)) slot++;
                        if (slot >= 53) break;
                    }
                } else {
                    SyncItem.getSomItem(uuid).setState(SyncItem.State.ItemInventory);
                    playerData.updateInventory();
                    playerData.sendMessage(somItem.getDisplay() + "§aを§e回収§aしました");
                }
            }
            index++;
        }
    }

    @Override
    public void topClick(int slot, ItemStack clickedItem, ClickType clickType) {
        if (CustomItemStack.hasCustomData(clickedItem, "UpScroll")) {
            scroll = scrollUp(scroll);
            update();
        } else if (CustomItemStack.hasCustomData(clickedItem, "DownScroll")) {
            scroll = scrollDown(0, 6, scroll);
            update();
        }
        if (CustomItemStack.hasCustomData(clickedItem, "SellMaterial")) {
            String id = CustomItemStack.getCustomData(clickedItem, "SellMaterial");
            MaterialData material = MaterialDataLoader.getMaterialData(id);
            if (!playerData.materialMenu().hasEmpty(material)) {
                playerData.materialMenu().sendNonHasEmpty();
                return;
            }
            if (playerData.marketSellMaterial().has(id)) {
                MarketPlayer.Pack pack = playerData.marketSellMaterial().get(id);
                playerData.addMaterial(id, pack.amount());
                playerData.marketSellMaterial().delete(id);
                playerData.sendMessage(material.getDisplay() + "x" + pack.amount() + "§aの§b出品§aを§c取下§aました", SomSound.Tick);
                MarketPlayer.Log(playerData, "CancelProductMaterial:" + id + ", Amount:" + pack.amount() + ", Mel:" + pack.amount());
            } else {
                sendPostTrade();
            }
            update();
        } else if (CustomItemStack.hasCustomData(clickedItem, "OrderMaterial")) {
            String id = CustomItemStack.getCustomData(clickedItem, "OrderMaterial");
            MaterialData material = MaterialDataLoader.getMaterialData(id);
            if (playerData.marketOrderMaterial().has(id)) {
                MarketPlayer.Pack pack = playerData.marketOrderMaterial().get(id);
                int mel = pack.mel() * pack.amount();
                playerData.addMel(mel);
                playerData.marketOrderMaterial().delete(id);
                playerData.sendMessage(material.getDisplay() + "x" + pack.amount() + "§aの§b注文§aを§c取下§aました", SomSound.Tick);
                MarketPlayer.Log(playerData, "CancelOrderMaterial:" + id + ", Amount:" + pack.amount() + ", Mel:" + pack.amount());
            } else {
                sendPostTrade();
            }
            update();
        } else if (CustomItemStack.hasCustomData(clickedItem, "SellCapsule")) {
            String id = CustomItemStack.getCustomData(clickedItem, "SellCapsule");
            CapsuleData capsule = CapsuleDataLoader.getCapsuleData(id);
            if (!playerData.capsuleMenu().hasEmpty(capsule)) {
                playerData.capsuleMenu().sendNonHasEmpty();
                return;
            }
            if (playerData.marketSellCapsule().has(id)) {
                MarketPlayer.Pack pack = playerData.marketSellCapsule().get(id);
                playerData.addCapsule(id, pack.amount());
                playerData.marketSellCapsule().delete(id);
                playerData.sendMessage(capsule.getDisplay() + "x" + pack.amount() + "§aの§b出品§aを§c取下§aました", SomSound.Tick);
                MarketPlayer.Log(playerData, "CancelProductCapsule:" + id + ", Amount:" + pack.amount() + ", Mel:" + pack.amount());
            } else {
                sendPostTrade();
            }
            update();
        } else if (CustomItemStack.hasCustomData(clickedItem, "OrderCapsule")) {
            String id = CustomItemStack.getCustomData(clickedItem, "OrderCapsule");
            CapsuleData capsule = CapsuleDataLoader.getCapsuleData(id);
            if (playerData.marketOrderCapsule().has(id)) {
                MarketPlayer.Pack pack = playerData.marketOrderCapsule().get(id);
                int mel = pack.mel() * pack.amount();
                playerData.addMel(mel);
                playerData.marketOrderCapsule().delete(id);
                playerData.sendMessage(capsule.getDisplay() + "x" + pack.amount() + "§aの§b注文§aを§c取下§aました", SomSound.Tick);
                MarketPlayer.Log(playerData, "CancelOrderCapsule:" + id + ", Amount:" + pack.amount() + ", Mel:" + pack.amount());
            } else {
                sendPostTrade();
            }
            update();
        } else if (CustomItemStack.hasCustomData(clickedItem, "SellItem")) {
            String uuid = CustomItemStack.getCustomData(clickedItem, "SellItem");
            SomItem somItem = SyncItem.getSomItem(uuid);
            if (somItem.getState() == SyncItem.State.Market && somItem.getOwner() == playerData) {
                MarketPlayer.ItemPack itemPack = MarketSellItem.get(uuid);
                String display = CustomItemStack.getCustomData(clickedItem, "Display");
                MarketSellItem.delete(uuid);
                playerData.sendMessage(display + "§aの§b出品§aを§c取下§aました", SomSound.Tick);
                playerData.updateInventory();
                update();
                MarketPlayer.Log(playerData, "CancelProductItem:" + uuid + ", ID:" + somItem.getId() + ", Mel:" + itemPack.mel());
            } else {
                sendPostTrade();
            }
        }
    }

    @Override
    public void bottomClick(int slot, ItemStack clickedItem, ClickType clickType) {

    }

    @Override
    public void close() {

    }

    public void sendPostTrade() {
        playerData.sendMessage("§aすでに§e取引§aされてた後です", SomSound.Nope);
    }
}
