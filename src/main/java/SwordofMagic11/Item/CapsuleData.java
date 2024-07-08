package SwordofMagic11.Item;

import SwordofMagic11.Component.Function;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Player.Setting.PlayerSetting;
import SwordofMagic11.StatusType;
import org.bukkit.Color;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static SwordofMagic11.Component.Function.*;

public class CapsuleData {

    public static int Sell = 10;

    private String id;
    private String display;
    private Material icon;
    private Color color;
    private String group;
    private double percent;
    private final List<HashMap<String, Integer>> recipes = new ArrayList<>();
    private final HashMap<StatusType, Double> status = new HashMap<>();

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

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public double getPercent() {
        return percent;
    }

    public void setPercent(double percent) {
        this.percent = percent;
    }

    public double getStatus(StatusType type) {
        return status.getOrDefault(type, 0.0);
    }

    public HashMap<StatusType, Double> getStatus() {
        return status;
    }

    public void setStatus(StatusType type, double value) {
        status.put(type, value);
    }

    public List<HashMap<String, Integer>> getRecipes() {
        return recipes;
    }

    public void addRecipe(HashMap<String, Integer> recipe) {
        recipes.add(recipe);
    }

    public CustomItemStack viewItem() {
        CustomItemStack item = new CustomItemStack(icon);
        CustomItemStack.setupColor(item, color);
        item.setDisplay(display);
        item.addLore(decoLore("系統") + group);
        item.addLore(decoLore("成功率") + scale(percent*100, 1) + "%");
        for (StatusType statusType : StatusType.values()) {
            if (getStatus(statusType) != 0) {
                item.addLore(decoLore(statusType.getDisplay()) + scale(getStatus(statusType)*100, -1) + "%");
            }
        }
        item.addSeparator("アイテム情報");
        item.addLore(decoLore("ID") + id);
        item.addLore(decoLore("カテゴリ") + "カプセル");
        item.addLore(decoLore("売値") + Sell + "メル");
        item.setCustomData("Capsule", id);
        return item;
    }

    public static void drop(PlayerData playerData, CapsuleData capsule, double percent) {
        boolean log = playerData.setting().is(PlayerSetting.BooleanEnum.CapsuleLog);
        if (log) log = !playerData.setting().is(PlayerSetting.BooleanEnum.RareOnlyLog) || percent <= 0.01;
        if (playerData.capsuleMenu().hasEmpty(capsule)) {
            if (randomDouble() < percent) {
                playerData.capsuleMenu().add(capsule.getId(), 1);
                String percentText = " §7(" + scale(percent * 100, 2) + "%)";
                if (log) playerData.sendMessage("§b[+]" + capsule.getDisplay() + percentText);
                if (capsule.id.contains("ソール")) {
                    if (playerData.setting().is(PlayerSetting.BooleanEnum.SoulCapsuleLog)) {
                        playerData.sendMessage(capsule.getDisplay() + "§aを§e獲得§aしました" + percentText, SomSound.Tick);
                    }
                    if (playerData.hasParty()) {
                        for (PlayerData member : playerData.getParty().getMember()) {
                            if (member.setting().is(PlayerSetting.BooleanEnum.SoulCapsuleLog)) {
                                member.sendMessage(playerData.getDisplayName() + "§aが§f" + capsule.getDisplay() + "§aを§e獲得§aしました" + percentText);
                            }
                        }
                    }
                }


            }
        } else if (log) {
            playerData.capsuleMenu().sendNonHasEmpty();
        }
    }
}
