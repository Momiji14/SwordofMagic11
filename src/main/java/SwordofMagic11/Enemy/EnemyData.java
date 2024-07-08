package SwordofMagic11.Enemy;

import SwordofMagic11.Component.SomTask;
import SwordofMagic11.Component.WorldManager;
import SwordofMagic11.Custom.CustomLocation;
import SwordofMagic11.DataBase.CapsuleDataLoader;
import SwordofMagic11.DataBase.MaterialDataLoader;
import SwordofMagic11.Enemy.Custom.EnemyParent;
import SwordofMagic11.Entity.Damage;
import SwordofMagic11.Entity.SearchEntity;
import SwordofMagic11.Entity.SomEffect;
import SwordofMagic11.Entity.SomEntity;
import SwordofMagic11.Item.CapsuleData;
import SwordofMagic11.Item.Material.MaterialData;
import SwordofMagic11.Map.DefenseBattle;
import SwordofMagic11.Pet.PetEntity;
import SwordofMagic11.Pet.PetLevelType;
import SwordofMagic11.Player.ClassType;
import SwordofMagic11.Player.Classes;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.SomCore;
import SwordofMagic11.StatusType;
import SwordofMagic11.TaskOwner;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.*;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BoundingBox;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

import static SwordofMagic11.Component.Function.*;
import static SwordofMagic11.DataBase.DataBase.PercentValue;
import static SwordofMagic11.DataBase.MobDataLoader.MemorialPercent;
import static SwordofMagic11.SomCore.Log;

public class EnemyData extends SomEntity {
    public static final int MaxMelDrop = 200;
    public static final int MelDropMultiply = 10;
    public static final int MaxLevel = 9999;
    private static final CopyOnWriteArrayList<EnemyData> enemyList = new CopyOnWriteArrayList<>();

    public static Collection<SomEntity> asSomEntities() {
        return new ArrayList<>(enemyList);
    }

    public static Collection<EnemyData> getList(World world) {
        Collection<EnemyData> list = new HashSet<>();
        for (EnemyData enemyData : enemyList) {
            if (enemyData.getWorld() == world) {
                list.add(enemyData);
            }
        }
        return list;
    }

    private final MobData mobData;
    private final EnemyParent parent;
    private final int level;
    private final Rank rank;
    private SomEntity target;
    private Location lastTargetLocation;
    private Location overrideLocation;
    private BukkitTask aiTask;
    private final Mob mob;
    private final BossBar bossBar;
    private final ConcurrentHashMap<SomEntity, Double> hateTable = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<SomEntity, Double> takeDamageTable = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<SomEntity, Double> makeDamageTable = new ConcurrentHashMap<>();
    private final CopyOnWriteArraySet<PlayerData> interactPlayer = new CopyOnWriteArraySet<>();
    private Disguise disguise;
    private long spawnTime;
    protected CustomLocation spawnLocation;
    private boolean delete = false;
    private HashMap<String, Integer> valueInt = new HashMap<>();
    private Set<String> tag = new HashSet<>();

    public static EnemyData spawn(MobData mobData, int level, Location location) {
        return spawn(mobData, level, location, null, Rank.Normal);
    }

    public static EnemyData spawn(MobData mobData, int level, Location location, Rank rank) {
        return spawn(mobData, level, location, null, rank);
    }

    public static EnemyData spawn(MobData mobData, int level, Location location, EnemyParent parent) {
        return spawn(mobData, level, location, parent, Rank.Normal);
    }

    public static EnemyData spawn(MobData mobData, int level, Location location, EnemyParent parent, Rank rank) {
        if (WorldManager.isUnload(location.getWorld())) {
            throw new RuntimeException("SomEntity Spawn Unload World");
        }
        if (mobData.hasCustomClass()) {
            try {
                Class<?> bossClass = Class.forName("SwordofMagic11.Enemy.Custom." + mobData.getCustomClass());
                Constructor<?> constructor = bossClass.getConstructor(MobData.class, int.class, Location.class);
                return (EnemyData) constructor.newInstance(mobData, level, location);
            } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
                     InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        } else {
            return new EnemyData(mobData, level, location, parent, rank);
        }
    }

    protected EnemyData(MobData mobData, int level, Location location, EnemyParent parent, Rank rank) {
        super((Mob) location.getWorld().spawnEntity(location, mobData.getEntityType(), false));
        this.parent = parent;
        this.mobData = mobData;
        this.level = level;
        this.rank = rank;
        enemyList.add(this);
        setBaseStatus(StatusType.MaxHealth, 50);
        setBaseStatus(StatusType.ATK, 20);
        setBaseStatus(StatusType.MAT, 20);
        setBaseStatus(StatusType.DEF, 20);
        setBaseStatus(StatusType.MDF, 20);
        setBaseStatus(StatusType.SDF, 20);
        setBaseStatus(StatusType.Movement, 200);
        setBaseStatus(StatusType.Penetration, mobData.getPenetration());
        if (mobData.hasMemorial()) {
            SomEffect effect = SomEffect.EnemyEffect("EnemyBuff");
            for (StatusType statusType : mobData.getMemorial().getStatusTypes()) {
                effect.setStatusMultiply(statusType, 0.1);
            }
            addEffect(effect);
        }
        if (mobData.getRank() == MobData.Rank.Boss) setBaseStatus(StatusType.Penetration, 5);
        double multiply = 4 + Math.max(0, level - 100) * 0.0625;
        mobData.getAttributeRatio().forEach((attr, value) -> setBaseAttribute(attr, ceil(value * level * Classes.PerAttrPoint * multiply)));
        mob = (Mob) getEntity();
        mob.customName(Component.text(rank.color + mobData.getDisplay() + " Lv" + (mobData.isLevelHide() ? "???" : level)));
        mob.setCustomNameVisible(true);
        mob.setMetadata("SomEntity", new FixedMetadataValue(SomCore.plugin(), this));
        mob.getPersistentDataContainer().set(SomCore.SomEntityKey, PersistentDataType.BOOLEAN, true);
        mob.setCollidable(false);

        switch (mob.getType()) {
            case SLIME -> ((Slime) mob).setSize(mobData.getSize());
            case MAGMA_CUBE -> ((MagmaCube) mob).setSize(mobData.getSize());
            case PHANTOM -> ((Phantom) mob).setSize(mobData.getSize());
        }

        if (mobData.hasDisguise()) {
            disguise = mobData.getDisguise().clone();
            disguise.setEntity(mob);
            disguise.startDisguise();
        }
        statusUpdate();
        heal();

        if (!mobData.isObject()) {
            aiTask = SomTask.asyncTimer(() -> {
                if (overrideLocation == null) {
                    if (System.currentTimeMillis() - spawnTime > 45 * 1000) {
                        if (healthPercent() >= 1) {
                            delete();
                            return;
                        }
                    }
                    hateTable.keySet().removeIf(TaskOwner::isInvalid);
                    hateTable.keySet().removeIf(SomEntity::isDeath);
                    hateTable.keySet().removeIf(enemy -> enemy.getWorld() != getWorld());
                    if (hateTable.isEmpty() && mobData.isHostile()) {
                        Collection<PlayerData> list = PlayerData.getPlayerList(getWorld());
                        if (!list.isEmpty()) hateTable.put(randomGet(list), 0.0);
                    }
                    if (!hateTable.isEmpty()) {
                        Map.Entry<SomEntity, Double> entry = Collections.max(hateTable.entrySet(), Map.Entry.comparingByValue());
                        if (entry == null) return;
                        target = entry.getKey();

                        if (getLocation().distance(target.getLocation()) > 1.5) {
                            SomTask.sync(() -> mob.getPathfinder().moveTo(target.getLocation()));
                        }

                        BoundingBox box = mob.getBoundingBox().clone();
                        box.expand(1.5);
                        boolean isHit = false;
                        for (SomEntity target : hateTable.keySet()) {
                            if (box.contains(target.getEntity().getBoundingBox())) {
                                Damage.makeDamage(this, target, Damage.Type.Physics, 1);
                                isHit = true;
                            }
                        }
                        if (!noAI) mob.setAI(!isHit);
                    } else {
                        target = null;
                    }

                    try {
                        tick();
                    } catch (Exception e) {
                        Log(e);
                        Log("§cAI-Tick実行中にエラーが発生したため" + getName() + "を削除しました");
                        delete();
                    }
                } else {
                    SomTask.sync(() -> mob.getPathfinder().moveTo(overrideLocation));
                }

            }, 10, 10, this);
        }

        if (mobData.getRank().isBossBar()) {
            bossBar = Bukkit.createBossBar("", BarColor.RED, BarStyle.SEGMENTED_20);
            SomTask.asyncTimer(() -> {
                for (PlayerData playerData : PlayerData.getPlayerList(getWorld())) {
                    bossBar.addPlayer(playerData.getPlayer());
                }
                for (Player player : bossBar.getPlayers()) {
                    if (player.getWorld() != getWorld() || isInvalid()) {
                        bossBar.removePlayer(player);
                    }
                }
                bossBar.setTitle("§c" + mobData.getDisplay() + " [" + scale(healthPercent() * 100, 2) + "%]");
                bossBar.setProgress(MinMax(healthPercent(), 0, 1));
            }, 20, this);
        } else {
            bossBar = null;
        }
        SomTask.registerEndTask(this, this::delete);
        spawnTime = System.currentTimeMillis();
        spawnLocation = getLocation();
    }

    public void tick() {}

    private boolean noAI = false;

    public void setNoAI() {
        noAI = true;
        mob.setAI(false);
    }

    public void resetNoAI() {
        noAI = false;
        mob.setAI(true);
    }

    public MobData getMobData() {
        return mobData;
    }

    public SomEntity getTarget() {
        return target;
    }

    public boolean hasTarget() {
        return target != null;
    }

    public void setOverrideLocation(Location overrideLocation) {
        this.overrideLocation = overrideLocation;
    }

    public Location getOverrideLocation() {
        return overrideLocation;
    }

    public void resetOverrideLocation() {
        this.overrideLocation = null;
    }

    public void setInt(String id, int value) {
        valueInt.put(id, value);
    }

    public int getInt(String id) {
        return valueInt.get(id);
    }

    public void addTag(String id) {
        tag.add(id);
    }

    public boolean hasTag(String id) {
        return tag.contains(id);
    }

    public void removeTag(String id) {
        tag.remove(id);
    }

    public void addHate(SomEntity entity, double hate) {
        hateTable.merge(entity, hate, Double::sum);
        if (entity instanceof PlayerData playerData) {
            interactPlayer.add(playerData);
        } else if (entity instanceof PetEntity petEntity) {
            interactPlayer.add(petEntity.getOwner());
        }
    }

    public void addTakeDamage(SomEntity entity, double damage) {
        takeDamageTable.merge(entity, damage, Double::sum);
    }

    public ConcurrentHashMap<SomEntity, Double> getTakeDamageTable() {
        return takeDamageTable;
    }

    public void addMakeDamage(SomEntity entity, double damage) {
        makeDamageTable.merge(entity, damage, Double::sum);
    }

    public ConcurrentHashMap<SomEntity, Double> getMakeDamageTable() {
        return makeDamageTable;
    }

    public void delete() {
        enemyList.remove(this);
        delete = true;
        if (aiTask != null) aiTask.cancel();
        if (bossBar != null) {
            bossBar.setVisible(false);
            bossBar.removeAll();
        }
        SomTask.syncDelay(() -> getEntity().remove(), 1);
    }

    public boolean hasDisguise() {
        return disguise != null;
    }

    public Disguise getDisguise() {
        return disguise;
    }

    public Rank rank() {
        return rank;
    }

    @Override
    public boolean isValid() {
        return !delete && !isDeath() && !PlayerData.getPlayerList(getWorld()).isEmpty();
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public void hurt() {
        hurtSound(mobData, getLocation());
    }

    public static void hurtSound(MobData mobData, CustomLocation location) {
        String type = mobData.getDisguise() != null ? mobData.getDisguise().getType().toString() : mobData.getEntityType().toString();
        try {
            Sound sound;
            switch (type) {
                case "CAVE_SPIDER" -> sound = Sound.ENTITY_SPIDER_HURT;
                case "ENDER_CRYSTAL" -> sound = Sound.BLOCK_GRASS_BREAK;
                case "MUSHROOM_COW" -> sound = Sound.ENTITY_COW_HURT;
                case "SNOWMAN" -> sound = Sound.ENTITY_SNOW_GOLEM_HURT;
                case "MINECART" -> sound = Sound.ENTITY_ITEM_BREAK;
                case "PRIMED_TNT" -> sound = Sound.BLOCK_SAND_BREAK;
                case "BLOCK_DISPLAY", "FALLING_BLOCK" -> sound = null;
                default -> sound = Sound.valueOf("ENTITY_" + type + "_HURT");
            }
            if (sound != null) for (PlayerData playerData : SearchEntity.nearPlayerNoAFK(location, 32)) {
                playerData.getPlayer().playSound(location, sound, SoundCategory.HOSTILE, 1f, 1f);
            }
        } catch (Exception e) {
            Log("§cEntityHurtSoundが存在しません -> " + type);
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getName() {
        return mobData.getDisplay();
    }

    @Override
    public Collection<SomEntity> enemies() {
        Set<SomEntity> enemies = new HashSet<>();
        for (PlayerData playerData : PlayerData.getPlayerList(getWorld())) {
            if (isEnemy(playerData) && playerData.isPlayMode() && !playerData.camera().isCamera()) {
                enemies.add(playerData);
                enemies.addAll(playerData.petMenu().entityInsList());
            }
        }
        return enemies;
    }

    public Collection<PlayerData> enemiesPlayer() {
        return PlayerData.getPlayerList(getWorld());
    }

    @Override
    public boolean isEnemy(SomEntity entity) {
        if (this != entity) {
            if (!entity.isDeath()) {
                return entity instanceof PlayerData || entity instanceof PetEntity;
            }
        }
        return false;
    }

    @Override
    public Collection<SomEntity> allies() {
        return new HashSet<>();
    }

    @Override
    public boolean isAlly(SomEntity entity) {
        return false;
    }

    @Override
    public Collection<SomEntity> alliesIsDeath() {
        return new HashSet<>();
    }

    @Override
    public boolean isAllyIsDeath(SomEntity entity) {
        return false;
    }

    @Override
    public synchronized void death(SomEntity killer) {
        super.death(killer);
        if (parent != null) parent.onChildDeath(this, killer);
    }

    private long liveTime = Long.MAX_VALUE;

    public long getLiveTime() {
        return liveTime;
    }

    @Override
    protected void deathProcess(SomEntity killer) {
        delete();
        liveTime = System.currentTimeMillis() - spawnTime;
        //if (mobData.isObject()) return;
        SomTask.async(() -> {
            if (killer instanceof PlayerData playerData) {
                playerData.getKillParticle().view(this, playerData);
            }

            if (hasTag("DefenseBattle")) {
                takeDamageTable.forEach(((somEntity, damage) -> {
                    if (somEntity instanceof PlayerData playerData) {
                        DefenseBattle.Instance.addScore(playerData, this, damage);
                    }
                }));
                return;
            }

            if (mobData.getRank() == MobData.Rank.Boss) {
                List<Map.Entry<SomEntity, Double>> ranking = new ArrayList<>(takeDamageTable.entrySet());
                ranking.sort(Map.Entry.comparingByValue());
                Collections.reverse(ranking);
                List<String> rankingMessage = new ArrayList<>();
                rankingMessage.add(decoText("ダメージランキング"));
                int index = 1;
                for (Map.Entry<SomEntity, Double> entry : ranking) {
                    rankingMessage.add("§e[" + index + "位]§r" + entry.getKey().getName() + "§7: §a" + scale(entry.getValue()));
                    index++;
                    if (index > 5) break;
                }
                for (PlayerData playerData : interactPlayer) {
                    playerData.sendMessage(rankingMessage);
                }
            }

            Set<PlayerData> dropPlayers = new HashSet<>(interactPlayer);
            for (PlayerData dropPlayer : interactPlayer) {
                if (dropPlayer.hasParty()) {
                    for (PlayerData member : dropPlayer.getParty().getMember()) {
                        if (dropPlayer.getWorld() == member.getWorld()) {
                            dropPlayers.add(member);
                        }
                    }
                }
            }
            HashMap<PlayerData, Set<PetEntity>> interactPet = new HashMap<>();
            for (SomEntity somEntity : hateTable.keySet()) {
                if (somEntity instanceof PetEntity petEntity) {
                    PlayerData owner = petEntity.getOwner();
                    if (!interactPet.containsKey(owner)) {
                        interactPet.put(owner, new HashSet<>());
                    }
                    interactPet.get(owner).add(petEntity);
                    dropPlayers.add(owner);
                }
            }
            dropPlayers.removeIf(playerData -> playerData.isAFK() || playerData.camera().isCamera());
            int playerCount = dropPlayers.size();
            for (PlayerData dropPlayer : dropPlayers) {
                if (dropPlayer.classes().getMainClass().getColor().equalsIgnoreCase("§e")) {
                    playerCount--;
                    break;
                }
            }
            double additionDrop = 0.0;
            double baseExp = BaseExp(level) * mobData.getRank().dropMultiply();
            int baseMel = (int) (Math.sqrt(Math.min(level, MaxMelDrop)) * MelDropMultiply * mobData.getRank().dropMel());
            int excess = Math.max(0, playerCount - getMap().getMaxBalance());
            double globalMultiply = rank.dropMultiply * (1 / (1 + excess * PercentValue * 0.5)) * WorldManager.totalMultiply(getWorld());
            Set<MobData.MaterialDrop> materialDrops = new HashSet<>(mobData.getMaterialDrop());
            int minGrinder = (int) mobData.getRank().dropMultiply();
            int maxGrinder = (int) ((1 + level / 10) * mobData.getRank().dropMultiply() * 1.666);
            materialDrops.add(new MobData.MaterialDrop(MaterialDataLoader.getMaterialData("グラインダー"), 0.01, minGrinder, maxGrinder));

            HashMap<CapsuleData, Double> capsuleDrop = new HashMap<>(mobData.getCapsuleDrop());
            switch (rank) {
                case Elite -> capsuleDrop.put(CapsuleDataLoader.getCapsuleData("エリート"), 0.1 / rank.dropMultiply);
                case Ultimate -> capsuleDrop.put(CapsuleDataLoader.getCapsuleData("アルティメット"), 1.0 / rank.dropMultiply);
            }

            if (hasEffect("GraceGarden")) {
                additionDrop += getEffect("GraceGarden").getDouble("Drop");
            }

            for (PlayerData playerData : dropPlayers) {
                playerData.statistics().add(this);
                if (mobData.hasMemorial() && !playerData.memorialMenu().has(mobData.getMemorial())) {
                    if (playerData.statistics().get(this) >= 1 / (MemorialPercent / 2.5)) {
                        playerData.memorialMenu().useMemorial(mobData.getMemorial());
                    }
                }

                double multiply = globalMultiply * (playerData.booster().multiply() + additionDrop);
                ClassType mainClass = playerData.classes().getMainClass();
                double exp = baseExp * multiply;
                playerData.classes().addExp(mainClass, exp);
                for (int i = 0; i < rank.dropMultiply; i++) {
                    playerData.chanceEquipExp();
                }

                if (interactPet.containsKey(playerData)) {
                    for (PetEntity petEntity : interactPet.get(playerData)) {
                        petEntity.instance().addKillCount(1);
                        petEntity.instance().addExp(PetLevelType.Combat, exp);
                    }
                }

                for (MobData.MaterialDrop materialDrop : materialDrops) {
                    int dropAmount = materialDrop.drop(multiply);
                    if (dropAmount > 0) {
                        double percent = materialDrop.percent() * multiply;
                        MaterialData.drop(playerData, materialDrop.material(), dropAmount, percent);

                    }
                }
                CapsuleDataLoader.DefaultCapsuleDrop.forEach((capsuleData, rawPercent) -> {
                    double percent = Math.min(Math.min(rawPercent * (1 + level * 0.05) * multiply, 0.05) * mobData.getRank().dropMultiply(), mobData.getRank().capsuleLimit());
                    CapsuleData.drop(playerData, capsuleData, percent);
                });
                capsuleDrop.forEach((capsuleData, percent) -> CapsuleData.drop(playerData, capsuleData, percent * multiply));

                if (baseMel > 0) playerData.addMel(baseMel);
            }
        });
    }

    @Override
    public void onMakeDamage(SomEntity victim, Damage.Type type, double multiply, boolean isCritical) {

    }

    @Override
    public void onTakeDamage(SomEntity attacker, Damage.Type type, double multiply, boolean isCritical) {

    }

    public static double BaseExp(int level) {
        return baseExp[level - 1];
    }

    private static final double[] baseExp = new double[MaxLevel];

    static {
        for (int i = 0; i < MaxLevel; i++) {
            baseExp[i] = 20 + Math.pow(i, 2);
        }
    }

    public enum Rank {
        Normal("§c", 1.0, 1.0),
        Elite("§5", 10.0, 10.0),
        Ultimate("§0", 100.0, 100.0),
        ;

        private final String color;
        private final double healthMultiply;
        private final double dropMultiply;

        Rank(String color, double healthMultiply, double dropMultiply) {
            this.color = color;
            this.healthMultiply = healthMultiply;
            this.dropMultiply = dropMultiply;
        }

        public double healthMultiply() {
            return healthMultiply;
        }
    }

}
