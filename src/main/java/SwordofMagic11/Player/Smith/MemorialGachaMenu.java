package SwordofMagic11.Player.Smith;

import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.DataBase.MemorialDataLoader;
import SwordofMagic11.Item.Material.MemorialMaterial;
import SwordofMagic11.Player.Menu.GUIManager;
import SwordofMagic11.Player.PlayerData;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import static SwordofMagic11.Component.Function.randomDouble;
import static SwordofMagic11.Component.Function.randomGet;

public class MemorialGachaMenu extends GUIManager {

    public static final int GachaPoint = 1000;

    public static CustomItemStack Icon = icon();
    private static CustomItemStack icon() {
        CustomItemStack item = new CustomItemStack(Material.GOLDEN_APPLE);
        item.setDisplay("メモリアルガチャ");
        item.addLore("§aメモリアルガチャを引けます");
        item.setCustomData("SmithMenu", "MemorialGachaMenu");
        return item;
    }

    public MemorialGachaMenu(PlayerData playerData) {
        super(playerData, "メモリアルガチャ", 1);
    }

    public CustomItemStack roll_1 () {
        CustomItemStack item = new CustomItemStack(Material.APPLE);
        item.setNonDecoDisplay("§e1回まわす");
        item.addLore("§e" + GachaPoint + "P");
        item.setCustomData("Gacha", 1);
        return item;
    }

    public CustomItemStack roll_10 () {
        CustomItemStack item = new CustomItemStack(Material.GOLDEN_APPLE);
        item.setNonDecoDisplay("§e10回まわす");
        item.addLore("§e" + GachaPoint * 10 + "P");
        item.setCustomData("Gacha", 10);
        return item;
    }

    @Override
    public void updateContainer() {
        setItem(2, roll_1());
        setItem(6, roll_10());
    }

    @Override
    public void topClick(int slot, ItemStack clickedItem, ClickType clickType) {
        if (CustomItemStack.hasCustomData(clickedItem, "Gacha")) {
            int roll = CustomItemStack.getCustomDataInt(clickedItem, "Gacha");
            int reqPoint = GachaPoint * roll;
            if (playerData.hasMemorialPoint(reqPoint)) {
                playerData.removeMemorialPoint(reqPoint);
                for (int i = 0; i < roll; i++) {
                    MemorialMaterial memorialMaterial = randomGet(MemorialDataLoader.getGachaList());
                    playerData.addMaterial(memorialMaterial, 1);
                    playerData.sendMessage("§b[+]" + memorialMaterial.getDisplay());
                }
                SomSound.Tick.play(playerData);
            } else {
                if (randomDouble() < 0.01) {
                    playerData.sendMessage("§cポイントが足りないっすねぇ（笑）", SomSound.Nope);
                } else {
                    playerData.sendMessage("§cポイントが足りません", SomSound.Nope);
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
}
