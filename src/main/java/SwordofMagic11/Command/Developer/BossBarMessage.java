package SwordofMagic11.Command.Developer;

import SwordofMagic11.Command.SomCommand;
import SwordofMagic11.Component.SomTask;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.SomCore;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BossBarMessage implements SomCommand {
    @Override
    public boolean PlayerCommand(Player player, PlayerData playerData, String[] args) {
        int time = Integer.parseUnsignedInt(args[0]);
        BossBar bossBar = Bukkit.createBossBar(args[1], BarColor.WHITE, BarStyle.SOLID);
        bossBar.setProgress(1);
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            bossBar.addPlayer(onlinePlayer);
        }
        float remove = 1f/(time*4);
        SomTask.asyncCount(() -> bossBar.setProgress(bossBar.getProgress()-remove), 5, time*4, () -> bossBar.setVisible(false), SomCore.TaskOwner);
        return true;
    }

    @Override
    public boolean Command(CommandSender sender, String[] args) {
        return false;
    }
}
