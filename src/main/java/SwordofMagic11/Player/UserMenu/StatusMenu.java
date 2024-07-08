package SwordofMagic11.Player.UserMenu;

import SwordofMagic11.AttributeType;
import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.DataBase.AchievementDataLoader;
import SwordofMagic11.DataBase.MemorialDataLoader;
import SwordofMagic11.Item.SomEquip;
import SwordofMagic11.Player.ClassType;
import SwordofMagic11.Player.Classes;
import SwordofMagic11.Player.Gathering.GatheringMenu;
import SwordofMagic11.Player.Menu.GUIManager;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Player.PlayerVote;
import SwordofMagic11.StatusType;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

import static SwordofMagic11.Component.Function.decoLore;
import static SwordofMagic11.Component.Function.scale;

public class StatusMenu extends GUIManager {

    private PlayerData targetData;

    public StatusMenu(PlayerData playerData) {
        super(playerData, "プレイヤー情報", 2);
    }

    public void open(PlayerData targetData) {
        this.targetData = targetData;
        super.open();
    }

    @Override
    public void updateContainer() {
        setItem(0, statusIcon(targetData));
        setItem(1, gatheringIcon(targetData));
        setItem(2, classesIcon(targetData));
        setItem(3, targetData.memorialMenu().icon().deleteCustomData("Menu"));
        setItem(4, targetData.statistics().viewItemInt());
        setItem(5, targetData.statistics().viewItemDouble());
        setItem(6, targetData.statistics().viewItemEnemyKill());
        int i = 9;
        for (SomEquip.Slot slot : SomEquip.Slot.values()) {
            setItem(i, viewEquip(slot));
            i++;
        }
    }

    @Override
    public void topClick(int slot, ItemStack clickedItem, ClickType clickType) {

    }

    @Override
    public void bottomClick(int slot, ItemStack clickedItem, ClickType clickType) {

    }

    @Override
    public void close() {

    }

    public CustomItemStack viewEquip(SomEquip.Slot slot) {
        SomEquip equip = targetData.getEquipment().get(slot);
        return equip != null ? equip.viewItem() : new CustomItemStack(Material.BARRIER).setNonDecoDisplay("§c" + slot.getDisplay());
    }

    public static CustomItemStack statusIcon(PlayerData playerData) {
        CustomItemStack item = new CustomItemStack(Material.PLAYER_HEAD).setDisplay("プレイヤー情報");
        item.setSkull(playerData);
        Classes classes = playerData.classes();
        ClassType mainClass = classes.getMainClass();
        item.addLore(decoLore("メル") + playerData.getMel());
        //item.addLore(decoLore("マップ") + playerData.getMap().getDisplay());
        item.addLore(decoLore("クラス") + mainClass.getColorDisplay() + " §eLv" + classes.getLevel(mainClass));
        item.addLore(decoLore("総討伐数") + playerData.statistics().totalEnemyKill() + "体");
        item.addLore(decoLore("称号実績") + playerData.achievementMenu().count() + "/" + AchievementDataLoader.size());
        item.addLore(decoLore("投票") + PlayerVote.get(playerData.getName()) + "/" + PlayerVote.total(playerData.getName()));
        item.addSeparator("メモリアル");
        item.addLore(decoLore("獲得種類") + playerData.memorialMenu().count() + "/" + MemorialDataLoader.size());
        item.addLore(decoLore("ポイント") + playerData.getMemorialPoint());
        item.addSeparator("§d寄付特典");
        item.addLore(decoLore("合計寄付") + playerData.donation().getTotalDonation());
        item.addLore(decoLore("エルド") + playerData.donation().getEld());
        item.addLore(decoLore("インスタンスブースター") + playerData.donation().getInsBoostText());
        item.addLore(decoLore("プライベートインスタンス") + playerData.donation().getPrivateInsText());
        item.addSeparator("ステータス");
        HashMap<StatusType, Double> multiply = playerData.getTotalCapsuleStatus();
        for (StatusType statusType : StatusType.values()) {
            String addition = multiply.containsKey(statusType) ? (" (" + scale(multiply.get(statusType) * 100, -1, true) + "%)") : "";
            item.addLore(decoLore(statusType.getDisplay()) + scale(playerData.getStatus(statusType), 1) + addition);
        }
        item.addSeparator("アトリビュート");
        for (AttributeType attr : AttributeType.values()) {
            item.addLore(decoLore(attr.getDisplay()) + playerData.getBaseAttribute().getOrDefault(attr, 0));
        }
        return item;
    }

    public static CustomItemStack gatheringIcon(PlayerData playerData) {
        GatheringMenu gathering = playerData.gatheringMenu();
        CustomItemStack item = new CustomItemStack(Material.IRON_PICKAXE).setDisplay("ギャザリング情報");
        for (GatheringMenu.Type type : GatheringMenu.Type.values()) {
            item.addLore("§7・§e" + type.getDisplay() + " Lv" + gathering.getLevel(type) + " §a" + scale(gathering.getExpPercent(type) * 100, 2) + "%");
        }
        for (GatheringMenu.Type type : GatheringMenu.Type.values()) {
            item.addSeparator(type.getDisplay() + "スキル");
            for (GatheringMenu.Skill skill : type.getSkill()) {
                item.addLore("§7・§e" + skill.getDisplay() + " Lv" + gathering.getSkillLevel(type, skill));
            }
        }
        return item;
    }

    public static CustomItemStack classesIcon(PlayerData playerData) {
        Classes classes = playerData.classes();
        CustomItemStack item = new CustomItemStack(Material.END_CRYSTAL).setDisplay("クラス情報");
        classes.getLevelList().forEach((classType, level) -> item.addLore(classType.getColorDisplay() + " §eLv" + level + " §a" + scale(classes.getExpPercent(classType) * 100, 2) + "%"));
        return item;
    }
}
