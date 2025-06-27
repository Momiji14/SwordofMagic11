package SwordofMagic11.Custom;

import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.SomCore;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.type.Light;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static SwordofMagic11.Component.Function.*;

public class CustomItemStack extends ItemStack implements Cloneable {

    private List<String> lore = new ArrayList<>();
    private static final UUID uuid = UUID.randomUUID();

    public static CustomItemStack Light(int level) {
        CustomItemStack item = new CustomItemStack(Material.LIGHT).setNonDecoDisplay("§f" +level);
        BlockDataMeta blockDataMeta = (BlockDataMeta) item.getItemMeta();
        BlockData blockData = item.getType().createBlockData();
        ((Levelled) blockData).setLevel(level);
        blockDataMeta.setBlockData(blockData);
        item.setItemMeta(blockDataMeta);
        item.setCustomData("Number", level);
        return item;
    }

    public CustomItemStack(Material icon) {
        super(icon, 1);
        ItemMeta meta = getItemMeta();
        meta.setUnbreakable(true);
        //meta.addAttributeModifier(Attribute.ATTACK_DAMAGE, new AttributeModifier(uuid, "generic.attackDamage", 4, AttributeModifier.Operation.ADD_NUMBER));
        for (ItemFlag flag : ItemFlag.values()) {
            meta.addItemFlags(flag);
        }
        setItemMeta(meta);
    }

    public void setAmount(int amount) {
        super.setAmount(MinMax(amount, 1, 64));
    }

    public CustomItemStack setAmountReturn(int amount) {
        setAmount(amount);
        return this;
    }

    public CustomItemStack setDisplay(String str, int flames) {
        ItemMeta meta = getItemMeta();
        meta.setDisplayName(decoText(str, flames));
        setItemMeta(meta);
        return this;
    }

    public CustomItemStack setDisplay(String str) {
        ItemMeta meta = getItemMeta();
        meta.setDisplayName(decoText(str));
        setItemMeta(meta);
        return this;
    }

    public String getDisplay() {
        ItemMeta meta = getItemMeta();
        return meta.getDisplayName();
    }

    public CustomItemStack setIcon(Material type) {
        super.setType(type);
        return this;
    }

    public CustomItemStack setNonDecoDisplay(String str) {
        ItemMeta meta = getItemMeta();
        meta.setDisplayName(str);
        setItemMeta(meta);
        return this;
    }

    public CustomItemStack setCustomModelData(int modelData) {
        ItemMeta meta = getItemMeta();
        meta.setCustomModelData(modelData);
        setItemMeta(meta);
        return this;
    }

    public CustomItemStack addLore(String str) {
        ItemMeta meta = getItemMeta();
        lore.add(str);
        meta.setLore(lore);
        setItemMeta(meta);
        return this;
    }

    public CustomItemStack removeLastLore(int i) {
        ItemMeta meta = getItemMeta();
        lore.remove((lore.size() - 1) - i);
        meta.setLore(lore);
        setItemMeta(meta);
        return this;
    }

    public CustomItemStack addLore(List<String> str) {
        ItemMeta meta = getItemMeta();
        lore.addAll(str);
        meta.setLore(lore);
        setItemMeta(meta);
        return this;
    }

    public CustomItemStack addSeparator(String str, int flames) {
        ItemMeta meta = getItemMeta();
        lore.add(decoSeparator(str, flames));
        meta.setLore(lore);
        setItemMeta(meta);
        return this;
    }

    public CustomItemStack addSeparator(String str) {
        ItemMeta meta = getItemMeta();
        lore.add(decoSeparator(str));
        meta.setLore(lore);
        setItemMeta(meta);
        return this;
    }

    public CustomItemStack setModel(int model) {
        ItemMeta meta = getItemMeta();
        meta.setCustomModelData(model);
        setItemMeta(meta);
        return this;
    }

    public CustomItemStack setGlowing() {
        return setGlowing(true);
    }

    public CustomItemStack setGlowing(boolean bool) {
        ItemMeta meta = getItemMeta();
        if (bool) {
            meta.addEnchant(Enchantment.UNBREAKING, 0, true);
        }
        setItemMeta(meta);
        return this;
    }

    public CustomItemStack setPotionColor(Color color) {
        PotionMeta potionMeta = (PotionMeta) getItemMeta();
        potionMeta.setColor(color);
        setItemMeta(potionMeta);
        return this;
    }

    public CustomItemStack setLeatherArmorColor(Color color) {
        LeatherArmorMeta armorMeta = (LeatherArmorMeta) getItemMeta();
        armorMeta.setColor(color);
        setItemMeta(armorMeta);
        return this;
    }

    public CustomItemStack addPattern(Pattern pattern) {
        BannerMeta bannerMeta = (BannerMeta) getItemMeta();
        bannerMeta.addPattern(pattern);
        setItemMeta(bannerMeta);
        return this;
    }

    public CustomItemStack addTrim(TrimMaterial trim) {
        TrimPattern trimPattern = (TrimPattern) getItemMeta();
        TrimMaterial trimMaterial = (TrimMaterial) getItemMeta();
        return this;
    }

    public CustomItemStack reloadCrossBowArrow() {
        ItemMeta meta = getItemMeta();
        if (meta instanceof CrossbowMeta crossbow) {
            crossbow.addChargedProjectile(new ItemStack(Material.ARROW));
            setItemMeta(crossbow);
        }
        return this;
    }

    public CustomItemStack setSkull(PlayerData playerData) {
        SkullMeta skullMeta = (SkullMeta) getItemMeta();
        skullMeta.setOwningPlayer(playerData.getPlayer());
        setItemMeta(skullMeta);
        return this;
    }

    public CustomItemStack setCustomData(String id, String value) {
        ItemMeta meta = getItemMeta();
        meta.getPersistentDataContainer().set(SomCore.Key(id), PersistentDataType.STRING, value);
        setItemMeta(meta);
        return this;
    }

    public CustomItemStack setCustomData(String id, int value) {
        ItemMeta meta = getItemMeta();
        meta.getPersistentDataContainer().set(SomCore.Key(id), PersistentDataType.INTEGER, value);
        setItemMeta(meta);
        return this;
    }

    public CustomItemStack setCustomData(String id, double value) {
        ItemMeta meta = getItemMeta();
        meta.getPersistentDataContainer().set(SomCore.Key(id), PersistentDataType.DOUBLE, value);
        setItemMeta(meta);
        return this;
    }

    public CustomItemStack setCustomData(String id, boolean value) {
        ItemMeta meta = getItemMeta();
        meta.getPersistentDataContainer().set(SomCore.Key(id), PersistentDataType.BOOLEAN, value);
        setItemMeta(meta);
        return this;
    }

    public CustomItemStack deleteCustomData(String id) {
        ItemMeta meta = getItemMeta();
        meta.getPersistentDataContainer().remove(SomCore.Key(id));
        setItemMeta(meta);
        return this;
    }

    public static boolean hasCustomData(ItemStack item, String id) {
        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            return meta.getPersistentDataContainer().getKeys().contains(SomCore.Key(id));
        } else return false;
    }

    public static String getCustomData(ItemStack item, String id) {
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().get(SomCore.Key(id), PersistentDataType.STRING);
    }

    public static Integer getCustomDataInt(ItemStack item, String id) {
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().get(SomCore.Key(id), PersistentDataType.INTEGER);
    }

    public static Double getCustomDataDouble(ItemStack item, String id) {
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().get(SomCore.Key(id), PersistentDataType.DOUBLE);
    }


    public static Boolean getCustomDataBoolean(ItemStack item, String id) {
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().get(SomCore.Key(id), PersistentDataType.BOOLEAN);
    }

    public boolean proximate(@Nullable ItemStack item) {
        return item != null && getType() == item.getType() && getDisplay().equals(item.getItemMeta().getDisplayName());
    }

    public boolean equals(ItemStack item) {
        if (item != null && proximate(item) && item.getAmount() == getAmount()) {
            ItemMeta meta = item.getItemMeta();
            if (meta.hasLore() && meta.getLore().size() == lore.size()) {
                for (int i = 0; i < meta.getLore().size(); i++) {
                    if (!lore.get(i).equals(meta.getLore().get(i))) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public @NotNull CustomItemStack clone() {
        CustomItemStack itemStack = (CustomItemStack) super.clone();
        itemStack.lore = new ArrayList<>(lore);
        return itemStack;
    }

    public static void setupColor(CustomItemStack item, Color color) {
        switch (item.getType()) {
            case POTION, SPLASH_POTION, LINGERING_POTION, TIPPED_ARROW -> item.setPotionColor(color);
            case LEATHER_HELMET, LEATHER_CHESTPLATE, LEATHER_LEGGINGS, LEATHER_BOOTS ->
                    item.setLeatherArmorColor(color);
        }
    }

    public static void setLeatherArmorColor(ItemStack item, Color color) {
        LeatherArmorMeta armorMeta = (LeatherArmorMeta) item.getItemMeta();
        armorMeta.setColor(color);
        item.setItemMeta(armorMeta);
    }
}

