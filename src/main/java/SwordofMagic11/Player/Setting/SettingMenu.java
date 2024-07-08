package SwordofMagic11.Player.Setting;

import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.Player.Menu.GUIManager;
import SwordofMagic11.Player.PlayerData;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import static SwordofMagic11.Component.Function.*;

public class SettingMenu extends GUIManager {
    public SettingMenu(PlayerData playerData) {
        super(playerData, "設定メニュー", 4);
    }

    public CustomItemStack icon() {
        CustomItemStack item = new CustomItemStack(Material.BOOK);
        item.setDisplay("設定メニュー");
        item.addLore("§aゲーム内の設定が行えます");
        item.setCustomData("Menu", "SettingMenu");
        return item;
    }

    @Override
    public void updateContainer() {
        int slot = 0;
        for (PlayerSetting.BooleanEnum bool : PlayerSetting.BooleanEnum.values()) {
            CustomItemStack item = bool.viewItem();
            item.addSeparator("設定値");
            item.addLore(decoLore("現在値") + boolText(playerData.setting().is(bool)));
            setItem(slot, item);
            slot++;
        }

        for (PlayerSetting.DoubleEnum doubleEnum : PlayerSetting.DoubleEnum.values()) {
            CustomItemStack item = doubleEnum.viewItem();
            item.addSeparator("設定値");
            item.addLore(decoLore("現在値") + playerData.setting().text(doubleEnum));
            setItem(slot, item);
            slot++;
        }
    }

    @Override
    public void topClick(int slot, ItemStack clickedItem, ClickType clickType) {
        if (CustomItemStack.hasCustomData(clickedItem, "BooleanSetting")) {
            playerData.setting().toggle(PlayerSetting.BooleanEnum.valueOf(CustomItemStack.getCustomData(clickedItem, "BooleanSetting")));
        } else if (CustomItemStack.hasCustomData(clickedItem, "ValueSetting")) {
            playerData.setting().valueNext(PlayerSetting.DoubleEnum.valueOf(CustomItemStack.getCustomData(clickedItem, "ValueSetting")));
        }
    }

    @Override
    public void bottomClick(int slot, ItemStack clickedItem, ClickType clickType) {

    }

    @Override
    public void close() {
        playerData.updateEquipView();
    }
}
