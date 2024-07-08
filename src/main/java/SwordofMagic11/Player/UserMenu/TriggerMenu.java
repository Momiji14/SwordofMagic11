package SwordofMagic11.Player.UserMenu;

import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.DataBase.MaterialDataLoader;
import SwordofMagic11.Item.Material.MaterialData;
import SwordofMagic11.Item.Material.MemorialMaterial;
import SwordofMagic11.Item.Material.UseAbleMaterial;
import SwordofMagic11.Player.Memorial.MemorialData;
import SwordofMagic11.Player.Menu.GUIManager;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Player.Setting.PlayerSetting;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

import static SwordofMagic11.Component.Function.decoLore;
import static SwordofMagic11.Component.Function.scale;

public class TriggerMenu extends GUIManager {

    public CustomItemStack icon() {
        CustomItemStack item = new CustomItemStack(Material.LEVER);
        item.setDisplay("トリガーメニュー");
        item.addLore("§aここから使用可能素材を使用できます");
        item.addSeparator("使用可能なマテリアル");
        int index = 0;
        for (Map.Entry<String, Integer> entry : playerData.materialMenu().getStorage().entrySet()) {
            MaterialData material = MaterialDataLoader.getMaterialData(entry.getKey());
            if (material.isUseAble()) {
                int amount = entry.getValue();
                item.addLore("§7・§f" + material.getDisplay() + "§ax" + amount);
                index++;
                if (playerData.setting().is(PlayerSetting.BooleanEnum.StorageViewShort) && index >= 20) {
                    item.addLore("§7その他..." + (playerData.materialMenu().size() - 20) + "種");
                    break;
                }
            }
        }
        item.setCustomData("Menu", "TriggerMenu");
        return item;
    }

    public TriggerMenu(PlayerData playerData) {
        super(playerData, "トリガーメニュー", 6);
    }

    @Override
    public void updateContainer() {
        int slot = 0;
        for (Map.Entry<String, Integer> entry : playerData.materialMenu().getStorage().entrySet()) {
            MaterialData material = MaterialDataLoader.getMaterialData(entry.getKey());
            if (material.isUseAble()) {
                CustomItemStack item = material.viewItem().setAmountReturn(entry.getValue());
                if (material instanceof MemorialMaterial memorialMaterial) {
                    MemorialData memorial = memorialMaterial.getMemorial();
                    if (playerData.memorialMenu().has(memorial)) {
                        item.addSeparator("メモリアル情報");
                        item.addLore(decoLore("復元率") + scale(playerData.memorialMenu().get(memorial) * 100, 3) + "%");
                    }
                }
                setItem(slot, item);
                slot++;
                if (slot >= 53) return;
            }
        }
    }

    @Override
    public void topClick(int slot, ItemStack clickedItem, ClickType clickType) {
        if (CustomItemStack.hasCustomData(clickedItem, "Material")) {
            String materialId = CustomItemStack.getCustomData(clickedItem, "Material");
            MaterialData material = MaterialDataLoader.getMaterialData(materialId);
            if (material instanceof UseAbleMaterial useAbleMaterial) {
                useAbleMaterial.use(playerData);
                update();
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
