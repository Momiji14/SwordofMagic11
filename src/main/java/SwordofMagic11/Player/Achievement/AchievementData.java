package SwordofMagic11.Player.Achievement;

import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.Player.PlayerData;
import kotlin.jvm.functions.Function2;
import org.bukkit.Color;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import static SwordofMagic11.Component.Function.randomInt;

public class AchievementData {
    private String id;
    private String display;
    private Material icon;
    private Color color;
    private Type type;
    private List<String> lore;
    private List<Line> line = new ArrayList<>();
    private Predicate<PlayerData> predicate;
    private List<Function2<PlayerData, String, String>> replace = new ArrayList<>();

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

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public List<String> getLore() {
        return lore;
    }

    public void setLore(List<String> lore) {
        this.lore = lore;
    }

    public List<Line> getLine() {
        return line;
    }

    public void setLine(List<String> lineData) {
        List<Line> line = new ArrayList<>();
        for (String lineText : lineData) {
            String[] split = lineText.split(",");
            String text = split[0];
            int tick = split.length == 2 ? Integer.parseUnsignedInt(split[1]) : 0;
            line.add(new Line(text, tick));
            if (text.contains("%random%")) {
                addReplace((playerData, inText) -> {
                    while (inText.contains("%random%")) {
                        inText = inText.replaceFirst("%random%", String.valueOf(randomInt(1, 9)));
                    }
                    return inText;
                });
            }
        }
        this.line = line;
    }

    public void addLine(String line, int tick) {
        this.line.add(new Line(line, tick));
    }

    public Predicate<PlayerData> getPredicate() {
        return predicate;
    }

    public void setPredicate(Predicate<PlayerData> predicate) {
        this.predicate = predicate;
    }

    public boolean predicate(PlayerData playerData) {
        return predicate.test(playerData);
    }

    public void addReplace(Function2<PlayerData, String, String> replace) {
        this.replace.add(replace);
    }

    public String replace(PlayerData playerData, String text) {
        for (Function2<PlayerData, String, String> function : replace) {
            text = function.invoke(playerData, text);
        }
        return text;
    }

    public CustomItemStack viewItem(PlayerData playerData) {
        CustomItemStack item = new CustomItemStack(icon);
        CustomItemStack.setupColor(item, color);
        item.setDisplay(display);
        item.addLore(lore);
        item.addSeparator("称号ビュー");
        for (Line line : line) {
            item.addLore("§7・§r" + replace(playerData, line.text));
        }
        item.setCustomData("Achievement", id);
        return item;
    }

    public record Line(String text, int tick) {}

    public enum Type {
        None,
        Custom,
        ClassLevel,
        GatheringLevel,
        Statistics,
        Achievement,
        Eld,
        BossTimeAttack,
    }
}
