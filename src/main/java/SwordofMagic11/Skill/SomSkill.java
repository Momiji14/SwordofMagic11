package SwordofMagic11.Skill;

import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.Entity.SomEntity;
import SwordofMagic11.Map.PvPRaid;
import SwordofMagic11.Player.Pallet;
import SwordofMagic11.Player.PlayerData;

public abstract class SomSkill implements Pallet {

    protected final PlayerData owner;
    protected final SkillData skillData;
    protected int stack = 0;
    protected int currentCoolTime = 0;

    public SomSkill(PlayerData owner, SkillData skillData) {
        this.owner = owner;
        this.skillData = skillData;
    }

    @Override
    public String getId() {
        return skillData.getId();
    }

    @Override
    public synchronized void use(PlayerData playerData) {
        playerData.skillManager().cast(skillData.getId());
    }

    public void instantCast(PlayerData playerData) {
        double manaCost = owner.hasEffect("ZeroCost") ? 0 : getManaCost() * playerData.skillManager().manaCostMultiply();
        if (playerData.getMana() >= manaCost) {
            playerData.removeMana(manaCost);
            useStack();
            active();
            inCoolTime();
        } else {
            owner.sendNonMana(this);
        }
    }

    public boolean cast() {
        return true;
    }

    public abstract boolean active();

    public SomEntity getOwner() {
        return owner;
    }

    public SkillData getSkillData() {
        return skillData;
    }

    public int getLevel() {
        return PvPRaid.isInPvPRaid(owner) ? skillData.getMaxLevel() : owner.classes().getSkillLevel(skillData.getId());
    }

    public int getStack() {
        return stack;
    }

    public void setStack(int stack) {
        this.stack = stack;
    }

    public double getManaCost() {
        return getManaCost(getLevel());
    }

    public double getManaCost(int level) {
        return skillData.getParam(owner, level).getManaCost(PvPRaid.isInPvPRaid(owner) ? 200 : owner.getLevel());
    }

    public int getCastTime() {
        return getCastTime(getLevel());
    }

    public int getCastTime(int level) {
        return skillData.getParam(owner, level).getCastTime();
    }

    public int getRigidTime() {
        return getRigidTime(getLevel());
    }

    public int getRigidTime(int level) {
        return skillData.getParam(owner, level).getRigidTime();
    }

    public int getCoolTime() {
        return getCoolTime(getLevel());
    }

    public int getCoolTime(int level) {
        return skillData.getParam(owner, level).getCoolTime();
    }

    public void useStack() {
        stack--;
        if (stack < 0) stack = 0;
    }

    public void resetStack() {
        setStack(skillData.getMaxStack());
    }

    public boolean hasStack() {
        return stack > 0;
    }

    public boolean isInCoolTime() {
        return currentCoolTime > 0;
    }

    public void inCoolTime() {
        setCurrentCoolTime(getCoolTime());
    }

    public int getCurrentCoolTime() {
        return currentCoolTime;
    }

    public void setCurrentCoolTime(int currentCoolTime) {
        this.currentCoolTime = currentCoolTime;
    }

    public Parameter getParam() {
        return skillData.getParam(owner, getLevel());
    }

    @Override
    public CustomItemStack viewItem() {
        return viewItem(false);
    }

    public CustomItemStack viewItem(boolean next) {
        CustomItemStack item = skillData.viewItem(owner, stack, getLevel(), next);
        item.setCustomData("SomSkill", skillData.getId());
        return item;
    }
}
