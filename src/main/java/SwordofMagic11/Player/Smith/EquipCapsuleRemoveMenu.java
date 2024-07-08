package SwordofMagic11.Player.Smith;

import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.DataBase.CapsuleDataLoader;
import SwordofMagic11.Item.CapsuleData;
import SwordofMagic11.Item.SomEquip;
import SwordofMagic11.Item.SyncItem;
import SwordofMagic11.Player.Menu.GUIManager;
import SwordofMagic11.Player.PlayerData;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class EquipCapsuleRemoveMenu extends GUIManager {

    public static CustomItemStack Icon = icon();

    private static CustomItemStack icon() {
        CustomItemStack item = new CustomItemStack(Material.LAVA_BUCKET);
        item.setDisplay("カプセル除去");
        item.addLore("§a装備からカプセルを除去します");
        item.setCustomData("SmithMenu", "EquipCapsuleRemoveMenu");
        return item;
    }

    private SomEquip equipment;

    public EquipCapsuleRemoveMenu(PlayerData playerData) {
        super(playerData, "カプセル除去", 1);
    }

    @Override
    public void updateContainer() {
        for (int i = 0; i < 9; i++) {
            setItem(i, ItemFlame);
        }

        if (equipment != null) {
            setItem(1, equipment.viewItem());
            int slot = 3;
            for (CapsuleData capsule : equipment.getCapsules()) {
                setItem(slot, capsule.viewItem());
                slot++;
            }
            for (; slot < 7; slot++) {
                setItem(slot, null);
            }
        } else {
            setItem(1, null);
            for (int i = 3; i < 7; i++) {
                setItem(i, null);
            }
        }
    }

    @Override
    public void topClick(int slot, ItemStack clickedItem, ClickType clickType) {
        if (CustomItemStack.hasCustomData(clickedItem, "Equip.Category")) {
            equipment = null;
            update();
        }
        if (CustomItemStack.hasCustomData(clickedItem, "Capsule")) {
            CapsuleData capsule = CapsuleDataLoader.getCapsuleData(CustomItemStack.getCustomData(clickedItem, "Capsule"));
            equipment.removeCapsule(capsule);
            if (equipment.getCapsules().isEmpty()) equipment = null;
            playerData.sendMessage(capsule.getDisplay() + "§aを§c除去§aしました", SomSound.Tick);
            playerData.updateInventory();
            update();
        }
    }

    @Override
    public void bottomClick(int slot, ItemStack clickedItem, ClickType clickType) {
        if (CustomItemStack.hasCustomData(clickedItem, "Equip.Category")) {
            equipment = (SomEquip) SyncItem.getSomItem(CustomItemStack.getCustomData(clickedItem, "UUID"));
            if (!equipment.getCapsules().isEmpty()) {
                update();
            } else {
                playerData.sendMessage("§eカプセル§aが§eセット§aされていません", SomSound.Nope);
                equipment = null;
            }
        }
    }

    @Override
    public void close() {
        equipment = null;
    }
}
