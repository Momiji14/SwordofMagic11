package SwordofMagic11.Player.Menu;

import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.Item.SomEquip;
import SwordofMagic11.Map.PvPRaid;
import SwordofMagic11.Player.ClassType;
import SwordofMagic11.Player.PlayerData;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static SwordofMagic11.Component.Function.decoText;

public class ClassesMenu extends GUIManager {


    public ClassesMenu(PlayerData playerData) {
        super(playerData, "クラス選択", 5);
    }

    @Override
    public void open() {
        if (!PvPRaid.isInPvPRaid(playerData)) {
            playerData.heal();
        }
        super.open();
    }

    @Override
    public void updateContainer() {
        for (ClassType classType : ClassType.values()) {
            setItem(classType.slot(), classType.viewItem());
        }
    }

    @Override
    public void topClick(int slot, ItemStack clickedItem, ClickType clickType) {
        if (CustomItemStack.hasCustomData(clickedItem, "ClassType")) {
            ClassType select = ClassType.valueOf(CustomItemStack.getCustomData(clickedItem, "ClassType"));
            if (select == playerData.classes().getMainClass()) {
                playerData.sendMessage("§aすでに" + select.getColorDisplay() + "§aです", SomSound.Nope);
                return;
            }
            List<String> message = new ArrayList<>();
            message.add(decoText(select.getColorDisplay()));
            boolean check = true;
            for (Map.Entry<ClassType, Integer> entry : select.getReqLevel().entrySet()) {
                ClassType classType = entry.getKey();
                int level = entry.getValue();
                boolean check2 = playerData.classes().getLevel(classType) >= level;
                if (!check2) {
                    check = false;
                }
                message.add((check2 ? "§b✓ " : "§c× ") + classType.getColorDisplay() + " §eLv" + level);
            }

            if (check) {
                if (PvPRaid.isInPvPRaid(playerData) && PvPRaid.disableClass(playerData, select)) return;
                playerData.sendTitle("§eClass Change !", playerData.classes().getMainClass().getColorDisplay() + " §a-> " + select.getColorDisplay(), 10, 40, 10);
                playerData.classes().setMainClass(select);
                playerData.sendMessage(select.getColorDisplay() + "§aに§b転職§aしました", SomSound.Level);
                playerData.closeInventory();
                for (SomEquip.Slot equipSlot : SomEquip.Slot.values()) {
                    if (playerData.hasEquip(equipSlot)) {
                        SomEquip equip = playerData.getEquip(equipSlot);
                        if (equip.getReqLevel() > playerData.classes().getLevel(select)) {
                            playerData.setEquip(equipSlot, null);
                        }
                    }
                }
                playerData.silence(60, playerData);
                playerData.statusUpdate();
                playerData.updateInventory();
            } else {
                playerData.sendMessage(message, SomSound.Nope);
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
