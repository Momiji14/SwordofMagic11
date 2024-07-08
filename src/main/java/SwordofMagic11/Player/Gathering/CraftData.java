package SwordofMagic11.Player.Gathering;

import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.Item.Material.MaterialData;
import SwordofMagic11.Item.SomItem;
import SwordofMagic11.Item.SyncItem;
import SwordofMagic11.Player.PlayerData;

import java.util.Collection;

import static SwordofMagic11.Component.Function.decoLore;
import static SwordofMagic11.Component.Function.scale;

public record CraftData(String id, Result result, Collection<Pack> recipe, double cost, double exp) {

    public CustomItemStack viewItem() {
        return viewItem(null);
    }

    public CustomItemStack viewItem(PlayerData playerData) {
        CustomItemStack item = result.viewItem();
        item.addSeparator("制作情報");
        item.addLore(decoLore("制作コスト") + scale(cost));
        if (playerData != null) item.addLore(decoLore("推定制作時間") + CraftMenu.time(cost, playerData));
        item.addLore(decoLore("経験値") + scale(exp));
        item.addSeparator("必要素材");
        for (Pack pack : recipe) {
            String hasAmount = playerData != null ? " §7(" + playerData.getMaterial(pack.material) + ")" : "";
            item.addLore("§7・§f" + pack.material.getDisplay() + "§ax" + pack.amount + hasAmount);
        }
        item.setAmount(result.amount);
        item.setCustomData("CraftData", id);
        return item;
    }

    public static class Result {

        private MaterialData material;
        private SomItem somItem;
        private final int amount;
        private final Type type;

        public Result(MaterialData material, int amount) {
            if (material == null) throw new RuntimeException("Material is Null");
            this.material = material;
            this.amount = amount;
            type = Type.Material;
        }

        public Result(SomItem somItem) {
            if (somItem == null) throw new RuntimeException("Material is Null");
            this.somItem = somItem;
            this.amount = 1;
            type = Type.Item;
        }

        public MaterialData material() {
            return material;
        }

        public SomItem somItem() {
            return somItem;
        }

        public int amount() {
            return amount;
        }

        public String getDisplay() {
            String display;
            switch (type) {
                case Item -> display = somItem.getDisplay();
                case Material -> display = material.getDisplay();
                default -> throw new RuntimeException("Error Type");
            }
            return display;
        }

        public Type type() {
            return type;
        }

        public CustomItemStack viewItem() {
            CustomItemStack item;
            switch (type) {
                case Item -> item = somItem.viewItem();
                case Material -> item = material.viewItem();
                default -> throw new RuntimeException("Error Type");
            }
            return item;
        }

        public void craft(PlayerData playerData) {
            switch (type) {
                case Item -> {
                    SyncItem.register(somItem.getId(), playerData, SyncItem.State.ItemInventory);
                    playerData.updateInventory();
                }
                case Material -> playerData.addMaterial(material, amount);
                default -> throw new RuntimeException("Error Type");
            }
        }

        public enum Type {
            Item,
            Material,
        }
    }

    public record Pack(MaterialData material, int amount) {
    }
}
