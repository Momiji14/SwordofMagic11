package SwordofMagic11.Player.Smith;

import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.DataBase.MaterialDataLoader;
import SwordofMagic11.Item.Material.MemorialMaterial;
import SwordofMagic11.Player.Memorial.MemorialData;
import SwordofMagic11.Player.Menu.GUIManager;
import SwordofMagic11.Player.PlayerData;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

import static SwordofMagic11.Component.Function.decoLore;

public class MemorialCrasherMenu extends GUIManager {

    public static CustomItemStack Icon = icon();
    private static CustomItemStack icon() {
        CustomItemStack item = new CustomItemStack(Material.BLAZE_POWDER);
        item.setDisplay("メモリアル分解");
        item.addLore("§aメモリアルを分解してポイントにします");
        item.setCustomData("SmithMenu", "MemorialCrasherMenu");
        return item;
    }

    private int amount = 0;

    public MemorialCrasherMenu(PlayerData playerData) {
        super(playerData, "メモリアル分解", 6);
    }

    private int amount() {
        return (int) Math.pow(10, amount);
    }

    private CustomItemStack amountIcon() {
        CustomItemStack item = new CustomItemStack(Material.GOLD_NUGGET);
        item.setNonDecoDisplay(decoLore("分解個数") + amount());
        item.setAmountReturn(amount+1);
        item.setCustomData("AmountIcon", true);
        return item;
    }

    @Override
    public void updateContainer() {
        int slot = 0;
        for (Map.Entry<String, Integer> entry : playerData.materialMenu().getStorage().entrySet()) {
            String id = entry.getKey();
            int amount = entry.getValue();
            if (id.contains("Memorial")) {
                MemorialMaterial memorialMaterial = (MemorialMaterial) MaterialDataLoader.getMaterialData(id);
                CustomItemStack item = memorialMaterial.viewItem();
                item.addLore(decoLore("個数") + amount);
                item.setAmount(amount);
                setItem(slot, item);
                slot++;
            }
        }
        setItem(53, amountIcon());
    }

    @Override
    public void topClick(int slot, ItemStack clickedItem, ClickType clickType) {
        if (CustomItemStack.hasCustomData(clickedItem, "AmountIcon")) {
            amount++;
            if (amount >= 4) amount = 0;
            SomSound.Tick.play(playerData);
            update();
        } else if (CustomItemStack.hasCustomData(clickedItem, "Material")) {
            String id = CustomItemStack.getCustomData(clickedItem, "Material");
            MemorialMaterial memorialMaterial = (MemorialMaterial) MaterialDataLoader.getMaterialData(id);
            int amount = Math.min(amount(), playerData.getMaterial(id));
            playerData.removeMaterial(id, amount);
            int point = memorialMaterial.getMemorial().getPoint() * amount;
            playerData.addMemorialPoint(point);
            playerData.sendMessage("§b[+]" + point + "P", SomSound.Tick);
            update();
        }
    }

    @Override
    public void bottomClick(int slot, ItemStack clickedItem, ClickType clickType) {

    }

    @Override
    public void close() {

    }
}
