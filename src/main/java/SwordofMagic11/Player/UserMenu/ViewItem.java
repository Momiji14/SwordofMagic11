package SwordofMagic11.Player.UserMenu;

import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.Item.SomItem;
import SwordofMagic11.Item.SyncItem;
import SwordofMagic11.Player.Menu.GUIManager;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.SomCore;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.List;

import static SwordofMagic11.Component.Function.scrollDown;
import static SwordofMagic11.Component.Function.scrollUp;
import static SwordofMagic11.DataBase.DataBase.DownScroll;
import static SwordofMagic11.DataBase.DataBase.UpScroll;
import static SwordofMagic11.Player.Classes.PerSkillPoint;

public class ViewItem extends GUIManager {

    private int scroll = 0;
    private int size = 0;

    public ViewItem(PlayerData playerData) {
        super(playerData, "アイテム表示", 6);
    }

    public CustomItemStack icon() {
        CustomItemStack item = new CustomItemStack(Material.GLOW_ITEM_FRAME);
        item.setDisplay("アイテム表示");
        item.addLore("§aアイテムをチャットに表示します");
        item.setCustomData("Menu", "ViewItem");
        return item;
    }

    @Override
    public void updateContainer() {
        inventory.clear();
        int slot = 0;
        List<SomItem> itemList = SyncItem.getList(playerData);
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
                Component text = SyncItem.getSomItem(uuid).toComponent();
                SomCore.sendMessageComponent(playerData.getPlayer(), Component.text("[").append(text).append(Component.text("]")));
                playerData.closeInventory();
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

    }
}
