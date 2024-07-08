package SwordofMagic11.Player.Smith;

import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.Item.SomEquip;
import SwordofMagic11.Item.SyncItem;
import SwordofMagic11.Player.Menu.GUIManager;
import SwordofMagic11.Player.PlayerData;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public abstract class EquipPlusBaseMenu extends GUIManager {
    public EquipPlusBaseMenu(PlayerData playerData, String display) {
        super(playerData, display, 1);
    }

    protected SomEquip equipment;

    public abstract SomEquip result();

    public abstract List<String> receipt();

    public abstract void process();

    public String leftGrinder() {
        return " §7(残数:" + playerData.getMaterial("グラインダー") + ")";
    }

    @Override
    public void updateContainer() {
        for (int i = 0; i < 9; i++) {
            setItem(i, ItemFlame);
        }
        if (equipment != null) {
            setItem(1, equipment.viewItem().setCustomData("Remove", true));
            CustomItemStack receipt = new CustomItemStack(Material.PAPER);
            CustomItemStack result = result().viewItem().setCustomData("UpgradeBase", true);
            List<String> receiptData = receipt();
            receipt.setDisplay(receiptData.get(0));
            result.addSeparator(receiptData.get(0));
            receiptData.remove(0);
            for (String str : receiptData) {
                receipt.addLore(str);
                result.addLore(str);
            }
            setItem(4, receipt);
            setItem(7, result);
        } else {
            setItem(1, null);
            setItem(4, null);
            setItem(7, null);
        }
    }

    @Override
    public void topClick(int slot, ItemStack clickedItem, ClickType clickType) {
        if (CustomItemStack.hasCustomData(clickedItem, "UpgradeBase")) {
            process();
            if (equipment.getPlus() >= EquipPlusMenu.MaxPlus) {
                playerData.sendMessage("§e強化値§aが§c最大§aです", SomSound.Tick);
                equipment = null;
            }
            update();
            playerData.statusUpdate();
            playerData.updateInventory();
        }
    }

    @Override
    public void bottomClick(int slot, ItemStack clickedItem, ClickType clickType) {
        if (CustomItemStack.hasCustomData(clickedItem, "Equip.Category")) {
            equipment = (SomEquip) SyncItem.getSomItem(CustomItemStack.getCustomData(clickedItem, "UUID"));
            if (equipment.getPlus() < EquipPlusMenu.MaxPlus) {
                update();
            } else {
                playerData.sendMessage("§e強化値§aが§c最大§aです", SomSound.Nope);
                equipment = null;
            }
        }
    }

    @Override
    public void close() {
        equipment = null;
    }
}
