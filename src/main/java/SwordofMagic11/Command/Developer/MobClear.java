package SwordofMagic11.Command.Developer;

import SwordofMagic11.Command.Player.Sit;
import SwordofMagic11.Command.SomCommand;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.SomCore;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Collection;

public class MobClear implements SomCommand {
    @Override
    public boolean PlayerCommand(Player player, PlayerData playerData, String[] args) {
        player.sendMessage("MobClear: " + clear());
        return true;
    }

    @Override
    public boolean Command(CommandSender sender, String[] args) {
        return false;
    }

    public static int clear() {
        int count = 0;
        for (World world : Bukkit.getWorlds()) {
            count += clear(world);
        }
        return count;
    }

    public static void clearForce(World world) {
        world.getEntities().forEach(MobClear::clearForce);
    }

    public static int clear(World world) {
        int count = 0;
        for (Entity entity : world.getEntities()) {
            if (clear(entity)) {
                count++;
            }
        }
        return count;
    }

    public static int clear(Chunk chunk) {
        int count = 0;
        for (Entity entity : chunk.getEntities()) {
            if (clear(entity)) {
                count++;
            }
        }
        return count;
    }

    public static boolean clear(Entity entity) {
        Collection<NamespacedKey> keys = entity.getPersistentDataContainer().getKeys();
        if (keys.contains(SomCore.SomEntityKey)) {
            entity.remove();
            return true;
        }
        if (keys.contains(SomCore.SomParticle)) {
            entity.remove();
            return true;
        }
        if (keys.contains(Sit.Key)) {
            entity.remove();
            return true;
        }
        return false;
    }

    public static void clearForce(Entity entity) {
        //Collection<NamespacedKey> keys = entity.getPersistentDataContainer().getKeys();
        //keys.contains(SomCore.SomNPCKey)
        if (entity.getScoreboardTags().contains("NPC")) {
            entity.remove();
            return;
        }
        clear(entity);
    }
}
