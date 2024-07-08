package SwordofMagic11.Player.UserMenu;

import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.DataBase.SkillDataLoader;
import SwordofMagic11.Player.Classes;
import SwordofMagic11.Player.Menu.GUIManager;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Skill.SkillData;
import SwordofMagic11.Skill.SomSkill;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import static SwordofMagic11.Component.Function.scale;
import static SwordofMagic11.DataBase.DataBase.ResetCost;
import static SwordofMagic11.Player.Classes.MaxLevel;
import static SwordofMagic11.Player.Classes.PerSkillPoint;

public class SkillMenu extends GUIManager {

    public SkillMenu(PlayerData playerData) {
        super(playerData, "スキルメニュー", 6);
    }

    public CustomItemStack icon() {
        CustomItemStack item = new CustomItemStack(Material.END_CRYSTAL);
        item.setDisplay("スキルメニュー");
        item.addLore("§aスキルポイントの振り分けが行えます");
        item.addLore("§aポイントはレベル毎に§e" + PerSkillPoint + "§a獲得します");
        item.addLore("§aスキルの§e発動設定§aは§eパレット§aから行えます");
        item.setCustomData("Menu", "SkillMenu");
        return item;
    }

    public CustomItemStack pointIcon() {
        CustomItemStack item = new CustomItemStack(Material.EXPERIENCE_BOTTLE);
        item.setNonDecoDisplay("§e" + playerData.classes().getSkillPoint() + "P");
        item.addLore("§c※経験値" + scale(ResetCost * 100, 1) + "%を消費してリセットできます");
        item.setCustomData("ResetSkill", true);
        return item;
    }

    @Override
    public void updateContainer() {
        int slot = 0;
        for (String id : playerData.classes().getMainClass().getSkill()) {
            SomSkill skill = playerData.skillManager().instance(id);
            setItem(slot, skill.viewItem(true).setAmountReturn(playerData.getSkillLevel(skill.getId())));
            slot++;
        }
        for (String id : playerData.classes().getMainClass().getPassive()) {
            SkillData skillData = SkillDataLoader.getSkillData(id);
            int level = playerData.classes().getSkillLevel(id);
            setItem(slot, skillData.viewItem(playerData, 0, level, true).setAmountReturn(level));
            slot++;
        }
        setItem(53, pointIcon());
    }

    @Override
    public void topClick(int slot, ItemStack clickedItem, ClickType clickType) {
        Classes classes = playerData.classes();
        if (CustomItemStack.hasCustomData(clickedItem, "SkillData")) {
            SkillData skillData = SkillDataLoader.getSkillData(CustomItemStack.getCustomData(clickedItem, "SkillData"));
            int usePoint = 1;
            int current = playerData.classes().getSkillLevel(skillData.getId());
            int maxLevel = skillData.getMaxLevel();
            if (clickType.isShiftClick()) usePoint = maxLevel - current;
            usePoint(skillData, usePoint);
        }
        if (CustomItemStack.hasCustomData(clickedItem, "ResetSkill")) {
            int usedPoint = 0;
            for (Integer value : classes.getSkillLevel().values()) {
                usedPoint += value;
            }
            if (usedPoint > 0) {
                double reqExp = playerData.getLevel() >= MaxLevel ? 0 : classes.reqExp() * ResetCost;
                if (reqExp <= classes.getExp()) {
                    classes.removeExp(reqExp);
                    resetSkill();
                    playerData.sendMessage("§eスキルポイント§aを§cリセット§aしました", SomSound.Tick);
                    update();
                } else {
                    playerData.sendMessage("§e経験値§aが足りません", SomSound.Nope);
                }
            }
        }
    }

    @Override
    public void bottomClick(int slot, ItemStack clickedItem, ClickType clickType) {

    }

    @Override
    public void close() {

    }

    public void usePoint(SkillData skillData, int usePoint) {
        Classes classes = playerData.classes();
        if (classes.getSkillPoint() >= usePoint) {
            if (skillData.isReqLevel(playerData.classes())) {
                if (skillData.getMaxLevel() > playerData.classes().getSkillLevel(skillData.getId())) {
                    playerData.classes().addSkillLevel(skillData.getId(), usePoint);
                    classes.removeSkillPoint(usePoint);
                    SomSound.Tick.play(playerData);
                    update();
                }
            } else {
                playerData.sendMessage("§c解放条件を満たしていません", SomSound.Nope);
            }
        } else {
            playerData.sendMessage("§cポイントが足りません", SomSound.Nope);
        }
    }

    public void resetSkill() {
        playerData.classes().setSkillPoint((playerData.getLevel() - 1) * Classes.PerSkillPoint);
        for (String skill : playerData.classes().getSkillLevel().keySet()) {
            playerData.classes().setSkillLevel(skill, 0);
        }
        playerData.silence(60, playerData);
    }
}
