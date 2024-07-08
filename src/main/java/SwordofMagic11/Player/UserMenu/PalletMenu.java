package SwordofMagic11.Player.UserMenu;

import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.DataBase.MaterialDataLoader;
import SwordofMagic11.Item.Material.MaterialData;
import SwordofMagic11.Player.Menu.GUIManager;
import SwordofMagic11.Player.Pallet;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Skill.SomSkill;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

import static SwordofMagic11.Component.Function.ceil;

public class PalletMenu extends GUIManager {
    private int select = -1;

    public PalletMenu(PlayerData playerData) {
        super(playerData, "パレットメニュー", 6);
    }

    public CustomItemStack icon() {
        CustomItemStack item = new CustomItemStack(Material.GLOW_ITEM_FRAME);
        item.setDisplay("パレットメニュー");
        item.addLore("§aパレットの設定が行えます");
        item.addLore("§aスキルの発動設定はここから行います");
        item.setCustomData("Menu", "PalletMenu");
        return item;
    }


    public int getSelect() {
        return select;
    }

    public void setSelect(int select) {
        this.select = select;
    }

    @Override
    public void updateContainer() {
        int slot = 0;
        for (String id : playerData.classes().getMainClass().getSkill()) {
            SomSkill skill = playerData.skillManager().instance(id);
            setItem(slot, skill.viewItem());
            slot++;
            if (slot >= 53) return;
        }
        slot = ceil(slot / 9.0) * 9;
        for (Map.Entry<String, Integer> entry : playerData.materialMenu().getStorage().entrySet()) {
            MaterialData material = MaterialDataLoader.getMaterialData(entry.getKey());
            if (material.isUseAble()) {
                setItem(slot, material.viewItem().setAmountReturn(entry.getValue()));
                slot++;
                if (slot >= 53) return;
            }
        }
    }

    @Override
    public void topClick(int slot, ItemStack clickedItem, ClickType clickType) {
        if (CustomItemStack.hasCustomData(clickedItem, "SomSkill")) {
            SomSkill skill = playerData.skillManager().instance(CustomItemStack.getCustomData(clickedItem, "SomSkill"));
            if (select == -1) {
                playerData.classes().setPallet(skill);
            } else {
                playerData.classes().setPallet(select, skill);
                select = -1;
            }
            SomSound.Tick.play(playerData);
        }
        if (CustomItemStack.hasCustomData(clickedItem, "Material")) {
            String materialId = CustomItemStack.getCustomData(clickedItem, "Material");
            MaterialData material = MaterialDataLoader.getMaterialData(materialId);
            if (material instanceof Pallet pallet) {
                if (select == -1) {
                    playerData.classes().setPallet(pallet);
                } else {
                    playerData.classes().setPallet(select, pallet);
                    select = -1;
                }
                SomSound.Tick.play(playerData);
            }
        }
    }

    @Override
    public void bottomClick(int slot, ItemStack clickedItem, ClickType clickType) {
        if (CustomItemStack.hasCustomData(clickedItem, "NonPallet")) {
            setSelect(CustomItemStack.getCustomDataInt(clickedItem, "NonPallet"));
            update();
        }
    }

    @Override
    public void close() {
        select = -1;
    }
}
