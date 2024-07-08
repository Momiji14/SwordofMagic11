package SwordofMagic11.Player.Smith;

import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.Item.SomEquip;
import SwordofMagic11.Player.PlayerData;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

import static SwordofMagic11.Component.Function.*;

public class EquipPlusMenu extends EquipPlusBaseMenu {

    public static int MaxPlus = 40;

    public static CustomItemStack Icon = icon();

    private static CustomItemStack icon() {
        CustomItemStack item = new CustomItemStack(Material.CHIPPED_ANVIL);
        item.setDisplay("装備一般強化");
        item.addLore("§a強化値を上げることができます");
        item.addLore("§a基礎値が強い武器ほど");
        item.addLore("§aグラインダーの必要量は多く");
        item.addLore("§a確率は低くなります");
        item.setCustomData("SmithMenu", "EquipPlusMenu");
        return item;
    }


    public EquipPlusMenu(PlayerData playerData) {
        super(playerData, "装備強化");
    }

    @Override
    public SomEquip result() {
        SomEquip result = equipment.clone();
        result.addPlus(1);
        return result;
    }

    @Override
    public List<String> receipt() {
        List<String> lore = new ArrayList<>();
        lore.add("強化情報");
        lore.add(decoLore("強化確率") + scale(percent(equipment) * 100, 2) + "%");
        lore.add(decoLore("必要グラインダー") + amount(equipment) + "個");
        return lore;
    }

    @Override
    public void process() {
        int amount = amount(equipment);
        if (playerData.hasMaterial("グラインダー", amount)) {
            playerData.removeMaterial("グラインダー", amount);
            double percent = percent(equipment);
            if (secureRandomDouble(0, 1) < percent) {
                equipment.addPlus(1);
                playerData.sendMessage(equipment.getDisplay() + "§aの§e強化§aに§b成功§aしました §e[" + scale(percent * 100, 2) + "%]" + leftGrinder(), SomSound.Level);
            } else {
                playerData.sendMessage(equipment.getDisplay() + "§aの§e強化§aに§c失敗§aしました §e[" + scale(percent * 100, 2) + "%]" + leftGrinder(), SomSound.Tick);
            }
        } else playerData.sendNonGrinder();
    }

    public static double amountDouble(SomEquip equipment) {
        if (equipment.getPower() <= 14.0) {
            return equipment.getPower() * 0.2;
        } else {
            return equipment.getPower() * 0.7;
        }
    }

    public static int amount(SomEquip equipment) {
        return ceil(amountDouble(equipment));
    }

    public static double percent(SomEquip equipment) {
        return Math.pow(0.995 / (1 + equipment.getPower() * 0.01), equipment.getPlus()) + 0.005;
    }
}
