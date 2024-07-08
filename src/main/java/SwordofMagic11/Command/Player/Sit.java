package SwordofMagic11.Command.Player;

import SwordofMagic11.Command.SomCommand;
import SwordofMagic11.Component.SomTask;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Player.Statistics.Statistics;
import SwordofMagic11.SomCore;
import SwordofMagic11.TaskOwner;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

public class Sit implements SomCommand {

    public static final NamespacedKey Key = SomCore.Key("SitEntity");

    @Override
    public boolean PlayerCommand(Player player, PlayerData playerData, String[] args) {
        if (player.getVehicle() == null) {
            new SitEntity(playerData);
        }
        return true;
    }

    @Override
    public boolean Command(CommandSender sender, String[] args) {
        return false;
    }

    public static class SitEntity implements TaskOwner {

        private final LivingEntity entity;

        public SitEntity(PlayerData playerData) {
            entity = (LivingEntity) playerData.getWorld().spawnEntity(playerData.getLocation().lower().addY(-0.8), EntityType.CHICKEN);
            entity.setAI(false);
            entity.setSilent(true);
            entity.setInvulnerable(true);
            entity.setInvisible(true);
            entity.addPassenger(playerData.getPlayer());
            entity.getPersistentDataContainer().set(Key, PersistentDataType.BOOLEAN, true);

            SomTask.asyncTimer(() -> {
                if (entity.getPassengers().isEmpty()) {
                    SomTask.sync(entity::remove);
                } else {
                    playerData.statistics().add(Statistics.IntEnum.SitTime, 1);
                }
            }, 20, this);
        }

        @Override
        public boolean isValid() {
            return entity.isValid();
        }
    }
}

