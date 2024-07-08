package SwordofMagic11.Component;

import SwordofMagic11.Entity.SomEntity;
import SwordofMagic11.Map.MapData;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.SomCore;
import SwordofMagic11.TaskOwner;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

import static SwordofMagic11.SomCore.Log;

public class SomTask {

    private static final ConcurrentHashMap<TaskOwner, List<BukkitTask>> ownerTask = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<TaskOwner, List<Runnable>> endTask = new ConcurrentHashMap<>();

    public static void run() {
        Bukkit.getScheduler().runTaskTimer(SomCore.plugin(), () -> ownerTask.forEach((owner, taskList) -> {
            if (owner.isInvalid()) {
                for (BukkitTask task : taskList) {
                    task.cancel();
                }
                if (endTask.containsKey(owner)) {
                    for (Runnable task : endTask.get(owner)) {
                        task.run();
                    }
                    endTask.remove(owner);
                }
                ownerTask.remove(owner);
            }
        }), 0, 20);

        Log("§b[Som11]§aSomTask");
    }

    public static void registerEndTask(TaskOwner owner, Runnable runnable) {
        if (!endTask.containsKey(owner)) {
            endTask.put(owner, new ArrayList<>());
        }
        endTask.get(owner).add(runnable);
    }

    public static BukkitTask register(TaskOwner owner, BukkitTask task) {
        if (!ownerTask.containsKey(owner)) {
            ownerTask.put(owner, new ArrayList<>());
        }
        ownerTask.get(owner).add(task);
        return task;
    }

    public static void stop(TaskOwner owner) {
        if (ownerTask.containsKey(owner)) {
            for (BukkitTask task : ownerTask.get(owner)) {
                task.cancel();
            }
            ownerTask.remove(owner);
        }
        endTask.remove(owner);
    }

    /**
     * 非同期処理を実行
     *
     * @param runnable 実行内容
     */
    public static void async(Runnable runnable) {
        Bukkit.getScheduler().runTaskAsynchronously(SomCore.plugin(), runnable);
    }

    /**
     * 非同期処理を実行
     *
     * @param runnable    実行内容
     * @param endRunnable 最終実行内容
     */
    public static void async(Runnable runnable, Runnable endRunnable) {
        Bukkit.getScheduler().runTaskAsynchronously(SomCore.plugin(), () -> {
            runnable.run();
            endRunnable.run();
        });
    }

    public static BukkitTask asyncTimer(Runnable runnable, int tick, TaskOwner owner) {
        return asyncTimer(runnable, tick, 0, owner);
    }

    /**
     * 非同期処理を一定間隔で実行
     *
     * @param runnable 実行内容
     * @param tick     実行間隔
     * @param delay    初期遅延
     * @param owner    タスクの所有者
     */
    public static BukkitTask asyncTimer(Runnable runnable, int tick, int delay, TaskOwner owner) {
        return register(owner, Bukkit.getScheduler().runTaskTimerAsynchronously(SomCore.plugin(), runnable, delay, tick));
    }

    public static BukkitTask asyncTimer(Runnable runnable, int tick, BooleanSupplier condition, TaskOwner owner) {
        return asyncTimer(runnable, tick, 0, condition, owner);
    }

    public static BukkitTask asyncTimer(Runnable runnable, int tick, int delay, BooleanSupplier condition, TaskOwner owner) {
        return new BukkitRunnable() {
            @Override
            public void run() {
                if (owner != null && owner.isInvalid()) {
                    this.cancel();
                    return;
                }
                if (condition.getAsBoolean()) {
                    runnable.run();
                } else {
                    this.cancel();
                }
            }
        }.runTaskTimer(SomCore.plugin(), delay, tick);
    }

    /**
     * 非同期処理を指定時間後で実行
     *
     * @param runnable 実行内容
     * @param delay    遅延 (tick)
     */
    public static BukkitTask asyncDelay(Runnable runnable, int delay) {
        return Bukkit.getScheduler().runTaskLaterAsynchronously(SomCore.plugin(), runnable, delay);
    }

    /**
     * 非同期処理を一定間隔・一定回数実行します
     *
     * @param runnable 実行内容
     * @param tick     実行間隔
     * @param count    実行回数
     */
    public static BukkitTask asyncCount(Runnable runnable, int tick, int count, TaskOwner owner) {
        return asyncCount(runnable, tick, count, 0, null, owner);
    }

    /**
     * 遅延した後に非同期処理を一定間隔・一定回数実行します
     *
     * @param runnable 実行内容
     * @param tick     実行間隔
     * @param count    実行回数
     * @param delay    実行遅延
     */
    public static BukkitTask asyncCount(Runnable runnable, int tick, int count, int delay, TaskOwner owner) {
        return asyncCount(runnable, tick, count, delay, null, owner);
    }

    /**
     * 非同期処理を一定間隔・一定回数実行します
     * 終了時に違う処理を1度実行します
     *
     * @param runnable    実行内容
     * @param tick        実行間隔
     * @param count       実行回数
     * @param endRunnable 終了後の実行内容
     */
    public static BukkitTask asyncCount(Runnable runnable, int tick, int count, Runnable endRunnable, TaskOwner owner) {
        return asyncCount(runnable, tick, count, 0, endRunnable, owner);
    }

    /**
     * 遅延した後に非同期処理を一定間隔・一定回数実行します
     * 終了時に違う処理を1度実行します
     *
     * @param runnable    実行内容
     * @param tick        実行間隔
     * @param count       実行回数
     * @param delay       実行遅延
     * @param endRunnable 終了後の実行内容
     */
    public static BukkitTask asyncCount(Runnable runnable, int tick, int count, int delay, Runnable endRunnable, TaskOwner owner) {
        return new BukkitRunnable() {
            int i = 0;

            @Override
            public void run() {
                if (owner != null && owner.isInvalid()) {
                    this.cancel();
                    return;
                }
                if (i < count) {
                    runnable.run();
                } else {
                    this.cancel();
                    if (endRunnable != null) endRunnable.run();
                }
                i++;
            }
        }.runTaskTimer(SomCore.plugin(), delay, tick);
    }

    /**
     * 非同期処理を一定間隔・一定回数実行します
     *
     * @param function 実行内容
     * @param tick     実行間隔
     * @param count    実行回数
     */
    public static BukkitTask asyncCount(Function<Integer, Void> function, int tick, int count, TaskOwner owner) {
        return asyncCount(function, tick, count, 0, null, owner);
    }

    /**
     * 遅延した後に非同期処理を一定間隔・一定回数実行します
     *
     * @param function 実行内容
     * @param tick     実行間隔
     * @param count    実行回数
     * @param delay    実行遅延
     */
    public static BukkitTask asyncCount(Function<Integer, Void> function, int tick, int count, int delay, TaskOwner owner) {
        return asyncCount(function, tick, count, delay, null, owner);
    }

    /**
     * 非同期処理を一定間隔・一定回数実行します
     * 終了時に違う処理を1度実行します
     *
     * @param function    実行内容
     * @param tick        実行間隔
     * @param count       実行回数
     * @param endRunnable 終了後の実行内容
     */
    public static BukkitTask asyncCount(Function<Integer, Void> function, int tick, int count, Runnable endRunnable, TaskOwner owner) {
        return asyncCount(function, tick, count, 0, endRunnable, owner);
    }

    /**
     * 遅延した後に非同期処理を一定間隔・一定回数実行します
     * 終了時に違う処理を1度実行します
     *
     * @param function    実行内容
     * @param tick        実行間隔
     * @param count       実行回数
     * @param delay       実行遅延
     * @param endRunnable 終了後の実行内容
     */
    public static BukkitTask asyncCount(Function<Integer, Void> function, int tick, int count, int delay, Runnable endRunnable, TaskOwner owner) {
        return new BukkitRunnable() {
            int i = 0;

            @Override
            public void run() {
                if (owner != null && owner.isInvalid()) {
                    this.cancel();
                    return;
                }
                if (i < count) {
                    function.apply(i);
                } else {
                    this.cancel();
                    if (endRunnable != null) endRunnable.run();
                }
                i++;
            }
        }.runTaskTimer(SomCore.plugin(), delay, tick);
    }

    /**
     * 遅延した後に非同期処理を一定間隔・一定回数実行します
     * 終了時に違う処理を1度実行します
     *
     * @param runnable    実行内容
     * @param tick        実行間隔
     * @param count       実行回数
     * @param delay       実行遅延
     * @param endRunnable 終了後の実行内容
     * @param condition   処理の継続条件
     */
    public static BukkitTask asyncCountIf(Runnable runnable, int tick, int count, int delay, BooleanSupplier condition, Runnable endRunnable, TaskOwner owner) {
        return new BukkitRunnable() {
            int i = 0;

            @Override
            public void run() {
                if (owner != null && owner.isInvalid()) {
                    this.cancel();
                    return;
                }
                if (i < count && condition.getAsBoolean()) {
                    runnable.run();
                } else {
                    this.cancel();
                    if (endRunnable != null) endRunnable.run();
                }
                i++;
            }
        }.runTaskTimer(SomCore.plugin(), delay, tick);
    }

    /**
     * 同期処理を実行
     *
     * @param runnable 実行内容
     */
    public static void sync(Runnable runnable) {
        Bukkit.getScheduler().runTask(SomCore.plugin(), runnable);
    }

    /**
     * 同期処理を一定間隔で実行
     *
     * @param runnable 実行内容
     * @param tick     実行間隔
     * @param owner    タスクの所有者
     */
    public static BukkitTask syncTimer(Runnable runnable, int tick, TaskOwner owner) {
        return syncTimer(runnable, tick, 0, owner);
    }

    /**
     * 同期処理を一定間隔で実行
     *
     * @param runnable 実行内容
     * @param tick     実行間隔
     * @param delay    初期遅延
     * @param owner    タスクの所有者
     */
    public static BukkitTask syncTimer(Runnable runnable, int tick, int delay, TaskOwner owner) {
        return register(owner, Bukkit.getScheduler().runTaskTimer(SomCore.plugin(), runnable, delay, tick));
    }

    /**
     * 同期処理を指定時間後で実行
     *
     * @param runnable 実行内容
     * @param delay    遅延 (tick)
     */
    public static BukkitTask syncDelay(Runnable runnable, int delay) {
        return Bukkit.getScheduler().runTaskLater(SomCore.plugin(), runnable, delay);
    }

    public static BukkitTask skillTaskCount(Runnable runnable, int tick, int count, SomEntity owner) {
        return skillTaskCount(runnable, tick, count, 0, null, owner);
    }

    public static BukkitTask skillTaskCount(Runnable runnable, int tick, int count, int delay, SomEntity owner) {
        return skillTaskCount(runnable, tick, count, delay, null, owner);
    }

    public static BukkitTask skillTaskCount(Runnable runnable, int tick, int count, Runnable endRunnable, SomEntity owner) {
        return skillTaskCount(runnable, tick, count, 0, endRunnable, owner);
    }

    public static BukkitTask skillTaskCount(Runnable runnable, int tick, int count, int delay, Runnable endRunnable, SomEntity owner) {
        return new BukkitRunnable() {
            int i = 0;

            @Override
            public void run() {
                try {
                    if (owner != null) {
                        if (owner.isInvalid() || owner.isDeath()) {
                            this.cancel();
                            return;
                        }
                        if (owner.isSilence() || owner.isDeath()) {
                            this.cancel();
                            if (owner instanceof PlayerData playerData) {
                                playerData.skillManager().sendSkillBreakMessage();
                            }
                            return;
                        }
                        if (owner instanceof PlayerData playerData && playerData.isAFK()) {
                            this.cancel();
                            playerData.skillManager().sendSkillBreakMessage();
                            return;
                        }
                    }
                    if (i < count) {
                        runnable.run();
                    } else {
                        this.cancel();
                        if (endRunnable != null) endRunnable.run();
                    }
                    i++;
                } catch (Exception e) {
                    this.cancel();
                    Log(e);
                }
            }
        }.runTaskTimer(SomCore.plugin(), delay, tick);
    }

    public static BukkitTask skillTaskTimer(Runnable runnable, int tick, SomEntity owner) {
        return skillTaskTimer(runnable, tick, 0, owner);
    }

    public static BukkitTask skillTaskTimer(Runnable runnable, int tick, int delay, SomEntity owner) {
        return new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    if (owner != null) {
                        if (owner.isInvalid() || owner.isDeath()) {
                            this.cancel();
                            return;
                        }
                        if (owner.isSilence()) {
                            this.cancel();
                            if (owner instanceof PlayerData playerData) {
                                playerData.skillManager().sendSkillBreakMessage();
                            }
                            return;
                        }
                        if (owner instanceof PlayerData playerData && playerData.isAFK()) {
                            this.cancel();
                            playerData.skillManager().sendSkillBreakMessage();
                            return;
                        }
                        runnable.run();
                    }
                } catch (Exception e) {
                    this.cancel();
                    Log(e);
                }
            }
        }.runTaskTimer(SomCore.plugin(), delay, tick);
    }
}
