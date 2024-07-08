package SwordofMagic11.Player.Menu;

import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.Item.SyncItem;
import SwordofMagic11.Player.PlayerData;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class TrashMenu extends GUIManager {

    public CustomItemStack icon() {
        return new CustomItemStack(Material.LAVA).setDisplay("ゴミ箱").setCustomData("Menu", "TrashMenu");
    }

    public TrashMenu(PlayerData playerData) {
        super(playerData, "ゴミ箱", 1);
    }

    @Override
    public void updateContainer() {
        for (int i = 0; i < 9; i++) {
            setItem(i, new CustomItemStack(Material.LAVA).setNonDecoDisplay("§c選択したアイテムを捨てます"));
        }
    }

    @Override
    public void topClick(int slot, ItemStack clickedItem, ClickType clickType) {

    }

    @Override
    public void bottomClick(int slot, ItemStack clickedItem, ClickType clickType) {
        if (CustomItemStack.hasCustomData(clickedItem, "UUID")) {
            playerData.itemInventory().delete(SyncItem.getSomItem(CustomItemStack.getCustomData(clickedItem, "UUID")));
        }
    }

    @Override
    public void close() {

    }
}
