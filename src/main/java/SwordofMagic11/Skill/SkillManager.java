package SwordofMagic11.Skill;

import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Component.SomTask;
import SwordofMagic11.DataBase.SkillDataLoader;
import SwordofMagic11.Map.PvPRaid;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Player.Setting.PlayerSetting;
import SwordofMagic11.Skill.Process.Passive.BattleManaRegen;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import static SwordofMagic11.Component.Function.scale;
import static SwordofMagic11.SomCore.Log;

public class SkillManager {


    private final PlayerData owner;
    private BukkitTask task;
    private final HashMap<String, SomSkill> instance = new HashMap<>();
    private final HashMap<String, Double> manaCostTable = new HashMap<>();
    private boolean cast = false;
    private boolean rigid = false;
    private int castProgress = 0;

    public void setManaCost(String id, double manaCost) {
        manaCostTable.put(id, manaCost);
    }

    public void resetManaCost(String id) {
        manaCostTable.remove(id);
    }

    public double manaCostMultiply() {
        double manaCost = 1;
        for (double value : manaCostTable.values()) {
            manaCost += value;
        }
        return manaCost;
    }

    public SkillManager(PlayerData owner) {
        this.owner = owner;
        BattleManaRegen.register(owner);

        SomTask.asyncTimer(() -> {
            for (SomSkill skill : instance.values()) {
                if (skill.isInCoolTime()) {
                    int currentCoolTime = skill.getCurrentCoolTime() - 5;
                    if (currentCoolTime > 0) {
                        skill.setCurrentCoolTime(currentCoolTime);
                    } else {
                        skill.setCurrentCoolTime(0);
                        skill.resetStack();
                    }
                }
            }
        }, 5, owner);
    }

    public SomSkill instance(String id) {
        if (!instance.containsKey(id)) {
            try {
                SkillData skillData = SkillDataLoader.getSkillData(id);
                Class<?> skillClass = Class.forName("SwordofMagic11.Skill.Process." + id);
                Constructor<?> constructor = skillClass.getConstructor(PlayerData.class, SkillData.class);
                SomSkill skill = (SomSkill) constructor.newInstance(owner, skillData);
                skill.resetStack();
                instance.put(id, skill);
            } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException |
                     InstantiationException | IllegalAccessException e) {
                Log("§cSkillInstanceError: " + id);
                throw new RuntimeException(e);
            }
        }
        return instance.get(id);
    }

    public boolean isCastable() {
        return !cast && !rigid;
    }

    public void cast(String id) {
        if (owner.isDeath() || owner.isAFK()) return;
        boolean log = owner.setting().is(PlayerSetting.BooleanEnum.SkillMessage);
        if (!owner.classes().getMainClass().getSkill().contains(id)) {
            sendMessage("§c別クラスのスキルです", SomSound.Nope);
            return;
        }
        if (!PvPRaid.isInPvPRaid(owner) && !owner.hasSkill(id)) {
            sendMessage("§c未所持スキルです", SomSound.Nope);
            return;
        }
        if (cast) {
            if (log) sendMessage("§c詠唱中です", SomSound.Nope);
            return;
        }
        if (rigid) {
            if (log) sendMessage("§c硬直中です", SomSound.Nope);
            return;
        }
        if (owner.isSilence()) {
            sendMessage("§c沈黙状態です", SomSound.Nope);
            return;
        }
        SomSkill skill = instance(id);
        double manaCost = owner.hasEffect("ZeroCost") ? 0 : skill.getManaCost() * manaCostMultiply();
        if (!skill.hasStack()) {
            if (log) sendMessage("§cクールタイム中です", SomSound.Nope);
            return;
        }
        if (owner.getMana() < manaCost) {
            if (log) owner.sendNonMana(skill);
            return;
        }

        cast = true;
        owner.getNamePlate().update();
        owner.removeMana(manaCost);
        skill.useStack();
        if (!skill.isInCoolTime()) skill.inCoolTime();
        if (task != null) task.cancel();
        task = SomTask.asyncTimer(() -> {
            try {
                if (skill.cast() && !owner.isSilence()) {
                    if (castProgress >= skill.getCastTime()) {
                        task.cancel();
                        active(skill);
                        cast = false;
                        owner.getNamePlate().update();
                    }
                    if (skill.getCastTime() > 0) {
                        owner.sendTitle("", "§b" + scale((double) castProgress / skill.getCastTime() * 100, 2) + "%", 0, 1, 10);
                    }
                    castProgress++;
                } else {
                    task.cancel();
                    sendMessage("§c詠唱が無効化されました", SomSound.Nope);
                    reset();
                }
            } catch (Exception e) {
                task.cancel();
                sendMessage("§c詠唱中にエラーが発生しました", SomSound.Nope);
                reset();
                throw new RuntimeException(e);
            }
        }, 1, owner);
    }

    public void active(SomSkill skill) {
        try {
            if (skill.active() && !owner.isSilence()) {
                rigid = true;
                SomTask.asyncDelay(this::reset, skill.getRigidTime());
            } else {
                sendMessage("§cスキルが無効化されました", SomSound.Nope);
            }
        } catch (Exception e) {
            sendSkillBreakMessage();
            reset();
            throw new RuntimeException(e);
        }
    }

    public void reset() {
        cast = false;
        rigid = false;
        castProgress = 0;
    }

    public void sendMessage(String message, SomSound sound) {
        owner.sendMessage(message, sound);
    }

    public void sendSkillBreakMessage() {
        sendMessage("§cスキルが無効化されました", SomSound.Nope);
    }

    public boolean isActiveAble() {
        if (owner.isSilence()) {
            sendSkillBreakMessage();
            return false;
        }
        return true;
    }

    public boolean isCasting() {
        return cast;
    }
}
