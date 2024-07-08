package SwordofMagic11.Player.Smith;

import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.DataBase.ItemDataLoader;
import SwordofMagic11.Item.SomEquip;
import SwordofMagic11.Item.SomItem;
import SwordofMagic11.Item.SyncItem;
import SwordofMagic11.Player.Classes;
import SwordofMagic11.Player.Menu.GUIManager;
import SwordofMagic11.Player.PlayerData;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import static SwordofMagic11.Component.Function.decoLore;

public class EquipSeriesChange extends GUIManager {

    public static CustomItemStack Icon = icon();
    public static int Eld = 250;

    private static CustomItemStack icon() {
        CustomItemStack item = new CustomItemStack(Material.SMITHING_TABLE);
        item.setDisplay("装備シリーズ変更");
        item.addLore("§a装備を同シリーズの装備に変更できます");
        item.addLore("§a強化値などは引き継がれます");
        item.addLore("§aメルまたはエルドを消費します");
        item.setCustomData("SmithMenu", "EquipSeriesChange");
        return item;
    }

    private boolean useEld = false;
    private SomEquip equipment = null;
    public EquipSeriesChange(PlayerData playerData) {
        super(playerData, "装備シリーズ変更", 1);
    }

    public CustomItemStack currency() {
        CustomItemStack item;
        if (useEld) {
            item = new CustomItemStack(Material.EMERALD);
            item.setDisplay("使用通貨");
            item.addLore(decoLore("所持エルド") + playerData.donation().getEld());
            item.addLore(decoLore("消費エルド") + Eld);
            item.setCustomData("Currency", "Eld");
        } else {
            item = new CustomItemStack(Material.GOLD_NUGGET);
            item.setDisplay("使用通貨");
            item.addLore(decoLore("所持メル") + playerData.getMel());
            item.addLore(decoLore("消費メル") + mel());
            item.setCustomData("Currency", "Mel");
        }
        return item;
    }

    public int mel() {
        if (equipment == null) return 0;
        return (int) (equipment.value(1000000) / Classes.MaxLevel);
    }

    @Override
    public void updateContainer() {
        setItem(1, ItemFlame);
        setItem(7, ItemFlame);
        setItem(8, currency());
        if (equipment != null) {
            setItem(0, equipment.viewItem().setCustomData("Remove", true));
            int slot = 2;
            for (SomItem series : ItemDataLoader.getSeries(equipment.getSeries())) {
                if (equipment.getId().equals(series.getId())) continue;
                if (series instanceof SomEquip equip) {
                    if (equipment.isWeapon() && equip.isArmor()) continue;
                    if (equipment.isArmor() && equip.isWeapon()) continue;
                    SomEquip viewEquip = equipment.copy(equip.clone());
                    setItem(slot, viewEquip.viewItem().setCustomData("SeriesChange", equip.getId()));
                    slot++;
                }
            }
        } else {
            setItem(0, null);
            for (int i = 2; i < 6; i++) {
                setItem(i, null);
            }
        }
    }

    @Override
    public void topClick(int slot, ItemStack clickedItem, ClickType clickType) {
        if (CustomItemStack.hasCustomData(clickedItem, "Currency")) {
            switch (CustomItemStack.getCustomData(clickedItem, "Currency")) {
                case "Eld" -> useEld = false;
                case "Mel" -> useEld = true;
            }
            update();
        }
        if (CustomItemStack.hasCustomData(clickedItem, "SeriesChange")) {
            Runnable checkOut = null;
            if (useEld) {
                if (playerData.donation().hasEld(Eld)) {
                    checkOut = () -> playerData.donation().removeEld(Eld);
                } else {
                    playerData.sendNonEld();
                }
            } else {
                int mel = mel();
                if (playerData.hasMel(mel)) {
                    checkOut = () -> playerData.removeMel(mel);
                } else {
                    playerData.sendNonMel();
                }
            }
            if (checkOut != null) {
                String newId = CustomItemStack.getCustomData(clickedItem, "SeriesChange");
                SomEquip newEquip = (SomEquip) SyncItem.register(newId, playerData, SyncItem.State.ItemInventory);
                equipment.copy(newEquip);
                playerData.itemInventory().delete(equipment);
                playerData.updateInventory();
                checkOut.run();
                playerData.sendMessage(equipment.getDisplay() + "§aに§e変換§aしました", SomSound.Level);
                equipment = null;
                update();
            }
        }
    }

    @Override
    public void bottomClick(int slot, ItemStack clickedItem, ClickType clickType) {
        if (CustomItemStack.hasCustomData(clickedItem, "Equip.Category")) {
            equipment = (SomEquip) SyncItem.getSomItem(CustomItemStack.getCustomData(clickedItem, "UUID"));
            if (equipment.hasSeries()) {
                update();
            } else {
                playerData.sendMessage("§eシリーズ装備§aではありません", SomSound.Nope);
                equipment = null;
            }
        }
    }

    @Override
    public void close() {
        equipment = null;
    }
}
