package SwordofMagic11.Player.Storage;

import SwordofMagic11.Command.Player.Trade;
import SwordofMagic11.Component.SomSQL;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.DataBase.CapsuleDataLoader;
import SwordofMagic11.DataBase.DataBase;
import SwordofMagic11.DataBase.MaterialDataLoader;
import SwordofMagic11.Item.CapsuleData;
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

public class CapsuleMenu extends GUIManager {

    public CustomItemStack icon() {
        CustomItemStack item = new CustomItemStack(Material.ENDER_CHEST);
        ResultSet resultSet = SomSQL.getSqlList(DataBase.Table.CapsuleStorage, "UUID", playerData.getUUID(), "*");
        int index = 0;
        int mel = 0;
        List<String> storageLore = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : capsuleStorage.entrySet()) {
            int amount = entry.getValue();
            mel += CapsuleData.Sell * amount;
        }
        for (Map.Entry<String, Integer> entry : capsuleStorage.entrySet()) {
            CapsuleData capsule = CapsuleDataLoader.getCapsuleData(entry.getKey());
            int amount = entry.getValue();
            storageLore.add("§7・§f" + capsule.getDisplay() + "§ax" + amount);
            index++;
            if (playerData.setting().is(PlayerSetting.BooleanEnum.StorageViewShort) && index >= ContainerViewSize) {
                storageLore.add("§7その他..." + (resultSet.size() - ContainerViewSize) + "種");
                break;
            }
        }
        item.setDisplay("カプセル倉庫");
        item.addLore(decoLore("容量") + size() + "/" + playerData.capsuleStorageSize());
        item.addLore(decoLore("所持") + playerData.getMel() + "メル");
        item.addLore(decoLore("資産") + mel + "メル");
        item.addSeparator("内容物");
        item.addLore(storageLore);
        item.setCustomData("Menu", "CapsuleMenu");
        lastTotalMel = mel;
        return item;
    }

    public CustomItemStack iconTrade() {
        CustomItemStack item = new CustomItemStack(Material.BIRCH_CHEST_BOAT);
        item.setDisplay("カプセル送信");
        item.addLore("§aカプセルを他のプレイヤーへ送ります");
        item.setCustomData("PlayerSelect", "SendCapsule");
        return item;
    }

    private final ConcurrentHashMap<String, Integer> capsuleStorage = new ConcurrentHashMap<>();
    private int scroll = 0;
    private int lastTotalMel = 0;
    private int sendAmount = 0;
    private PlayerData targetPlayer;

    public CapsuleMenu(PlayerData playerData) {
        super(playerData, "カプセル倉庫", 6);
        for (RowData objects : SomSQL.getSqlList(DataBase.Table.CapsuleStorage, "UUID", playerData.getUUID(), "*")) {
            capsuleStorage.put(objects.getString("Capsule"), objects.getInt("Amount"));
        }
    }

    public void trade(PlayerData targetPlayer) {
        this.targetPlayer = targetPlayer;
        super.open();
    }

    public ConcurrentHashMap<String, Integer> getStorage() {
        return capsuleStorage;
    }

    public int lastTotalMel() {
        return lastTotalMel;
    }

    public int sendAmount() {
        return (int) Math.pow(10, sendAmount);
    }

    private String[] key = new String[]{"UUID", "Capsule"};

    private String[] value(String capsule) {
        return new String[]{playerData.getUUID(), capsule};
    }

    public int get(String capsule) {
        if (!capsuleStorage.containsKey(capsule)) {
            if (SomSQL.exists(DataBase.Table.CapsuleStorage, key, value(capsule), "Amount")) {
                capsuleStorage.put(capsule, SomSQL.getInt(DataBase.Table.CapsuleStorage, key, value(capsule), "Amount"));
            } else {
                return 0;
            }
        }
        return capsuleStorage.get(capsule);
    }

    public void set(String capsule, int amount) {
        if (amount > 0) {
            capsuleStorage.put(capsule, amount);
            SomSQL.setSql(DataBase.Table.CapsuleStorage, key, value(capsule), "Amount", amount);
        } else {
            delete(capsule);
        }
    }

    public void add(String capsule, int amount) {
        set(capsule, get(capsule) + amount);
        playerData.sideBar().triggerCapsule(CapsuleDataLoader.getCapsuleData(capsule));
    }

    public void remove(String capsule, int amount) {
        set(capsule, get(capsule) - amount);
    }

    public boolean has(String capsule, int amount) {
        return get(capsule) >= amount;
    }

    public void delete(String capsule) {
        capsuleStorage.remove(capsule);
        SomSQL.delete(DataBase.Table.CapsuleStorage, key, value(capsule));
    }

    public int size() {
        return capsuleStorage.size();
    }

    public boolean hasEmpty(CapsuleData capsule) {
        return hasEmpty(capsule.getId());
    }

    public boolean hasEmpty(String capsule) {
        return capsuleStorage.containsKey(capsule) || playerData.capsuleStorageSize() > size();
    }

    @Override
    public void updateContainer() {
        int slot = 0;
        int index = 0;
        for (RowData objects : SomSQL.getSqlList(DataBase.Table.CapsuleStorage, "UUID", playerData.getUUID(), "*")) {
            if (index >= scroll * 8) {
                String capsuleId = objects.getString("Capsule");
                if (CapsuleDataLoader.getComplete().contains(capsuleId)) {
                    CapsuleData capsule = CapsuleDataLoader.getCapsuleData(capsuleId);
                    CustomItemStack item = capsule.viewItem();
                    int amount = objects.getInt("Amount");
                    item.setAmount(amount);
                    item.addLore(decoLore("個数") + amount);
                    item.setCustomData("Capsule", capsuleId);
                    setItem(slot, item);
                    slot++;
                    if (isInvalidSlot(slot)) slot++;
                    if (slot >= 53) break;
                } else {
                    delete(capsuleId);
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
            scroll = scrollDown(size(), 6, scroll);
            update();
        } else if (CustomItemStack.hasCustomData(clickedItem, "AmountIcon")) {
            sendAmount++;
            if (sendAmount >= Digit) sendAmount = 0;
            SomSound.Tick.play(playerData);
            update();
        } else if (CustomItemStack.hasCustomData(clickedItem, "Capsule")) {
            if (targetPlayer != null) {
                if (targetPlayer.isValid()) {
                    String id = CustomItemStack.getCustomData(clickedItem, "Capsule");
                    int amount = sendAmount();
                    Trade.tradeCapsule(playerData, targetPlayer, id, amount);
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
        playerData.sendMessage("§cカプセル倉庫に空きがありません", SomSound.Nope);
    }
}
