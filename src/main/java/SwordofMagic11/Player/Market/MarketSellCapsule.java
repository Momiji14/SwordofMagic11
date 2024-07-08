package SwordofMagic11.Player.Market;

import SwordofMagic11.Component.SomSQL;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.DataBase.CapsuleDataLoader;
import SwordofMagic11.DataBase.DataBase;
import SwordofMagic11.Item.CapsuleData;
import SwordofMagic11.Player.Menu.GUIManager;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Player.Setting.PlayerSetting;
import com.github.jasync.sql.db.ResultSet;
import com.github.jasync.sql.db.RowData;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.*;

import static SwordofMagic11.Component.Function.*;
import static SwordofMagic11.DataBase.DataBase.*;

public class MarketSellCapsule extends GUIManager {

    public CustomItemStack icon() {
        HashMap<String, Integer> map = new LinkedHashMap<>();
        CustomItemStack item = new CustomItemStack(Material.ENDER_CHEST);
        item.setDisplay("マーケット販売 [カプセル]");
        ResultSet resultSet = SomSQL.getSqlList(DataBase.Table.MarketSellCapsule, "*", "ORDER BY `CapsuleID` ASC");
        for (RowData objects : resultSet) {
            String capsuleId = objects.getString("CapsuleID");
            if (CapsuleDataLoader.getComplete().contains(capsuleId)) {
                map.merge(capsuleId, 1, Integer::sum);
            }
        }
        int register = 0;
        int index = 0;
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            CapsuleData capsule = CapsuleDataLoader.getCapsuleData(entry.getKey());
            item.addLore("§7・§f" + capsule.getDisplay() + " §a" + entry.getValue() + "件の登録");
            register += entry.getValue();
            if (playerData.setting().is(PlayerSetting.BooleanEnum.StorageViewShort) && index > ContainerViewSize) {
                item.addLore("§7その他..." + (map.size() - ContainerViewSize) + "種," + (resultSet.size() - register) + "件");
                break;
            }
            index++;
        }
        item.setCustomData("Market", "MarketSellCapsule");
        return item;
    }

    private int scroll = 0;
    private int amount = 0;

    public MarketSellCapsule(PlayerData playerData) {
        super(playerData, "マーケット販売 [カプセル]", 6);
    }

    private int amount() {
        return (int) Math.pow(10, amount);
    }

    private CustomItemStack amountIcon() {
        CustomItemStack item = new CustomItemStack(Material.GOLD_NUGGET);
        item.setNonDecoDisplay(decoLore("購入個数") + amount());
        item.setAmountReturn(amount+1);
        item.setCustomData("AmountIcon", true);
        return item;
    }

    private static final String[] key = new String[]{"UUID", "CapsuleID"};

    private String[] value(String id) {
        return new String[]{playerData.getUUID(), id};
    }

    public void set(MarketPlayer.Pack pack) {
        setAmount(pack.id(), pack.amount());
        setMel(pack.id(), pack.mel());
    }

    public static void set(String uuid, MarketPlayer.Pack pack) {
        String[] value = new String[]{uuid, pack.id()};
        SomSQL.setSql(DataBase.Table.MarketSellCapsule, key, value, "Amount", pack.amount());
        SomSQL.setSql(DataBase.Table.MarketSellCapsule, key, value, "Mel", pack.mel());
    }

    public void setAmount(CapsuleData capsule, int amount) {
        setAmount(capsule.getId(), amount);
    }

    public void setAmount(String id, int amount) {
        setAmount(playerData.getUUID(), id, amount);
    }

    public static void setAmount(String uuid, String id, int amount) {
        if (amount <= 0) {
            delete(uuid, id);
        } else {
            String[] value = new String[]{uuid, id};
            SomSQL.setSql(DataBase.Table.MarketSellCapsule, key, value, "Amount", amount);
        }
    }

    public void setMel(CapsuleData capsule, int mel) {
        setMel(capsule.getId(), mel);
    }

    public void setMel(String id, int mel) {
        SomSQL.setSql(DataBase.Table.MarketSellCapsule, key, value(id), "Mel", mel);
    }

    public static void delete(String uuid, String id) {
        String[] value = new String[]{uuid, id};
        SomSQL.delete(DataBase.Table.MarketSellCapsule, key, value);
    }

    public void delete(String id) {
        delete(playerData.getUUID(), id);
    }

    public static MarketPlayer.Pack get(String uuid, String id) {
        String[] value = new String[]{uuid, id};
        RowData objects = SomSQL.getSql(DataBase.Table.MarketSellCapsule, key, value, "*");
        int amount = objects.getInt("Amount");
        int mel = objects.getInt("Mel");
        MarketPlayer.Pack pack = new MarketPlayer.Pack(id, amount, mel);
        pack.setOwner(uuid);
        return pack;
    }

    public MarketPlayer.Pack get(String id) {
        return new MarketPlayer.Pack(id, getAmount(id), getMel(id));
    }

    public int getAmount(CapsuleData capsule) {
        return getAmount(capsule.getId());
    }

    public int getAmount(String id) {
        if (SomSQL.exists(DataBase.Table.MarketSellCapsule, key, value(id), "Amount")) {
            return SomSQL.getInt(DataBase.Table.MarketSellCapsule, key, value(id), "Amount");
        } else return 0;
    }

    public int getMel(CapsuleData capsule) {
        return getMel(capsule.getId());
    }

    public int getMel(String id) {
        if (SomSQL.exists(DataBase.Table.MarketSellCapsule, key, value(id), "Mel")) {
            return SomSQL.getInt(DataBase.Table.MarketSellCapsule, key, value(id), "Mel");
        } else return 0;
    }

    public boolean has(String id) {
        return SomSQL.exists(DataBase.Table.MarketSellCapsule, key, value(id));
    }

    private String capsuleId = null;

    @Override
    public void updateContainer() {
        if (capsuleId == null) {
            HashMap<String, Set<MarketPlayer.Pack>> map = new LinkedHashMap<>();
            Set<String> keys = new HashSet<>();
            ResultSet resultSet = SomSQL.getSqlList(DataBase.Table.MarketSellCapsule, "*", "ORDER BY `CapsuleID` ASC");
            for (RowData objects : resultSet) {
                String capsuleId = objects.getString("CapsuleID");
                keys.add(capsuleId);
                if (keys.size() > scroll * 8) {
                    int amount = objects.getInt("Amount");
                    int mel = objects.getInt("Mel");
                    if (!map.containsKey(capsuleId)) map.put(capsuleId, new HashSet<>());
                    MarketPlayer.Pack pack = new MarketPlayer.Pack(capsuleId, amount, mel);
                    pack.setOwner(objects.getString("UUID"));
                    map.get(capsuleId).add(pack);
                    if (map.size() >= 48) break;
                }
            }
            int slot = 0;
            for (Map.Entry<String, Set<MarketPlayer.Pack>> entry : map.entrySet()) {
                if (entry.getValue().isEmpty()) continue;
                String id = entry.getKey();
                CapsuleData capsule = CapsuleDataLoader.getCapsuleData(id);
                CustomItemStack item = capsule.viewItem();
                item.addSeparator("マーケット情報");
                item.addLore(decoLore("平均取引価格") + (MarketPlayer.hasAverage(id) ? MarketPlayer.getAverageMel(id)  + "メル" : "§c取引情報少量"));
                item.addLore(decoLore("過去取引個数") + MarketPlayer.getAverageAmount(id) + "個");
                item.addSeparator("出品者一覧");
                int indexPlayer = 0;
                for (MarketPlayer.Pack pack : entry.getValue()) {
                    item.addLore("§7・§f" + pack.getOwnerName() + " §e" + pack.amount() + "個 単価" + pack.mel() + "メル");
                    indexPlayer++;
                    if (playerData.setting().is(PlayerSetting.BooleanEnum.StorageViewShort) && indexPlayer > ContainerViewSize) {
                        item.addLore("§7その他..." + (entry.getValue().size() - ContainerViewSize) + "名");
                        break;
                    }
                }
                item.setCustomData("SellCapsule", id);
                setItem(slot, item);
                slot++;
                if (isInvalidSlot(slot)) slot++;
            }
        } else {
            CapsuleData capsule = CapsuleDataLoader.getCapsuleData(capsuleId);
            CustomItemStack itemBase = capsule.viewItem();
            itemBase.addSeparator("マーケット情報");
            ResultSet resultSet = SomSQL.getSqlList(DataBase.Table.MarketSellCapsule, "CapsuleID", capsuleId, "*", "ORDER BY `Mel` ASC");
            int slot = 0;
            for (RowData objects : resultSet) {
                String uuid = objects.getString("UUID");
                CustomItemStack item = itemBase.clone();
                item.addLore(decoLore("販売個数") + objects.getInt("Amount") + "個");
                item.addLore(decoLore("販売単価") + objects.getInt("Mel") + "メル");
                item.addLore(decoLore("出品者") + PlayerData.Username(uuid));
                item.setCustomData("SellOwner", uuid);
                setItem(slot, item);
                slot++;
                if (isInvalidSlot(slot)) slot++;
                if (slot >= 53) break;
            }
        }
        setItem(8, UpScroll());
        setItem(17, amountIcon());
        setItem(26, ItemFlame);
        setItem(35, ItemFlame);
        setItem(44, ItemFlame);
        setItem(53, DownScroll());
    }

    @Override
    public void topClick(int slot, ItemStack clickedItem, ClickType clickType) {
        if (CustomItemStack.hasCustomData(clickedItem, "AmountIcon")) {
            amount++;
            if (amount >= 6) amount = 0;
            SomSound.Tick.play(playerData);
            update();
        } else if (CustomItemStack.hasCustomData(clickedItem, "UpScroll")) {
            scroll = scrollUp(scroll);
            update();
        } else if (CustomItemStack.hasCustomData(clickedItem, "DownScroll")) {
            scroll = scrollDown(inventory, scroll);
            update();
        }
        if (CustomItemStack.hasCustomData(clickedItem, "SellCapsule")) {
            capsuleId = CustomItemStack.getCustomData(clickedItem, "SellCapsule");
            SomSound.Tick.play(playerData);
            update();
        } else if (CustomItemStack.hasCustomData(clickedItem, "SellOwner")) {
            String uuid = CustomItemStack.getCustomData(clickedItem, "SellOwner");
            if (playerData.getUUID().equals(uuid)) {
                playerData.sendMessage("§a自身の§e出品§aです", SomSound.Nope);
                return;
            }
            if (!playerData.capsuleMenu().hasEmpty(capsuleId)) {
                playerData.capsuleMenu().sendNonHasEmpty();
                return;
            }
            MarketPlayer.Pack pack = get(uuid, capsuleId);
            int amount = Math.min(pack.amount(), amount());
            int mel = pack.mel() * amount;
            if (playerData.hasMel(mel)) {
                playerData.removeMel(mel);
                playerData.addCapsule(capsuleId, amount);
                setAmount(uuid, capsuleId,pack.amount() - amount());
                MarketPlayer.addMel(uuid, mel);
                MarketPlayer.applyAverage(capsuleId, amount, pack.mel());
                CapsuleData capsule = CapsuleDataLoader.getCapsuleData(capsuleId);
                playerData.sendMessage(capsule.getDisplay() + "x" + amount + "§aを§e" + mel + "メル§aで§b購入§aしました", SomSound.Tick);
                update();
                MarketPlayer.Log(playerData, "BuyCapsule:" + capsuleId + ", Amount:" + amount + ", Mel:" + mel + ", Seller:" + pack.getOwnerName());
            } else {
                playerData.sendNonMel();
            }
        }
    }

    @Override
    public void bottomClick(int slot, ItemStack clickedItem, ClickType clickType) {

    }

    @Override
    public void close() {
        capsuleId = null;
    }
}
