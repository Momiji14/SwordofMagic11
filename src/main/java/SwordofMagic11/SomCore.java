package SwordofMagic11;

import SwordofMagic11.Command.CommandRegister;
import SwordofMagic11.Command.Developer.Clean;
import SwordofMagic11.Command.Developer.MobClear;
import SwordofMagic11.Component.SomSQL;
import SwordofMagic11.Component.SomTask;
import SwordofMagic11.Component.WorldManager;
import SwordofMagic11.DataBase.DataBase;
import SwordofMagic11.DataBase.MapDataLoader;
import SwordofMagic11.Entity.Cardinal;
import SwordofMagic11.Entity.Damage;
import SwordofMagic11.Entity.SomEntity;
import SwordofMagic11.Item.SomEquip;
import SwordofMagic11.Map.DefenseBattle;
import SwordofMagic11.Map.MapData;
import SwordofMagic11.Map.RaidScheduler;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Player.PlayerVote;
import SwordofMagic11.Map.PvPRaid;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Team;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import static SwordofMagic11.Component.Function.broadcast;

public final class SomCore extends JavaPlugin {

    private static Plugin plugin;
    public static boolean SomReload = false;
    public static boolean FullReload = true;
    public static String ID = "Error";
    public static final TaskOwner TaskOwner = () -> true;
    public static NamespacedKey SomEntityKey;
    public static NamespacedKey SomNPCKey;
    public static NamespacedKey SomParticle;
    public static SomEntity Cardinal;
    //public static Team NameInvisible;

    public static NamespacedKey Key(String key) {
        return new NamespacedKey(plugin(), key);
    }
    public static File Root = new File(".").getAbsoluteFile().getParentFile();
    public static String SNCChannel = "snc:main";
    public static boolean Restart = false;
    private BukkitTask restartTimer;

    public static boolean isDev() {
        return ID.equals("ServerDev");
    }

    public static Location defaultSpawn() {
        MapData mapData = MapDataLoader.getMapData("Hanjibal");
        return mapData.getGlobalInstance(false).getSpawnLocation();
    }

    @Override
    public void onEnable() {
        plugin = this;
        saveDefaultConfig();
        reloadConfig();
        ID = Root.getName();
        SomEntityKey = SomCore.Key("SomEntity");
        SomNPCKey = SomCore.Key("SomNPC");
        SomParticle = SomCore.Key("SomParticle");
        Cardinal = new Cardinal();
        //NameInvisible = Bukkit.getScoreboardManager().getMainScoreboard().getTeam("NameInvisible");
        //if (NameInvisible == null) NameInvisible = Bukkit.getScoreboardManager().getMainScoreboard().registerNewTeam("NameInvisible");
        //NameInvisible.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, SNCChannel);

        MobClear.clear();
        SomSQL.connection();

        SomEquip.Category.load();
        if (isDev()) WorldManager.load();
        DataBase.load();
        CommandRegister.run();
        SomTask.run();

        PvPRaid.load();
        WorldManager.instanceChecker();
        Damage.run();
        DefenseBattle.initialize();
        RaidScheduler.run();

        plugin.getServer().getPluginManager().registerEvents(new Events(), this);
        if (ID.equals("Server1")) plugin.getServer().getPluginManager().registerEvents(new PlayerVote(), this);

        SomTask.syncDelay(() -> {
            for (Player player : getPlayers()) {
                PlayerData.create(player).load();
                //NameInvisible.addPlayer(player);
            }
        }, 10);

        World world = Bukkit.getWorld("world");
        SomTask.syncTimer(() -> {
            if (!world.getPlayers().isEmpty()) {
                Location defaultSpawn = defaultSpawn();
                for (Player player : world.getPlayers()) {
                    player.teleport(defaultSpawn);
                }
            }
        }, 200, 200, TaskOwner);

//        SomTask.asyncTimer(() -> {
//            broadcast("§b[System.Clean]§a5秒後、メモリ掃除を行うためラグが発生するかもしれません");
//            SomTask.syncDelay(Clean::all, 100);
//        }, 20*3600, TaskOwner);

//        SomTask.asyncDelay(() -> {
//            restartTimer = SomTask.asyncTimer(() -> {
//                LocalDateTime time = LocalDateTime.now();
//                if (time.getHour() == 4) {
//                    restartTimer.cancel();
//                    Restart = true;
//                    broadcast("§b[System.restart]§a60秒後、サーバーを再起動します");
//                    SomTask.asyncDelay(() -> {
//                        broadcast("§b[System.restart]§aサーバーを再起動します");
//                        for (Player player : Bukkit.getOnlinePlayers()) {
//                            player.kick(Component.text("§aサーバーを再起動します"));
//                        }
//                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "restart");
//                    }, 20*60);
//                }
//            }, 20*30, TaskOwner);
//        }, 20*60*60);

        Log("§b[Som11]§aEnable");
    }

    @Override
    public void onDisable() {
        MobClear.clear();
        //WorldManager.unload();
        Bukkit.getBossBars().forEachRemaining(BossBar::removeAll);
        this.getServer().getMessenger().unregisterOutgoingPluginChannel(this);
        this.getServer().getMessenger().unregisterIncomingPluginChannel(this);
    }

    public static Plugin plugin() {
        return plugin;
    }

    public static Collection<Player> getPlayers() {
        Collection<Player> list = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.isOnline()) {
                list.add(player);
            }
        }
        return list;
    }

    public static void Log(Exception e) {
        Log(e.toString());
        for (int i = 0; i < e.getStackTrace().length; i++) {
            Log(e.getStackTrace()[i].toString());
        }
    }

    public static void Log(String log) {
        Log(log, false);
    }

    public static void Log(String log, boolean stacktrace) {
        for (Player player : SomCore.getPlayers()) {
            if (player.hasPermission("som10.reload")) {
                player.sendMessage(log);
            }
        }
        Bukkit.getLogger().info(log);
        if (stacktrace) {
            new RuntimeException("StackTrace Log").printStackTrace();
        }
    }

    public static void sendMessageComponent(Player player, Component component) {
        try {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(b);
            out.writeUTF("Component");
            out.writeUTF(player.getName());
            out.writeUTF(JSONComponentSerializer.json().serialize(component));
            player.sendPluginMessage(SomCore.plugin(), SNCChannel, b.toByteArray());
            b.close();
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void sendGlobalMessageComponent(Component component) {
        try {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                ByteArrayOutputStream b = new ByteArrayOutputStream();
                DataOutputStream out = new DataOutputStream(b);
                out.writeUTF("GlobalComponent");
                out.writeUTF(JSONComponentSerializer.json().serialize(component));
                onlinePlayer.sendPluginMessage(SomCore.plugin(), SNCChannel, b.toByteArray());
                b.close();
                out.close();
                return;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Location getGlobalSpawnLocation() {
        return MapDataLoader.getMapData("Hanjibal").getGlobalInstance(false).getSpawnLocation();
    }
}
