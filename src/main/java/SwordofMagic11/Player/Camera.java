package SwordofMagic11.Player;

import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Component.SomTask;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class Camera {

    private BukkitTask task;
    private final PlayerData playerData;
    private final Player player;

    public Camera(PlayerData playerData) {
        this.playerData = playerData;
        player = playerData.getPlayer();
    }

    public boolean isCamera() {
        return task != null && !task.isCancelled();
    }

    public void start(PlayerData target) {
        if (playerData.getMap().isCity()) {
            task = SomTask.syncTimer(() -> {
                if (target.isInvalid()) {
                    task.cancel();
                    task = null;
                    return;
                }
                if (player.getGameMode() != GameMode.SPECTATOR) {
                    player.setGameMode(GameMode.SPECTATOR);
                }
                playerData.teleport(target.getLocation());
            }, 20, playerData);
            playerData.sendMessage(target.getDisplayName() + "を追跡します", SomSound.Tick);
        } else {
            playerData.sendReqInCity();
        }
    }

    public void stop() {
        if (isCamera()) {
            task.cancel();
            task = null;
            player.setGameMode(GameMode.SURVIVAL);
            playerData.teleport(playerData.lastSpawnLocation());
        } else {
            playerData.sendMessage("§eカメラモード§aではありません", SomSound.Nope);
        }
    }

}
