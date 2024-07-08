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

import static SwordofMagic11.Component.Function.decoLore;

public class EquipLevelDownMenu extends GUIManager {

    public static CustomItemStack Icon = icon();

    private static CustomItemStack icon() {
        CustomItemStack item = new CustomItemStack(Material.EXPERIENCE_BOTTLE);
        item.setDisplay("装備必要レベルダウン");
        item.addLore("§a必要レベルを下げることができます");
        item.addLore("§a必要レベルが高い装備ほど");
        item.addLore("§aメルの必要量は多くなります");
        item.setCustomData("SmithMenu", "EquipLevelDownMenu");
        return item;
    }

    private SomEquip equipment;

    public EquipLevelDownMenu(PlayerData playerData) {
        super(playerData, "装備必要レベルダウン", 1);
    }

    public int mel(SomEquip equip) {
        return equip.getRawReqLevel() * (1 + equip.getLevelDown());
    }

    @Override
    public void updateContainer() {
        for (int i = 0; i < 9; i++) {
            setItem(i, ItemFlame);
        }
        if (equipment != null) {
            setItem(1, equipment.viewItem().setCustomData("Remove", true));
            CustomItemStack receipt = new CustomItemStack(Material.PAPER);
            SomEquip resultEquip = equipment.clone();
            resultEquip.addLevelDown(1);
            CustomItemStack result = resultEquip.viewItem().setCustomData("Result", true);
            String display = "レベルダウン情報";
            String lore = decoLore("必要メル") + mel(equipment) + "メル";
            receipt.setDisplay(display);
            receipt.addLore(lore);
            result.addSeparator(display);
            result.addLore(lore);

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
        if (CustomItemStack.hasCustomData(clickedItem, "Result")) {
            int mel = mel(equipment);
            if (playerData.hasMel(mel)) {
                playerData.removeMel(mel);
                equipment.addLevelDown(1);
                playerData.sendMessage(equipment.getDisplay() + "§aの§c必要レベル§aが§eLv" + equipment.getReqLevel() + "§aになりました", SomSound.Level);
                if (equipment.getReqLevel() <= 1) equipment = null;
                playerData.statusUpdate();
                playerData.updateInventory();
                update();
            } else {
                playerData.sendNonMel();
            }
        }
    }

    @Override
    public void bottomClick(int slot, ItemStack clickedItem, ClickType clickType) {
        if (CustomItemStack.hasCustomData(clickedItem, "Equip.Category")) {
            equipment = (SomEquip) SyncItem.getSomItem(CustomItemStack.getCustomData(clickedItem, "UUID"));
            if (equipment.getReqLevel() > 1) {
                update();
            } else {
                playerData.sendMessage("§e必要レベル§aが§bLv1§aです", SomSound.Nope);
                equipment = null;
            }
        }
    }

    @Override
    public void close() {
        equipment = null;
    }
}
