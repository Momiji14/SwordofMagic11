package SwordofMagic11.Skill;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;

public class Parameter implements Cloneable {

    private double manaCost = 0;
    private int castTime = 0;
    private int rigidTime = 0;
    private int coolTime = 0;
    private HashMap<ParamType, Double> parameter = new HashMap<>();

    public static Parameter fromFile(FileConfiguration data) {
        return fromFile(data, "");
    }

    public static Parameter fromFile(FileConfiguration data, String prefix) {
        Parameter parameter = new Parameter();
        parameter.setManaCost(data.getDouble(prefix + "ManaCost"));
        parameter.setCastTime(data.getInt(prefix + "CastTime"));
        parameter.setRigidTime(data.getInt(prefix + "RigidTime"));
        parameter.setCoolTime(data.getInt(prefix + "CoolTime"));
        for (ParamType paramType : ParamType.values()) {
            String key = prefix + "Parameter." + paramType;
            if (data.isSet(key)) {
                parameter.setParam(paramType, data.getDouble(key));
            }
        }
        return parameter;
    }

    private Parameter() {
    }

    public void increase(Parameter parameter) {
        manaCost += parameter.manaCost;
        castTime += parameter.castTime;
        rigidTime += parameter.rigidTime;
        coolTime += parameter.coolTime;
        parameter.parameter.forEach((paramType, value) -> this.parameter.merge(paramType, value, Double::sum));
    }

    public double getManaCost(int level) {
        return manaCost * (1 + (level - 1) * 0.04);
    }

    public void setManaCost(double manaCost) {
        this.manaCost = manaCost;
    }

    public int getCastTime() {
        return castTime;
    }

    public void setCastTime(int castTime) {
        this.castTime = castTime;
    }

    public int getRigidTime() {
        return rigidTime;
    }

    public void setRigidTime(int rigidTime) {
        this.rigidTime = rigidTime;
    }

    public int getCoolTime() {
        return coolTime;
    }

    public void setCoolTime(int coolTime) {
        this.coolTime = coolTime;
    }

    public void setParam(ParamType param, double value) {
        parameter.put(param, value);
    }

    public double getParam(ParamType param) {
        return parameter.getOrDefault(param, 0.0);
    }

    public int getParamInt(ParamType param) {
        return (int) ((double) parameter.getOrDefault(param, 0.0));
    }

    public HashMap<ParamType, Double> getParam() {
        return parameter;
    }

    public boolean hasParam(ParamType param) {
        return parameter.containsKey(param);
    }

    @Override
    public Parameter clone() {
        try {
            Parameter clone = (Parameter) super.clone();
            clone.parameter = new HashMap<>(parameter);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
