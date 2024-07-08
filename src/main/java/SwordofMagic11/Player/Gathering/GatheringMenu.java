package SwordofMagic11.Player.Gathering;

import SwordofMagic11.Component.SomSQL;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.DataBase.DataBase;
import SwordofMagic11.Player.Classes;
import SwordofMagic11.Player.Menu.GUIManager;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Player.Setting.PlayerSetting;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static SwordofMagic11.Component.Function.*;
import static SwordofMagic11.DataBase.DataBase.ResetCost;
import static SwordofMagic11.Player.Classes.MaxLevel;
import static SwordofMagic11.Player.Classes.PerSkillPoint;

public class GatheringMenu extends GUIManager {
    private static final double[] reqExp = new double[MaxLevel+1];

    static {
        for (int i = 0; i < MaxLevel; i++) {
            reqExp[i] = 95 + (Math.pow(i, 3) + i * 10) * 0.5;
            reqExp[i] *= Classes.border(i+1);
        }
    }

    public static double ReqExp(int level) {
        if (level > MaxLevel) {
            return Double.MAX_VALUE;
        }
        if (level <= 1) {
            return reqExp[0];
        }
        return reqExp[level - 1];
    }

    public CustomItemStack icon() {
        CustomItemStack item = new CustomItemStack(Material.IRON_PICKAXE);
        item.setDisplay("ギャザリングメニュー");
        item.addLore("§aギャザリングのスキル振りなどが行えます");
        item.addLore("§aポイントはレベル毎に§e" + PerSkillPoint + "§a獲得します");
        item.addLore("§7左クリックx1 右クリックx10 シフトクリックx100");
        item.addSeparator("ステータス");
        for (Type type : Type.values()) {
            item.addLore("§7・§e" + type.getDisplay() + " Lv" + getLevel(type) + " §a" + scale(getExpPercent(type)*100, 2) + "%");
        }
        item.setCustomData("Menu", "GatheringMenu");
        return item;
    }

    private final HashMap<Type, Instance> instance = new HashMap<>();

    public GatheringMenu(PlayerData playerData) {
        super(playerData, "ギャザリングメニュー", 4);
        for (Type type : Type.values()) {
            instance.put(type, new Instance(playerData, type));
        }
    }

    @Override
    public void updateContainer() {
        int slot = 0;
        for (Type type : Type.values()) {
            setItem(slot, type.viewItem(this));
            setItem(slot+1, ItemFlame);
            int slot2 = 2;
            for (Skill skill : type.getSkill()) {
                CustomItemStack item = new CustomItemStack(Material.EXPERIENCE_BOTTLE);
                item.setNonDecoDisplay("§e" + getSkillPoint(type) + "P");
                item.addLore("§c※経験値" + scale(ResetCost * 100, 1) + "%を消費してリセットできます");
                item.setCustomData("ResetSkill", type.toString());

                setItem(slot+slot2, skill.viewItem(getSkillLevel(type, skill)).setCustomData("Gathering", type.toString()));
                setItem(slot+8, item);
                slot2++;
            }
            slot += 9;
        }
    }

    public void apply(Type type, Skill skill, int usePoint) {
        if (type.getSkill().contains(skill)) {
            usePoint = Math.min(getSkillPoint(type)/skill.getReqPoint(), usePoint);
            if (usePoint > 0) {
                usePoint = Math.min(skill.getMaxLevel() - getSkillLevel(type, skill), usePoint);
                if (usePoint > 0) {
                    removeSkillPoint(type, skill.getReqPoint() * usePoint);
                    addSkillLevel(type, skill, usePoint);
                    update();
                } else {
                    playerData.sendMessage("§e" + type.getDisplay() + "§aは§e最大レベル§aです", SomSound.Nope);
                }
            } else {
                playerData.sendMessage("§eポイント§aが足りません", SomSound.Nope);
            }
        } else {
            playerData.sendMessage("§e" + type.getDisplay() + "§aは§e" + skill.getDisplay() + "§aを扱えません", SomSound.Nope);
        }
    }

    private final String[] key = new String[]{"UUID", "Type"};
    private String[] value(Type type) {
        return new String[]{playerData.getUUID(), type.toString()};
    }

    @Override
    public void topClick(int slot, ItemStack clickedItem, ClickType clickType) {
        if (CustomItemStack.hasCustomData(clickedItem, "ResetSkill")) {
            Type type = Type.valueOf(CustomItemStack.getCustomData(clickedItem, "ResetSkill"));
            if (type == Type.Craft && !playerData.craftMenu().isRawEmpty()) {
                playerData.sendMessage("§c制作中のスロットがあります", SomSound.Nope);
                return;
            }
            double reqExp = getLevel(type) >= MaxLevel ? 0 : ReqExp(getLevel(type)) * ResetCost;
            if (getExp(type) >= reqExp) {
                removeExp(type, reqExp);
                setSkillPoint(type, PerSkillPoint*(getLevel(type)-1));
                for (Skill skill : type.getSkill()) {
                    setSkillLevel(type, skill, 0);
                }
                SomSQL.delete(DataBase.Table.PlayerGatheringSkill, key, value(type));
                update();
            } else {
                playerData.sendMessage("§e経験値§aが足りません", SomSound.Nope);
            }
        }
        if (CustomItemStack.hasCustomData(clickedItem, "GatheringSkill")) {
            Type type = Type.valueOf(CustomItemStack.getCustomData(clickedItem, "Gathering"));
            Skill skill = Skill.valueOf(CustomItemStack.getCustomData(clickedItem, "GatheringSkill"));
            int usePoint = clickMultiply(clickType);
            apply(type, skill, usePoint);
        }
    }

    @Override
    public void bottomClick(int slot, ItemStack clickedItem, ClickType clickType) {

    }

    @Override
    public void close() {
        playerData.updateMainHand();
    }

    public int getLevel(Type type) {
        return instance.get(type).getLevel();
    }

    public void setLevel(Type type, int level) {
        instance.get(type).setLevel(level);
    }

    public void addLevel(Type type, int level) {
        setLevel(type, getLevel(type) + level);
        addSkillPoint(type, PerSkillPoint * level);
        if (MaxLevel <= getLevel(type)) setExp(type, 0);
        String text = "§e" + type.getDisplay() + "レベル§aが§eLv" + getLevel(type) + "§aになりました";
        playerData.sendMessage(text, SomSound.Level);
        for (PlayerData playerData : PlayerData.getPlayerList()) {
            if (playerData != this.playerData && playerData.setting().is(PlayerSetting.BooleanEnum.LevelLog)) {
                playerData.sendMessage(this.playerData.getName() + "§aの" + text);
            }
        }
    }

    public double getExp(Type type) {
        return instance.get(type).getExp();
    }

    public void setExp(Type type, double exp) {
        instance.get(type).setExp(exp);
    }

    public synchronized void addExp(Type type, double exp) {
        double current = getExp(type) + exp;
        double classExp = exp * 0.25;
        if (getLevel(type) >= MaxLevel) {
            double reqExp = ReqExp(getLevel(type));
            if (current >= reqExp) {
                current = reqExp-1;
                classExp = exp;
            }
        } else {
            int addLevel = 0;
            while (current >= ReqExp(getLevel(type) + addLevel)) {
                current -= ReqExp(getLevel(type) + addLevel);
                addLevel++;
            }
            if (addLevel > 0) addLevel(type, 1);
        }
        setExp(type, current);
        playerData.classes().addExp(classExp);
        if (playerData.setting().is(PlayerSetting.BooleanEnum.ExpLog)) {
            playerData.sendMessage("§a[+]" + type.getDisplay() + " §a+" + scale(exp, 2));
        }
    }

    public void removeExp(Type type, double exp) {
        setExp(type, getExp(type) - exp);
    }

    public double getExpPercent(Type type) {
        return instance.get(type).getExp() / ReqExp(getLevel(type));
    }

    public int getSkillPoint(Type type) {
        return instance.get(type).getSkillPoint();
    }

    public void setSkillPoint(Type type, int skillPoint) {
        instance.get(type).setSkillPoint(skillPoint);

    }

    public void addSkillPoint(Type type, int skillPoint) {
        setSkillPoint(type, getSkillPoint(type) + skillPoint);
    }

    public void removeSkillPoint(Type type, int skillPoint) {
        setSkillPoint(type, getSkillPoint(type) - skillPoint);
    }

    public int getSkillLevel(Type type, Skill skill) {
        return instance.get(type).getSkillLevel(skill);
    }

    public void setSkillLevel(Type type, Skill skill, int level) {
        instance.get(type).setSkillLevel(skill, level);
    }

    public void addSkillLevel(Type type, Skill skill, int level) {
        setSkillLevel(type, skill, getSkillLevel(type, skill) + level);
    }

    public double getSkillValue(Type type, Skill skill) {
        return instance.get(type).getSkillValue(skill);
    }

    public int getSkillValueInt(Type type, Skill skill) {
        return (int) getSkillValue(type, skill);
    }

    public enum Type {
        Mining("採掘", Material.IRON_PICKAXE, new Skill[]{Skill.Percent, Skill.TimeSave, Skill.Efficiency, Skill.MaxUp}),
        Collect("採集", Material.SHEARS, new Skill[]{Skill.Percent, Skill.TimeSave, Skill.Efficiency, Skill.MaxUp}),
        Fishing("漁獲", Material.FISHING_ROD, new Skill[]{Skill.Percent, Skill.TimeSave, Skill.MaxUp}),
        Craft("制作", Material.CRAFTING_TABLE, new Skill[]{Skill.TimeSave, Skill.SlotUp, Skill.OfflineMaxTime}),
        ;

        private final String display;
        private final Material icon;
        private final List<Skill> skill;

        Type(String display, Material icon, Skill[] skill) {
            this.display = display;
            this.icon = icon;
            this.skill = List.of(skill);
        }

        public String getDisplay() {
            return display;
        }

        public Material getIcon() {
            return icon;
        }

        public List<Skill> getSkill() {
            return skill;
        }

        public CustomItemStack viewItem(GatheringMenu gathering) {
            CustomItemStack item = new CustomItemStack(icon);
            item.setDisplay(display);
            item.addLore(decoLore("レベル") + gathering.getLevel(this));
            item.addLore(decoLore("経験値") + scale(gathering.getExpPercent(this)*100, 3) + "%");
            item.addSeparator("スキル");
            for (Skill skill : skill) {
                item.addLore("§7・§e" + skill.display + " Lv" + gathering.getSkillLevel(this, skill));
            }
            return item;
        }

        public static List<String> complete() {
            List<String> complete = new ArrayList<>();
            for (Type type : values()) {
                complete.add(type.getDisplay());
            }
            return complete;
        }

        public static Type from(String from) {
            for (Type type : values()) {
                if (type.toString().equals(from) || type.getDisplay().equals(from)) {
                    return type;
                }
            }
            throw new RuntimeException("Not Found GatheringMenu.Type");
        }
    }

    public static class Instance {
        private final PlayerData playerData;
        private final Type type;

        int level = 1;
        double exp = 0;
        int skillPoint = 0;
        HashMap<Skill, Integer> skillLevel = new HashMap<>();

        private final String[] key = new String[]{"UUID", "Type"};
        private final String[] value;
        private final String[] skillKey = new String[]{"UUID", "Type", "Skill"};
        private String[] skillValue(Skill skill) {
            return new String[]{playerData.getUUID(), type.toString(), skill.toString()};
        }

        public Instance(PlayerData playerData, Type type) {
            this.playerData = playerData;
            this.type = type;

            value = new String[]{playerData.getUUID(), type.toString()};

            if (SomSQL.exists(DataBase.Table.PlayerGathering, key, value)) {
                level = SomSQL.getInt(DataBase.Table.PlayerGathering, key, value, "Level");
                exp = SomSQL.getDouble(DataBase.Table.PlayerGathering, key, value, "Exp");
                skillPoint = SomSQL.getInt(DataBase.Table.PlayerGathering, key, value, "SkillPoint");
            }
            for (Skill skill : type.getSkill()) {
                String[] value = skillValue(skill);
                if (SomSQL.exists(DataBase.Table.PlayerGatheringSkill, skillKey, value)) {
                    skillLevel.put(skill, SomSQL.getInt(DataBase.Table.PlayerGatheringSkill, skillKey, value, "Level"));
                }
            }
        }

        public int getLevel() {
            return level;
        }

        public void setLevel(int level) {
            this.level = level;
            SomSQL.setSql(DataBase.Table.PlayerGathering, key, value, "Level", level);
        }

        public double getExp() {
            return exp;
        }

        public void setExp(double exp) {
            this.exp = exp;
            SomSQL.setSql(DataBase.Table.PlayerGathering, key, value, "Exp", exp);
        }

        public int getSkillPoint() {
            return skillPoint;
        }

        public void setSkillPoint(int skillPoint) {
            this.skillPoint = skillPoint;
            SomSQL.setSql(DataBase.Table.PlayerGathering, key, value, "SkillPoint", skillPoint);
        }

        public HashMap<Skill, Integer> getSkillLevel() {
            return skillLevel;
        }

        public int getSkillLevel(Skill skill) {
            return skillLevel.getOrDefault(skill, 0);
        }


        public void setSkillLevel(Skill skill, int level) {
            skillLevel.put(skill, level);
            SomSQL.setSql(DataBase.Table.PlayerGatheringSkill, skillKey, skillValue(skill), "Level", level);
        }

        public void addSkillLevel(Skill skill, int level) {
            setSkillLevel(skill, getSkillLevel(skill) + level);
        }

        public double getSkillValue(Skill skill) {
            return skill.value(getSkillLevel(skill));
        }
    }

    public enum Skill {
        Percent("獲得確率", Material.REPEATER, "§e獲得確率§aが上がります", 1, 999),
        TimeSave("時間短縮", Material.CLOCK, "§eリポップ時間§aや§e制作時間§aが§b短縮§aされます", 1, 999),
        Efficiency("効率化", Material.IRON_PICKAXE, "§e獲得速度§aが上がります", 3, 30),
        MaxUp("最大量増加", Material.CHEST, "一回の§e獲得§aで取れる§c個数上限§aが増えます", 7, 17),
        SlotUp("スロット増加", Material.GLOW_ITEM_FRAME, "同時に製作できる§eスロット§aが増えます", 8, 6),
        OfflineMaxTime("オフライン最大時間", Material.COMMAND_BLOCK, "§bオフライン再生§aの§e最大時間§aが増えます", 5, 16),
        ;

        private final String display;
        private final Material icon;
        private final List<String> lore;
        private final int reqPoint;
        private final int maxLevel;

        Skill(String display, Material icon, String lore, int reqPoint, int maxLevel) {
            this.display = display;
            this.icon = icon;
            this.lore = loreText(lore);
            this.reqPoint = reqPoint;
            this.maxLevel = maxLevel;
        }

        public String getDisplay() {
            return display;
        }

        public Material getIcon() {
            return icon;
        }

        public List<String> getLore() {
            return lore;
        }

        public int getReqPoint() {
            return reqPoint;
        }

        public int getMaxLevel() {
            return maxLevel;
        }

        public double value(int level) {
            double value;
            switch (this) {
                case Percent -> value = 1 + level * 0.05;
                case TimeSave -> value = 1 / (1 + level * 0.05);
                case Efficiency, MaxUp, SlotUp -> value = level;
                case OfflineMaxTime -> value = CraftMenu.OfflineMaxTime + level;
                default -> value = 0;
            }
            return value;
        }

        public String text(int level) {
            String text;
            switch (this) {
                case Percent -> text = decoLore("獲得") + scale((value(level)-1)*100, true) + "%";
                case TimeSave -> text = decoLore("短縮") + scale((value(level)-1)*100, true) + "%";
                case Efficiency -> text = decoLore("効率") + scale(value(level), true) ;
                case MaxUp -> text = decoLore("最大数") + scale((value(level)), true) + "個";
                case SlotUp -> text = decoLore("スロット") + scale((value(level)), true);
                case OfflineMaxTime -> text = decoLore("オフライン最大時間") + scale((value(level))) + "時間";
                default -> text = "";
            }
            return text;
        }

        public CustomItemStack viewItem(int level) {
            CustomItemStack item = new CustomItemStack(icon);
            item.setDisplay("§e" + display);
            item.addLore(lore);
            item.addSeparator("ステータス");
            item.addLore(decoLore("レベル") + level + "/" + maxLevel);
            item.addLore(text(level));
            item.addLore(decoLore("必要ポイント") + reqPoint);
            item.setAmount(level);
            item.setCustomData("GatheringSkill", this.toString());
            return item;
        }

        public static List<String> complete() {
            List<String> complete = new ArrayList<>();
            for (Skill skill : values()) {
                complete.add(skill.getDisplay());
            }
            return complete;
        }

        public static Skill from(String from) {
            for (Skill skill : values()) {
                if (skill.toString().equals(from) || skill.getDisplay().equals(from)) {
                    return skill;
                }
            }
            throw new RuntimeException("Not Found GatheringMenu.Skill");
        }
    }
}
