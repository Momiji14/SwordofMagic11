package SwordofMagic11.Player.Shop;

import SwordofMagic11.Component.SomSQL;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.DataBase.DataBase;
import SwordofMagic11.DataBase.ItemDataLoader;
import SwordofMagic11.DataBase.MaterialDataLoader;
import SwordofMagic11.Item.Material.MaterialData;
import SwordofMagic11.Item.SomItem;
import SwordofMagic11.Item.SyncItem;
import SwordofMagic11.Player.Menu.GUIManager;
import SwordofMagic11.Player.PlayerData;
import com.github.jasync.sql.db.RowData;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static SwordofMagic11.Component.Function.decoText;

public class ShopManager extends GUIManager {

    private ShopData shopData;
    private boolean isOpen = false;

    public ShopManager(PlayerData playerData) {
        super(playerData, "ショップ", 6);
    }

    public void open(ShopData shopData) {
        isOpen = true;
        this.shopData = shopData;
        super.open();
        playerData.updateMenu();
    }

    public boolean isOpen() {
        return isOpen;
    }

    ShopData.Container[] containerData = new ShopData.Container[54];

    @Override
    public void updateContainer() {
        if (shopData != null) {
            containerData = new ShopData.Container[54];
            for (ShopData.Container container : shopData.getContainers()) {
                CustomItemStack item = ItemDataLoader.getItemData(container.itemId()).viewItem();
                item.addSeparator("販売情報");
                item.addLore("§7・§e" + container.mel() + "メル §7(" + playerData.getMel() + ")");
                for (ShopData.Recipe recipe : container.recipe()) {
                    MaterialData material = MaterialDataLoader.getMaterialData(recipe.materialId());
                    String hasAmount = " §7(" + playerData.getMaterial(recipe.materialId()) + ")";
                    item.addLore("§7・§f" + material.getDisplay() + "§ax" + recipe.amount() + hasAmount);
                }
                containerData[container.slot()] = container;
                setItem(container.slot(), item);
            }
        } else {
            int slot = 0;
            for (RowData objects : SomSQL.getSqlList(DataBase.Table.MaterialStorage, "UUID", playerData.getUUID(), "*")) {
                MaterialData material = MaterialDataLoader.getMaterialData(objects.getString("MaterialID"));
                CustomItemStack item = new CustomItemStack(material.getIcon()).setNonDecoDisplay(material.getDisplay());
                item.setAmount(objects.getInt("Amount"));
                setItem(slot, item);
            }
        }
    }

    @Override
    public void topClick(int slot, ItemStack clickedItem, ClickType clickType) {
        if (containerData[slot] != null) {
            ShopData.Container container = containerData[slot];
            if (clickType == ClickType.DROP) {
                for (ShopData.Recipe recipe : container.recipe()) {
                    playerData.sideBar().amountMaterial(recipe.materialId(), recipe.amount());
                }
                return;
            }
            SomItem item = ItemDataLoader.getItemData(container.itemId());
            List<String> message = new ArrayList<>();
            message.add(decoText(item.getDisplay()));
            boolean check = true;
            if (playerData.hasMel(container.mel())) {
                message.add("§b✓ §e" + container.mel() + "メル §7(" + playerData.getMel() + ")");
            } else {
                message.add("§c× §e" + container.mel() + "メル §7(" + playerData.getMel() + ")");
                check = false;
            }
            for (ShopData.Recipe recipe : container.recipe()) {
                MaterialData material = MaterialDataLoader.getMaterialData(recipe.materialId());
                String hasAmount = " §7(" + playerData.getMaterial(recipe.materialId()) + ")";
                if (playerData.hasMaterial(recipe.materialId(), recipe.amount())) {
                    message.add("§b✓ §f" + material.getDisplay() + "§ax" + recipe.amount() + hasAmount);
                } else {
                    message.add("§c× §f" + material.getDisplay() + "§ax" + recipe.amount() + hasAmount);
                    check = false;
                }
            }
            if (check) {
                playerData.removeMel(container.mel());
                for (ShopData.Recipe recipe : container.recipe()) {
                    playerData.removeMaterial(recipe.materialId(), recipe.amount());
                }
                SyncItem.register(container.itemId(), playerData, SyncItem.State.ItemInventory);
                playerData.sendMessage(item.getDisplay() + "を§b購入§aしました", SomSound.Tick);
                playerData.updateInventory();
            } else {
                playerData.sendMessage(message);
            }
        }
    }

    @Override
    public void bottomClick(int slot, ItemStack clickedItem, ClickType clickType) {
        if (CustomItemStack.hasCustomData(clickedItem, "SmithMenu")) {
            if (CustomItemStack.getCustomData(clickedItem, "SmithMenu").equals("MaterializeMenu")) {
                playerData.smithMenu().getMaterializeMenu().open();
            }
        }
    }

    @Override
    public void close() {
        isOpen = false;
        containerInit();
        playerData.updateMenu();
    }
}
