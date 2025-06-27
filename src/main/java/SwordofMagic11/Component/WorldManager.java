package SwordofMagic11.Component;

import SwordofMagic11.Command.Developer.MobClear;
import SwordofMagic11.DataBase.MapDataLoader;
import SwordofMagic11.Enemy.EnemyData;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Player.SomParty;
import SwordofMagic11.SomCore;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static SwordofMagic11.Map.MapData.ArshaMultiply;
import static SwordofMagic11.Player.Donation.InstanceBoosterMultiply;
import static SwordofMagic11.SomCore.Log;

public class WorldManager {

    private static final HashMap<World, Instance> instance = new HashMap<>();
    public static Instance instance(World world) {
        if (instance.containsKey(world)) {
            return instance.get(world);
        }
        throw new RuntimeException("Not Found WorldInstance");
    }
    public static Instance register(World world, String id) {
        Instance instance = new Instance(world, id);
        WorldManager.instance.put(world, instance);
        return instance;
    }

    public static class Instance {
        private final World world;
        private final String id;
        private boolean isLoaded = true;
        private boolean isInitialize = true;
        private boolean isArsha = false;
        private SomParty Party = null;

        public Instance(World world, String id) {
            this.world = world;
            this.id = id;
        }

        public void setLoaded(boolean loaded) {
            isLoaded = loaded;
        }

        public void setInitialize(boolean initialize) {
            isInitialize = initialize;
        }

        public void setArsha(boolean arsha) {
            isArsha = arsha;
        }

        public void setPrivate(SomParty party) {
            Party = party;
        }
    }

    public static boolean isLoaded(World world) {
        return instance.containsKey(world) && instance.get(world).isLoaded && Bukkit.getWorlds().contains(world);
    }

    public static boolean isUnload(World world) {
        return !isLoaded(world);
    }

    public static boolean isInitialize(World world) {
        return instance.containsKey(world) && instance.get(world).isInitialize;
    }

    public static boolean isGlobal(World world) {
        return instance.containsKey(world) && instance.get(world).Party == null;
    }

    public static boolean isPrivate(World world) {
        return instance.containsKey(world) && instance.get(world).Party != null;
    }

    public static SomParty getPrivate(World world) {
        return instance.get(world).Party;
    }

    public static boolean isArsha(World world) {
        return instance.containsKey(world) && instance.get(world).isArsha;
    }

    public static boolean isInstance(World world) {
        return world.getName().contains("Instance");
    }

    public static boolean hasInstance(World world) {
        return instance.containsKey(world);
    }

    public static World getContainer(String id) {
        return Bukkit.getWorld("WorldContainer_" + id);
    }

    public static String ID(World world) {
        if (world.getName().contains("WorldContainer")) {
            return world.getName().replace("WorldContainer_", "");
        }
        if (hasInstance(world)) {
            return instance(world).id;
        } else {
            return world.getPersistentDataContainer().get(Key, PersistentDataType.STRING);
        }
    }

    public static boolean isActive(World world) {
        return MapDataLoader.getComplete().contains(ID(world));
    }

    public static boolean isInsBoost(World world) {
        for (PlayerData playerData : PlayerData.getPlayerList(world)) {
            if (playerData.donation().hasInsBoost()) {
                return true;
            }
        }
        return false;
    }

    public static double totalMultiply(World world) {
        return 1 + (isArsha(world) ? ArshaMultiply : 0) + (isInsBoost(world) ? InstanceBoosterMultiply : 0);
    }

    public static final NamespacedKey Key = SomCore.Key("Id");

    public static void load() {
        File worldContainer = new File(".");
        List<String> container = new ArrayList<>();
        for (File file : worldContainer.listFiles()) {
            if (file.getName().contains("WorldContainer")) {
                container.add(file.getName());
            }
        }
        for (World world : Bukkit.getWorlds()) {
            container.remove(world.getName());
        }
        for (String id : container) {
            register(Bukkit.createWorld(new WorldCreator(id)), id);
        }
    }

    public static void deleteInstance(World world) {
        if (instance.containsKey(world)) {
            instance(world).setLoaded(false);
        }
        SomTask.sync(() -> {
            if (Bukkit.unloadWorld(world, false)) {
                SomTask.async(() -> {
                    instance.remove(world);
                    try {
                        FileUtils.deleteDirectory(world.getWorldFolder());
                        Log("§7delete instance " + world.getName());
                    } catch (IOException ignored) {
                        Log("§cdelete instance error " + world.getName());
                    }
                });
            } else {
                Log("§cUnload World Error " + world.getName());
            }
        });
    }

    public static void instanceChecker() {
        SomTask.asyncTimer(() -> {
            for (World world : instance.keySet()) {
                if (instanceEmpty(world)) {
                    Instance instance = instance(world);
                    if (isGlobal(world)) {
                        if (!isInitialize(world)) {
                            SomTask.sync(() -> {
                                MobClear.clearForce(world);
                                instance.setInitialize(true);
                            });
                        }
                    } else if (isPrivate(world)) {
                        if (!SomParty.partyList.contains(getPrivate(world))) {
                            deleteInstance(world);
                        }
                    } else {
                        deleteInstance(world);
                    }
                }
            }
        }, 200, SomCore.TaskOwner);
    }

    public static boolean instanceEmpty(World world) {
        return world.getName().contains("WorldInstance") && world.getPlayerCount() == 0;
    }

    public static World createInstance(String id, String suffix) {
        try {
            String baseId = "WorldContainer_" + id;
            String newId = "WorldInstance_" + suffix + "_" + id;
            FileUtils.copyDirectory(new File(SomCore.Root.getParent(), "ServerDev\\" + baseId), new File(newId), WorldManager::pathIgnore);
            World world = Bukkit.createWorld(new WorldCreator(newId));
            assert world != null;
            world.getPersistentDataContainer().set(Key, PersistentDataType.STRING, newId);
            world.setAutoSave(false);
            register(world, id);
            return world;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean pathIgnore(File pathname) {
        Collection<String> ignore = new HashSet<>();
        ignore.add("session.lock");
        ignore.add("uid.dat");
        ignore.add("paper-world.yml");
        for (String str : ignore) {
            if (pathname.getName().contains(str)) {
                return false;
            }
        }
        return true;
    }
}
