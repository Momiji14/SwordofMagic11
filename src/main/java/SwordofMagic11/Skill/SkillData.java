package SwordofMagic11.Skill;

import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.DataBase.SkillDataLoader;
import SwordofMagic11.Player.Classes;
import SwordofMagic11.Player.PlayerData;
import org.bukkit.Color;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static SwordofMagic11.Component.Function.*;

public class SkillData {
    private String id;
    private String display;
    private Material icon;
    private Color color;
    private List<String> lore;
    private List<String> addition = new ArrayList<>();
    private boolean isPassive = false;
    private int maxLevel = 1;
    private int maxStack = 1;
    private Parameter[] parameter;
    private final HashMap<String, Integer> reqLevel = new HashMap<>();

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

    public void setLore(List<String> lore) {
        this.lore = lore;
    }

    public List<String> getLore() {
        return lore;
    }

    public void setAddition(List<String> addition) {
        this.addition = addition;
    }

    public void setPassive(boolean passive) {
        isPassive = passive;
    }

    public boolean isPassive() {
        return isPassive;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
        parameter = new Parameter[maxLevel];
    }

    public int getMaxStack() {
        return maxStack;
    }

    public void setMaxStack(int maxStack) {
        this.maxStack = maxStack;
    }

    public Parameter getParam(PlayerData owner, int level) {
        if (level < 1) return parameter[0];
        Parameter param = parameter[level - 1].clone();
        for (String id : addition) {
            if (owner.hasSkill(id)) {
                SkillData skillData = SkillDataLoader.getSkillData(id);
                Parameter additionParam = skillData.getParam(owner, owner.getSkillLevel(id));
                param.increase(additionParam);
            }
        }
        return param;
    }

    public void setParam(int level, Parameter parameter) {
        this.parameter[level - 1] = parameter;
    }

    public HashMap<String, Integer> getReqLevel() {
        return reqLevel;
    }

    public void setReqLevel(String id, int level) {
        reqLevel.put(id, level);
    }

    public boolean isReqLevel(Classes classes) {
        for (Map.Entry<String, Integer> entry : reqLevel.entrySet()) {
            String id = entry.getKey();
            int level = entry.getValue();
            if (classes.getSkillLevel(id) < level) {
                return false;
            }
        }
        return true;
    }

    public CustomItemStack viewItem(PlayerData owner, int stack, int level, boolean next) {
        CustomItemStack item = new CustomItemStack(icon);
        CustomItemStack.setupColor(item, color);
        Parameter param = getParam(owner, level);
        if (level == maxLevel) next = false;
        Parameter nextParam = next ? getParam(owner, level + 1) : null;
        item.setDisplay(display + " [" + level + "/" + maxLevel + "]");
        item.addLore(loreText(lore, param));
        if (next) {
            item.addSeparator("スキル説明 §eLv" + (level + 1));
            item.addLore(loreText(lore, nextParam));
        }
        List<String> skillInfo = new ArrayList<>();
        double manaCost = param.getManaCost(owner.getLevel());
        if (maxStack > 0) skillInfo.add(decoLore("スタック") + stack + "/" + maxStack);
        if (manaCost != 0) skillInfo.add(decoLore("消費マナ") + scale(manaCost, 1) + (next ? " -> " + scale(nextParam.getManaCost(owner.getLevel()), 1) : ""));
        if (param.getCastTime() != 0) skillInfo.add(decoLore("詠唱時間") + timeString(param.getCastTime()) + (next ? " -> " + timeString(nextParam.getCastTime()) : ""));
        if (param.getRigidTime() != 0) skillInfo.add(decoLore("硬直時間") + timeString(param.getRigidTime()) + (next ? " -> " + timeString(nextParam.getRigidTime()) : ""));
        if (param.getCoolTime() != 0) skillInfo.add(decoLore("再使用時間") + timeString(param.getCoolTime()) + (next ? " -> " + timeString(nextParam.getCoolTime()) : ""));
        if (!skillInfo.isEmpty()) {
            item.addSeparator("スキル情報");
            item.addLore(skillInfo);
        }
        if (!reqLevel.isEmpty()) {
            item.addSeparator("解放条件");
            reqLevel.forEach((id, reqLevel) -> item.addLore("§7・§e" + SkillDataLoader.getSkillData(id).getDisplay() + " Lv" + reqLevel));
        }
        item.setCustomData("SkillData", id);
        item.setAmount(maxStack);
        return item;
    }

    public static String timeString(double value) {
        return scale(value / 20.0, -1) + "秒";
    }

    public static String paramText(Parameter param, ParamType paramType) {
        return scale(param.getParam(paramType) * paramType.getViewMultiply(), paramType.getScale(), paramType.isPrefix()) + paramType.getSuffix();
    }
}
