package SwordofMagic11.Player.Storage;

import SwordofMagic11.Command.Player.Trade;
import SwordofMagic11.Component.SomSQL;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.DataBase.CapsuleDataLoader;
import SwordofMagic11.DataBase.DataBase;
import SwordofMagic11.DataBase.MaterialDataLoader;
import SwordofMagic11.Item.Material.MaterialData;
import SwordofMagic11.Player.Menu.GUIManager;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Player.Setting.PlayerSetting;
import com.github.jasync.sql.db.ResultSet;
import com.github.jasync.sql.db.RowData;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static SwordofMagic11.Component.Function.*;
import static SwordofMagic11.DataBase.DataBase.*;

public class MaterialMenu extends GUIManager {

    public CustomItemStack icon() {
        CustomItemStack item = new CustomItemStack(Material.CHEST);
        ResultSet resultSet = SomSQL.getSqlList(DataBase.Table.MaterialStorage, "UUID", playerData.getUUID(), "*");
        int index = 0;
        int mel = 0;
        List<String> storageLore = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : materialStorage.entrySet()) {
            MaterialData material = MaterialDataLoader.getMaterialData(entry.getKey());
            int amount = entry.getValue();
            mel += material.getSell() * amount;
        }
        for (Map.Entry<String, Integer> entry : materialStorage.entrySet()) {
            MaterialData material = MaterialDataLoader.getMaterialData(entry.getKey());
            int amount = entry.getValue();
            storageLore.add("§7・§f" + material.getDisplay() + "§ax" + amount);
            index++;
            if (playerData.setting().is(PlayerSetting.BooleanEnum.StorageViewShort) && index >= ContainerViewSize) {
                storageLore.add("§7その他..." + (resultSet.size() - ContainerViewSize) + "種");
                break;
            }
        }
        item.setDisplay("素材倉庫");
        item.addLore(decoLore("容量") + size() + "/" + playerData.materialStorageSize());
        item.addLore(decoLore("所持") + playerData.getMel() + "メル");
        item.addLore(decoLore("資産") + mel + "メル");
        item.addSeparator("内容物");
        item.addLore(storageLore);
        item.setCustomData("Menu", "MaterialMenu");
        lastTotalMel = mel;
        return item;
    }

    public CustomItemStack iconTrade() {
        CustomItemStack item = new CustomItemStack(Material.OAK_CHEST_BOAT);
        item.setDisplay("素材送信");
        item.addLore("§a素材を他のプレイヤーへ送ります");
        item.setCustomData("PlayerSelect", "SendMaterial");
        return item;
    }

    private final ConcurrentHashMap<String, Integer> materialStorage = new ConcurrentHashMap<>();
    private int scroll = 0;
    private int lastTotalMel = 0;
    private int sendAmount = 0;
    private PlayerData targetPlayer;

    public MaterialMenu(PlayerData playerData) {
        super(playerData, "素材倉庫", 6);
        for (RowData objects : SomSQL.getSqlList(DataBase.Table.MaterialStorage, "UUID", playerData.getUUID(), "*")) {
            String materialId = objects.getString("MaterialID");
            if (MaterialDataLoader.getComplete().contains(materialId)) {
                materialStorage.put(materialId, objects.getInt("Amount"));
            } else {
                delete(materialId);
            }
        }
    }

    public void trade(PlayerData targetPlayer) {
        this.targetPlayer = targetPlayer;
        super.open();
    }

    public ConcurrentHashMap<String, Integer> getStorage() {
        return materialStorage;
    }

    public int lastTotalMel() {
        return lastTotalMel;
    }

    public int sendAmount() {
        return (int) Math.pow(10, sendAmount);
    }

    private static final String[] key = new String[]{"UUID", "MaterialID"};

    private String[] value(String materialID) {
        return new String[]{playerData.getUUID(), materialID};
    }

    public int get(String material) {
        if (!materialStorage.containsKey(material)) {
            if (SomSQL.exists(DataBase.Table.MaterialStorage, key, value(material), "Amount")) {
                materialStorage.put(material, SomSQL.getInt(DataBase.Table.MaterialStorage, key, value(material), "Amount"));
            } else {
                return 0;
            }
        }
        return materialStorage.get(material);
    }

    public void set(String material, int amount) {
        if (amount > 0) {
            materialStorage.put(material, amount);
            SomSQL.setSql(DataBase.Table.MaterialStorage, key, value(material), "Amount", amount);
        } else {
            delete(material);
        }
    }

    public void add(String material, int amount) {
        playerData.sideBar().triggerMaterial(MaterialDataLoader.getMaterialData(material));
        set(material, get(material) + amount);
    }

    public void remove(String material, int amount) {
        set(material, get(material) - amount);
    }

    public void delete(String material) {
        materialStorage.remove(material);
        SomSQL.delete(DataBase.Table.MaterialStorage, key, value(material));
    }

    public int size() {
        int size = materialStorage.size();
        for (String id : MaterialDataLoader.getNonUseSlotList()) {
            if (materialStorage.containsKey(id)) {
                size--;
            }
        }
        return size;
    }

    public int rawSize() {
        return materialStorage.size();
    }

    public boolean hasEmpty(MaterialData material) {
        return hasEmpty(material.getId());
    }

    public boolean hasEmpty(String material) {
        return materialStorage.containsKey(material) || playerData.materialStorageSize() > size();
    }

    @Override
    public void updateContainer() {
        int slot = 0;
        int index = 0;
        for (RowData objects : SomSQL.getSqlList(DataBase.Table.MaterialStorage, "UUID", playerData.getUUID(), "*")) {
            if (index >= scroll * 8) {
                String materialId = objects.getString("MaterialID");
                if (MaterialDataLoader.getComplete().contains(materialId)) {
                    MaterialData material = MaterialDataLoader.getMaterialData(materialId);
                    CustomItemStack item = material.viewItem();
                    int amount = objects.getInt("Amount");
                    item.setAmount(amount);
                    item.addLore(decoLore("個数") + amount);
                    item.setCustomData("Material", materialId);
                    setItem(slot, item);
                    slot++;
                    if (isInvalidSlot(slot)) slot++;
                    if (slot >= 53) break;
                } else {
                    delete(materialId);
                }
            }
            index++;
        }
        for (int i = 17; i < 45; i += 9) {
            setItem(i, ItemFlame);
        }
        if (targetPlayer != null) {
            CustomItemStack item = new CustomItemStack(Material.GOLD_NUGGET);
            item.setNonDecoDisplay(decoLore("送信個数") + sendAmount());
            item.setAmountReturn(sendAmount+1);
            item.setCustomData("AmountIcon", true);
            setItem(17, item);
        }
        setItem(8, UpScroll());
        setItem(53, DownScroll());
    }

    @Override
    public void topClick(int slot, ItemStack clickedItem, ClickType clickType) {
        if (CustomItemStack.hasCustomData(clickedItem, "UpScroll")) {
            scroll = scrollUp(scroll);
            update();
        } else if (CustomItemStack.hasCustomData(clickedItem, "DownScroll")) {
            scroll = scrollDown(rawSize(), 6, scroll);
            update();
        } else if (CustomItemStack.hasCustomData(clickedItem, "AmountIcon")) {
            sendAmount++;
            if (sendAmount >= Digit) sendAmount = 0;
            SomSound.Tick.play(playerData);
            update();
        } else if (CustomItemStack.hasCustomData(clickedItem, "Material")) {
            if (targetPlayer != null) {
                if (targetPlayer.isValid()) {
                    String id = CustomItemStack.getCustomData(clickedItem, "Material");
                    int amount = sendAmount();
                    Trade.tradeMaterial(playerData, targetPlayer, id, amount);
                    update();
                } else {
                    playerData.sendMessage(targetPlayer.getDisplayName() + "§aは§cオフライン§aです", SomSound.Nope);
                }
            }
        }
    }

    @Override
    public void bottomClick(int slot, ItemStack clickedItem, ClickType clickType) {

    }

    @Override
    public void close() {
        scroll = 0;
        sendAmount = 0;
        targetPlayer = null;
    }

    public void sendNonHasEmpty() {
        playerData.sendMessage("§c素材倉庫に空きがありません", SomSound.Nope);
    }
}
