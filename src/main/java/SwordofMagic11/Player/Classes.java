package SwordofMagic11.Player;

import SwordofMagic11.AttributeType;
import SwordofMagic11.Component.SomSQL;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Component.SomTask;
import SwordofMagic11.DataBase.DataBase;
import SwordofMagic11.DataBase.SkillDataLoader;
import SwordofMagic11.Player.Gathering.GatheringMenu;
import SwordofMagic11.Player.Setting.PlayerSetting;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;

import static SwordofMagic11.Component.Function.scale;

public class Classes {
    public static final int MaxLevel = 260;
    public static final int PerAttrPoint = 5;
    public static final int PerSkillPoint = 1;
    private static final double[] reqExp = new double[MaxLevel+1];
    private final String[] key = new String[]{"UUID", "ClassType"};

    static {
        for (int i = 0; i < MaxLevel; i++) {
            reqExp[i] = 200 + Math.pow(i, 3);
            reqExp[i] *= border(i + 1);
        }
    }

    public static double border(int level) {
        double multiply = 1;
        if (level >= 50) multiply *= 2;
        if (level >= 100) multiply *= 2;
        if (level >= 200) multiply *= 3;
        if (level >= 250) multiply *= 10;
        return multiply;
    }

    public static double ReqExp(int level, int tier) {
        if (level > MaxLevel) {
            return Double.MAX_VALUE;
        }
        if (level <= 1) {
            return reqExp[0];
        }
        double multiply = 1 + tier;
        if (tier < 4) multiply /= 3;
        return reqExp[level - 1] * multiply;
    }


    private final PlayerData playerData;
    private ClassType mainClass = ClassType.Adventurer;

    private final HashMap<ClassType, Integer> level = new HashMap<>();
    private final HashMap<ClassType, Double> exp = new HashMap<>();
    private final HashMap<ClassType, Pallet[]> pallet = new HashMap<>();
    private final HashMap<ClassType, HashMap<String, Integer>> skillLevel = new HashMap<>() {{
        for (ClassType classType : ClassType.values()) {
            put(classType, new HashMap<>());
        }
    }};

    public Classes(PlayerData playerData) {
        this.playerData = playerData;
        playerData.setBaseStatus(mainClass.getBaseStatus());
    }

    public HashMap<ClassType, Integer> getLevelList() {
        return level;
    }

    public int getLevel() {
        return getLevel(mainClass);
    }

    public int getLevel(ClassType classType) {
        if (!level.containsKey(classType)) {
            String[] value = new String[]{playerData.getUUID(), classType.toString()};
            if (SomSQL.exists(DataBase.Table.Classes, key, value, "Level")) {
                level.put(classType, SomSQL.getInt(DataBase.Table.Classes, key, value, "Level"));
            } else {
                level.put(classType, classType.startLevel());
            }
        }
        if (level.get(classType) < classType.startLevel()) {
            level.put(classType, classType.startLevel());
            playerData.attributeMenu().resetAttribute();
            playerData.skillMenu().resetSkill();
        }
        return level.get(classType);
    }

    public void setLevel(ClassType classType, int level) {
        this.level.put(classType, level);
        String[] value = new String[]{playerData.getUUID(), classType.toString()};
        SomSQL.setSql(DataBase.Table.Classes, key, value, "Level", level);
    }

    public void addLevel(ClassType classType, int level) {
        setLevel(classType, getLevel(classType) + level);
        addAttributePoint(PerAttrPoint * level);
        addSkillPoint(PerSkillPoint * level);
        String text = classType.getColorDisplay() + "§aが§eLv" + getLevel(classType) + "§aになりました";
        playerData.sendMessage(text, SomSound.Level);
        for (PlayerData playerData : PlayerData.getPlayerList()) {
            if (playerData != this.playerData && playerData.setting().is(PlayerSetting.BooleanEnum.LevelLog)) {
                playerData.sendMessage(this.playerData.getName() + "§aの" + text);
            }
        }
        playerData.heal();
        if (MaxLevel <= getLevel(classType)) setExp(classType, 0);
        SomSound.Level.play(playerData);
    }

    public double getExp() {
        return getExp(mainClass);
    }

    public double getExp(ClassType classType) {
        if (!exp.containsKey(classType)) {
            String[] value = new String[]{playerData.getUUID(), classType.toString()};
            if (SomSQL.exists(DataBase.Table.Classes, key, value, "Exp")) {
                exp.put(classType, SomSQL.getDouble(DataBase.Table.Classes, key, value, "Exp"));
            } else {
                exp.put(classType, 0.0);
            }
        }
        return exp.get(classType);
    }

    public void setExp(ClassType classType, double exp) {
        this.exp.put(classType, exp);
        String[] value = new String[]{playerData.getUUID(), classType.toString()};
        SomSQL.setSql(DataBase.Table.Classes, key, value, "Exp", exp);
    }

    public void addExp(double exp) {
        addExp(mainClass, exp);
    }

    private double expBuffer = 0;
    private BukkitTask bufferTask;

    public synchronized void addExp(ClassType classType, double exp) {
        expBuffer += exp;
        if (bufferTask == null) {
            bufferTask = SomTask.asyncDelay(() -> {
                double currentExp = getExp(classType) + expBuffer;
                bufferTask = null;
                expBuffer = 0;
                if (getLevel(classType) >= MaxLevel) {
                    double reqExp = ReqExp(getLevel(classType), classType.tier());
                    if (currentExp >= reqExp) {
                        currentExp = reqExp-1;
                    }
                } else {
                    int addLevel = 0;
                    while (currentExp >= ReqExp(getLevel(classType) + addLevel, classType.tier())) {
                        currentExp -= ReqExp(getLevel(classType) + addLevel, classType.tier());
                        addLevel++;
                    }
                    if (addLevel >= 1) {
                        addLevel(classType, addLevel);
                    }
                }
                setExp(classType, currentExp);
            }, 5);
        }
        if (playerData.setting().is(PlayerSetting.BooleanEnum.ExpLog)) {
            playerData.sendMessage("§a[+]" + classType.getColorDisplay() + " §a+" + scale(exp, 2));
        }
    }

    public void removeExp(double exp) {
        removeExp(mainClass, exp);
    }

    public void removeExp(ClassType classType, double exp) {
        double currentExp = getExp(classType) - exp;
        setExp(classType, currentExp);
    }

    public double getExpPercent(ClassType classType) {
        return getExp(classType) / ReqExp(getLevel(classType), classType.tier());
    }

    public double reqExp() {
        return ReqExp(getLevel(), mainClass.tier());
    }

    public String[] value() {
        return new String[]{playerData.getUUID(), mainClass.toString()};
    }

    public int getAttributePoint() {
        return SomSQL.getInt(DataBase.Table.Classes, key, value(), "AttributePoint");
    }

    public void setAttributePoint(int point) {
        SomSQL.setSql(DataBase.Table.Classes, key, value(), "AttributePoint", point);
    }

    public void addAttributePoint(int point) {
        SomSQL.addNumber(DataBase.Table.Classes, key, value(), "AttributePoint", point);
    }

    public void removeAttributePoint(int point) {
        SomSQL.removeNumber(DataBase.Table.Classes, key, value(), "AttributePoint", point);
    }

    public void setAttribute(AttributeType attr, int value) {
        playerData.setBaseAttribute(attr, value);
        SomSQL.setSql(DataBase.Table.Classes, key, value(), attr.toString(), value);
        playerData.statusUpdate();
    }

    public void addAttribute(AttributeType attr, int value) {
        setAttribute(attr, playerData.getBaseAttribute().getOrDefault(attr, 0) + value);
    }

    public HashMap<String, Integer> getSkillLevel() {
        return getSkillLevel(mainClass);
    }

    public int getSkillLevel(String id) {
        return getSkillLevel(mainClass, id);
    }

    public HashMap<String, Integer> getSkillLevel(ClassType classType) {
        return skillLevel.get(classType);
    }

    public int getSkillLevel(ClassType classType, String id) {
        HashMap<String, Integer> skillLevel = this.skillLevel.get(classType);
        if (!skillLevel.containsKey(id)) {
            String[] key = new String[]{"UUID", "ClassType", "Skill"};
            String[] value = new String[]{playerData.getUUID(), classType.toString(), id};
            if (SomSQL.exists(DataBase.Table.PlayerSkill, key, value, "Level")) {
                skillLevel.put(id, SomSQL.getInt(DataBase.Table.PlayerSkill, key, value, "Level"));
            } else {
                skillLevel.put(id, 0);
            }
        }
        return Math.min(skillLevel.get(id), SkillDataLoader.getSkillData(id).getMaxLevel());
    }

    public void setSkillLevel(String id, int level) {
        setSkillLevel(mainClass, id, level);
    }

    public void setSkillLevel(ClassType classType, String id, int level) {
        skillLevel.get(classType).put(id, level);
        String[] key = new String[]{"UUID", "ClassType", "Skill"};
        String[] value = new String[]{playerData.getUUID(), classType.toString(), id};
        SomSQL.setSql(DataBase.Table.PlayerSkill, key, value, "Level", level);
    }

    public void addSkillLevel(String id, int level) {
        addSkillLevel(mainClass, id, level);
    }

    public void addSkillLevel(ClassType classType, String id, int level) {
        setSkillLevel(classType, id, getSkillLevel(classType, id) + level);
    }

    public int getSkillPoint() {
        return SomSQL.getInt(DataBase.Table.Classes, key, value(), "SkillPoint");
    }

    public void setSkillPoint(int point) {
        SomSQL.setSql(DataBase.Table.Classes, key, value(), "SkillPoint", point);
    }

    public void addSkillPoint(int point) {
        SomSQL.addNumber(DataBase.Table.Classes, key, value(), "SkillPoint", point);
    }

    public void removeSkillPoint(int point) {
        SomSQL.removeNumber(DataBase.Table.Classes, key, value(), "SkillPoint", point);
    }

    public int nextNonPallet() {
        Pallet[] pallet = getPallet(mainClass);
        for (int i = 0; i < pallet.length; i++) {
            if (pallet[i] == null) return i;
        }
        return 0;
    }

    public void setPallet(Pallet pallet) {
        setPallet(mainClass, nextNonPallet(), pallet);
    }

    public void setPallet(int slot, Pallet pallet) {
        setPallet(mainClass, slot, pallet);
    }

    public void setPallet(ClassType classType, int slot, Pallet pallet) {
        getPallet(classType)[slot] = pallet;
    }

    public Pallet[] getPallet() {
        return getPallet(mainClass);
    }

    public Pallet getPallet(int slot) {
        return getPallet(mainClass)[slot];
    }

    public Pallet[] getPallet(ClassType classType) {
        if (!pallet.containsKey(classType)) {
            pallet.put(classType, new Pallet[8]);
        }
        return pallet.get(classType);
    }

    public void setMainClass(ClassType classType) {
        mainClass = classType;
        playerData.setBaseStatus(classType.getBaseStatus());
        SomSQL.setSql(DataBase.Table.PlayerData, "UUID", playerData.getUUID(), "MainClass", classType);
        if (!SomSQL.exists(DataBase.Table.Classes, key, value())) setLevel(classType, 1);
        for (AttributeType attr : AttributeType.values()) {
            playerData.setBaseAttribute(attr, SomSQL.getInt(DataBase.Table.Classes, key, value(), attr.toString()));
        }
    }

    public ClassType getMainClass() {
        return mainClass;
    }
}
