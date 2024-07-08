package SwordofMagic11.Player.Gathering;

import SwordofMagic11.Component.SomSQL;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Component.SomTask;
import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.DataBase.CraftDataLoader;
import SwordofMagic11.DataBase.DataBase;
import SwordofMagic11.Item.Material.MaterialData;
import SwordofMagic11.Item.SomEquip;
import SwordofMagic11.Player.Menu.GUIManager;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Player.Statistics.Statistics;
import SwordofMagic11.StatusType;
import com.github.jasync.sql.db.RowData;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static SwordofMagic11.Component.Function.*;
import static SwordofMagic11.DataBase.DataBase.DownScroll;
import static SwordofMagic11.DataBase.DataBase.UpScroll;

public class CraftMenu extends GUIManager {

    public static int OfflineMaxTime = 8;

    private final CustomItemStack lockSlot = new CustomItemStack(Material.BARRIER).setNonDecoDisplay("§c未開放スロット");
    private final HashMap<CraftData, Process> process = new HashMap<>();
    private final int rawSlot = 3;
    private int scroll = 0;
    private int amount = 0;
    private SomEquip craftTool;

    private int amount() {
        return (int) Math.pow(10, amount);
    }

    private CustomItemStack amountIcon() {
        CustomItemStack item = new CustomItemStack(Material.GOLD_NUGGET);
        item.setNonDecoDisplay(decoLore("制作個数") + amount());
        item.setAmountReturn(amount);
        item.setCustomData("AmountIcon", true);
        return item;
    }

    public static int time(double cost, PlayerData playerData) {
        return ceil(cost / playerData.getStatus(StatusType.GatheringPower) * 100 * playerData.gatheringMenu().getSkillValue(GatheringMenu.Type.Craft, GatheringMenu.Skill.TimeSave));
    }

    public CraftMenu(PlayerData playerData) {
        super(playerData, "制作メニュー", 6);

        for (RowData objects : SomSQL.getSqlList(DataBase.Table.PlayerCraft, "UUID", playerData.getUUID(), "*")) {
            CraftData craftData = CraftDataLoader.getCraftData(objects.getString("CraftData"));
            int time = objects.getInt("Time");
            int amount = objects.getInt("Amount");
            int resetTime = objects.getInt("ResetTime");
            process.put(craftData, new Process(craftData, amount, time, resetTime));
        }

        if (!process.isEmpty()) {
            Duration difference = Duration.between(playerData.lastOnlineTime(), LocalDateTime.now());
            SomTask.asyncDelay(() -> {
                if (difference.toMinutes() > 0) {
                    long second = Math.min(difference.toSeconds(), 3600L * (OfflineMaxTime + playerData.gatheringMenu().getSkillLevel(GatheringMenu.Type.Craft, GatheringMenu.Skill.OfflineMaxTime)));
                    playerData.sendMessage("§bオフライン時間分(" + (second / 60) + "分)§aの§e制作§aを§e再生§aしています\n§c※完了前にログアウトすると失います", SomSound.Tick);
                    SomTask.async(() -> {
                        for (int i = 0; i < second; i++) {
                            processTick(false);
                        }
                        playerData.sendMessage("§e制作再生§aが§b完了§aしました", SomSound.Tick);
                        craftTimer();
                    });
                } else {
                    craftTimer();
                }
            }, 50);
        } else {
            craftTimer();
        }
    }

    public void processTick(boolean save) {
        process.values().removeIf(process -> process.tick(save));
    }

    private void craftTimer() {
        SomTask.asyncTimer(() -> {
            processTick(true);
            if (playerData.getPlayer().getOpenInventory().getTopInventory().equals(inventory)) {
                updateSlot();
                fastUpdate();
            }
        }, 20, playerData);
    }

    private final String[] key = new String[]{"UUID", "CraftData"};

    private String[] value(CraftData craftData) {
        return new String[]{playerData.getUUID(), craftData.id()};
    }

    public int slot() {
        return rawSlot + (int) playerData.gatheringMenu().getSkillValue(GatheringMenu.Type.Craft, GatheringMenu.Skill.SlotUp);
    }

    public boolean isRawEmpty() {
        return process.size() <= rawSlot;
    }

    public void setCraftTool(SomEquip craftTool) {
        this.craftTool = craftTool;
    }

    public void updateSlot() {
        int slot = 0;
        for (Process process : process.values()) {
            setItem(slot, process.viewItem());
            slot++;
        }
        for (; slot < slot(); slot++) {
            setItem(slot, null);
        }
    }

    @Override
    public void open() {
        process.values().forEach(process -> process.update(playerData));
        super.open();
    }

    @Override
    public void updateContainer() {
        int slot = slot();
        for (; slot < 9; slot++) {
            setItem(slot, lockSlot);
        }
        for (int i = 9; i < 18; i++) {
            setItem(i, ItemFlame);
        }
        slot = 18;
        int index = 0;
        for (CraftData craftData : CraftDataLoader.getCraftDataList()) {
            if (index >= scroll * 8) {
                setItem(slot, craftData.viewItem(playerData));
                slot++;
                if (isInvalidSlot(slot)) slot++;
                if (slot >= 53) break;
            }
            index++;
        }
        setItem(26, UpScroll());
        setItem(35, amountIcon());
        setItem(44, ItemFlame);
        setItem(53, DownScroll());
        updateSlot();
    }

    @Override
    public void topClick(int slot, ItemStack clickedItem, ClickType clickType) {
        if (CustomItemStack.hasCustomData(clickedItem, "AmountIcon")) {
            amount++;
            if (amount >= 4) amount = 0;
            SomSound.Tick.play(playerData);
            update();
        } else if (CustomItemStack.hasCustomData(clickedItem, "UpScroll")) {
            scroll = scrollUp(scroll);
            update();
        } else if (CustomItemStack.hasCustomData(clickedItem, "DownScroll")) {
            scroll = scrollDown(CraftDataLoader.getCraftDataList().size(), 4, scroll);
            update();
        } else if (CustomItemStack.hasCustomData(clickedItem, "CraftData")) {
            CraftData craftData = CraftDataLoader.getCraftData(CustomItemStack.getCustomData(clickedItem, "CraftData"));
            if (clickType == ClickType.DROP) {
                for (CraftData.Pack recipe : craftData.recipe()) {
                    playerData.sideBar().amountMaterial(recipe.material(), recipe.amount());
                }
                return;
            }
            int time = time(craftData.cost(), playerData);
            int amount = amount();
            List<String> message = new ArrayList<>();
            message.add(decoText(craftData.result().getDisplay()));
            boolean check = true;
            for (CraftData.Pack recipe : craftData.recipe()) {
                MaterialData recipeMaterial = recipe.material();
                String hasAmount = " §7(" + playerData.getMaterial(recipeMaterial) + ")";
                if (playerData.hasMaterial(recipeMaterial, recipe.amount() * amount)) {
                    message.add("§b✓ §f" + recipeMaterial.getDisplay() + "§ax" + recipe.amount() * amount + hasAmount);
                } else {
                    message.add("§c× §f" + recipeMaterial.getDisplay() + "§ax" + recipe.amount() * amount + hasAmount);
                    check = false;
                }
            }
            if (check) {
                if (!process.containsKey(craftData) && process.size() >= slot()) {
                    playerData.sendMessage("§e制作スロット§aが足りません", SomSound.Nope);
                    return;
                }
                for (CraftData.Pack recipe : craftData.recipe()) {
                    playerData.removeMaterial(recipe.material(), recipe.amount() * amount);
                }
                if (process.containsKey(craftData)) {
                    process.get(craftData).add(amount, time);
                } else {
                    process.put(craftData, new Process(craftData, amount, time, time));
                }
                update();
                SomSound.Tick.play(playerData);
            } else {
                playerData.sendMessage(message, SomSound.Nope);
            }
        }
    }

    @Override
    public void bottomClick(int slot, ItemStack clickedItem, ClickType clickType) {

    }

    @Override
    public void close() {
        amount = 0;
    }

    public class Process {
        private final CraftData craftData;
        private int amount;
        private int time;
        private int resetTime;

        public Process(CraftData craftData, int amount, int time, int resetTime) {
            this.craftData = craftData;
            this.amount = amount;
            this.time = time;
            this.resetTime = resetTime;
            SomSQL.setSql(DataBase.Table.PlayerCraft, key, value(craftData), "Time", time);
            SomSQL.setSql(DataBase.Table.PlayerCraft, key, value(craftData), "Amount", amount);
            SomSQL.setSql(DataBase.Table.PlayerCraft, key, value(craftData), "ResetTime", resetTime);
        }

        public void add(int amount, int time) {
            this.amount += amount;
            resetTime = time;
            SomSQL.setSql(DataBase.Table.PlayerCraft, key, value(craftData), "Amount", this.amount);
            SomSQL.setSql(DataBase.Table.PlayerCraft, key, value(craftData), "ResetTime", resetTime);
        }

        public void update(PlayerData playerData) {
            int newTime = time(craftData.cost(), playerData);
            int diffTime = resetTime - newTime;
            if (diffTime > 0) {
                int progress = resetTime - time;
                resetTime = newTime;
                time = resetTime - progress;
                SomSQL.setSql(DataBase.Table.PlayerCraft, key, value(craftData), "ResetTime", resetTime);
                playerData.sendMessage(craftData.result().getDisplay() + "§aの§e制作時間§aが§b短縮§aしました", SomSound.Tick);
            }
        }

        public boolean tick(boolean save) {
            time--;
            if (time <= 0) {
                amount--;
                craftData.result().craft(playerData);
                playerData.gatheringMenu().addExp(GatheringMenu.Type.Craft, craftData.exp());
                playerData.statistics().add(Statistics.IntEnum.GatheringCraftCount, 1);
                if (craftTool != null && craftTool.isSync() && craftTool.getOwner() == playerData) craftTool.chanceExp();
                if (amount > 0) {
                    time = resetTime;
                    SomTask.async(() -> {
                        SomSQL.setSql(DataBase.Table.PlayerCraft, key, value(craftData), "Time", time);
                        SomSQL.setSql(DataBase.Table.PlayerCraft, key, value(craftData), "Amount", amount);
                    });
                    return false;
                } else {
                    playerData.sendMessage(craftData.result().getDisplay() + "§aの§eクラフト§aが§b完了§aしました", SomSound.Tick);
                    SomTask.async(() -> SomSQL.delete(DataBase.Table.PlayerCraft, key, value(craftData)));
                    return true;
                }
            }
            if (save) SomTask.async(() -> SomSQL.setSql(DataBase.Table.PlayerCraft, key, value(craftData), "Time", time));
            return false;
        }

        public CustomItemStack viewItem() {
            CustomItemStack item = craftData.viewItem();
            item.addSeparator("制作進捗");
            item.addLore(decoLore("制作予定個数") + amount);
            item.addLore(decoLore("現在残り時間") + time + "秒");
            if (amount > 1) item.addLore(decoLore("合計残り時間") + (resetTime * (amount - 1) + time) + "秒");
            item.setAmount(amount);
            return item;
        }
    }
}
