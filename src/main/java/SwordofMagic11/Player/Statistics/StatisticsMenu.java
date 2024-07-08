package SwordofMagic11.Player.Statistics;

import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.Player.Menu.GUIManager;
import SwordofMagic11.Player.PlayerData;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class StatisticsMenu extends GUIManager {

    public CustomItemStack icon() {
        CustomItemStack item = new CustomItemStack(Material.KNOWLEDGE_BOOK);
        item.setDisplay("統計情報");
        item.addLore("§a統計情報を見ることができます");
        item.setCustomData("Menu", "StatisticsMenu");
        return item;
    }

    public StatisticsMenu(PlayerData playerData) {
        super(playerData, "統計情報", 1);
    }

    @Override
    public void updateContainer() {
        setItem(0, playerData.statistics().viewItemInt());
        setItem(1, playerData.statistics().viewItemDouble());
        setItem(2, playerData.statistics().viewItemEnemyKill());
    }

    @Override
    public void topClick(int slot, ItemStack clickedItem, ClickType clickType) {

    }

    @Override
    public void bottomClick(int slot, ItemStack clickedItem, ClickType clickType) {

    }

    @Override
    public void close() {

    }
}
