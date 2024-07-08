package SwordofMagic11.Player;

import SwordofMagic11.Component.SomRay;
import SwordofMagic11.Component.SomTask;
import SwordofMagic11.Player.Setting.PlayerSetting;
import SwordofMagic11.Player.Statistics.Statistics;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import static SwordofMagic11.Component.Function.VectorFromYawPitch;

public interface StrafeWallKick {
    PlayerData playerData();

    default void strafe() {
        SomTask.async(() -> {
            PlayerData playerData = playerData();
            Player player = playerData.getPlayer();
            if (playerData.isSilence()) return;
            if (!playerData.hasTimer("Strafe")) {
                if (player.isInWater()) {
                    player.setVelocity(player.getLocation().getDirection());
                } else {
                    if (playerData.setting().is(PlayerSetting.BooleanEnum.BackStrafe) && player.isSneaking()) {
                        player.setVelocity(player.getVelocity().add(VectorFromYawPitch(playerData().yaw() + 180, -15)));
                    } else {
                        player.setVelocity(player.getVelocity().add(VectorFromYawPitch(playerData().yaw(), -15)));
                    }
                }
                strafeCoolDown();
                playerData.statistics().add(Statistics.IntEnum.StrafeCount, 1);
            }
        });
    }

    default void strafeCoolDown() {
        playerData().timer("Strafe", 20);
    }

    default void wallKick() {
        SomTask.async(() -> {
            PlayerData playerData = playerData();
            if (!playerData.setting().is(PlayerSetting.BooleanEnum.WallKick)) return;
            if (playerData.isSilence()) return;
            Player player = playerData.getPlayer();
            if (SomRay.rayLocationBlock(playerData.getHipsLocation().pitch(0), 1, true).isHitBlock()) {
                player.setVelocity(VectorFromYawPitch(playerData.yaw() + 180, -25));
                if (!playerData.hasTimer("Strafe")) {
                    playerData.timer("Strafe", 5);
                }
                playerData.statistics().add(Statistics.IntEnum.WallKickCount, 1);
            }
        });
    }
}
