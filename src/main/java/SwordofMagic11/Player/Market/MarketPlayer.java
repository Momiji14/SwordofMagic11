package SwordofMagic11.Player.Market;

import SwordofMagic11.Component.DiscordWebhook;
import SwordofMagic11.Component.SomSQL;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Component.SomTask;
import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.DataBase.CapsuleDataLoader;
import SwordofMagic11.DataBase.DataBase;
import SwordofMagic11.DataBase.MaterialDataLoader;
import SwordofMagic11.Item.CapsuleData;
import SwordofMagic11.Item.Material.MaterialData;
import SwordofMagic11.Item.SyncItem;
import SwordofMagic11.Player.Menu.GUIManager;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Player.Statistics.Statistics;
import com.github.jasync.sql.db.RowData;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static SwordofMagic11.Component.Function.decoLore;
import static SwordofMagic11.Component.Function.decoSeparator;

public class MarketPlayer extends GUIManager {

    public static double MarketSystem = 2.5;
    public static double MarketSystemAverage = 1.5;
    public static double Tax = 0.01;

    public CustomItemStack icon() {
        CustomItemStack item = new CustomItemStack(Material.CHEST_MINECART);
        item.setDisplay("マーケット");
        item.addLore("§aプレイヤーの出品物を購入できます");
        item.addLore("§aプレイヤーの注文品を売却できます");
        item.addLore("§a出品・注文は§e/market§aから行います");
        item.addSeparator("マーケット情報");
        item.addLore(marketLore());
        item.setCustomData("Menu", "MarketPlayer");
        return item;
    }

    public static boolean hasAverage(String id) {
        if (SomSQL.exists(DataBase.Table.MarketAverage, "ID", id)) {
            return SomSQL.getInt(DataBase.Table.MarketAverage, "ID", id, "Amount") >= 25;
        } else return false;
    }

    public static int getAverageAmount(String id) {
        if (SomSQL.exists(DataBase.Table.MarketAverage, "ID", id)) {
            return SomSQL.getInt(DataBase.Table.MarketAverage, "ID", id, "Amount");
        } else return 0;
    }

    public static int getAverageMel(String id) {
        if (SomSQL.exists(DataBase.Table.MarketAverage, "ID", id)) {
            return SomSQL.getInt(DataBase.Table.MarketAverage, "ID", id, "Mel");
        } else return 0;
    }

    public static void applyAverage(String id, int amount, int mel) {
        int currentMel = getAverageMel(id);
        int currentAmount = getAverageAmount(id);
        int maxAverage = currentMel * 10;
        int mathAmount = Math.min(maxAverage, currentAmount);
        int totalMel = (currentMel * mathAmount) + (mel * amount);
        currentMel = Math.max(1, totalMel/(mathAmount+amount));
        SomSQL.setSql(DataBase.Table.MarketAverage, "ID", id, "Amount", currentAmount + amount);
        SomSQL.setSql(DataBase.Table.MarketAverage, "ID", id, "Mel", currentMel);
    }

    public MarketPlayer(PlayerData playerData) {
        super(playerData, "マーケット", 3);
    }

    public int getMel() {
        return getMel(playerData.getUUID());
    }

    public static int getMel(String uuid) {
        if (SomSQL.exists(DataBase.Table.MarketPlayer, "UUID", uuid, "Mel")) {
            return SomSQL.getInt(DataBase.Table.MarketPlayer, "UUID", uuid, "Mel");
        } else {
            return 0;
        }
    }

    public void setMel(int mel) {
        setMel(playerData.getUUID(), mel);
    }

    public static void setMel(String uuid, int mel) {
        SomSQL.setSql(DataBase.Table.MarketPlayer, "UUID", uuid, "Mel", mel);
    }

    public void addMel(int mel) {
        addMel(playerData.getUUID(), mel);
    }

    public static void addMel(String uuid, int mel) {
        setMel(uuid, getMel(uuid) + mel);
    }

    public void removeMel(int mel) {
        removeMel(playerData.getUUID(), mel);
    }

    public static void removeMel(String uuid, int mel) {
        setMel(uuid, getMel(uuid) - mel);
    }

    public static HashMap<String, Integer> getMaterial(String uuid) {
        HashMap<String, Integer> map = new HashMap<>();
        for (RowData objects : SomSQL.getSqlList(DataBase.Table.MarketPlayerMaterial, "UUID", uuid, "*")) {
            map.put(objects.getString("MaterialID"), objects.getInt("Amount"));
        }
        return map;
    }

    public static void addMaterial(String uuid, String id, int amount) {
        String[] key = new String[]{"UUID", "MaterialID"};
        String[] value = new String[]{uuid, id};
        int current = 0;
        if (SomSQL.exists(DataBase.Table.MarketPlayerMaterial, key, value)) {
            current = SomSQL.getInt(DataBase.Table.MarketPlayerMaterial, key, value, "Amount");
        }
        current += amount;
        SomSQL.setSql(DataBase.Table.MarketPlayerMaterial, key, value, "Amount", current);
    }

    public static void deleteMaterial(String uuid) {
        SomSQL.delete(DataBase.Table.MarketPlayerMaterial, "UUID", uuid);
    }

    public static HashMap<String, Integer> getCapsule(String uuid) {
        HashMap<String, Integer> map = new HashMap<>();
        for (RowData objects : SomSQL.getSqlList(DataBase.Table.MarketPlayerCapsule, "UUID", uuid, "*")) {
            map.put(objects.getString("CapsuleID"), objects.getInt("Amount"));
        }
        return map;
    }

    public static void addCapsule(String uuid, String id, int amount) {
        String[] key = new String[]{"UUID", "CapsuleID"};
        String[] value = new String[]{uuid, id};
        int current = 0;
        if (SomSQL.exists(DataBase.Table.MarketPlayerCapsule, key, value)) {
            current = SomSQL.getInt(DataBase.Table.MarketPlayerCapsule, key, value, "Amount");
        }
        current += amount;
        SomSQL.setSql(DataBase.Table.MarketPlayerCapsule, key, value, "Amount", current);
    }

    public static void deleteCapsule(String uuid) {
        SomSQL.delete(DataBase.Table.MarketPlayerCapsule, "UUID", uuid);
    }

    public int sellSlot() {
        return 30;
    }

    public boolean hasSellEmpty(String id) {
        int index = 0;
        for (RowData objects : SomSQL.getSqlList(DataBase.Table.MarketSellMaterial, "UUID", playerData.getUUID(), "*")) {
            if (objects.getString("MaterialID").equals(id)) return true;
            index++;
            if (index >= sellSlot()) return false;
        }
        return true;
    }

    public int orderSlot() {
        return 15;
    }

    public boolean hasOrderEmpty(String id) {
        int index = 0;
        for (RowData objects : SomSQL.getSqlList(DataBase.Table.MarketOrderMaterial, "UUID", playerData.getUUID(), "*")) {
            if (objects.getString("MaterialID").equals(id)) return true;
            index++;
            if (index >= orderSlot()) return false;
        }
        return true;
    }

    public boolean hasItemEmpty() {
        return SyncItem.getList(playerData, SyncItem.State.Market).size() < sellSlot();
    }

    public List<String> marketLore() {
        List<String> lore = new ArrayList<>();
        lore.add(decoLore("売上メル") + getMel() + "メル");
        HashMap<String, Integer> material = getMaterial(playerData.getUUID());
        if (!material.isEmpty()) {
            lore.add(decoSeparator("注文完了 [素材]"));
            for (Map.Entry<String, Integer> entry : material.entrySet()) {
                MaterialData materialData = MaterialDataLoader.getMaterialData(entry.getKey());
                lore.add("§7・§f" + materialData.getDisplay() + "x" + entry.getValue());
            }
        }
        HashMap<String, Integer> capsule = getCapsule(playerData.getUUID());
        if (!capsule.isEmpty()) {
            lore.add(decoSeparator("注文完了 [カプセル]"));
            for (Map.Entry<String, Integer> entry : capsule.entrySet()) {
                CapsuleData capsuleData = CapsuleDataLoader.getCapsuleData(entry.getKey());
                lore.add("§7・§f" + capsuleData.getDisplay() + "x" + entry.getValue());
            }
        }
        return lore;
    }

    public void updateMel() {
        CustomItemStack item = new CustomItemStack(Material.GOLD_NUGGET);
        item.setDisplay("マーケット売上");
        item.addLore(marketLore());
        item.setCustomData("Market", "Mel");
        setItem(26, item);
    }

    @Override
    public void open() {
        if (!playerData.getMap().isCity()) {
            playerData.sendReqInCity();
            return;
        }
        super.open();
    }

    @Override
    public void updateContainer() {
        setItem(0, playerData.marketSellMaterial().icon());
        setItem(1, playerData.marketSellCapsule().icon());
        setItem(2, playerData.marketSellItem().icon());
        setItem(3, playerData.marketSystemMaterial().icon());
        setItem(4, playerData.marketSystemCapsule().icon());

        setItem(7, playerData.marketOrderMaterial().icon());
        setItem(8, playerData.marketOrderCapsule().icon());

        setItem(18, playerData.marketSell().iconMaterial());
        setItem(19, playerData.marketSell().iconCapsule());
        setItem(20, playerData.marketSell().iconItem());
        setItem(25, playerData.marketCancel().icon());
        updateMel();

        for (int i = 9; i < 18; i++) {
            setItem(i, ItemFlame);
        }
    }

    @Override
    public void topClick(int slot, ItemStack clickedItem, ClickType clickType) {
        if (CustomItemStack.hasCustomData(clickedItem, "Market")) {
            switch (CustomItemStack.getCustomData(clickedItem, "Market")) {
                case "MarketSellMaterial" -> playerData.marketSellMaterial().open();
                case "MarketSellCapsule" -> playerData.marketSellCapsule().open();
                case "MarketSellItem" -> playerData.marketSellItem().open();
                case "MarketOrderMaterial" -> playerData.marketOrderMaterial().open();
                case "MarketOrderCapsule" -> playerData.marketOrderCapsule().open();
                case "MarketSystemMaterial" -> playerData.marketSystemMaterial().open();
                case "MarketSystemCapsule" -> playerData.marketSystemCapsule().open();
                case "MarketProductMaterial" -> playerData.marketSell().open(MarketProduct.Type.Material);
                case "MarketProductCapsule" -> playerData.marketSell().open(MarketProduct.Type.Capsule);
                case "MarketProductItem" -> playerData.marketSell().open(MarketProduct.Type.Item);
                case "MarketCancel" -> playerData.marketCancel().open();
                case "Mel" -> {
                    int mel = getMel();
                    HashMap<String, Integer> material = getMaterial(playerData.getUUID());
                    HashMap<String, Integer> capsule = getCapsule(playerData.getUUID());
                    List<String> message = new ArrayList<>();
                    if (mel > 0) {
                        setMel(0);
                        playerData.addMel(mel);
                        playerData.statistics().add(Statistics.IntEnum.MarketSellMel, mel);
                        message.add("§e" + mel + "メル§aを§b回収§aしました");
                        MarketPlayer.Log(playerData, "CollectMel:" + mel);
                    }
                    if (!material.isEmpty()) {
                        deleteMaterial(playerData.getUUID());
                        material.forEach((id, amount) -> {
                            MaterialData materialData = MaterialDataLoader.getMaterialData(id);
                            playerData.addMaterial(id, amount);
                            message.add(materialData.getDisplay() + "x" + amount + "§aを§b回収§aしました");
                        });
                    }
                    if (!capsule.isEmpty()) {
                        deleteCapsule(playerData.getUUID());
                        capsule.forEach((id, amount) -> {
                            CapsuleData capsuleData = CapsuleDataLoader.getCapsuleData(id);
                            playerData.addCapsule(id, amount);
                            message.add(capsuleData.getDisplay() + "x" + amount + "§aを§b回収§aしました");
                        });
                    }
                    if (!message.isEmpty()) {
                        playerData.sendMessage(message, SomSound.Tick);
                        updateMel();
                        fastUpdate();
                    } else {
                        playerData.sendMessage("§c売上および注文完了がありません", SomSound.Nope);
                    }
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

    public static class Pack {

        private final String id;
        private int amount;
        private int mel;
        private String ownerUUID;
        private String ownerName;

        public Pack(String id, int amount, int mel) {
            this.id = id;
            this.amount = amount;
            this.mel = mel;
        }

        public Pack(String id, int amount, int mel, String uuid) {
            this.id = id;
            this.amount = amount;
            this.mel = mel;
            setOwner(uuid);
        }

        public String id() {
            return id;
        }

        public int amount() {
            return amount;
        }

        public void addAmount(int amount) {
            this.amount += amount;
        }

        public void removeAmount(int amount) {
            this.amount -= amount;
        }

        public int mel() {
            return mel;
        }

        public void setMel(int mel) {
            this.mel = mel;
        }

        public void setOwner(PlayerData playerData) {
            setOwner(playerData.getUUID());
        }

        public void setOwner(String uuid) {
            ownerUUID = uuid;
            ownerName = PlayerData.Username(uuid);
        }

        public String getOwnerUUID() {
            return ownerUUID;
        }

        public String getOwnerName() {
            return ownerName;
        }
    }

    public static class ItemPack {

        private final String uuid;
        private int mel;

        public ItemPack(String uuid) {
            this.uuid = uuid;
            this.mel = SomSQL.getInt(DataBase.Table.MarketSellItem, "UUID", uuid, "Mel");
        }

        public ItemPack(String uuid, int mel) {
            this.uuid = uuid;
            this.mel = mel;
        }

        public String getUUID() {
            return uuid;
        }

        public String getItemID() {
            return SomSQL.getString(DataBase.Table.ItemStorage, "UUID", uuid, "ItemID");
        }

        public int mel() {
            return mel;
        }

        public void setMel(int mel) {
            this.mel = mel;
        }

        public String getOwnerUUID() {
            return SomSQL.getString(DataBase.Table.ItemStorage, "UUID", uuid, "Owner");
        }

        public String getOwnerName() {
            return PlayerData.Username(getOwnerUUID());
        }
    }

    public static void Log(PlayerData playerData, String log) {
        if (true) return;
        SomTask.async(() -> {
            try {
                DiscordWebhook discordWebhook = new DiscordWebhook("https://canary.discord.com/api/webhooks/1236243300357378076/pCkt45DtirW322rO2VehieqDrBFMmZ015EecWe5kB2KgSpopxiy9vzixLGZNs_J594Hq");
                discordWebhook.setContent("```[" + playerData.getName() + "] " + log + "```");
                discordWebhook.execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
