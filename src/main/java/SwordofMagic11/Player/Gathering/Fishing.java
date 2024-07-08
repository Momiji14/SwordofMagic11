package SwordofMagic11.Player.Gathering;

import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Component.SomTask;
import SwordofMagic11.Item.SomEquip;
import SwordofMagic11.Map.Gathering;
import SwordofMagic11.Map.GatheringData;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Player.Setting.PlayerSetting;
import SwordofMagic11.Player.Statistics.Statistics;
import org.bukkit.Material;
import org.bukkit.entity.FishHook;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static SwordofMagic11.Component.Function.*;

public class Fishing {

    private final PlayerData playerData;
    private BukkitTask task;
    private boolean isFishing = false;
    private boolean isInteract = false;
    private final List<Combo> combo = new CopyOnWriteArrayList<>();
    private long startFishing = 0;
    private static final int max = 7;

    public Fishing(PlayerData playerData) {
        this.playerData = playerData;
    }

    public double cps() {
        return max/((System.currentTimeMillis()-startFishing)/1000.0);
    }

    public boolean isFishing() {
        return isFishing;
    }

    public boolean isHoldFishingRod() {
        if (playerData.getPlayer().getInventory().getItemInMainHand().getType() == Material.FISHING_ROD) {
            if (playerData.hasEquip(SomEquip.Slot.MainHand)) {
                return playerData.getEquip(SomEquip.Slot.MainHand).getEquipCategory() == SomEquip.Category.FishingTool;
            }
        }
        return false;
    }

    public void trigger(PlayerFishEvent event) {
        if (playerData.isAFK()) {
            SomTask.sync(() -> event.getHook().remove());
            event.setCancelled(true);
            return;
        }
        FishHook hook = event.getHook();
        switch (event.getState()) {
            case FISHING -> entry(hook);
            case BITE -> {
                hook.setWaitTime(Integer.MAX_VALUE, Integer.MAX_VALUE);
                start(hook);
                event.setCancelled(true);
            }
            case REEL_IN -> {
                if (isFishing) {
                    click(Combo.Rim);
                    event.setCancelled(true);
                } else {
                    playerData.getPlayer().resetTitle();
                }
            }
            case IN_GROUND -> playerData.getPlayer().resetTitle();
        }
    }

    public void entry(FishHook hook) {
        hook.setRainInfluenced(false);
        hook.setSkyInfluenced(false);
        int wait = ceil(100 * playerData.gatheringMenu().getSkillValue(GatheringMenu.Type.Fishing, GatheringMenu.Skill.TimeSave));
        hook.setLureAngle(0, 360);
        hook.setWaitTime(1);
        hook.setLureTime(wait, wait);

        combo.clear();
        for (int i = 0; i < max; i++) {
            combo.add(randomBool() ? Combo.Centre : Combo.Rim);
        }

        StringBuilder builder = new StringBuilder();
        StringBuilder space = new StringBuilder();
        for (Combo combo : combo) {
            space.append(" ").append("§7").append(combo.getDisplay());
            builder.append("  ");
        }
        playerData.sendTitle(builder.append(space).toString(), "", 10, 100, 0);
    }

    public void start(FishHook hook) {
        if (task != null) reset();
        task = SomTask.asyncTimer(() -> {
            if (isFishing && hook.isValid()) {
                sendTitle();
            } else {
                reset();
                entry(hook);
            }
        }, 2, playerData);

        isFishing = true;
        startFishing = System.currentTimeMillis();
    }

    public void reset() {
        task.cancel();
        task = null;
        isFishing = false;
        playerData.getPlayer().resetTitle();
    }

    public synchronized void click(Combo click) {
        if (isInteract || !isFishing) return;
        isInteract = true;
        SomTask.async(() -> {
            if (!combo.isEmpty()) {
                if (combo.get(0) == click) {
                    combo.remove(0);
                    SomSound.Tick.radius(playerData);
                    if (combo.isEmpty()) {
                        playerData.resetAFK();
                        playerData.statistics().add(Statistics.IntEnum.GatheringFishingCount, 1);
                        Statistics.IntEnum intEnum = Statistics.IntEnum.GatheringFishingCount;
                        GatheringData gatheringData = playerData.getMap().getFishingData();
                        SomEquip tool = playerData.getEquip(SomEquip.Slot.MainHand);
                        Gathering.drop(playerData, intEnum, gatheringData, GatheringMenu.Type.Fishing, tool, playerData.getWorld());
                        isFishing = false;
                        double cps = cps();
                        if (playerData.setting().is(PlayerSetting.BooleanEnum.GatheringLog)) {
                            playerData.sendMessage("§a[Fishing] §e" + scale(cps, 3) + "CPS");
                        }
                        double currentCPS = playerData.statistics().get(Statistics.DoubleEnum.FishingCPS);
                        if (cps > currentCPS) {
                            playerData.statistics().set(Statistics.DoubleEnum.FishingCPS, cps);
                            playerData.sendMessage("§e" + Statistics.DoubleEnum.FishingCPS.getDisplay() + "§aを§b更新§aしました §e[" + scale(currentCPS, 3) + " -> " + scale(cps, 3) + "]", SomSound.Level);
                        }
                    } else {
                        sendTitle();
                    }
                } else {
                    combo.add(randomBool() ? Combo.Centre : Combo.Rim);
                    SomSound.Nope.radius(playerData);
                }
            }
            isInteract = false;
        });
    }

    public void sendTitle() {
        StringBuilder builder = new StringBuilder();
        StringBuilder space = new StringBuilder();
        for (Combo combo : combo) {
            space.append(" ").append(combo.getColor()).append(combo.getDisplay());
            builder.append("  ");
        }
        playerData.sendTitle(builder.append(space).toString(), "§e" + scale(cps(), 3) + "CPS", 0, 5, 0);
    }

    public enum Combo {
        Centre("L", "§c"),
        Rim("R", "§b"),
        ;

        private final String display;
        private final String color;

        Combo(String display, String color) {
            this.display = display;
            this.color = color;
        }

        public String getDisplay() {
            return display;
        }

        public String getColor() {
            return color;
        }

        public Combo next() {
            if (this == Centre) {
                return Rim;
            } else {
                return Centre;
            }
        }
    }
}
