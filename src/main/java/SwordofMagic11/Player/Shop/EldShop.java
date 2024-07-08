package SwordofMagic11.Player.Shop;

import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.DataBase.ItemDataLoader;
import SwordofMagic11.Item.Donation.*;
import SwordofMagic11.Item.SomItem;
import SwordofMagic11.Item.SyncItem;
import SwordofMagic11.Player.Menu.GUIManager;
import SwordofMagic11.Player.PlayerData;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashSet;

public class EldShop extends GUIManager {

    public CustomItemStack icon() {
        CustomItemStack item = new CustomItemStack(Material.EMERALD);
        item.setDisplay("エルドショップ");
        item.addLore("§aエルドで購入できるアイテムを販売しています");
        item.setCustomData("Menu", "EldShop");
        return item;
    }

    private static final Collection<Container> containers = new HashSet<>();

    public static void donationItemRegister() {
        new PrivateInstanceTicket(7, 500, 0);
        new MaterialStorageSizeTicket(10, 250, 1);
        new CapsuleStorageSizeTicket(10, 250, 2);
        new PalletStorageSizeTicket(250, 3);
        new PetCageSizeTicket(5, 250, 4);
        new InstanceBooster(500, 5);
    }

    public static void addContainer(SomDonationItem item, int eld, int slot) {
        ItemDataLoader.register(item);
        containers.add(new Container(item.getId(), 1, eld, slot));
    }

    public EldShop(PlayerData playerData) {
        super(playerData, "エルドショップ", 6);
    }

    Container[] containerData = new Container[54];

    @Override
    public void updateContainer() {
        containerData = new Container[54];
        for (Container container : containers) {
            CustomItemStack item = ItemDataLoader.getItemData(container.itemId()).viewItem();
            item.addSeparator("販売情報");
            item.addLore("§7・§e" + container.eld() + "エルド");
            containerData[container.slot()] = container;
            setItem(container.slot(), item);
        }
    }

    @Override
    public void topClick(int slot, ItemStack clickedItem, ClickType clickType) {
        if (containerData[slot] != null) {
            Container container = containerData[slot];
            SomItem item = ItemDataLoader.getItemData(container.itemId());
            if (playerData.donation().hasEld(container.eld())) {
                playerData.donation().removeEld(container.eld());
                for (int i = 0; i < container.amount(); i++) {
                    SyncItem.register(container.itemId(), playerData, SyncItem.State.ItemInventory);
                }
                playerData.sendMessage(item.getDisplay() + "を§b購入§aしました", SomSound.Tick);
                playerData.updateInventory();
            } else {
                playerData.sendNonEld();
            }
        }
    }

    @Override
    public void bottomClick(int slot, ItemStack clickedItem, ClickType clickType) {

    }

    @Override
    public void close() {
        containerInit();
        playerData.updateInventory();
    }

    public record Container(String itemId, int amount, int eld, int slot) {
    }
}
