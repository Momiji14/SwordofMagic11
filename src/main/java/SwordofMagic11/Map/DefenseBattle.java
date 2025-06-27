package SwordofMagic11.Map;

import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Component.SomTask;
import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.Custom.CustomLocation;
import SwordofMagic11.DataBase.MapDataLoader;
import SwordofMagic11.DataBase.MaterialDataLoader;
import SwordofMagic11.DataBase.MobDataLoader;
import SwordofMagic11.Enemy.EnemyData;
import SwordofMagic11.Enemy.MobData;
import SwordofMagic11.Entity.SomEffect;
import SwordofMagic11.Item.Material.MaterialData;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.SomCore;
import SwordofMagic11.StatusType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import static SwordofMagic11.Component.Function.*;
import static SwordofMagic11.SomCore.Log;

public class DefenseBattle {

    public static DefenseBattle Instance;
    public static double BaseHealthMultiply = 50.0;
    public static double BossHealthMultiply = 4.0;

    public static final String Display = "§c防衛戦";
    public static CustomItemStack viewItem() {
        CustomItemStack item = new CustomItemStack(Material.CRYING_OBSIDIAN);
        item.setDisplay(Display);
        item.addLore("§a一致団結し§eエネミー§aから§bコア§aを守ります");
        item.addSeparator("開催情報");
        if (isOpen) {
            item.addLore("§7・§cWave" + Instance.wave);
            item.addLore(decoLore("参加人数") + Instance.member.size() + "人");
        } else {
            item.addLore(decoLore("開催状況") + "§c未開催");
        }
        item.setCustomData("DefenseBattle", true);
        return item;
    }

    public static void clickItem(PlayerData playerData) {
        if (isOpen) {
            if (playerData.getMap().isCity()) {
                Instance.joinPlayer(playerData);
            } else {
                playerData.sendReqInCity();
            }
        } else {
            playerData.sendMessage("§c現在は開催されていません", SomSound.Nope);
        }
    }

    private static World World;
    private static CustomLocation SpawnLocation;
    private static final CustomLocation[][] Pathfinder = new CustomLocation[3][7];
    private static final String Prefix = "§c[防衛戦]§r";
    public static final List<MobData> mobList = new ArrayList<>();
    private static final List<MobData> bossList = new ArrayList<>();
    private static final SomEffect normalEffect = SomEffect.EnemyEffect("DefenseBattle");
    private static final SomEffect bossEffect = SomEffect.EnemyEffect("DefenseBattleBoss");

    public static void initialize() {
        for (MobData mobData : MobDataLoader.getMobDataList()) {
            if (mobData.hasMemorial() && !mobData.isObject() && mobData.isDefenseBattle()) {
                switch (mobData.getRank()) {
                    case Normal -> mobList.add(mobData);
                    case Middle -> bossList.add(mobData);
                }
            }
        }

        normalEffect.setStatus(StatusType.Movement, 75);
        normalEffect.setStatusMultiplyDefense(-0.95);
        normalEffect.setStatusMultiply(StatusType.CriticalResist,-0.7);
        normalEffect.setStatusMultiply(StatusType.MaxHealth, BaseHealthMultiply);

        bossEffect.setStatus(StatusType.Movement, 100);
        bossEffect.setStatusMultiplyDefense(-0.90);
        bossEffect.setStatusMultiply(StatusType.CriticalResist,-0.5);
        bossEffect.setStatusMultiply(StatusType.MaxHealth, BaseHealthMultiply * BossHealthMultiply);

    }

    public static boolean isOpen = false;

    public static void open() {
        isOpen = true;
        if (Instance != null) Instance.delete();
        Instance = new DefenseBattle();
        broadcast(Display + "§aが§b開催§aしました", SomSound.BossSpawn);
    }

    public static void openToggle() {
        isOpen = !isOpen;
        if (isOpen) {
            Instance = new DefenseBattle();
            broadcast(Display + "§aが§b開催§aしました");
        } else {
            Instance.delete();
            broadcast(Display + "§aが§c終了§aされました");
        }
    }

    private int wave = 1;
    private final long gameStart = System.currentTimeMillis();
    private final Set<PlayerData> member = new CopyOnWriteArraySet<>();
    private final ConcurrentHashMap<Integer, Set<EnemyData>> enemies = new ConcurrentHashMap<>();
    private final HashMap<PlayerData, Double> scoreMap = new HashMap<>();
    private boolean start = false;
    private boolean end = false;
    private int CoreMaxHealth = 200;
    private int CoreHealth = CoreMaxHealth;
    private long startTime = 0;
    private final BossBar bossBar = Bukkit.createBossBar("§eマッチング待機中...", BarColor.YELLOW, BarStyle.SEGMENTED_10);

    private final MapData mapData;
    private BukkitTask task;
    public DefenseBattle() {
        for (int i = 0; i < Pathfinder.length; i++) {
            enemies.put(i, new HashSet<>());
        }
        mapData = MapDataLoader.getMapData("DefenseBattle");

        task = SomTask.asyncTimer(() -> {
            for (PlayerData member : member) {
                if (!member.isValid()) {
                    resetPlayer(member);
                }
            }
        }, 50, member::isEmpty, SomCore.TaskOwner);
    }

    public void delete() {
        if (task != null) task.cancel();
        end = true;
        isOpen = false;
        enemies.values().forEach(enemies -> enemies.forEach(EnemyData::delete));
        for (PlayerData playerData : member) {
            resetPlayer(playerData);
        }
    }

    public void setWave(int wave) {
        this.wave = wave;
    }

    public void addScore(PlayerData playerData, EnemyData victim, double damage) {
        if (isBonus(victim)) {
            damage *= 10;
        }
        if (victim.hasEffect("DefenseBattleBoss")) {
            damage *= BossHealthMultiply;
        }
        scoreMap.merge(playerData, damage / victim.getStatus(StatusType.MaxHealth) * 10000, Double::sum);
    }

    public Set<PlayerData> getMember() {
        return member;
    }

    public Set<EnemyData> getEnemies() {
        Set<EnemyData> list = new HashSet<>();
        for (Set<EnemyData> enemies : enemies.values()) {
            list.addAll(enemies);
        }
        return list;
    }

    public void joinPlayer(PlayerData playerData) {
        member.add(playerData);
        bossBar.addPlayer(playerData.getPlayer());
        sendMessage(playerData.getDisplayName() + "§aが§b参加§aしました", SomSound.Tick);
        if (start) {
            joinTeleport(playerData);
        } else if (member.size() >= 4) {
            start();
        }
    }

    public void resetPlayer(PlayerData playerData) {
        sendMessage(playerData.getDisplayName() + "§aが" + (playerData.isAFK() ? "§7AFK§aにより§4強制退場" : "§c退場") + "§aしました", SomSound.Tick);
        try {
            member.remove(playerData);
            bossBar.removePlayer(playerData.getPlayer());
            SomTask.sync(() -> playerData.teleport(playerData.lastSpawnLocation()));
        } catch (Exception e) {
            Log("DefenseBattleError -> resetPlayer -> " + playerData.getName());
        }
    }

    public int getWave() {
        return wave;
    }

    public long getGameStart() {
        return gameStart;
    }

    public boolean isEnd() {
        return end;
    }

    public long getStartTime() {
        return startTime;
    }

    public void joinTeleport(PlayerData playerData) {
        playerData.teleport(SpawnLocation);
        mapData.enter(playerData, false);
    }

    public void startInti() {
        World = MapDataLoader.getMapData("DefenseBattle").getGlobalInstance(false);
        SpawnLocation = new CustomLocation(World, 2.5, -7, 15.5);

        Pathfinder[0][0] = new CustomLocation(World, -94, -37, 9);
        Pathfinder[0][1] = new CustomLocation(World, -56, -32, -8);
        Pathfinder[0][2] = new CustomLocation(World, -56, -26, 27);
        Pathfinder[0][3] = new CustomLocation(World, -46, -24, 27);
        Pathfinder[0][4] = new CustomLocation(World, -46, -19, -7);
        Pathfinder[0][5] = new CustomLocation(World, -38, -17, -7);
        Pathfinder[0][6] = new CustomLocation(World, -30, -8, 25);

        Pathfinder[1][0] = new CustomLocation(World, -2, -37, -84);
        Pathfinder[1][1] = new CustomLocation(World, -22, -34, -51);
        Pathfinder[1][2] = new CustomLocation(World, 14, -23, -48);
        Pathfinder[1][3] = new CustomLocation(World, 14, -21, -36);
        Pathfinder[1][4] = new CustomLocation(World, -8, -17, -35);
        Pathfinder[1][5] = new CustomLocation(World, -7, -13, -24);
        Pathfinder[1][6] = new CustomLocation(World, 24, -8, -24);

        Pathfinder[2][0] = new CustomLocation(World, 107, -37, 9);
        Pathfinder[2][1] = new CustomLocation(World, 70, -33, -8);
        Pathfinder[2][2] = new CustomLocation(World, 65, -26, 27);
        Pathfinder[2][3] = new CustomLocation(World, 53, -24, 25);
        Pathfinder[2][4] = new CustomLocation(World, 50, -18, -9);
        Pathfinder[2][5] = new CustomLocation(World, 35, -15, -9);
        Pathfinder[2][6] = new CustomLocation(World, 35, -8, 25);
    }

    public boolean isBonus(EnemyData enemyData) {
        switch (enemyData.getMobData().getEntityType()) {
            case OCELOT, BEE -> {
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    public void start() {
        if (!start) {
            start = true;
            sendTitleMessage("§eMember Ready","§eメンバーがそろいました", "§e開始条件§aを満たしたため§c防衛戦§aを§b開始§aします", SomSound.Level, 60);
            SomTask.sync(() -> {
                startInti();
                SomTask.asyncDelay(() -> {
                    for (PlayerData playerData : member) {
                        joinTeleport(playerData);
                    }
                    sendTitle("","§eまもなく準備フェーズです", 10, 30, 10, SomSound.Tick);
                    startTime = System.currentTimeMillis();
                    if (task != null) task.cancel();
                    task = new BukkitRunnable() {
                        int readyTime = 15;
                        int leftCount = 0;
                        int enemyCount;
                        int spawnCount;
                        boolean readyPhase = true;
                        boolean coreAttack = false;
                        boolean bossWave = false;
                        boolean bossSpawn = false;
                        @Override
                        public void run() {
                            if (readyPhase) {
                                int skipMember = 0;
                                for (PlayerData member : member) if (member.getPlayer().isSneaking()) skipMember++;
                                int border = (int) (member.size() * 0.7);
                                if (border <= skipMember) readyTime = 0;
                                bossBar.setTitle("§e準備フェーズ " + readyTime + "秒");
                                for (PlayerData playerData : member) {
                                    playerData.sendTitle("§e準備フェーズ " + readyTime + "秒", "§aスニークをしているとスキップされます ( " + skipMember + " / "+ border + " )", 0, 21, 0);
                                    if (readyTime <= 5) SomSound.Tick.play(playerData);
                                }
                                readyTime--;
                                if (readyTime < 0) {
                                    leftCount = count();
                                    bossBar.setColor(BarColor.RED);
                                    sendTitleMessage("§4§nWave " + wave, "§c戦闘フェーズ", "§eWave" + wave + "§aを§b開始§aします", SomSound.BossSpawn);
                                    bossBar.setColor(BarColor.RED);
                                    bossBar.setTitle("§c戦闘フェーズ");
                                    readyPhase = false;
                                    bossSpawn = true;
                                    scoreMap.clear();
                                    bossWave = Math.floorMod(wave, 3) == 0;
                                }
                            } else {
                                coreAttack = false;
                                String id = "DefenseBattle";
                                enemies.forEach((index, enemies) -> {
                                    enemies.removeIf(EnemyData::isInvalid);
                                    for (EnemyData enemy : enemies) {
                                        if (enemy.getOverrideLocation() == SpawnLocation) {
                                            if (enemy.getLocation().distance(SpawnLocation) < 5) {
                                                CoreHealth--;
                                                coreAttack = true;
                                            }
                                        } else if (enemy.getOverrideLocation().distance(enemy.getLocation()) < 2.0) {
                                            enemy.setInt(id, enemy.getInt(id)+1);
                                            if (enemy.getInt(id) < Pathfinder[index].length) {
                                                enemy.setOverrideLocation(Pathfinder[index][enemy.getInt(id)].clone().addXZ(randomDouble(-1.5, 1.5), randomDouble(-1.5, 1.5)));
                                            } else {
                                                enemy.setOverrideLocation(SpawnLocation);
                                            }
                                        } else {
                                            for (int i = 0; i < Pathfinder[index].length; i++) {
                                                if (enemy.getLocation().getY() < Pathfinder[index][i].getY()) {
                                                    enemy.setInt(id, i);
                                                    enemy.setOverrideLocation(Pathfinder[index][i]);
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                });
                                if (coreAttack) {
                                    sendTitle("", "§dディメンションコア§aが§c攻撃§aされています", 0, 20, 5, SomSound.Nope);
                                }
                                spawnCount = 0;
                                for (Set<EnemyData> enemies : enemies.values()) {
                                    spawnCount += enemies.size();
                                }
                                enemyCount = leftCount + spawnCount;
                                bossBar.setTitle("§cWave" + wave + "    エネミー数 " + enemyCount + " §8(" + leftCount + ")");
                                if (leftCount > 0) {
                                    SomTask.sync(() -> {
                                        for (int i = 0; i < 9; i++) {
                                            if (leftCount > 0 && spawnCount < 64) {
                                                int index = randomInt(0, Pathfinder.length-1);
                                                EnemyData enemyData = EnemyData.spawn(randomGet(mobList), level(), Pathfinder[index][0]);
                                                enemyData.setOverrideLocation(Pathfinder[index][1]);
                                                enemyData.addTag(id);
                                                enemyData.addEffect(normalEffect);
                                                enemyData.statusUpdate();
                                                enemyData.heal();
                                                enemyData.ignoreCrowdControl(true);
                                                if (isBonus(enemyData)) {
                                                    sendMessage(enemyData.getName() + "が§e" + index + "番ゲート§aに§4出現§aしました", SomSound.Tick);
                                                }
                                                enemies.get(index).add(enemyData);
                                                spawnCount++;
                                                leftCount--;
                                            } else break;
                                        }
                                        if (bossWave && bossSpawn) {
                                            //bossSpawn();
                                            bossSpawn = false;
                                        }
                                    });
                                } else {
                                    if (enemyCount <= 5) {
                                        enemies.values().forEach(enemies -> enemies.forEach(enemy -> enemy.getEntity().setGlowing(true)));
                                        if (enemyCount <= 0) {
                                            sendTitleMessage("§b§nWave " + wave + " Clear!", "§eフェーズクリア", "§eWave" + wave + "§aを§bクリア§aしました", SomSound.Level);

                                            HashMap<PlayerData, Double> multiples = new HashMap<>();
                                            List<Map.Entry<PlayerData, Double>> ranking = new ArrayList<>(scoreMap.entrySet());
                                            ranking.sort(Map.Entry.comparingByValue());
                                            Collections.reverse(ranking);
                                            List<String> rankingMessage = new ArrayList<>();
                                            rankingMessage.add(decoText("スコアランキング"));
                                            int index = 1;
                                            for (Map.Entry<PlayerData, Double> entry : ranking) {
                                                rankingMessage.add("§e[" + index + "位]§r" + entry.getKey().getName() + "§7: §a" + scale(entry.getValue()));
                                                multiples.put(entry.getKey(), 1 + (16 - index) * 0.075);
                                                index++;
                                                if (index > 15) break;
                                            }

                                            for (PlayerData playerData : member) {
                                                if (!playerData.isAFK()) {
                                                    int level = level();
                                                    double multiply = multiples.getOrDefault(playerData, 1.0);
                                                    int mel = (int) (wave * 1000 * multiply);
                                                    double exp = Math.pow(level, 3.0) * multiply;
                                                    int grinderAmount = (int) (wave * 15 * multiply);
                                                    List<String> message = new ArrayList<>(rankingMessage);
                                                    message.add(decoText("Wave" + wave + " Reward"));
                                                    message.add("§7・§a" + mel + "メル");
                                                    message.add("§7・§a" + scale(exp) + "Exp");
                                                    message.add("§7・§fグラインダーx" + grinderAmount);
                                                    playerData.addMel(mel);
                                                    playerData.classes().addExp(exp);
                                                    playerData.addMaterial("グラインダー", grinderAmount);
                                                    if (multiply > 1.0) {
                                                        double percent =  multiply * wave * 0.0001;
                                                        if (randomDouble() < percent) {
                                                            MaterialData material = MaterialDataLoader.getMaterialData("ディメンションコアMemorial");
                                                            message.add("§7・§b" + material.getDisplay() + " §7(" + scale(percent * 100, 3) + ")");
                                                            playerData.addMaterial(material, 1);
                                                            broadcast(playerData.getDisplayName() + "§aさんが§b" + material.getDisplay() + "§aを§e獲得§aしました");
                                                        }
                                                    }
                                                    playerData.sendMessage(message);
                                                }
                                            }
                                            bossWave = false;
                                            readyTime = 10;
                                            wave++;
                                            bossBar.setColor(BarColor.YELLOW);
                                            readyPhase = true;
                                        }
                                    }
                                }
                            }
                            bossBar.setProgress(MinMax((double) CoreHealth/CoreMaxHealth,0, 1));
                            if (CoreHealth < 0) {
                                this.cancel();
                                sendTitleMessage("§4§nDimensionCore is Broken", "§dディメンションコア§aが§4破壊§aされました", "§dディメンションコア§aが§4破壊§aされました", SomSound.ErrorOver, 80);
                                SomTask.asyncDelay(() -> delete(), 100);
                            }
                            if (!readyPhase && System.currentTimeMillis()- gameStart > 1000*60*45) {
                                this.cancel();
                                sendTitleMessage("§4§nTime Over", "§4タイムオーバー", "§4タイムオーバー", SomSound.ErrorOver, 80);
                                SomTask.asyncDelay(() -> delete(), 100);
                            }
                            for (PlayerData member : member) {
                                if (member.isInvalid() || member.isAFK() || member.getMap() != mapData) {
                                    resetPlayer(member);
                                }
                            }
                            if (member.isEmpty()) {
                                this.cancel();
                                delete();
                            }
                        }
                    }.runTaskTimerAsynchronously(SomCore.plugin(), 60, 20);
                }, 60);
            });
        }
    }

    public void bossSpawn() {
        CustomLocation location = new CustomLocation(World, -1004.5, -37, -58.5);
        MobData mobData = randomGet(bossList);
        EnemyData enemyData = EnemyData.spawnForceNormal(mobData, level(), location);
        enemyData.setOverrideLocation(Pathfinder[1][1]);
        enemyData.addTag("DefenseBattle");
        enemyData.addEffect(bossEffect);
        enemyData.statusUpdate();
        enemyData.heal();
        enemyData.ignoreCrowdControl(true);
        enemies.get(1).add(enemyData);
        sendMessage("§4" + mobData.getDisplay() + "§aが§c侵入§aしてきました", SomSound.BossSpawn);
    }

    public int count() {
        return (wave-1)*3 + 30;
    }

    public int level() {
        return level(wave);
    }

    public int level(int wave) {
        return ((wave-1) * 15);
    }

    public void sendMessage(String message, SomSound sound) {
        for (PlayerData playerData : member) {
            playerData.sendMessage(Prefix + message, sound);
        }
    }

    public void sendTitle(String title, String subtitle, int fade, int time, int out, SomSound sound) {
        for (PlayerData playerData : member) {
            playerData.sendTitle(title, subtitle, fade, time, out);
            sound.play(playerData);
        }
    }

    public void sendTitleMessage(String title, String subtitle, String message, SomSound sound) {
        sendTitleMessage(title, subtitle, message, sound, 10);
    }

    public void sendTitleMessage(String title, String subtitle, String message, SomSound sound, int time) {
        for (PlayerData playerData : member) {
            playerData.sendTitle(title, subtitle, 10, time, 10);
            playerData.sendMessage(Prefix + message, sound);
        }
    }
}
