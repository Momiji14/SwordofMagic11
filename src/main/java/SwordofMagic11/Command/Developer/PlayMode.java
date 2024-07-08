package SwordofMagic11.Command.Developer;

import SwordofMagic11.Command.SomCommand;
import SwordofMagic11.Player.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class PlayMode implements SomCommand {

    private final HashMap<Player, ItemStack[]> inventory = new HashMap<>();

    @Override
    public boolean PlayerCommand(Player player, PlayerData playerData, String[] args) {
        playerData.setPlayMode(!playerData.isPlayMode());
        playerData.getNamePlate().remove();
        player.setGameMode(playerData.isPlayMode() ? GameMode.SURVIVAL : GameMode.CREATIVE);
        player.getInventory().clear();
        player.sendMessage("PlayMode: " + playerData.isPlayMode());
        save(playerData);
        if (playerData.isPlayMode()) {
            playerData.updateMenu();
            playerData.updateInventory();
        }
        return true;
    }

    @Override
    public boolean Command(CommandSender sender, String[] args) {
        return true;
    }

    private static final File file = new File("playMode.yml");
    private static final FileConfiguration data = YamlConfiguration.loadConfiguration(file);

    public static void save(PlayerData playerData) {
        try {
            data.save(file);
            if (playerData.isPlayMode()) {
                data.set(playerData.getName(), null);
            } else {
                data.set(playerData.getName(), false);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isTrue(PlayerData playerData) {
        return !data.isSet(playerData.getName());
    }

}

