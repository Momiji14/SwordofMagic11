package SwordofMagic11.Player.UserMenu;

import SwordofMagic11.Command.Player.Trade;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.Item.SomItem;
import SwordofMagic11.Item.SyncItem;
import SwordofMagic11.Player.Menu.GUIManager;
import SwordofMagic11.Player.PlayerData;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.List;

import static SwordofMagic11.Component.Function.scrollDown;
import static SwordofMagic11.Component.Function.scrollUp;
import static SwordofMagic11.DataBase.DataBase.DownScroll;
import static SwordofMagic11.DataBase.DataBase.UpScroll;

public class SendItem extends GUIManager {

    private PlayerData targetData;
    private int scroll = 0;
    private int size = 0;
    public SendItem(PlayerData playerData) {
        super(playerData, "アイテム送信", 6);
    }

    public CustomItemStack icon() {
        CustomItemStack item = new CustomItemStack(Material.SPRUCE_CHEST_BOAT);
        item.setDisplay("アイテム送信");
        item.addLore("§aアイテムを他のプレイヤーへ送ります");
        item.setCustomData("PlayerSelect", "SendItem");
        return item;
    }

    public void open(PlayerData targetData) {
        if (targetData == playerData) {
            playerData.sendNoTargetMe();
            return;
        }
        this.targetData = targetData;
        super.open();
    }

    @Override
    public void updateContainer() {
        inventory.clear();
        int slot = 0;
        List<SomItem> itemList = playerData.itemInventory().getList();
        size = itemList.size();
        for (int i = scroll * 8; i < itemList.size(); i++) {
            setItem(slot, itemList.get(i).viewItem());
            slot++;
            if (isInvalidSlot(slot)) slot++;
            if (slot >= 53) break;
        }
        for (int i = 17; i < 45; i += 9) {
            setItem(i, ItemFlame);
        }
        setItem(8, UpScroll());
        setItem(53, DownScroll());
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
            String uuid = CustomItemStack.getCustomData(clickedItem, "UUID");
            if (SyncItem.hasSomItem(uuid)) {
                SomItem item = SyncItem.getSomItem(uuid);
                item.setOwner(targetData);
                playerData.sendMessage(targetData.getName() + "§aに§f" + item.getDisplay() + "§aを送りました", SomSound.Tick);
                targetData.sendMessage(playerData.getName() + "§aから§f" + item.getDisplay() + "§aが送られてきました", SomSound.Tick);
                playerData.updateInventory();
                targetData.updateInventory();
                update();
                Trade.log(playerData, targetData, "Item:" + item.getId() + ":" + item.getUUID());
            } else {
                playerData.sendMessage("§c存在しないアイテムです", SomSound.Nope);
            }
        }
    }

    @Override
    public void bottomClick(int slot, ItemStack clickedItem, ClickType clickType) {

    }

    @Override
    public void close() {
        targetData = null;
    }
}
