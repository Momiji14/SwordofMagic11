package SwordofMagic11.Player.UserMenu;

import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.Map.DefenseBattle;
import SwordofMagic11.Player.Menu.GUIManager;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Map.PvPRaid;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class RaidMenu extends GUIManager {

    public CustomItemStack icon() {
        CustomItemStack item = new CustomItemStack(Material.CALIBRATED_SCULK_SENSOR);
        item.setDisplay("レイドメニュー");
        item.addLore("§aここからレイドに参加できます");
        item.setCustomData("Menu", "RaidMenu");
        return item;
    }

    public RaidMenu(PlayerData playerData) {
        super(playerData, "レイドメニュー", 1);
    }

    @Override
    public void updateContainer() {
        setItem(2, PvPRaid.viewItem(playerData));
        setItem(6, DefenseBattle.viewItem());
    }

    @Override
    public void topClick(int slot, ItemStack clickedItem, ClickType clickType) {
        if (CustomItemStack.hasCustomData(clickedItem, "PvPRaid")) {
            PvPRaid.clickItem(playerData);
        } else  if (CustomItemStack.hasCustomData(clickedItem, "DefenseBattle")) {
            DefenseBattle.clickItem(playerData);
        }
    }

    @Override
    public void bottomClick(int slot, ItemStack clickedItem, ClickType clickType) {

    }

    @Override
    public void close() {

    }
}
