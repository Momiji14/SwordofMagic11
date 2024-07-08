package SwordofMagic11.Player.Smith;

import SwordofMagic11.Component.SomSQL;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.DataBase.CapsuleDataLoader;
import SwordofMagic11.DataBase.DataBase;
import SwordofMagic11.Item.CapsuleData;
import SwordofMagic11.Item.SomEquip;
import SwordofMagic11.Item.SyncItem;
import SwordofMagic11.Player.Menu.GUIManager;
import SwordofMagic11.Player.PlayerData;
import com.github.jasync.sql.db.RowData;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import static SwordofMagic11.Component.Function.*;
import static SwordofMagic11.DataBase.DataBase.DownScroll;
import static SwordofMagic11.DataBase.DataBase.UpScroll;

public class EquipCapsuleMenu extends GUIManager {

    private int scroll = 0;
    private SomEquip equipment;
    private CapsuleData capsule;
    private int amount = 0;

    public EquipCapsuleMenu(PlayerData playerData) {
        super(playerData, "装備カプセル", 6);
    }

    public static CustomItemStack Icon = icon();

    private static CustomItemStack icon() {
        CustomItemStack item = new CustomItemStack(Material.HEART_OF_THE_SEA);
        item.setDisplay("装備カプセル");
        item.addLore("§a装備にカプセルをつけることができます");
        item.setCustomData("SmithMenu", "EquipCapsuleMenu");
        return item;
    }

    @Override
    public void updateContainer() {
        for (int i = 0; i < 18; i++) {
            setItem(i, ItemFlame);
        }
        if (equipment != null) {
            setItem(1, equipment.viewItem().setCustomData("Equipment", true));
            if (capsule != null) {
                setItem(4, capsule.viewItem().setAmountReturn(amount).setCustomData("EquipCapsuleMenu", "Capsule"));
                CustomItemStack receipt = new CustomItemStack(Material.PAPER);
                receipt.setDisplay("カプセル追加情報");
                receipt.addLore(decoLore("成功確率") + scale(capsule.getPercent() * amount * 100) + "%");
                receipt.setCustomData("EquipCapsuleMenu", "Receipt");
                setItem(7, receipt);
            } else {
                setItem(4, null);
                setItem(7, null);
            }
        } else {
            setItem(1, null);
            setItem(4, null);
            setItem(7, null);
        }


        int slot = 18;
        int index = 0;
        for (RowData objects : SomSQL.getSqlList(DataBase.Table.CapsuleStorage, "UUID", playerData.getUUID(), "*")) {
            if (index >= scroll * 8) {
                String capsuleId = objects.getString("Capsule");
                if (CapsuleDataLoader.getComplete().contains(capsuleId)) {
                    int amount = objects.getInt("Amount");
                    if (amount <= 0) continue;
                    CapsuleData capsule = CapsuleDataLoader.getCapsuleData(capsuleId);
                    CustomItemStack item = capsule.viewItem();
                    item.setAmount(amount);
                    item.addLore(decoLore("個数") + amount);
                    setItem(slot, item);
                    slot++;
                    if (isInvalidSlot(slot)) slot++;
                    if (slot >= 53) break;
                } else {
                    playerData.capsuleMenu().delete(capsuleId);
                }
            }
            index++;
        }
        setItem(26, UpScroll());
        setItem(35, ItemFlame);
        setItem(44, ItemFlame);
        setItem(53, DownScroll());
    }

    @Override
    public void topClick(int slot, ItemStack clickedItem, ClickType clickType) {
        if (CustomItemStack.hasCustomData(clickedItem, "UpScroll")) {
            scroll = scrollUp(scroll);
            update();
        } else if (CustomItemStack.hasCustomData(clickedItem, "DownScroll")) {
            scroll = scrollDown(playerData.capsuleMenu().size(), 4, scroll);
            update();
        }
        if (equipment != null) {
            if (CustomItemStack.hasCustomData(clickedItem, "Equipment")) {
                equipment = null;
                update();
            } else if (CustomItemStack.hasCustomData(clickedItem, "EquipCapsuleMenu")) {
                switch (CustomItemStack.getCustomData(clickedItem, "EquipCapsuleMenu")) {
                    case "Receipt" -> {
                        double percent = capsule.getPercent() * amount;
                        playerData.capsuleMenu().remove(capsule.getId(), amount);
                        if (secureRandomDouble(0, 1) < percent) {
                            equipment.addCapsule(capsule);
                            if (equipment.getCapsules().size() >= equipment.getCapsuleSlot()) {
                                equipment = null;
                            }
                            playerData.sendMessage("§eカプセル§aの§e装着§aに§b成功§aしました §7(" + scale(percent * 100) + "%)", SomSound.Level);
                            playerData.updateInventory();
                        } else {
                            playerData.sendMessage("§eカプセル§aの§e装着§aに§c失敗§aしました §7(" + scale(percent * 100) + "%)", SomSound.Tick);
                        }
                        capsule = null;
                        update();
                    }
                    case "Capsule" -> {
                        amount--;
                        if (amount == 0) capsule = null;
                        update();
                    }
                }
            } else if (CustomItemStack.hasCustomData(clickedItem, "Capsule")) {
                CapsuleData capsuleData = CapsuleDataLoader.getCapsuleData(CustomItemStack.getCustomData(clickedItem, "Capsule"));
                for (CapsuleData equipCapsule : equipment.getCapsules()) {
                    if (equipCapsule.getGroup().equals(capsuleData.getGroup())) {
                        playerData.sendMessage("§aすでに§b" + equipCapsule.getGroup() + "系§aの§eカプセル§aがついています", SomSound.Nope);
                        return;
                    }
                }
                if (capsule == capsuleData) {
                    if (amount < 10 && playerData.capsuleMenu().has(capsule.getId(), amount + 1)) {
                        amount++;
                        update();
                    }
                } else {
                    capsule = capsuleData;
                    amount = 1;
                    update();
                }
            }
        }
    }

    @Override
    public void bottomClick(int slot, ItemStack clickedItem, ClickType clickType) {
        if (CustomItemStack.hasCustomData(clickedItem, "Equip.Category")) {
            equipment = (SomEquip) SyncItem.getSomItem(CustomItemStack.getCustomData(clickedItem, "UUID"));
            if (equipment.getCapsules().size() < equipment.getCapsuleSlot()) {
                update();
            } else {
                playerData.sendMessage("§eカプセルスロット§aに空きがありません", SomSound.Nope);
                equipment = null;
            }
        }
    }

    @Override
    public void close() {
        scroll = 0;
        capsule = null;
        equipment = null;
        amount = 0;
    }
}
