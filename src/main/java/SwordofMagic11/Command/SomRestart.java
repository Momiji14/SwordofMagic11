package SwordofMagic11.Command;

import SwordofMagic11.Component.SomTask;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.SomCore;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SomRestart implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        SomCore.Restart = true;
        for (PlayerData playerData : PlayerData.getPlayerList()) {
            try {
                playerData.save();
                playerData.getPlayer().kick(Component.text("§bServer Restart"));
            } catch (Exception ignore) {}
        }
        SomTask.syncDelay(() -> {
            for (World world : Bukkit.getWorlds()) {
                try {
                    if (!Bukkit.unloadWorld(world, false)) {
                        SomCore.Log("fail unload " + world.getName());
                    }
                } catch (Exception ignore) {}
            }
            SomCore.plugin().getServer().restart();
        }, 5);
        return true;
    }

    public static void register() {
        Bukkit.getPluginCommand("SomRestart").setExecutor(new SomRestart());
    }
}
