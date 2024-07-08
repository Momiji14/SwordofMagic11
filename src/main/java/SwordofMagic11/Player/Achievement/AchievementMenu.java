package SwordofMagic11.Player.Achievement;

import SwordofMagic11.Component.SomSQL;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Component.SomTask;
import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.DataBase.AchievementDataLoader;
import SwordofMagic11.DataBase.DataBase;
import SwordofMagic11.Player.Menu.GUIManager;
import SwordofMagic11.Player.PlayerData;
import com.github.jasync.sql.db.RowData;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Set;

import static SwordofMagic11.Component.Function.*;
import static SwordofMagic11.DataBase.DataBase.DownScroll;
import static SwordofMagic11.DataBase.DataBase.UpScroll;

public class AchievementMenu extends GUIManager {

    public CustomItemStack icon() {
        CustomItemStack item = new CustomItemStack(Material.OAK_SIGN);
        item.setDisplay("称号・実績メニュー");
        item.addLore("§e称号§aの設定および§e実績§aを確認できます");
        item.setCustomData("Menu", "AchievementMenu");
        return item;
    }

    private final Set<String> achievement = new HashSet<>();
    private AchievementData selectAchievement;
    private String selectAchievementText;
    private int flame;
    private int tick;
    private int nextTick;
    private BukkitTask task;
    private int scroll;

    public AchievementMenu(PlayerData playerData) {
        super(playerData, "称号・実績メニュー", 6);

        if (SomSQL.exists(DataBase.Table.PlayerData, "UUID", playerData.getUUID(), "SelectAchievement")) {
            String select = SomSQL.getString(DataBase.Table.PlayerData, "UUID", playerData.getUUID(), "SelectAchievement");
            if (!select.isEmpty()) {
                selectAchievement = AchievementDataLoader.getAchievementData(select);
            }
        }

        for (RowData objects : SomSQL.getSqlList(DataBase.Table.PlayerAchievement, "UUID", playerData.getUUID(), "*")) {
            String id = objects.getString("Achievement");
            if (AchievementDataLoader.getComplete().contains(id)) {
                achievement.add(id);
            }
        }

        if (hasSelectAchievement()) viewer();
    }

    public void viewer() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        AchievementData achievement = getSelectAchievement();
        if (achievement.getLine().size() > 1) {
            task = SomTask.asyncTimer(() -> {
                if (achievement.getLine().size() <= flame) flame = 0;
                AchievementData.Line line = achievement.getLine().get(flame);
                if (nextTick <= tick) {
                    selectAchievementText = line.text() + "§r";
                    playerData.getNamePlate().updateView();
                    flame++;
                    nextTick = line.tick();
                    tick = 0;
                } else {
                    tick++;
                }
            }, 1, 20, playerData);
        } else {
            selectAchievementText = achievement.getLine().get(0).text() + "§r";
        }
    }

    private final String[] key = new String[]{"UUID", "Achievement"};

    private String[] value(String id) {
        return new String[]{playerData.getUUID(), id};
    }

    public void set(AchievementData achievement, boolean bool) {
        set(achievement.getId(), bool);
    }

    public void set(String id, boolean bool) {
        if (bool) {
            achievement.add(id);
            SomSQL.setSql(DataBase.Table.PlayerAchievement, "UUID", playerData.getUUID(), "Achievement", id);
        } else {
            achievement.remove(id);
            SomSQL.delete(DataBase.Table.PlayerAchievement, key, value(id));
        }
    }

    public boolean has(AchievementData achievement) {
        return has(achievement.getId());
    }

    public boolean has(String id) {
        return achievement.contains(id);
    }

    public int count() {
        return achievement.size();
    }

    public void setSelectAchievement(AchievementData selectAchievement) {
        this.selectAchievement = selectAchievement;
        SomSQL.setSql(DataBase.Table.PlayerData, "UUID", playerData.getUUID(), "SelectAchievement", selectAchievement.getId());
        viewer();
    }

    public void resetSelectAchievement() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        selectAchievement = null;
        selectAchievementText = null;
    }

    public boolean hasSelectAchievement() {
        return selectAchievement != null;
    }

    public AchievementData getSelectAchievement() {
        return selectAchievement;
    }

    public String getSelectAchievementText() {
        return selectAchievementText != null ? selectAchievement.replace(playerData, selectAchievementText) : "";
    }

    @Override
    public void open() {
        for (AchievementData achievement : AchievementDataLoader.getAchievementDataList()) {
            if (!has(achievement) && achievement.predicate(playerData)) {
                set(achievement, true);
                playerData.sendMessage("§e" + achievement.getDisplay() + "§aを§e獲得§aしました", SomSound.Tick);
            }
        }
        super.open();
    }

    @Override
    public void updateContainer() {
        int slot = 0;
        int index = 0;
        for (AchievementData achievement : AchievementDataLoader.getAchievementDataList()) {
            if (index >= scroll * 8) {
                CustomItemStack item = achievement.viewItem(playerData);
                if (has(achievement)) item.setGlowing(true);
                setItem(slot, item);
                slot++;
                if (isInvalidSlot(slot)) slot++;
                if (slot >= 53) break;
            }
            index++;
        }
        for (int i = 17; i < 45; i += 9) {
            setItem(i, ItemFlame);
        }
        setItem(8, UpScroll());
        setItem(53, DownScroll());
    }

    @Override
    public void topClick(int slot, ItemStack clickedItem, ClickType clickType) {
        if (CustomItemStack.hasCustomData(clickedItem, "UpScroll")) {
            scroll = scrollUp(scroll);
            update();
        } else if (CustomItemStack.hasCustomData(clickedItem, "DownScroll")) {
            scroll = scrollDown(AchievementDataLoader.getComplete().size(), 6, scroll);
            update();
        } else if (CustomItemStack.hasCustomData(clickedItem, "Achievement")) {
            String id = CustomItemStack.getCustomData(clickedItem, "Achievement");
            AchievementData achievement = AchievementDataLoader.getAchievementData(id);
            if (selectAchievement != achievement) {
                if (has(achievement)) {
                    setSelectAchievement(achievement);
                    playerData.sendMessage("§e" + achievement.getDisplay() + "§aを§e表示§aします", SomSound.Tick);
                } else {
                    playerData.sendMessage("§e" + achievement.getDisplay() + "§aを§e獲得§aしていません", SomSound.Nope);
                }
            } else {
                resetSelectAchievement();
                playerData.sendMessage("§e称号表示§aを外しました", SomSound.Tick);
            }
        }
    }

    @Override
    public void bottomClick(int slot, ItemStack clickedItem, ClickType clickType) {

    }

    @Override
    public void close() {

    }
}
