package SwordofMagic11.Enemy;

import SwordofMagic11.Component.SomTask;
import SwordofMagic11.Component.WorldManager;
import SwordofMagic11.Custom.CustomLocation;
import SwordofMagic11.Entity.SearchEntity;
import SwordofMagic11.SomCore;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static SwordofMagic11.Component.Function.randomDouble;
import static SwordofMagic11.Component.Function.randomInt;
import static SwordofMagic11.SomCore.Log;

public class SpawnerData {
    private String id;
    private MobData mobData;
    private int minLevel;
    private int maxLevel;
    private int radius;
    private int maxEnemy;
    private int coolTime;
    private Vector location;
    private final Set<World> worlds = new HashSet<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public MobData getMobData() {
        return mobData;
    }

    public void setMobData(MobData mobData) {
        this.mobData = mobData;
    }

    public int getMinLevel() {
        return minLevel;
    }

    public void setMinLevel(int minLevel) {
        this.minLevel = minLevel;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public int getMaxEnemy() {
        return maxEnemy;
    }

    public void setMaxEnemy(int maxEnemy) {
        this.maxEnemy = maxEnemy;
    }

    public int getCoolTime() {
        return coolTime;
    }

    public void setCoolTime(int coolTime) {
        this.coolTime = coolTime;
    }

    public Vector getLocation() {
        return location;
    }

    public void setLocation(Vector location) {
        this.location = location;
    }

    public void addWorld(World world) {
        worlds.add(world);
    }

    private final HashMap<World, Set<EnemyData>> allEnemies = new HashMap<>();

    public void start() {
        SomTask.asyncTimer(() -> {
            worlds.removeIf(WorldManager::isUnload);
            allEnemies.keySet().removeIf(world -> !worlds.contains(world));
            for (World world : worlds) {
                if (!allEnemies.containsKey(world)) allEnemies.put(world, new HashSet<>());
                CustomLocation center = new CustomLocation(world, location);
                if (SearchEntity.isInPlayerXZNoAFK(center, radius + 32)) {
                    Set<EnemyData> enemies = allEnemies.get(world);
                    enemies.removeIf(EnemyData::isInvalid);
                    if (maxEnemy > enemies.size()) {
                        CustomLocation spawnLocation = center.clone();
                        spawnLocation.add(randomInt(-radius, radius)+0.5, 3, randomInt(-radius, radius)+0.5);
                        spawnLocation.lower(16);
                        if (spawnLocation.isSpawnAble()) {
                            if (SearchEntity.isInPlayerXZNoAFK(spawnLocation, 24)) {
                                EnemyData.Rank rank;
                                double rankValue = randomDouble();
                                if (rankValue < 0.0001) {
                                    rank = EnemyData.Rank.Ultimate;
                                } else if (rankValue < 0.01) {
                                    rank = EnemyData.Rank.Elite;
                                } else {
                                    rank = EnemyData.Rank.Normal;
                                }
                                SomTask.sync(() -> {
                                    EnemyData enemyData = EnemyData.spawn(mobData, randomInt(minLevel, maxLevel), spawnLocation, rank);
                                    enemies.add(enemyData);
                                });
                            }
                        }
                    }
                }
            }
        }, coolTime, 50, SomCore.TaskOwner);
    }
}
