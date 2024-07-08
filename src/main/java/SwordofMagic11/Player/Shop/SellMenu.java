package SwordofMagic11.Player.Shop;

import SwordofMagic11.Command.Player.Lock;
import SwordofMagic11.Component.SomSQL;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.DataBase.CapsuleDataLoader;
import SwordofMagic11.DataBase.DataBase;
import SwordofMagic11.DataBase.MaterialDataLoader;
import SwordofMagic11.Item.CapsuleData;
import SwordofMagic11.Item.Donation.SomDonationItem;
import SwordofMagic11.Item.Material.MaterialData;
import SwordofMagic11.Item.SomEquip;
import SwordofMagic11.Item.SomItem;
import SwordofMagic11.Item.SyncItem;
import SwordofMagic11.Pet.SomPet;
import SwordofMagic11.Pet.SyncPet;
import SwordofMagic11.Player.Market.MarketSystemCapsule;
import SwordofMagic11.Player.Market.MarketSystemMaterial;
import SwordofMagic11.Player.Menu.GUIManager;
import SwordofMagic11.Player.PlayerData;
import com.github.jasync.sql.db.RowData;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import static SwordofMagic11.Component.Function.*;
import static SwordofMagic11.DataBase.DataBase.DownScroll;
import static SwordofMagic11.DataBase.DataBase.UpScroll;

public class SellMenu extends GUIManager {
    private int scroll = 0;
    private int amount = 0;

    private int amount() {
        return (int) Math.pow(10, amount);
    }

    private CustomItemStack amountIcon() {
        CustomItemStack item = new CustomItemStack(Material.GOLD_NUGGET);
        item.setNonDecoDisplay(decoLore("売却個数") + amount());
        item.setAmountReturn(amount+1);
        item.setCustomData("AmountIcon", true);
        return item;
    }

    public SellMenu(PlayerData playerData) {
        super(playerData, "買取屋", 6);
    }

    @Override
    public void updateContainer() {
        int slot = 0;
        int index = 0;
        for (RowData objects : SomSQL.getSqlList(DataBase.Table.MaterialStorage, "UUID", playerData.getUUID(), "*")) {
            if (index >= scroll * 8) {
                MaterialData material = MaterialDataLoader.getMaterialData(objects.getString("MaterialID"));
                CustomItemStack item = material.viewItem();
                int amount = objects.getInt("Amount");
                item.setAmount(amount);
                item.addLore(decoLore("個数") + amount);
                setItem(slot, item);
                slot++;
                if (isInvalidSlot(slot)) slot++;
                if (slot >= 53) break;
            }
            index++;
        }
        if (slot < 54) {
            for (RowData objects : SomSQL.getSqlList(DataBase.Table.CapsuleStorage, "UUID", playerData.getUUID(), "*")) {
                if (index >= scroll * 8) {
                    CapsuleData capsule = CapsuleDataLoader.getCapsuleData(objects.getString("Capsule"));
                    CustomItemStack item = capsule.viewItem();
                    int amount = objects.getInt("Amount");
                    item.setAmount(amount);
                    item.addLore(decoLore("個数") + amount);
                    setItem(slot, item);
                    slot++;
                    if (isInvalidSlot(slot)) slot++;
                    if (slot >= 53) break;
                }
                index++;
            }
        }
        if (slot < 54) {
            for (SomPet somPet : playerData.petMenu().getCageList()) {
                if (index >= scroll * 8) {
                    setItem(slot, somPet.viewItem());
                    slot++;
                    if (isInvalidSlot(slot)) slot++;
                    if (slot >= 53) break;
                }
                index++;
            }
        }
        setItem(8, UpScroll());
        setItem(17, amountIcon());
        setItem(26, ItemFlame);
        setItem(35, ItemFlame);
        setItem(44, ItemFlame);
        setItem(53, DownScroll());
    }

    private String checkMaterial;

    @Override
    public void topClick(int slot, ItemStack clickedItem, ClickType clickType) {
        if (CustomItemStack.hasCustomData(clickedItem, "AmountIcon")) {
            amount++;
            if (amount >= Digit) amount = 0;
            SomSound.Tick.play(playerData);
            update();
        } else if (CustomItemStack.hasCustomData(clickedItem, "UpScroll")) {
            scroll = scrollUp(scroll);
            update();
        } else if (CustomItemStack.hasCustomData(clickedItem, "DownScroll")) {
            int size = playerData.materialMenu().rawSize() + playerData.capsuleMenu().size() + playerData.petMenu().size() + 1;
            scroll = scrollDown(size, 6, scroll);
            update();
        }
        if (CustomItemStack.hasCustomData(clickedItem, "Material")) {
            String material = CustomItemStack.getCustomData(clickedItem, "Material");
            if (Lock.check(playerData, material)) return;
            int amount = Math.min(playerData.getMaterial(material), amount());
            MaterialData memorialData = MaterialDataLoader.getMaterialData(material);
            if (memorialData.isRare()) {
                if (!material.equals(checkMaterial)) {
                    checkMaterial = material;
                    playerData.sendMessage("§aこの§eアイテム§aは§eレアアイテム§aです\n§a本当に§c売却§aする場合は§eもう一度クリック§aしてください", SomSound.Nope);
                    return;
                }
            }
            if (playerData.hasMaterial(material, amount)) {
                playerData.removeMaterial(material, amount);
                playerData.addMel(memorialData.getSell() * amount);
                MarketSystemMaterial.add(material, amount);
                SomSound.Tick.play(playerData);
            }
            update();
        } else if (CustomItemStack.hasCustomData(clickedItem, "Capsule")) {
            String capsule = CustomItemStack.getCustomData(clickedItem, "Capsule");
            if (Lock.check(playerData, capsule)) return;
            int amount = Math.min(playerData.capsuleMenu().get(capsule), amount());
            if (playerData.capsuleMenu().has(capsule, amount)) {
                playerData.capsuleMenu().remove(capsule, amount);
                playerData.addMel(CapsuleData.Sell * amount);
                MarketSystemCapsule.add(capsule, amount);
                SomSound.Tick.play(playerData);
            }
            update();
        } else if (CustomItemStack.hasCustomData(clickedItem, "Pet")) {
            SyncPet.delete(CustomItemStack.getCustomData(clickedItem, "Pet"));
            playerData.addMel(SomPet.Sell);
            update();
        }
    }

    private String checkUUID;

    @Override
    public void bottomClick(int slot, ItemStack clickedItem, ClickType clickType) {
        if (CustomItemStack.hasCustomData(clickedItem, "UUID")) {
            String uuid = CustomItemStack.getCustomData(clickedItem, "UUID");
            if (SyncItem.hasSomItem(uuid)) {
                SomItem item = SyncItem.getSomItem(uuid);
                if (Lock.check(playerData, item.getUUID())) return;
                if (item instanceof SomEquip equip) {
                    if (equip.getPlus() > 0) {
                        if (equip.getUUID().equals(checkUUID)) {
                            checkUUID = null;
                        } else {
                            checkUUID = equip.getUUID();
                            playerData.sendMessage("§aこの§e装備§aは§c強化§aされています\n§a本当に§c売却§aする場合は§eもう一度クリック§aしてください", SomSound.Nope);
                            return;
                        }
                    }
                } else if (item instanceof SomDonationItem) {
                    playerData.sendMessage("§d寄付アイテム§aは売却できません", SomSound.Nope);
                    return;
                }
                playerData.addMel(item.getSell());
                playerData.itemInventory().delete(item);
                item.setSync(false);
                update();
            }
        }
    }

    @Override
    public void close() {
        checkUUID = null;
    }
}
