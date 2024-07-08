package SwordofMagic11.Player.MiniGame;

import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Component.SomTask;
import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.Player.Menu.GUIManager;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Player.Statistics.Statistics;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static SwordofMagic11.Component.Function.randomInt;
import static SwordofMagic11.Component.Function.scale;

public class LightsOut extends GUIManager {

    public LightsOut(PlayerData playerData) {
        super(playerData, "ライツアウト", 6);
    }

    private long gameStartTime;
    private boolean isStart = false;
    private CustomItemStack[] content = new CustomItemStack[54];

    @Override
    public void open() {
        isStart = true;
        super.open();
    }

    @Override
    public void updateContainer() {
        gameStartTime = System.currentTimeMillis();
        content = new CustomItemStack[54];
        boolean[] map = new boolean[54];
        Arrays.fill(map, true);
        Set<Integer> slots = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            int slot = randomInt(0, 54);
            while (slots.contains(slot)) {
                slot = randomInt(0, 54);
            }
            slots.add(slot);
        }
        for (int slot : slots) {
            int[] xy = getSlot(slot);
            int x = xy[0];
            int y = xy[1];
            map[slot] = !map[slot];
            if (x > 0) {
                int index = getSlot(x-1, y);
                map[index] = !map[index];
            }
            if (x < 8) {
                int index = getSlot(x+1, y);
                map[index] = !map[index];
            }
            if (y > 0) {
                int index = getSlot(x, y-1);
                map[index] = !map[index];
            }
            if (y < 5) {
                int index = getSlot(x, y+1);
                map[index] = !map[index];
            }
        }
        for (int i = 0; i < 54; i++) {
            setItem(i, lightsOut(map[i]));
        }
    }

    @Override
    public void topClick(int slot, ItemStack clickedItem, ClickType clickType) {
        if (isStart && clickedItem != null) {
            int[] xy = getSlot(slot);
            int x = xy[0];
            int y = xy[1];
            content[slot] = lightsOut(!CustomItemStack.getCustomDataBoolean(content[slot], "LightsOut"));
            if (x > 0) {
                int index = getSlot(x-1, y);
                content[index] = lightsOut(!CustomItemStack.getCustomDataBoolean(content[index], "LightsOut"));
            }
            if (x < 8) {
                int index = getSlot(x+1, y);
                content[index] = lightsOut(!CustomItemStack.getCustomDataBoolean(content[index], "LightsOut"));
            }
            if (y > 0) {
                int index = getSlot(x, y-1);
                content[index] = lightsOut(!CustomItemStack.getCustomDataBoolean(content[index], "LightsOut"));
            }
            if (y < 5) {
                int index = getSlot(x, y+1);
                content[index] = lightsOut(!CustomItemStack.getCustomDataBoolean(content[index], "LightsOut"));
            }
            fastUpdate(content);
            for (ItemStack item : content) {
                if (!CustomItemStack.getCustomDataBoolean(item, "LightsOut")) {
                    return;
                }
            }
            isStart = false;
            for (int i = 0; i < 10; i++) {
                playerData.craftMenu().processTick(false);
            }
            playerData.statistics().add(Statistics.IntEnum.LightsOutClearCount, 1);
            double endTime = (System.currentTimeMillis() - gameStartTime)/1000.0;
            double time = playerData.statistics().get(Statistics.DoubleEnum.LightsOutClearTime);
            if (time == 0 || time > endTime) {
                playerData.sendMessage("§e" + Statistics.DoubleEnum.LightsOutClearTime.getDisplay() + "§aを§b更新§aしました §e[" + scale(time, 3) + " -> " + scale(endTime, 3) + "]", SomSound.Level);
                playerData.statistics().set(Statistics.DoubleEnum.LightsOutClearTime, endTime);
            } else playerData.sendMessage("§e[ライツアウト] " + scale(endTime, 3) + "秒");
            for (int i = 0; i < 54; i++) {
                setItem(i, new CustomItemStack(Material.BARRIER).setNonDecoDisplay(" "));
            }
            fastUpdate();
            SomTask.asyncDelay(() -> {
                isStart = true;
                update();
            }, 500);
        }
    }

    @Override
    public void bottomClick(int slot, ItemStack clickedItem, ClickType clickType) {

    }

    @Override
    public void close() {

    }

    public CustomItemStack lightsOut(boolean bool) {
        return new CustomItemStack(bool ? Material.EMERALD_BLOCK : Material.REDSTONE_BLOCK).setNonDecoDisplay(" ").setCustomData("LightsOut", bool);
    }
}
