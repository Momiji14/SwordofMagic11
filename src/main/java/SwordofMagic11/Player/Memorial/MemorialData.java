package SwordofMagic11.Player.Memorial;

import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.Enemy.MobData;
import SwordofMagic11.StatusType;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static SwordofMagic11.Component.Function.decoLore;
import static SwordofMagic11.Component.Function.scale;

public class MemorialData {

    private String id;
    private Material icon;
    private String display;
    private final List<StatusType> statusTypes = new ArrayList<>();
    private MobData.Rank rank;
    private int point = 0;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Material getIcon() {
        return icon;
    }

    public void setIcon(Material icon) {
        this.icon = icon;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public void addStatus(StatusType statusType) {
        statusTypes.add(statusType);
    }

    public List<StatusType> getStatusTypes() {
        return statusTypes;
    }

    public MobData.Rank getRank() {
        return rank;
    }

    public void setRank(MobData.Rank rank) {
        this.rank = rank;
    }

    public void setPoint(int point) {
        this.point = point;
    }

    public int getPoint() {
        return point;
    }

    public CustomItemStack viewItem(double value) {
        CustomItemStack item = new CustomItemStack(icon);
        item.setDisplay(display);
        item.addLore("§a" + display + "のメモリアルです");
        item.addSeparator("ステータス");
        item.addLore(decoLore("復元率") + scale(value * 100, 3) + "%");
        HashMap<StatusType, Double> status = value(value);
        for (StatusType statusType : statusTypes) {
            item.addLore(decoLore(statusType.getDisplay()) + scale(status.get(statusType) * 100, 3, true) + "%");
        }
        return item;
    }

    public HashMap<StatusType, Double> value(double value) {
        HashMap<StatusType, Double> status = new HashMap<>();
        for (StatusType statusType : statusTypes) {
            status.put(statusType, (0.02 + (0.015 * value * rank.memorialMultiply())) / statusTypes.size());
        }
        return status;
    }
}
