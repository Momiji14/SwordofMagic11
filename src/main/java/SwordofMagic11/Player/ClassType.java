package SwordofMagic11.Player;

import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.DataBase.DataBase;
import SwordofMagic11.DataBase.SkillDataLoader;
import SwordofMagic11.Skill.SkillData;
import SwordofMagic11.StatusType;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static SwordofMagic11.Component.Function.*;

public enum ClassType {
    Adventurer(Material.CRAFTING_TABLE, 1, 0, 0),
    SwordMan(Material.WOODEN_SWORD, 15, 1, 9),
    Magician(Material.BLAZE_POWDER, 15, 1, 18),
    Archer(Material.BOW, 15, 1, 27),
    Acolyte(Material.APPLE, 15, 1, 36),

    Tamer(Material.TURTLE_EGG, 40, 2, 1),
    Warrior(Material.STONE_SWORD, 40, 2, 10),
    Wizard(Material.BLAZE_POWDER, 40, 2, 19),
    Hunter(Material.BOW, 40, 2, 28),
    HolyKnight(Material.APPLE, 40, 2, 37),

    DualStar(Material.TURTLE_HELMET, 50, 3, 2),
    SwordKaiser(Material.IRON_SWORD, 50, 3, 11),
    Sorcerer(Material.BLAZE_POWDER, 50, 3, 20),
    Harmit(Material.BOW, 50, 3, 29),
    Priest(Material.APPLE, 50, 3, 38),

    Gladiator(Material.GOLDEN_SWORD, 50, 4, 12),
    GranMagia(Material.BLAZE_POWDER, 50, 4, 21),
    Avengista(Material.BOW, 50, 4, 30),
    Arshive(Material.APPLE, 50, 4, 39),
    ;

    double abc, ABC;

    private String display;
    private final Material icon;
    private String nick;
    private String color;
    private final int startLevel;
    private final int tier;
    private final int slot;
    private List<String> lore;
    private List<String> skill;
    private List<String> passive = new ArrayList<>();
    private final HashMap<StatusType, Double> baseStatus = new HashMap<>();
    private final HashMap<ClassType, Integer> reqLevel = new HashMap<>();

    ClassType(Material icon, int startLevel, int tier, int slot) {
        this.icon = icon;
        this.startLevel = startLevel;
        this.tier = tier;
        this.slot = slot;
    }

    public String getDisplay() {
        return display;
    }

    public String getNick() {
        return nick;
    }

    public String getColor() {
        return color;
    }

    public int tier() {
        return tier;
    }

    public int slot() {
        return slot;
    }

    public int startLevel() {
        return startLevel;
    }

    public String getColorDisplay() {
        return color + display;
    }

    public String getDecoNick() {
        return color + "[" + nick + "]§r";
    }

    public List<String> getLore() {
        return lore;
    }

    public List<String> getSkill() {
        return skill;
    }

    public List<String> getPassive() {
        return passive;
    }

    public HashMap<StatusType, Double> getBaseStatus() {
        return baseStatus;
    }

    public HashMap<ClassType, Integer> getReqLevel() {
        return reqLevel;
    }

    public CustomItemStack viewItem() {
        CustomItemStack item = new CustomItemStack(icon);
        item.setDisplay(getColorDisplay() + " [" + nick + "]");
        item.addLore(lore);
        item.addSeparator("基礎ステータス");
        for (StatusType statusType : StatusType.values()) {
            double value = getBaseStatus().getOrDefault(statusType, 0.0);
            if (value != 0) {
                item.addLore(decoLore(statusType.getDisplay()) + scale(value, 1));
            }
        }
        item.addSeparator("クラス情報");
        item.addLore(decoLore("クラスティア") + "T" + tier);
        item.addLore(decoLore("短縮表示") + getDecoNick());
        item.addLore(decoLore("初期レベル") + startLevel);
        item.addLore(decoLore("アクティブスキル") + skill.size() + "種");
        item.addLore(decoLore("パッシブスキル") + passive.size() + "種");
        item.addSeparator("転職条件");
        if (reqLevel.isEmpty()) {
            item.addLore("§7・§e無条件");
        } else {
            reqLevel.forEach(((classType, level) -> item.addLore("§7・" + classType.getColorDisplay() + " §eLv" + level)));
        }
        item.setCustomData("ClassType", this.toString());
        return item;
    }

    public void load(FileConfiguration data) {
        display = data.getString("Display");
        nick = data.getString("Nick");
        color = data.getString("Color");
        lore = loreText(data.getStringList("Lore"));
        skill = data.getStringList("Skill");
        if (data.isSet("Passive")) passive = data.getStringList("Passive");

        for (String reqText : data.getStringList("ReqLevel")) {
            String[] split = reqText.split(":");
            reqLevel.put(valueOf(split[0]), Integer.valueOf(split[1]));
        }

        for (StatusType type : StatusType.values()) {
            if (data.isSet(type.toString())) {
                baseStatus.put(type, data.getDouble(type.toString()));
            }
        }
    }

    public static void load() {
        for (File file : DataBase.dumpFile(new File(DataBase.Path, "Classes"))) {
            try {
                FileConfiguration data = YamlConfiguration.loadConfiguration(file);
                ClassType classType = ClassType.valueOf(DataBase.fileId(file));
                classType.load(data);
            } catch (Exception e) {
                DataBase.error(file, e);
            }
        }
    }
}
