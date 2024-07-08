package SwordofMagic11.Player.UserMenu;

import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.Item.SomItem;
import SwordofMagic11.Item.SyncItem;
import SwordofMagic11.Player.Menu.GUIManager;
import SwordofMagic11.Player.PlayerData;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import static SwordofMagic11.Command.Player.Lock.isLock;
import static SwordofMagic11.Command.Player.Lock.setLock;
import static SwordofMagic11.Component.Function.boolText;

public class LockItem extends GUIManager {

    public LockItem(PlayerData playerData) {
        super(playerData, "アイテムロック", 6);
    }

    public CustomItemStack icon() {
        CustomItemStack item = new CustomItemStack(Material.OBSERVER);
        item.setDisplay("アイテムロック");
        item.addLore("§aアイテムの売却防止を切り替えます");
        item.setCustomData("Menu", "LockItem");
        return item;
    }

    @Override
    public void updateContainer() {
        inventory.clear();
        int slot = 0;
        for (SomItem somItem : playerData.itemInventory().getList()) {
            setItem(slot, somItem.viewItem());
            slot++;
            if (slot >= 53) break;
        }
    }

    @Override
    public void topClick(int slot, ItemStack clickedItem, ClickType clickType) {
        if (CustomItemStack.hasCustomData(clickedItem, "UUID")) {
            String uuid = CustomItemStack.getCustomData(clickedItem, "UUID");
            if (SyncItem.hasSomItem(uuid)) {
                SomItem item = SyncItem.getSomItem(uuid);
                boolean next = !isLock(playerData, uuid);
                setLock(playerData, uuid, next);
                playerData.sendMessage(item.getDisplay() + "§aの§e売却防止§aを" + boolText(next) + "§aにしました", SomSound.Tick);
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
