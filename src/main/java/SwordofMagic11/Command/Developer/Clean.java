package SwordofMagic11.Command.Developer;

import SwordofMagic11.Command.SomCommand;
import SwordofMagic11.Component.WorldManager;
import SwordofMagic11.Player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static SwordofMagic11.Component.Function.broadcast;

public class Clean implements SomCommand {
    @Override
    public boolean PlayerCommand(Player player, PlayerData playerData, String[] args) {
        return false;
    }

    @Override
    public boolean Command(CommandSender sender, String[] args) {
        if (args.length == 1) {
            switch (args[0].toLowerCase()) {
                case "instance" -> instance();
                case "playerdata" -> playerData();
                case "gc" -> gc();
            }
        } else {
            all();
        }
        return true;
    }

    public static void all() {
        long start = System.currentTimeMillis();
        broadcast("§b[System.Clean]§aメモリ掃除を開始します");
        instance();
        playerData();
        gc();
        broadcast("§b[System.Clean]§aメモリ掃除が完了しました [" + (System.currentTimeMillis()-start) + "ms]");
    }

    public static void instance() {
        for (World world : Bukkit.getWorlds()) {
            if (WorldManager.instanceEmpty(world)) {
                WorldManager.deleteInstance(world);
            }
        }
        broadcast("§b[System.Clean]§a使用していないインスタンスを削除しました");
    }

    public static void playerData() {
        PlayerData.clean();
        broadcast("§b[System.Clean]§a使用していないプレイヤーキャッシュを削除しました");
    }

    public static void gc() {
        System.gc();
        broadcast("§b[System.Clean]§aガベージコレクションを実行します");
    }
}
