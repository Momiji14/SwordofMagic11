package SwordofMagic11.Player.Storage;

import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.Item.SomItem;
import SwordofMagic11.Item.SyncItem;
import SwordofMagic11.Player.Menu.GUIManager;
import SwordofMagic11.Player.PlayerData;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.List;

import static SwordofMagic11.Component.Function.scrollDown;
import static SwordofMagic11.Component.Function.scrollUp;
import static SwordofMagic11.DataBase.DataBase.DownScroll;
import static SwordofMagic11.DataBase.DataBase.UpScroll;

public class StorageMenu extends GUIManager {

    private int scroll = 0;
    private int size = 0;

    public StorageMenu(PlayerData playerData) {
        super(playerData, "アイテム倉庫", 6);
    }

    @Override
    public void updateContainer() {
        int slot = 0;
        List<SomItem> itemList = SyncItem.getList(playerData, SyncItem.State.ItemStorage);
        size = itemList.size();
        for (int i = scroll * 8; i < itemList.size(); i++) {
            inventory.setItem(slot, itemList.get(i).viewItem());
            slot++;
            if (isInvalidSlot(slot)) slot++;
            if (slot >= 53) break;
        }
        while (slot < 53) {
            inventory.setItem(slot, null);
            slot++;
            if (isInvalidSlot(slot)) slot++;
        }
        for (int i = 17; i < 45; i += 9) {
            setItem(i, ItemFlame);
        }
        inventory.setItem(8, UpScroll());
        inventory.setItem(53, DownScroll());
    }

    @Override
    public void topClick(int slot, ItemStack clickedItem, ClickType clickType) {
        if (CustomItemStack.hasCustomData(clickedItem, "UpScroll")) {
            scroll = scrollUp(scroll);
            update();
        } else if (CustomItemStack.hasCustomData(clickedItem, "DownScroll")) {
            scroll = scrollDown(size, 6, scroll);
            update();
        } else if (CustomItemStack.hasCustomData(clickedItem, "UUID")) {
            SomItem item = SyncItem.getSomItem(CustomItemStack.getCustomData(clickedItem, "UUID"));
            item.setState(SyncItem.State.ItemInventory);
            playerData.updateInventory();
            update();
        }
    }

    @Override
    public void bottomClick(int slot, ItemStack clickedItem, ClickType clickType) {
        if (CustomItemStack.hasCustomData(clickedItem, "UUID")) {
            SomItem item = SyncItem.getSomItem(CustomItemStack.getCustomData(clickedItem, "UUID"));
            item.setState(SyncItem.State.ItemStorage);
            playerData.updateInventory();
            update();
        }
    }

    @Override
    public void close() {

    }
}
