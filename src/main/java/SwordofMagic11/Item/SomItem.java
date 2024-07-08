package SwordofMagic11.Item;

import SwordofMagic11.Command.Player.Lock;
import SwordofMagic11.Component.SomSQL;
import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.DataBase.DataBase;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Player.Smith.EquipPlusMenu;
import SwordofMagic11.StatusType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Color;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static SwordofMagic11.Component.Function.*;

public class SomItem implements Cloneable, Comparable<SomItem> {
    private String id;
    private String display;
    private Material icon;
    private Color color;
    private List<String> lore;
    private Category category;
    private int sell;
    private String uuid = UUID.randomUUID().toString();
    private boolean isSync = false;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public Material getIcon() {
        return icon;
    }

    public void setIcon(Material icon) {
        this.icon = icon;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public List<String> getLore() {
        return lore;
    }

    public void setLore(List<String> lore) {
        this.lore = lore;
    }

    public void setLore(String lore) {
        this.lore = loreText(lore);
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public int getSell() {
        return sell;
    }

    public void setSell(int sell) {
        this.sell = sell;
    }

    public void randomUUID() {
        uuid = UUID.randomUUID().toString();
    }

    public String getUUID() {
        return uuid;
    }

    public boolean isSync() {
        return isSync;
    }

    public void setSync(boolean sync) {
        isSync = sync;
    }

    protected void setUUID(String uuid) {
        this.uuid = uuid;
    }

    private PlayerData owner;

    public void setOwner(PlayerData playerData) {
        if (owner != playerData) {
            owner = playerData;
            SomSQL.setSql(DataBase.Table.ItemStorage, "UUID", uuid, "Owner", owner.getUUID());
        }
    }

    public PlayerData getOwner() {
        return owner;
    }

    public boolean hasOwner() {
        return owner != null;
    }

    private SyncItem.State state;

    public void setState(SyncItem.State state) {
        this.state = state;
        SomSQL.setSql(DataBase.Table.ItemStorage, "UUID", uuid, "State", state.toString());
    }

    public void setRawState(SyncItem.State state) {
        this.state = state;
    }

    public SyncItem.State getState() {
        return state;
    }

    public CustomItemStack viewItem() {
        CustomItemStack item = new CustomItemStack(icon);
        CustomItemStack.setupColor(item, color);
        List<String> viewText = viewText();
        item.setDisplay(viewText.get(0));
        viewText.remove(0);
        item.addLore(viewText);
        if (this instanceof SomEquip equip) {
            item.setCustomData("Equip.Category", equip.getEquipCategory().toString());
        }
        if (this instanceof SomUseItem) {
            item.setCustomData("SomUseItem", id);
        }
        item.setCustomData("UUID", uuid);
        return item;
    }

    public Component toComponent() {
        List<String> viewText = viewText();
        Component hover = Component.text(decoText(viewText.get(0)));
        Component component = Component.text(viewText.get(0).replace("[", "").replace("]", "").replace(" ", ""));
        viewText.remove(0);
        for (String text : viewText) {
            hover = hover.appendNewline().append(Component.text(text));
        }
        return component.hoverEvent(HoverEvent.showText(hover));
    }

    public List<String> viewText() {
        List<String> text = new ArrayList<>();
        text.add(display);
        if (lore != null) {
            text.addAll(lore);
        } else {
            text.add("§a" + display + "です");
        }
        text.add(decoSeparator("アイテム情報"));
        text.add(decoLore("カテゴリ") + category.getDisplay());
        text.add(decoLore("売値") + sell);
        if (this instanceof SomEquip equip) {
            String prefix = equip.getTrance() > 0 ? "§e[" + numberRoma(equip.getTrance()) + "]§r " : "";
            String suffix = equip.getPlus() > 0 ? " §e[+" + equip.getPlus() + "]" : "";
            text.removeFirst();
            text.addFirst(prefix + equip.getColorDisplay() + suffix);
            text.add(decoSeparator("装備情報"));
            text.add(decoLore("装備種") + equip.getEquipCategory().getDisplay());
            for (StatusType statusType : StatusType.values()) {
                if (equip.getStatus(statusType) != 0) {
                    text.add(decoLore(statusType.getDisplay()) + scale(equip.getStatus(statusType), 1) + " §7(" + scale(equip.getBaseStatus(statusType), 1) + ")");
                }
            }
            text.add(decoSeparator("強化情報"));
            text.add(decoLore("超越値") + equip.getTrance());
            text.add(decoLore("強化値") + equip.getPlus() + "/" + EquipPlusMenu.MaxPlus);
            text.add(decoLore("熟練度") + scale(equip.getExp() * 100, 2) + "%");
            text.add(decoSeparator("カプセル"));
            int index = 0;
            for (CapsuleData capsule : equip.getCapsules()) {
                text.add("§7・§e" + capsule.getDisplay());
                index++;
            }
            while (index < equip.getCapsuleSlot()) {
                text.add("§7・§7カプセル未装着");
                index++;
            }
            text.add(decoSeparator("装備条件"));
            text.add(decoLore("§c必要レベル") + "Lv" + equip.getReqLevel());
        }
        return text;
    }

    @Override
    public SomItem clone() {
        try {
            SomItem item = (SomItem) super.clone();
            item.setSync(false);
            return item;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    private static final String[] key = new String[]{"UUID", "Param"};

    public String getMetaData(DataBase.Table table, String defaultValue) {
        if (SomSQL.exists(table, "UUID", uuid, "Value")) {
            return SomSQL.getString(table, "UUID", uuid, "Value");
        } else return defaultValue;
    }

    public double getMetaDataDouble(DataBase.Table table, double defaultValue) {
        if (SomSQL.exists(table, "UUID", uuid, "Value")) {
            return Double.parseDouble(SomSQL.getString(table, "UUID", uuid, "Value"));
        } else return defaultValue;
    }

    public int getMetaDataInt(DataBase.Table table, int defaultValue) {
        if (SomSQL.exists(table, "UUID", uuid, "Value")) {
            return Integer.parseInt(SomSQL.getString(table, "UUID", uuid, "Value"));
        } else return defaultValue;
    }

    public void setMetaData(String param, String value) {
        if (!isSync) return;
        String[] priKey = new String[]{"UUID", "Param"};
        String[] priValue = new String[]{uuid, param};
        SomSQL.setSql(DataBase.Table.ItemMetaData, priKey, priValue, "Value", value);
    }

    public void setMetaData(String param, double value) {
        setMetaData(param, String.valueOf(value));
    }

    public void setMetaData(String param, int value) {
        setMetaData(param, String.valueOf(value));
    }

    public void deleteMetaData(String param) {
        if (!isSync) return;
        String[] priKey = new String[]{"UUID", "Param"};
        String[] priValue = new String[]{uuid, param};
        SomSQL.delete(DataBase.Table.ItemMetaData, priKey, priValue);
    }

    @Override
    public int compareTo(SomItem item) {
        return display.compareTo(item.getDisplay());
    }

    public enum Category {
        Equip("装備"),
        Tool("ツール"),
        TeleportCrystal("転移結晶"),
        Donation("寄付アイテム"),
        Vote("投票アイテム")
        ;

        private final String display;

        Category(String display) {
            this.display = display;
        }

        public String getDisplay() {
            return display;
        }
    }
}
