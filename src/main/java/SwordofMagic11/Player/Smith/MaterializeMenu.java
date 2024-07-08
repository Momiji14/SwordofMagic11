package SwordofMagic11.Player.Smith;

import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.DataBase.MaterialDataLoader;
import SwordofMagic11.Item.SomEquip;
import SwordofMagic11.Item.SyncItem;
import SwordofMagic11.Player.Menu.GUIManager;
import SwordofMagic11.Player.PlayerData;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class MaterializeMenu extends GUIManager {

    public static CustomItemStack Icon = icon();

    private static CustomItemStack icon() {
        CustomItemStack item = new CustomItemStack(Material.JUKEBOX);
        item.setDisplay("装備素材化");
        item.addLore("§a装備を素材化します");
        item.addLore("§a素材化すると§e強化値§aなどは§c消滅§aします");
        item.setCustomData("SmithMenu", "MaterializeMenu");
        return item;
    }

    public MaterializeMenu(PlayerData playerData) {
        super(playerData, "素材化", 1);
    }

    @Override
    public void updateContainer() {
        for (int i = 0; i < 9; i++) {
            setItem(i, new CustomItemStack(Material.JUKEBOX).setNonDecoDisplay("§c選択した素材化します"));
        }
    }

    @Override
    public void topClick(int slot, ItemStack clickedItem, ClickType clickType) {

    }

    private String checkUUID;

    @Override
    public void bottomClick(int slot, ItemStack clickedItem, ClickType clickType) {
        if (CustomItemStack.hasCustomData(clickedItem, "UUID")) {
            SomEquip equip = (SomEquip) SyncItem.getSomItem(CustomItemStack.getCustomData(clickedItem, "UUID"));
            if (equip.isWeapon() || equip.isArmor()) {
                String materialize = "素材化" + MaterialDataLoader.Type.getType(equip).getDisplay() + equip.getSeries();
                if (MaterialDataLoader.getComplete().contains(materialize)) {
                    if (!playerData.materialMenu().hasEmpty(materialize)) {
                        playerData.materialMenu().sendNonHasEmpty();
                        return;
                    }
                    if (equip.getPlus() > 0) {
                        if (equip.getUUID().equals(checkUUID)) {
                            checkUUID = null;
                        } else {
                            checkUUID = equip.getUUID();
                            playerData.sendMessage("§aこの装備は強化されています\n§a本当に素材化する場合はもう一度クリックしてください", SomSound.Nope);
                            return;
                        }
                    }
                    playerData.addMaterial(materialize, 1);
                    playerData.itemInventory().delete(equip);
                    playerData.sendMessage(equip.getDisplay() + "§aを§e素材化§aしました", SomSound.Tick);
                } else {
                    playerData.sendMessage(equip.getDisplay() + "§aは§e素材化§a出来ません", SomSound.Nope);
                }
            }
        }
    }

    @Override
    public void close() {
        checkUUID = null;
    }
}
