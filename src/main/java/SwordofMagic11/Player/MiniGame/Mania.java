package SwordofMagic11.Player.MiniGame;

import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Component.SomTask;
import SwordofMagic11.Custom.CustomLocation;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Player.Statistics.Statistics;
import SwordofMagic11.SomCore;
import org.bukkit.Color;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TextDisplay;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static SwordofMagic11.Component.Function.*;

public class Mania {

    public static final String Prefix = "§b[Mania]";

    private final PlayerData playerData;
    private BukkitTask task;
    private final List<Integer> notes = new CopyOnWriteArrayList<>();
    private boolean start = false;
    private long startTime = 0;
    private int count = 0;
    private int combo = 0;
    private int maxCombo = 0;
    private int miss = 0;
    private double topKps = 0;
    private double kps = 0;
    private TextDisplay display;

    public Mania(PlayerData playerData) {
        this.playerData = playerData;
    }

    public boolean isStart() {
        return start;
    }

    public void start() {
        playerData.closeInventory();
        playerData.getPlayer().getInventory().setHeldItemSlot(5);
        start = true;
        startTime = 0;
        combo = 0;
        count = 0;
        miss = 0;
        notes.clear();
        notes.add(randomInt(0, 3));
        topKps = 0;
        kps = 0;
        for (int i = 0; i < 6; i++) {
            notes.add(random());
        }
        if (task != null) task.cancel();
        SomTask.sync(() -> {
            if (display != null) display.remove();
            display = (TextDisplay) playerData.getWorld().spawnEntity(playerData.getLocation().frontHorizon(3), EntityType.TEXT_DISPLAY);
            display.getPersistentDataContainer().set(SomCore.SomParticle, PersistentDataType.BOOLEAN, true);
            display.setBackgroundColor(Color.fromARGB(255, 0, 0, 0));
            display.setAlignment(TextDisplay.TextAlignment.LEFT);
            display.setBillboard(Display.Billboard.FIXED);
            display.setRotation(playerData.getLocation().getYaw() - 180, 0);
            update();

            task = new BukkitRunnable() {
                final CustomLocation location = playerData.getLocation();

                @Override
                public void run() {
                    if (playerData.isValid()) {
                        if (playerData.getLocation().distance(location) > 5) {
                            this.cancel();
                            end();
                        }
                    } else {
                        this.cancel();
                        end();
                    }
                }
            }.runTaskTimerAsynchronously(SomCore.plugin(), 50, 50);
        });
    }

    public TextDisplay getDisplay() {
        return display;
    }

    public int random() {
        int last = notes.get(notes.size() - 1);
        if (last == 0) {
            return randomInt(last + 1, 3);
        } else if (last == 3) {
            return randomInt(0, last - 1);
        } else {
            return randomBool() ? randomInt(0, last - 1) : randomInt(last + 1, 3);
        }
    }

    private final String space = "     ";
    private final String spaceLine = space.repeat(4) + "\n";

    public void update() {
        StringBuilder builder = new StringBuilder(playerData.getDisplayName());
        builder.append("\n§e計測時間: ").append(startTime > 0 ? (System.currentTimeMillis() - startTime) / 1000 : 0).append("秒");
        builder.append("\n§e最高KPS: ").append(topKps > 0 ? scale(topKps, 3) : "計測中");
        builder.append("\n§e現在KPS: ").append(kps > 0 ? scale(kps, 3) : "計測中");
        builder.append("\n§e最高コンボ: ").append(maxCombo);
        builder.append("\n§e現在コンボ: ").append(combo);
        builder.append("\n§eカウント: ").append(count);
        builder.append("\n§eミス: ").append(miss);
        builder.append("\n--------------\n");
        for (int i = notes.size(); i > 0; i--) {
            int note = notes.get(i - 1);
            builder.append(spaceLine).append(space.repeat(note)).append(noteText(note)).append(space.repeat(3 - note)).append(" \n");
        }
        builder.append("§e--------------");
        display.setText(builder.toString());
    }

    public String noteText(int i) {
        switch (i) {
            case 0, 3 -> {
                return " §f■■■";
            }
            case 1, 2 -> {
                return " §b■■■";
            }
        }
        return "";
    }

    public void tap(int key) {
        if (startTime == 0) startTime = System.currentTimeMillis();
        if (key == notes.get(0)) {
            notes.remove(0);
            notes.add(random());
            double time = (System.currentTimeMillis() - startTime) / 1000.0;
            if (time > 5) {
                kps = count / time;
                topKps = Math.max(kps, topKps);
            }
            if (randomDouble() < 0.2) {
                playerData.craftMenu().processTick(false);
            }
            combo++;
            count++;
            maxCombo = Math.max(maxCombo, combo);
            playerData.statistics().add(Statistics.IntEnum.ManiaTapCount, 1);
            SomSound.Tick.radius(playerData, 5);
        } else {
            combo = 0;
            miss++;
            playerData.getPlayer().getInventory().setHeldItemSlot(5);
            SomSound.Nope.radius(playerData);
        }
        if (miss > 100 && (double) miss / count > 0.5) {
            end();
            playerData.sendMessage(Prefix + "§cミス§aし過ぎです！(ミス率50%) §eもう少し慎重になりましょう", SomSound.Nope);
        }
        update();
    }

    public void end() {
        if (task != null) task.cancel();
        if (isStart()) {
            double currentKps = playerData.statistics().get(Statistics.DoubleEnum.ManiaKPS);
            if (topKps > currentKps) {
                playerData.sendMessage(Prefix + "§e" + Statistics.DoubleEnum.ManiaKPS.getDisplay() + "§aを§b更新§aしました §e[" + scale(currentKps, 3) + " -> " + scale(topKps, 3) + "]", SomSound.Level);
                playerData.statistics().set(Statistics.DoubleEnum.ManiaKPS, topKps);
            }
            int currentCombo = playerData.statistics().get(Statistics.IntEnum.ManiaMaxCombo);
            if (maxCombo > currentCombo) {
                playerData.sendMessage(Prefix + "§e" + Statistics.IntEnum.ManiaMaxCombo.getDisplay() + "§aを§b更新§aしました §e[" + currentCombo + " -> " + maxCombo + "]", SomSound.Level);
                playerData.statistics().set(Statistics.IntEnum.ManiaMaxCombo, maxCombo);
            }
            List<String> message = new ArrayList<>();
            message.add(Prefix + "§e" + scale(topKps, 3) + "KPS");
            message.add(Prefix + "§e" + maxCombo + "Combo");
            playerData.sendMessage(message, SomSound.Tick);
            start = false;
            SomTask.sync(() -> display.remove());
        }
    }
}
