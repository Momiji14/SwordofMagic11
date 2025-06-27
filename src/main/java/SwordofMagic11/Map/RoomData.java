package SwordofMagic11.Map;

import SwordofMagic11.Command.Developer.MobClear;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Component.SomTask;
import SwordofMagic11.Component.WorldManager;
import SwordofMagic11.Custom.CustomLocation;
import SwordofMagic11.Custom.UnsetLocation;
import SwordofMagic11.Enemy.EnemyData;
import SwordofMagic11.Enemy.MobData;
import SwordofMagic11.Player.Menu.BossTimeAttackMenu;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.SomCore;
import SwordofMagic11.StatusType;
import org.bukkit.World;

import java.util.*;

import static SwordofMagic11.Component.Function.scale;
import static SwordofMagic11.SomCore.Log;

public class RoomData {

    private MapData mapData;
    private MobData mobData;
    private int level;
    private UnsetLocation spawn;
    private UnsetLocation exit;
    private UnsetLocation enter;
    private MapData exitMap;
    private HashMap<StatusType, Double> limitStatus = new HashMap<>();

    public RoomData(MapData mapData) {
        this.mapData = mapData;
    }


    public MobData getMobData() {
        return mobData;
    }

    public void setMobData(MobData mobData) {
        this.mobData = mobData;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public UnsetLocation getSpawn() {
        return spawn;
    }

    public void setSpawn(UnsetLocation spawn) {
        this.spawn = spawn;
    }

    public UnsetLocation getExit() {
        return exit;
    }

    public void setExit(UnsetLocation exit) {
        this.exit = exit;
    }

    public MapData getExitMap() {
        return exitMap;
    }

    public void setExitMap(MapData exitMap) {
        this.exitMap = exitMap;
    }

    public void setEnter(UnsetLocation enter) {
        this.enter = enter;
    }

    public UnsetLocation getEnter() {
        return enter;
    }

    public HashMap<StatusType, Double> getLimitStatus() {
        return limitStatus;
    }

    public void setLimitStatus(HashMap<StatusType, Double> limitStatus) {
        this.limitStatus = limitStatus;
    }

    public void addWorld(World world) {
        worlds.add(world);
    }

    public void registerTimeAttack(World world) {
        isTimeAttack.add(world);
    }

    private final Set<World> worlds = new HashSet<>();
    private final Set<World> isEnding = new HashSet<>();
    private final Set<World> isTimeAttack = new HashSet<>();
    private final Set<PlayerData> playerEntry = new HashSet<>();
    private final HashMap<World, EnemyData> bossTable = new HashMap<>();

    public void start() {
        SomTask.asyncTimer(() -> {
            worlds.removeIf(WorldManager::isUnload);
            bossTable.keySet().removeIf(world -> !worlds.contains(world));
            playerEntry.removeIf(playerData -> {
                if (!playerData.bossModeMenu().isInBossTimeAttackMode()) {
                    playerData.resetLimitStatus();
                    return true;
                }
                return false;
            });
            for (World world : worlds) {
                if (!isEnding.contains(world)) {
                    if (bossTable.containsKey(world)) {
                        if (bossTable.get(world) == null) {
                            for (PlayerData playerData : PlayerData.getPlayerList(world)) {
                                playerData.sendTitle("§cBoss Room !", "§c" + mobData.getDisplay() + "を討伐してください", 10, 90, 10);
                                playerData.sendMessage("§c" + mobData.getDisplay() + "を討伐してください", SomSound.BossSpawn);
                            }
                            SomTask.sync(() -> bossTable.put(world, EnemyData.spawn(mobData, level, spawn.as(world))));
                            continue;
                        }
                        if (isTimeAttack.contains(world)) playerEntry.addAll(PlayerData.getPlayerList(world));
                        EnemyData enemyData = bossTable.get(world);
                        if (enemyData.isDeath()) {
                            isEnding.add(world);
                            double clearTime = enemyData.getLiveTime()/1000.0;
                            int playerCount = PlayerData.getPlayerList(world).size();
                            for (PlayerData playerData : PlayerData.getPlayerList(world)) {
                                playerData.silence(100, playerData);
                                playerData.timer("BossClear", 100);
                                playerData.sendTitle("§eBoss Clear !", "§a5.0秒後転送されます", 10, 90, 10);
                                playerData.sendMessage("§c" + bossTable.get(world).getName() + "§aを§c討伐§aしました！ §e[" + scale(clearTime, 3) + "秒]", SomSound.Winner);
                                if (isTimeAttack.contains(world)) {
                                    double takeDamage = enemyData.getTakeDamageTable().getOrDefault(playerData, 0.0);
                                    double makeDamage = enemyData.getMakeDamageTable().getOrDefault(playerData, 0.0);
                                    playerData.bossTimeAttackMenu().trigger(new BossTimeAttackMenu.ClearRecord(mapData, clearTime, playerCount, playerData.classes().getMainClass(), makeDamage, takeDamage));
                                }
                            }
                            isTimeAttack.remove(world);
                            SomTask.syncDelay(() -> {
                                CustomLocation location;
                                if (WorldManager.isPrivate(world)) {
                                    location = exit.as(exitMap.getPrivateInstance(WorldManager.getPrivate(world)));
                                } else {
                                    location = exit.as(exitMap.getGlobalInstance(WorldManager.isArsha(world)));
                                }
                                isEnding.remove(world);
                                bossTable.remove(world);
                                for (PlayerData playerData : PlayerData.getPlayerList(world)) {
                                    if (playerData.hasParty()) {
                                        if (playerData.getParty().isBossRepeat()) {
                                            continue;
                                        }
                                    }
                                    playerData.teleport(location);
                                }
                            }, 100);
                        } else if (enemyData.isInvalid()) {
                            bossTable.remove(world);
                        }
                    } else if (world.getPlayerCount() > 0) {
                        bossTable.put(world, null);
                    }
                }
            }
        }, 30, SomCore.TaskOwner);
    }
}
