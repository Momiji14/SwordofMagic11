package SwordofMagic11;

import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.Player.PlayerData;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static SwordofMagic11.Component.Function.decoLore;
import static SwordofMagic11.Component.Function.scale;

public enum AttributeType {
    Strength("筋力", "STR", Material.RED_DYE),
    Intelligence("知能", "INT", Material.PURPLE_DYE),
    Dexterity("技量", "DEX", Material.YELLOW_DYE),
    Agility("敏捷", "AGI", Material.LIGHT_BLUE_DYE),
    Spirit("精神", "SPI", Material.CYAN_DYE),
    Vitality("耐力", "VIT", Material.ORANGE_DYE),
    ;

    private final String display;
    private final String nick;
    private final Material icon;

    AttributeType(String display, String nick, Material icon) {
        this.display = display;
        this.nick = nick;
        this.icon = icon;
    }

    public String getDisplay() {
        return display;
    }

    public String getNick() {
        return nick;
    }

    public Material getIcon() {
        return icon;
    }

    public CustomItemStack viewItem(PlayerData playerData) {
        int attr = playerData.getBaseAttribute().getOrDefault(this, 0);
        CustomItemStack item = new CustomItemStack(icon);
        item.setDisplay(display + " [" + attr + "]");
        for (StatusType statusType : StatusType.values()) {
            if (getPerStatus().containsKey(statusType)) {
                double value = getPerStatus().get(statusType);
                double current = value * attr;
                double next = value * (attr + 1);
                item.addLore(decoLore(statusType.getDisplay()) + scale(current, 1) + " -> " + scale(next, 1));
            }
        }
        item.setCustomData("Attribute", this.toString());
        return item;
    }

    public HashMap<StatusType, Double> getPerStatus() {
        HashMap<StatusType, Double> perStatus = new HashMap<>();
        switch (this) {
            case Strength -> {
                perStatus.put(StatusType.ATK, 1.5);
                perStatus.put(StatusType.DEF, 0.2);
                perStatus.put(StatusType.CriticalDamage, 0.6);
            }
            case Intelligence -> {
                perStatus.put(StatusType.MAT, 1.5);
                perStatus.put(StatusType.MDF, 0.2);
                perStatus.put(StatusType.CriticalDamage, 0.2);
                perStatus.put(StatusType.ManaRegen, 0.01);
            }
            case Dexterity -> {
                perStatus.put(StatusType.SPT, 0.5);
                perStatus.put(StatusType.HealthRegen, 0.01);
                perStatus.put(StatusType.ManaRegen, 0.01);
                perStatus.put(StatusType.CriticalRate, 1.0);
                perStatus.put(StatusType.GatheringPower, 2.0);
            }
            case Agility -> {
                perStatus.put(StatusType.SAT, 1.5);
                perStatus.put(StatusType.SDF, 0.2);
                perStatus.put(StatusType.CriticalDamage, 0.6);
                perStatus.put(StatusType.CriticalResist, 0.3);
            }
            case Spirit -> {
                perStatus.put(StatusType.MaxMana, 10.0);
                perStatus.put(StatusType.ManaRegen, 0.12);
                perStatus.put(StatusType.MDF, 1.25);
                perStatus.put(StatusType.SPT, 1.5);
            }
            case Vitality -> {
                perStatus.put(StatusType.MaxHealth, 10.0);
                perStatus.put(StatusType.HealthRegen, 0.1);
                perStatus.put(StatusType.CriticalResist, 1.0);
                perStatus.put(StatusType.DEF, 1.25);
                perStatus.put(StatusType.SDF, 1.25);
            }
        }
        return perStatus;
    }

    public static List<String> complete() {
        List<String> complete = new ArrayList<>();
        for (AttributeType type : values()) {
            complete.add(type.getDisplay());
        }
        return complete;
    }

    public static AttributeType from(String from) {
        for (AttributeType type : values()) {
            if (type.toString().equals(from) || type.getDisplay().equals(from) || type.getNick().equals(from)) {
                return type;
            }
        }
        throw new RuntimeException("Not Found AttributeType");
    }
}
