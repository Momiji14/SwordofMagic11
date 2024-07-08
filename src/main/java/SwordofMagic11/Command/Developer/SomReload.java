package SwordofMagic11.Command.Developer;

import SwordofMagic11.Command.SomCommand;
import SwordofMagic11.Component.SomTask;
import SwordofMagic11.Component.Updater;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.SomCore;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static SwordofMagic11.SomCore.Log;

public class SomReload implements SomCommand {
    @Override
    public boolean PlayerCommand(Player player, PlayerData playerData, String[] args) {
        return false;
    }

    @Override
    public boolean Command(CommandSender sender, String[] args) {
        SomCore.SomReload = true;
        if (args.length >= 1) SomCore.FullReload = false;
        for (PlayerData playerData : PlayerData.getPlayerList()) {
            playerData.sendTitle("§4§n§lシステムをリロードします", "", 10, 60, 0);
            playerData.getPlayer().closeInventory();
        }
        SomTask.syncDelay(() -> {
            for (PlayerData playerData : PlayerData.getPlayerList()) {
                playerData.save();
            }
            long startTime = System.currentTimeMillis();
            Updater.UpdatePlugin();
            Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "plugman reload swordofmagic11");
            Log("§a[SomCore]§bSomReload - " + (System.currentTimeMillis() - startTime) + "ms");
        }, 60);
        return true;
    }
}

