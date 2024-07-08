package SwordofMagic11.Player.Smith;

import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.Player.Menu.GUIManager;
import SwordofMagic11.Player.PlayerData;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class SmithMenu extends GUIManager {

    private final EquipPlusMenu equipPlusMenu;
    private final EquipPlusConfirmMenu equipPlusConfirmMenu;
    private final EquipPlusRiskMenu equipPlusRiskMenu;
    private final EquipLevelDownMenu equipLevelDownMenu;
    private final MaterializeMenu materializeMenu;
    private final EquipCapsuleMenu equipCapsuleMenu;
    private final EquipCapsuleRemoveMenu equipCapsuleRemoveMenu;
    private final CapsuleShopMenu capsuleShopMenu;
    private final EquipTranceMenu equipTranceMenu;
    private final EquipSeriesChange equipSeriesChange;
    private final MemorialCrasherMenu memorialCrasherMenu;
    private final MemorialGachaMenu memorialGachaMenu;

    public SmithMenu(PlayerData playerData) {
        super(playerData, "鍛冶屋", 2);
        equipPlusMenu = new EquipPlusMenu(playerData);
        equipPlusConfirmMenu = new EquipPlusConfirmMenu(playerData);
        equipPlusRiskMenu = new EquipPlusRiskMenu(playerData);
        equipLevelDownMenu = new EquipLevelDownMenu(playerData);
        materializeMenu = new MaterializeMenu(playerData);
        equipCapsuleMenu = new EquipCapsuleMenu(playerData);
        equipCapsuleRemoveMenu = new EquipCapsuleRemoveMenu(playerData);
        capsuleShopMenu = new CapsuleShopMenu(playerData);
        equipTranceMenu = new EquipTranceMenu(playerData);
        equipSeriesChange = new EquipSeriesChange(playerData);
        memorialCrasherMenu = new MemorialCrasherMenu(playerData);
        memorialGachaMenu = new MemorialGachaMenu(playerData);
    }

    @Override
    public void updateContainer() {
        setItem(0, EquipPlusMenu.Icon);
        setItem(1, EquipPlusConfirmMenu.Icon);
        setItem(2, EquipPlusRiskMenu.Icon);
        setItem(3, EquipLevelDownMenu.Icon);
        setItem(4, MaterializeMenu.Icon);
        setItem(5, EquipCapsuleMenu.Icon);
        setItem(6, EquipCapsuleRemoveMenu.Icon);
        setItem(7, CapsuleShopMenu.Icon);
        setItem(8, EquipTranceMenu.Icon);
        setItem(9, EquipSeriesChange.Icon);
        setItem(10, MemorialCrasherMenu.Icon);
        setItem(11, MemorialGachaMenu.Icon);
    }

    @Override
    public void topClick(int slot, ItemStack clickedItem, ClickType clickType) {
        if (CustomItemStack.hasCustomData(clickedItem, "SmithMenu")) {
            switch (CustomItemStack.getCustomData(clickedItem, "SmithMenu")) {
                case "EquipPlusMenu" -> equipPlusMenu.open();
                case "EquipPlusConfirmMenu" -> equipPlusConfirmMenu.open();
                case "EquipPlusRiskMenu" -> equipPlusRiskMenu.open();
                case "EquipLevelDownMenu" -> equipLevelDownMenu.open();
                case "MaterializeMenu" -> materializeMenu.open();
                case "EquipCapsuleMenu" -> equipCapsuleMenu.open();
                case "EquipCapsuleRemoveMenu" -> equipCapsuleRemoveMenu.open();
                case "CapsuleShopMenu" -> capsuleShopMenu.open();
                case "EquipTranceMenu" -> equipTranceMenu.open();
                case "EquipSeriesChange" -> equipSeriesChange.open();
                case "MemorialCrasherMenu" -> memorialCrasherMenu.open();
                case "MemorialGachaMenu" -> memorialGachaMenu.open();
            }
        }
    }

    @Override
    public void bottomClick(int slot, ItemStack clickedItem, ClickType clickType) {

    }

    @Override
    public void close() {

    }

    public MaterializeMenu getMaterializeMenu() {
        return materializeMenu;
    }
}
