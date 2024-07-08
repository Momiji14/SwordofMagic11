package SwordofMagic11.Player.Smith;

import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.Item.SomEquip;
import SwordofMagic11.Player.PlayerData;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

import static SwordofMagic11.Component.Function.ceil;
import static SwordofMagic11.Component.Function.decoLore;

public class EquipPlusConfirmMenu extends EquipPlusBaseMenu {

    public static CustomItemStack Icon = icon();

    private static CustomItemStack icon() {
        CustomItemStack item = new CustomItemStack(Material.ANVIL);
        item.setDisplay("装備確定強化");
        item.addLore("§a強化値を上げることができます");
        item.addLore("§a基礎値が強い武器ほど");
        item.addLore("§aグラインダーの必要量は多くなります");
        item.setCustomData("SmithMenu", "EquipPlusConfirmMenu");
        return item;
    }

    public EquipPlusConfirmMenu(PlayerData playerData) {
        super(playerData, "装備確定強化");
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
        lore.add(decoLore("強化確率") + "§c確定強化");
        lore.add(decoLore("必要グラインダー") + amount(equipment) + "個");
        return lore;
    }


    @Override
    public void process() {
        int amount = amount(equipment);
        if (playerData.hasMaterial("グラインダー", amount)) {
            playerData.removeMaterial("グラインダー", amount);
            equipment.addPlus(1);
            playerData.sendMessage(equipment.getDisplay() + "§aの§e強化値§aが§b" + equipment.getPlus() + "§aになりました" + leftGrinder(), SomSound.Level);
        } else playerData.sendNonGrinder();
    }

    public static int amount(SomEquip equipment) {
        return ceil(EquipPlusMenu.amountDouble(equipment) / EquipPlusMenu.percent(equipment));
    }
}
