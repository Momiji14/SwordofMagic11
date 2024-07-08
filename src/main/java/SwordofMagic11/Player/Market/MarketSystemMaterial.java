package SwordofMagic11.Player.Market;

import SwordofMagic11.Component.SomSQL;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Custom.CustomItemStack;
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

import static SwordofMagic11.Component.Function.*;
import static SwordofMagic11.DataBase.DataBase.*;

public class MarketSystemMaterial extends GUIManager {

    public CustomItemStack icon() {
        CustomItemStack item = new CustomItemStack(Material.CHEST_MINECART);
        item.setDisplay("システムマーケット販売 [素材]");
        int index = 0;
        boolean isShort = false;
        ResultSet resultSet = SomSQL.getSqlList(DataBase.Table.MarketSystemMaterial, "*");
        for (RowData objects : resultSet) {
            String materialId = objects.getString("MaterialID");
            int amount = objects.getInt("Amount");
            MaterialData material = MaterialDataLoader.getMaterialData(materialId);
            item.addLore("§7・§f" + material.getDisplay() + "§ax" + amount);
            index++;
            if (playerData.setting().is(PlayerSetting.BooleanEnum.StorageViewShort) && index > ContainerViewSize) {
                isShort = true;
                break;
            }
        }
        if (isShort) {
            item.addLore("§7その他..." + (resultSet.size() - ContainerViewSize) + "種");
        }
        item.setCustomData("Market", "MarketSystemMaterial");
        return item;
    }

    private int scroll = 0;
    private int amount = 0;
    public MarketSystemMaterial(PlayerData playerData) {
        super(playerData, "システムマーケット販売 [素材]", 6);
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

    public static void set(String id, int amount) {
        if (amount >= 2000000000) amount = 2000000000;
        if (amount <= 0) {
            delete(id);
        } else {
            SomSQL.setSql(DataBase.Table.MarketSystemMaterial, "MaterialID", id, "Amount", amount);
        }
    }

    public static int get(String id) {
        if (SomSQL.exists(DataBase.Table.MarketSystemMaterial, "MaterialID", id)) {
            return SomSQL.getInt(DataBase.Table.MarketSystemMaterial, "MaterialID", id, "Amount");
        } else return 0;
    }

    public static void add(String id, int amount) {
        set(id, get(id) + amount);
    }

    public static void remove(String id, int amount) {
        set(id, get(id) - amount);
    }

    public static void delete(String id) {
        SomSQL.delete(Table.MarketSystemMaterial, "MaterialID", id);
    }

    public static int mel(String id) {
        MaterialData material = MaterialDataLoader.getMaterialData(id);
        int mel;
        int current = ceil(material.getSell() * MarketPlayer.MarketSystem);
        if (MarketPlayer.hasAverage(id)) {
            int average = ceil(MarketPlayer.getAverageMel(id) * MarketPlayer.MarketSystemAverage);
            mel = Math.max(current, average);
        } else {
            mel = current;
        }
        return mel;
    }

    private int size;

    @Override
    public void updateContainer() {
        ResultSet resultSet = SomSQL.getSqlList(DataBase.Table.MarketSystemMaterial, "*");
        size = resultSet.size();
        int index = 0;
        int slot = 0;
        for (RowData objects : resultSet) {
            if (index >= scroll * 8) {
                String id = objects.getString("MaterialID");
                int amount = objects.getInt("Amount");
                MaterialData material = MaterialDataLoader.getMaterialData(id);
                CustomItemStack item = material.viewItem();
                item.addSeparator("マーケット情報");
                item.addLore(decoLore("販売個数") + amount + "個");
                item.addLore(decoLore("販売価格") + mel(id) + "メル");
                item.setCustomData("SystemMaterial", id);
                setItem(slot, item);
                slot++;
                if (isInvalidSlot(slot)) slot++;
                if (slot >= 53) break;
            }
            index++;
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
            scroll = scrollDown(size, 6, scroll);
            update();
        }
        if (CustomItemStack.hasCustomData(clickedItem, "SystemMaterial")) {
            String id = CustomItemStack.getCustomData(clickedItem, "SystemMaterial");
            MaterialData material = MaterialDataLoader.getMaterialData(id);
            if (!playerData.materialMenu().hasEmpty(material)) {
                playerData.materialMenu().sendNonHasEmpty();
                return;
            }
            int amount = Math.min(get(id), amount());
            int mel = mel(id) * amount;
            if (amount <= 0) {
                playerData.sendMessage("§aすでに§e他プレイヤー§aが§b購入§aしたため§e在庫§aがありません");
            } else if (playerData.hasMel(mel)) {
                remove(id, amount);
                playerData.removeMel(mel);
                playerData.addMaterial(material, amount);
                playerData.sendMessage(material.getDisplay() + "x" + amount + "§aを§b購入§aしました", SomSound.Tick);
                update();
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

    }
}
