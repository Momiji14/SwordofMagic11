package SwordofMagic11.Player.Market;

import SwordofMagic11.Command.Player.Lock;
import SwordofMagic11.Command.Player.Market;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.DataBase.CapsuleDataLoader;
import SwordofMagic11.DataBase.MaterialDataLoader;
import SwordofMagic11.Item.CapsuleData;
import SwordofMagic11.Item.Material.MaterialData;
import SwordofMagic11.Item.SomItem;
import SwordofMagic11.Item.SyncItem;
import SwordofMagic11.Player.Menu.GUIManager;
import SwordofMagic11.Player.Menu.NumberInput;
import SwordofMagic11.Player.PlayerData;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static SwordofMagic11.Component.Function.*;
import static SwordofMagic11.DataBase.DataBase.DownScroll;
import static SwordofMagic11.DataBase.DataBase.UpScroll;

public class MarketProduct extends GUIManager {

    public CustomItemStack iconMaterial() {
        CustomItemStack item = new CustomItemStack(Material.CHEST);
        item.setDisplay("マーケット出品 [素材]");
        item.addLore("§aマーケットに素材を出品をします");
        item.setCustomData("Market", "MarketProductMaterial");
        return item;
    }

    public CustomItemStack iconCapsule() {
        CustomItemStack item = new CustomItemStack(Material.ENDER_CHEST);
        item.setDisplay("マーケット出品 [カプセル]");
        item.addLore("§aマーケットに素材を出品をします");
        item.setCustomData("Market", "MarketProductCapsule");
        return item;
    }

    public CustomItemStack iconItem() {
        CustomItemStack item = new CustomItemStack(Material.ANVIL);
        item.setDisplay("マーケット出品 [アイテム]");
        item.addLore("§aマーケットに素材を出品をします");
        item.setCustomData("Market", "MarketProductItem");
        return item;
    }

    private Type type;
    private Object object;
    private int scroll = 0;
    private int amount = 0;
    private final InputMel inputMel;

    public MarketProduct(PlayerData playerData) {
        super(playerData, "マーケット出品", 6);
        inputMel = new InputMel(playerData, "出品額入力");
    }

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

    public void open(Type type) {
        this.type = type;
        super.open();
    }

    @Override
    public void updateContainer() {
        for (int i = 17; i < 45; i += 9) {
            setItem(i, ItemFlame);
        }
        setItem(8, UpScroll());
        setItem(53, DownScroll());

        int slot = 0;
        int index = 0;
        switch (type) {
            case Material -> {
                for (Map.Entry<String, Integer> entry : playerData.materialMenu().getStorage().entrySet()) {
                    if (index >= scroll * 8) {
                        String id = entry.getKey();
                        int amount = entry.getValue();
                        MaterialData material = MaterialDataLoader.getMaterialData(id);
                        CustomItemStack item = material.viewItem();
                        item.setAmount(amount);
                        item.addLore(decoLore("個数") + amount);
                        setItem(slot, item);
                        slot++;
                        if (isInvalidSlot(slot)) slot++;
                        if (slot >= 53) break;
                    }
                    index++;
                }
                setItem(17, amountIcon());
            }
            case Capsule -> {
                for (Map.Entry<String, Integer> entry : playerData.capsuleMenu().getStorage().entrySet()) {
                    if (index >= scroll * 8) {
                        String id = entry.getKey();
                        int amount = entry.getValue();
                        CapsuleData capsule = CapsuleDataLoader.getCapsuleData(id);
                        CustomItemStack item = capsule.viewItem();
                        item.setAmount(amount);
                        item.addLore(decoLore("個数") + amount);
                        setItem(slot, item);
                        slot++;
                        if (isInvalidSlot(slot)) slot++;
                        if (slot >= 53) break;
                    }
                    index++;
                }
                setItem(17, amountIcon());
            }
            case Item -> {
                List<SomItem> itemList = playerData.itemInventory().getList();
                for (int i = scroll * 8; i < itemList.size(); i++) {
                    setItem(slot, itemList.get(i).viewItem());
                    slot++;
                    if (isInvalidSlot(slot)) slot++;
                    if (slot >= 53) break;
                }
            }
        }
    }

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
            int size = playerData.materialMenu().size() + playerData.capsuleMenu().size() + 1;
            scroll = scrollDown(size, 6, scroll);
            update();
        } else if (CustomItemStack.hasCustomData(clickedItem, "Material")) {
            String id = CustomItemStack.getCustomData(clickedItem, "Material");
            if (Lock.check(playerData, id)) return;
            MaterialData material = MaterialDataLoader.getMaterialData(id);
            object = material;
            CustomItemStack viewItem = material.viewItem();
            viewItem.setAmount(Math.min(amount(), playerData.getMaterial(material)));
            inputMel.setViewItem(viewItem);
            inputMel.open();
        } else if (CustomItemStack.hasCustomData(clickedItem, "Capsule")) {
            String id = CustomItemStack.getCustomData(clickedItem, "Capsule");
            if (Lock.check(playerData, id)) return;
            CapsuleData capsule = CapsuleDataLoader.getCapsuleData(id);
            object = capsule;
            CustomItemStack viewItem = capsule.viewItem();
            viewItem.setAmount(Math.min(amount(), playerData.getCapsule(capsule)));
            inputMel.setViewItem(viewItem);
            inputMel.open();
        } else if (CustomItemStack.hasCustomData(clickedItem, "UUID")) {
            String id = CustomItemStack.getCustomData(clickedItem, "UUID");
            if (Lock.check(playerData, id)) return;
            SomItem somItem = SyncItem.cloneCache(id);
            object = somItem;
            inputMel.setViewItem(somItem.viewItem());
            inputMel.open();
        }
    }

    protected void product(int input) {
        if (object instanceof MaterialData material) {
            productMaterial(material, input, Math.min(amount(), playerData.getMaterial(material)));
        } else if (object instanceof CapsuleData capsule) {
            productCapsule(capsule, input, Math.min(amount(), playerData.getCapsule(capsule)));
        } else if (object instanceof SomItem somItem) {
            productItem(somItem, input);
        } else {
            playerData.sendMessage("§cMarketSellError -> object is null", SomSound.Nope);
        }
        playerData.closeInventory();
    }

    public boolean productMaterial(MaterialData material, int mel, int amount) {
        String id = material.getId();
        if (Market.checkAverage(id, mel, playerData)) return true;
        if (playerData.hasMaterial(id, amount)) {
            int reqMel = ceil(safeMultiply(mel, amount) * MarketPlayer.Tax);
            if (playerData.hasMel(reqMel)) {
                MarketPlayer.Pack pack;
                if (playerData.marketSellMaterial().has(id)) {
                    pack = playerData.marketSellMaterial().get(id);
                    if (pack.mel() != mel) {
                        playerData.sendMessage("§aすでに§e" + material.getDisplay() + "§aは§e単価" + mel + "§b出品§aされています\n§e単価§aを§e変更§aする際は§c過去§aの§b出品§aを取り下げてください", SomSound.Nope);
                        return true;
                    } else {
                        pack.addAmount(amount);
                        playerData.sendMessage(material.getDisplay() + "x" + amount + "§aに§b更新§aしました", SomSound.Tick);
                    }
                } else {
                    pack = new MarketPlayer.Pack(id, amount, mel);
                    playerData.sendMessage(material.getDisplay() + "x" + amount + "§aを§e単価" + mel + "メル§aで§b出品§aしました", SomSound.Tick);
                }
                playerData.removeMaterial(id, amount);
                playerData.removeTax(reqMel);
                playerData.marketSellMaterial().set(pack);
                playerData.sendMessage("§e[出品手数料]§c-" + reqMel + "メル");
                MarketPlayer.Log(playerData, "ProductMaterial:" + id + ", Amount:" + amount + ", Mel:" + mel);
            } else {
                playerData.sendMessage("§e出品手数料§aが足りません §e[" + reqMel + "メル]", SomSound.Nope);
            }
            return true;
        }
        return false;
    }

    public boolean productCapsule(CapsuleData capsule, int mel, int amount) {
        String id = capsule.getId();
        if (Market.checkAverage(id, mel, playerData)) return true;
        if (playerData.hasCapsule(id, amount)) {
            int reqMel = ceil(safeMultiply(mel, amount) * MarketPlayer.Tax);
            if (playerData.hasMel(reqMel)) {
                MarketPlayer.Pack pack;
                if (playerData.marketSellCapsule().has(id)) {
                    pack = playerData.marketSellCapsule().get(id);
                    if (pack.mel() != mel) {
                        playerData.sendMessage("§aすでに§e" + capsule.getDisplay() + "§aは§e単価" + mel + "§b出品§aされています\n§e単価§aを§e変更§aする際は§c過去§aの§b出品§aを取り下げてください", SomSound.Nope);
                        return true;
                    } else {
                        pack.addAmount(amount);
                        playerData.sendMessage(capsule.getDisplay() + "x" + amount + "§aに§b更新§aしました", SomSound.Tick);
                    }
                } else {
                    pack = new MarketPlayer.Pack(id, amount, mel);
                    playerData.sendMessage(capsule.getDisplay() + "x" + amount + "§aを§e単価" + mel + "メル§aで§b出品§aしました", SomSound.Tick);
                }
                playerData.removeCapsule(id, amount);
                playerData.removeTax(reqMel);
                playerData.marketSellCapsule().set(pack);
                playerData.sendMessage("§e[出品手数料]§c-" + reqMel + "メル");
                MarketPlayer.Log(playerData, "ProductCapsule:" + id + ", Amount:" + amount + ", Mel:" + mel);
            } else {
                playerData.sendMessage("§e出品手数料§aが足りません §e[" + reqMel + "メル]", SomSound.Nope);
            }
            return true;
        }
        return false;
    }

    public void productItem(SomItem somItem, int mel) {
        int reqMel = ceil(mel * MarketPlayer.Tax);
        if (playerData.hasMel(reqMel)) {
            MarketPlayer.ItemPack pack = new MarketPlayer.ItemPack(somItem.getUUID(), mel);
            SyncItem.setState(somItem.getUUID(), SyncItem.State.Market);
            playerData.removeTax(reqMel);
            MarketSellItem.set(pack);
            playerData.sendMessage(somItem.getDisplay() + "§aを§e" + mel + "メル§aで§b出品§aしました", SomSound.Tick);
            playerData.sendMessage("§e[出品手数料]§c-" + reqMel + "メル");
            playerData.updateInventory();
            MarketPlayer.Log(playerData, "ProductItem:" + pack.getOwnerUUID() + ", ID:" + pack.getItemID() + ", Mel:" + mel);
        } else {
            playerData.sendMessage("§e出品手数料§aが足りません §e[" + reqMel + "メル]", SomSound.Nope);
        }
    }

    @Override
    public void bottomClick(int slot, ItemStack clickedItem, ClickType clickType) {

    }

    @Override
    public void close() {
        scroll = 0;
        //amount = 0;
    }

    public enum Type {
        Material,
        Capsule,
        Item,
    }

    public static class InputMel extends NumberInput {

        public InputMel(PlayerData playerData, String display) {
            super(playerData, display);
        }

        @Override
        public void enter(int input) {
            if (isEntered()) {
                playerData.marketSell().product(input);
            } else {
                playerData.sendMessage("§e金額§aを§b入力§aしてください", SomSound.Nope);
            }
        }
    }
}
