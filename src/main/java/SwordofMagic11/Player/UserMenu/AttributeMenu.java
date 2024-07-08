package SwordofMagic11.Player.UserMenu;

import SwordofMagic11.AttributeType;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.Player.ClassType;
import SwordofMagic11.Player.Classes;
import SwordofMagic11.Player.Menu.GUIManager;
import SwordofMagic11.Player.PlayerData;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import static SwordofMagic11.Component.Function.clickMultiply;
import static SwordofMagic11.Component.Function.scale;
import static SwordofMagic11.DataBase.DataBase.ResetCost;
import static SwordofMagic11.Player.Classes.MaxLevel;
import static SwordofMagic11.Player.Classes.PerAttrPoint;

public class AttributeMenu extends GUIManager {

    private static final double MaxPerLevel = 2.5;

    public CustomItemStack icon() {
        CustomItemStack item = new CustomItemStack(Material.EXPERIENCE_BOTTLE);
        item.setDisplay("アトリビュート");
        item.addLore("§aアトリビュートポイントの振り分けが行えます");
        item.addLore("§aポイントはレベル毎に§e" + PerAttrPoint + "§a獲得します");
        item.addLore("§a最大§e" + PerAttrPoint * 10 + "§aまたは§eLv*" + MaxPerLevel + "§aまで割り振れます");
        item.addLore("§7左クリックx1 右クリックx10 シフト左クリックx100 シフト右クリックx1000");
        item.setCustomData("Menu", "AttributeMenu");
        return item;
    }

    public CustomItemStack pointIcon() {
        CustomItemStack item = new CustomItemStack(Material.EXPERIENCE_BOTTLE);
        item.setNonDecoDisplay("§e" + playerData.classes().getAttributePoint() + "P");
        item.addLore("§c※経験値" + scale(ResetCost * 100, 1) + "%を消費してリセットできます");
        item.setCustomData("ResetAttribute", true);
        return item;
    }

    public AttributeMenu(PlayerData playerData) {
        super(playerData, "アトリビュートメニュー", 1);
    }

    @Override
    public void updateContainer() {
        int slot = 0;
        for (AttributeType attr : AttributeType.values()) {
            CustomItemStack item = attr.viewItem(playerData);
            setItem(slot, item);
            slot++;
        }
        setItem(8, pointIcon());
    }

    public int max() {
        return (int) (Math.max(PerAttrPoint * 10, playerData.getLevel() * MaxPerLevel) + playerData.memorialMenu().count());
    }

    public void apply(AttributeType attr, int usePoint) {
        Classes classes = playerData.classes();
        if (classes.getAttributePoint() >= usePoint) {
            int addition = Math.min(max() - playerData.getBaseAttribute(attr), usePoint);
            if (addition > 0) {
                playerData.classes().addAttribute(attr, addition);
                classes.removeAttributePoint(addition);
                SomSound.Tick.play(playerData);
                update();
            } else {
                playerData.sendMessage("§e" + attr.getDisplay() + "§aは§e最大§aです", SomSound.Nope);
            }
        } else {
            playerData.sendMessage("§cポイントが足りません", SomSound.Nope);
        }
    }

    @Override
    public void topClick(int slot, ItemStack clickedItem, ClickType clickType) {
        Classes classes = playerData.classes();
        if (CustomItemStack.hasCustomData(clickedItem, "Attribute")) {
            AttributeType attr = AttributeType.valueOf(CustomItemStack.getCustomData(clickedItem, "Attribute"));
            int usePoint = clickMultiply(clickType);
            apply(attr, usePoint);
        }
        if (CustomItemStack.hasCustomData(clickedItem, "ResetAttribute")) {
            int usedPoint = 0;
            for (Integer value : playerData.getBaseAttribute().values()) {
                usedPoint += value;
            }
            if (usedPoint > 0) {
                double reqExp = playerData.getLevel() >= MaxLevel ? 0 : classes.reqExp() * ResetCost;
                if (reqExp <= classes.getExp()) {
                    playerData.classes().removeExp(reqExp);
                    resetAttribute();
                    playerData.sendMessage("§eアトリビュートポイント§aを§cリセット§aしました", SomSound.Tick);
                    update();
                } else {
                    playerData.sendMessage("§e経験値§aが足りません", SomSound.Nope);
                }
            }
        }
    }

    @Override
    public void bottomClick(int slot, ItemStack clickedItem, ClickType clickType) {

    }

    @Override
    public void close() {

    }

    public void resetAttribute() {
        int memorialPoint = playerData.memorialMenu().count() * 2;
        for (Double value : playerData.memorialMenu().getMemorial().values()) {
            if (value == 1) memorialPoint++;
        }
        int point = ((playerData.getLevel() - 1) * PerAttrPoint) + memorialPoint;
        playerData.classes().setAttributePoint(point);
        for (AttributeType attr : AttributeType.values()) {
            playerData.classes().setAttribute(attr, 0);
        }
    }
}
