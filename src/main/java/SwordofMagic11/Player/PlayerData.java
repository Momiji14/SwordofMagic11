package SwordofMagic11.Player;

import SwordofMagic11.Command.Developer.PlayMode;
import SwordofMagic11.Component.SomSQL;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Component.SomTask;
import SwordofMagic11.Component.WorldManager;
import SwordofMagic11.Custom.CustomLocation;
import SwordofMagic11.Custom.UnsetLocation;
import SwordofMagic11.DataBase.DataBase;
import SwordofMagic11.DataBase.MapDataLoader;
import SwordofMagic11.DataBase.SkillDataLoader;
import SwordofMagic11.Entity.Damage;
import SwordofMagic11.Entity.SomEntity;
import SwordofMagic11.Item.SomEquip;
import SwordofMagic11.Item.SyncItem;
import SwordofMagic11.Map.MapData;
import SwordofMagic11.Map.PvPRaid;
import SwordofMagic11.Player.Gathering.Fishing;
import SwordofMagic11.Player.Market.MarketSystemMaterial;
import SwordofMagic11.Player.Memorial.MemorialPoint;
import SwordofMagic11.Player.Menu.MenuHolder;
import SwordofMagic11.Player.MiniGame.LightsOut;
import SwordofMagic11.Player.MiniGame.Mania;
import SwordofMagic11.Player.Setting.PlayerSetting;
import SwordofMagic11.Player.Shop.ShopManager;
import SwordofMagic11.Player.Statistics.Statistics;
import SwordofMagic11.Skill.Parameter;
import SwordofMagic11.Skill.SkillManager;
import SwordofMagic11.SomCore;
import SwordofMagic11.StatusType;
import SwordofMagic11.TaskOwner;
import com.github.jasync.sql.db.RowData;
import kotlin.jvm.functions.Function4;
import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static SwordofMagic11.Component.Function.*;
import static SwordofMagic11.DataBase.DataBase.DateFormat;
import static SwordofMagic11.Map.MapData.ArshaEnemyRisk;
import static SwordofMagic11.Map.MapData.ArshaRisk;

public class PlayerData extends HumanData implements Interact, StrafeWallKick, NormalAttack, MenuHolder.Interface, PlayerInventory.Interface, PvPTimer, SizeManager, MemorialPoint {

    private static final ConcurrentHashMap<UUID, PlayerData> playerData = new ConcurrentHashMap<>();

    public static Collection<PlayerData> getPlayerList() {
        clean();
        return new HashSet<>(playerData.values());
    }

    public static Collection<PlayerData> getPlayerList(World world) {
        Collection<PlayerData> list = new HashSet<>();
        for (Player player : world.getPlayers()) {
            list.add(get(player));
        }
        return list;
    }

    public static boolean isEntry(Player player) {
        return playerData.containsKey(player.getUniqueId());
    }

    public static boolean isEntry(HumanData humanData) {
        return playerData.get(humanData.getPlayer().getUniqueId()) == humanData;
    }


    public static PlayerData create(Player player) {
        playerData.put(player.getUniqueId(), new PlayerData(player));
        return playerData.get(player.getUniqueId());
    }

    public static PlayerData get(Player player) {
        if (!playerData.containsKey(player.getUniqueId())) {
            return create(player);
        }
        return playerData.get(player.getUniqueId());
    }

    public static String Username(String uuid) {
        return SomSQL.getString(DataBase.Table.PlayerData, "UUID", uuid, "Username");
    }

    public static void clean() {
        playerData.values().removeIf(TaskOwner::isInvalid);
    }

    public void delete() {
        SomTask.stop(this);
        itemInventory().clearCache();
        playerData.remove(player.getUniqueId());
    }

    private final Classes classes;
    private final SkillManager skillManager;
    private final MenuHolder menuHolder;
    private final ShopManager shopManager;
    private final NamePlate namePlate;
    private final PlayerInventory inventory;
    private final Statistics statistics;
    private final Donation donation;
    private final Mania mania;
    private final LightsOut lightsOut;
    private final PlayerSideBar playerSideBar;
    private final PlayerBooster playerBooster;
    private final Fishing fishing;
    private int afkTime = 0;
    private double dps = 0;
    private long dpsTime = 0;
    private CustomLocation lastLocation;
    private Runnable leftClickOverride;
    private PlayerData pvpAttacker;
    private final HashMap<String, Function4<SomEntity, Damage.Type, Double, Boolean, Void>> onMakeDamageFunction = new HashMap<>();
    private final HashMap<String, Function4<SomEntity, Damage.Type, Double, Boolean, Void>> onTakeDamageFunction = new HashMap<>();
    private final Camera camera;

    private PlayerData(Player player) {
        super(player);
        this.player = player;
        player.setMetadata("SomEntity", new FixedMetadataValue(SomCore.plugin(), this));
        uuid = player.getUniqueId().toString();
        classes = new Classes(this);
        skillManager = new SkillManager(this);
        menuHolder = new MenuHolder(this);
        shopManager = new ShopManager(this);
        namePlate = new NamePlate(this);
        inventory = new PlayerInventory(this);
        statistics = new Statistics(this);
        donation = new Donation(this);
        mania = new Mania(this);
        lightsOut = new LightsOut(this);
        playerSideBar = new PlayerSideBar(this);
        playerBooster = new PlayerBooster(this);
        fishing = new Fishing(this);
        lastLocation = getLocation();
        camera = new Camera(this);

        int time = 20*60*15;
        SomTask.asyncTimer(this::save, time, time, this);
    }

    @Override
    public PlayerData playerData() {
        return this;
    }

    public String getColorDisplayName() {
        return (isAFK() ? "§8" : isPvPMode() ? "§c" : "§f") + getName();
    }

    public String getDisplayName() {
        return "§f" + getName();
    }

    public Classes classes() {
        return classes;
    }

    public SkillManager skillManager() {
        return skillManager;
    }

    public ShopManager getShopManager() {
        return shopManager;
    }

    public NamePlate getNamePlate() {
        return namePlate;
    }

    @Override
    public MenuHolder menuHolder() {
        return menuHolder;
    }

    @Override
    public PlayerInventory inventory() {
        return inventory;
    }

    public Statistics statistics() {
        return statistics;
    }

    public Donation donation() {
        return donation;
    }

    public Mania mania() {
        return mania;
    }

    public LightsOut lightsOut() {
        return lightsOut;
    }

    public PlayerSideBar sideBar() {
        return playerSideBar;
    }

    public PlayerBooster booster() {
        return playerBooster;
    }

    public Fishing fishing() {
        return fishing;
    }

    public boolean isAFK() {
        return isPlayMode() && afkTime >= 300;
    }

    public int getAFKTime() {
        return afkTime;
    }

    public double getDPS() {
        long milli = System.currentTimeMillis() - dpsTime;
        return dps / milli * 1000;
    }

    public void addDPS(double dps) {
        if (this.dps == 0) dpsTime = System.currentTimeMillis();
        this.dps += dps;
    }

    public void resetDPS() {
        dps = 0;
    }

    @Override
    public int getLevel() {
        return classes.getLevel();
    }

    public boolean hasSkill(String id) {
        return classes.getSkillLevel(id) >= 1;
    }

    public int getSkillLevel(String id) {
        return classes.getSkillLevel(id);
    }

    public Parameter getSkillParam(String id) {
        return SkillDataLoader.getSkillData(id).getParam(this, getSkillLevel(id));
    }

    public int getMel() {
        return SomSQL.getInt(DataBase.Table.PlayerData, "UUID", uuid, "Mel");
    }

    public void setMel(int mel) {
        SomSQL.setSql(DataBase.Table.PlayerData, "UUID", uuid, "Mel", mel);
    }

    public void addMel(int mel) {
        setMel(getMel() + mel);
        if (setting().is(PlayerSetting.BooleanEnum.MelLog)) player.sendMessage("§b[+]§e" + mel + "メル");
    }

    public void removeMel(int mel) {
        setMel(getMel() - mel);
    }

    public void removeTax(int mel, int tax) {
        setMel(getMel() - (mel + tax));
        SomSQL.addNumber(DataBase.Table.SystemBank, "CurrentTax", tax);
        SomSQL.addNumber(DataBase.Table.SystemBank, "TotalTax", tax);
    }

    public void removeTax(int tax) {
        setMel(getMel() - tax);
        SomSQL.addNumber(DataBase.Table.SystemBank, "CurrentTax", tax);
        SomSQL.addNumber(DataBase.Table.SystemBank, "TotalTax", tax);
    }

    public boolean hasMel(int mel) {
        if (mel < 0) return false;
        return getMel() >= mel;
    }

    public void leftClickOverride(Runnable runnable) {
        leftClickOverride = runnable;
    }

    public void resetLeftClickOverride() {
        leftClickOverride = null;
    }

    public Runnable leftClickOverride() {
        return leftClickOverride;
    }

    public boolean hasLeftClickOverride() {
        return leftClickOverride != null;
    }

    public void pvpAttacker(PlayerData playerData) {
        pvpAttacker = playerData;
    }

    public void resetPvPAttacker() {
        pvpAttacker = null;
    }

    public PlayerData pvpAttacker() {
        return pvpAttacker;
    }

    public boolean hasPvPAttacker() {
        return pvpAttacker != null;
    }


    public int palletStorageSize() {
        return donation.additionPalletStorageSize();
    }

    public Camera camera() {
        return camera;
    }

    private boolean isViewerRun = false;

    public void updateViewer() {
        if (isViewerRun) return;
        isViewerRun = true;

        SomTask.asyncTimer(() -> {
            if (isPlayMode()) {
                Component component;
                if (isAFK()) {
                    component = Component.text("§4AFK " + afkTime + " sec");
                } else {
                    addHealth(getStatus(StatusType.HealthRegen) / 4);
                    addMana(getStatus(StatusType.ManaRegen) / 4);
                    ClassType mainClass = classes.getMainClass();
                    int level = classes.getLevel(mainClass);
                    double expPercent = classes.getExpPercent(mainClass);

                    SomTask.sync(() -> {
                        player.setLevel(level);
                        player.setExp((float) MinMax(expPercent, 0, 0.999));
                        player.setHealth(MinMax(getHealth() / getStatus(StatusType.MaxHealth) * 20, 0.01, 20));
                        player.setFoodLevel(MinMax(ceil(getMana() / getStatus(StatusType.MaxMana) * 20), 0, 20));
                    });

                    updatePallet();
                    namePlate.update();

                    component = Component.text(
                            mainClass.getColor() + "《" + mainClass.getDisplay() + " Lv" + level + "》" +
                                    "§c《Health: " + scale(getHealth()) + "/" + scale(getStatus(StatusType.MaxHealth)) + "》" +
                                    "§b《Mana: " + scale(getMana()) + "/" + scale(getStatus(StatusType.MaxMana)) + "》" +
                                    "§a《Exp: " + scale(expPercent * 100, 3) + "%》" +
                                    "§e《DPS: " + scale(getDPS(), 1) + "》"
                    );
                }
                player.sendActionBar(component);
            }
        }, 5, this);

        SomTask.asyncTimer(() -> {
            if (donation.hasInsBoost()) {
                donation.removeInsBoost();
            }
            namePlate.updateTabList();
            if (lastLocation.distance(getLocation()) < 1.5 || player.isInsideVehicle()) {
                afkTime++;
            } else {
                lastLocation = getLocation();
                afkTime = 0;
            }
            if (isAFK()) {
                statistics.add(Statistics.IntEnum.AFKTime, 1);
            } else {
                statistics.add(Statistics.IntEnum.PlayTime, 1);
                if (isArsha()) {
                    statistics.add(Statistics.IntEnum.PvPTime, 1);
                }
            }
        }, 20, this);
    }

    public void resetAFK() {
        afkTime = 0;
    }

    private PlayerData resurrection = null;

    public void resurrection(PlayerData playerData) {
        resurrection = playerData;
    }

    @Override
    protected void deathProcess(SomEntity killer) {
        SomTask.sync(() -> {
            teleport(getHipsLocation());
            player.setAllowFlight(true);
            player.setFlying(true);
            player.setInvisible(true);
            player.setFlySpeed(0);
            saveLastCity(true);
            sendTitle("§4You Are Dead", " ", 20, 200, 0);
            SomTask.asyncCountIf(() -> {
                sendTitle("§4You Are Dead", " ", 0, 30, 20);
            }, 20, 10, 0, () -> resurrection == null, () -> {
                if (resurrection != null) {
                    SomTask.sync(() -> respawn(getLocation()));
                    sendTitle("§bRevive!", " ", 10, 20, 10);
                    SomSound.Level.play(this);
                } else {
                    deathInstantProcess(killer);
                }
            }, this);
        });
    }

    @Override
    public void onMakeDamage(SomEntity victim, Damage.Type type, double multiply, boolean isCritical) {
        for (Function4<SomEntity, Damage.Type, Double, Boolean, Void> function : onMakeDamageFunction.values()) {
            function.invoke(victim, type, multiply, isCritical);
        }
    }

    @Override
    public void onTakeDamage(SomEntity attacker, Damage.Type type, double multiply, boolean isCritical) {
        for (Function4<SomEntity, Damage.Type, Double, Boolean, Void> function : onTakeDamageFunction.values()) {
            function.invoke(attacker, type, multiply, isCritical);
        }
    }

    public void setOnMakeDamageFunction(String id, Function4<SomEntity, Damage.Type, Double, Boolean, Void> function) {
        onMakeDamageFunction.put(id, function);
    }

    public void deleteOnMakeDamageFunction(String id) {
        onMakeDamageFunction.remove(id);
    }

    public void setOnTakeDamageFunction(String id, Function4<SomEntity, Damage.Type, Double, Boolean, Void> function) {
        onTakeDamageFunction.put(id, function);
    }

    public void deleteOnTakeDamageFunction(String id) {
        onTakeDamageFunction.remove(id);
    }

    public void deathInstantProcess(SomEntity killer) {
        Location spawnLocation = lastSpawnLocation();
        if (killer instanceof PlayerData killerData) {
            if (PvPRaid.isInPvPRaid(this) && PvPRaid.isInPvPRaid(killerData)) {
                PvPRaid.kill(killerData, this);
                spawnLocation = PvPRaid.getTeam(this).getSpawnLocation();
            }
        }
        respawn(spawnLocation);
        if (isArsha()) respawnArsha(killer);
    }

    public void respawnArsha(SomEntity killer) {
        if (killer instanceof PlayerData killerData) {
            killerData.chanceEquipExp();
            killerData.statistics.add(Statistics.IntEnum.PlayerKillAttackerCount, 1);
            statistics.add(Statistics.IntEnum.PlayerKillVictimCount, 1);
            int grinder = Math.min(ceil(getMaterial("グラインダー") * ArshaRisk), killerData.getMaterial("グラインダー"));
            if (grinder > 0) {
                statistics.add(Statistics.IntEnum.LostPvP_Grinder, grinder);
                killerData.statistics.add(Statistics.IntEnum.GivenPvP_Grinder, grinder);
                removeMaterial("グラインダー", grinder);
                killerData.addMaterial("グラインダー", grinder);
                sendMessage("§c[PvP]§f" + killerData.getName() + "§aに§eグラインダーx" + grinder + "§aを奪われました");
                killerData.sendMessage("§c[PvP]§f" + getName() + "§aから§eグラインダーx" + grinder + "§aを奪いました");
            }
            double exp = Math.min(classes.getExp() * ArshaRisk, killerData.classes().getExp());
            if (exp > 0) {
                statistics.add(Statistics.DoubleEnum.LostPvP_Exp, exp);
                killerData.statistics.add(Statistics.DoubleEnum.GivenPvP_Exp, exp);
                classes.removeExp(exp);
                killerData.classes().addExp(exp);
                sendMessage("§c[PvP]§f" + killerData.getName() + "§aに§e" + scale(exp, 2) + "経験値§aを奪われました");
                killerData.sendMessage("§c[PvP]§f" + getName() + "§aから§e" + scale(exp, 2) + "経験値§aを奪いました");
            }
        } else {
            int grinder = ceil(getMaterial("グラインダー") * ArshaEnemyRisk);
            double exp = classes.getExp() * ArshaEnemyRisk;
            if (grinder > 0) {
                statistics.add(Statistics.IntEnum.LostPvE_Grinder, grinder);
                SomSQL.addNumber(DataBase.Table.GlobalPvPDrop, "Grinder", grinder);
                MarketSystemMaterial.add("グラインダー", grinder);
                removeMaterial("グラインダー", grinder);
                sendMessage("§eグラインダーx" + grinder + "§aがシステムに吸収されました");
            }
            if (exp > 0) {
                statistics.add(Statistics.DoubleEnum.LostPvE_Exp, exp);
                SomSQL.addNumber(DataBase.Table.GlobalPvPDrop, "Exp", exp);
                classes.removeExp(exp);
                sendMessage("§e" + scale(exp, 2) + "経験値§aがシステムに吸収されました");
            }
        }
    }

    public void respawn(Location location) {
        reset();
        player.setAllowFlight(false);
        player.setFlying(false);
        player.setInvisible(false);
        player.setFlySpeed(0.2f);
        invinciblePvP(100);
        saveLastCity(false);
        teleport(location);
        heal();
        resurrection = null;
        setDeath(false);
        updateGameMode();
    }

    public void chanceEquipExp() {
        for (SomEquip equip : getEquipment().values()) {
            if (equip != null) {
                equip.chanceExp();
            }
        }
    }

    public void firstSpawn() {
        MapData mapData = MapDataLoader.getMapData("Hanjibal");
        teleport(mapData.getGlobalInstance(false).getSpawnLocation());
        mapData.enter(playerData(), false);
    }

    public void loadSpawn() {
        boolean arsha = Boolean.parseBoolean(SomSQL.getString(DataBase.Table.PlayerData, "UUID", uuid, "Arsha"));
        MapData mapData = MapDataLoader.getMapData(SomSQL.getString(DataBase.Table.PlayerData, "UUID", uuid, "Map"));
        teleport(new UnsetLocation(SomSQL.getString(DataBase.Table.PlayerData, "UUID", uuid, "Location")).as(mapData.getGlobalInstance(arsha)));
        mapData.enter(this, arsha);
    }

    public void load() {
        if (SomSQL.exists(DataBase.Table.PlayerData, "UUID", uuid)) {
            classes.setMainClass(ClassType.valueOf(SomSQL.getString(DataBase.Table.PlayerData, "UUID", uuid, "MainClass")));
            Pallet.load(this);
            setPlayMode(PlayMode.isTrue(this));

            for (RowData objects : SomSQL.getSqlList(DataBase.Table.ItemStorage, "Owner", uuid, "*")) {
                SyncItem.updateCache(objects.getString("UUID"));
            }

            for (RowData objects : SomSQL.getSqlList(DataBase.Table.PlayerEquipment, "UUID", uuid, "*")) {
                equipment.put(SomEquip.Slot.valueOf(objects.getString("EquipSlot")), (SomEquip) SyncItem.getSomItem(objects.getString("EquipUUID")));
            }

            if (!player.getWorld().getName().contains("WorldContainer")) {
                loadSpawn();
            }

            petMenu().load();
            statusUpdate();
            setHealth(SomSQL.getDouble(DataBase.Table.PlayerData, "UUID", uuid, "Health"));
            setMana(SomSQL.getDouble(DataBase.Table.PlayerData, "UUID", uuid, "Mana"));
        } else {
            SomSQL.setSql(DataBase.Table.PlayerData, "UUID", uuid, "PlayStart", LocalDateTime.now().format(DateFormat));
            SomSQL.setSql(DataBase.Table.PlayerData, "UUID", uuid, "Username", player.getName());
            classes.setMainClass(ClassType.Adventurer);
            firstSpawn();
            statusUpdate();
            heal();
        }

        if (isPlayMode()) {
            player.setGameMode(GameMode.ADVENTURE);
            player.getInventory().clear();
        }
        updateViewer();
        updateMenu();
        updateInventory();
        updateGameMode();
        setting().updateNightVision();
    }

    private boolean saveLastCity = false;
    public void saveLastCity(boolean bool) {
        saveLastCity = bool;
    }

    public void saveLastCityTime(int tick) {
        saveLastCity = true;
        SomTask.asyncDelay(() -> saveLastCity = false, tick);
    }

    public void saveLastOnlineTime() {
        SomSQL.setSql(DataBase.Table.PlayerData, "UUID", uuid, "LastOnlineTime", LocalDateTime.now().format(DateFormat));
    }

    public LocalDateTime lastOnlineTime() {
        if (SomSQL.exists(DataBase.Table.PlayerData, "UUID", uuid, "LastOnlineTime")) {
            String date = SomSQL.getString(DataBase.Table.PlayerData, "UUID", uuid, "LastOnlineTime");
            if (!date.isEmpty()) {
                return LocalDateTime.parse(date, DateFormat);
            }
        }
        return LocalDateTime.now();
    }

    public void save() {
        SomSQL.setSql(DataBase.Table.PlayerData, "UUID", uuid, "Username", player.getName());
        SomSQL.setSql(DataBase.Table.PlayerData, "UUID", uuid, "Health", getHealth());
        SomSQL.setSql(DataBase.Table.PlayerData, "UUID", uuid, "Mana", getMana());
        saveLocation();
        Pallet.save(this);
        PlayMode.save(this);
        saveLastOnlineTime();
    }

    public void saveLocation() {
        if (saveLastCity || getMap().isRoom()) {
            if (getMap().isRoom()) removeTax(getLastCity().getTeleportMel());
            SomSQL.setSql(DataBase.Table.PlayerData, "UUID", uuid, "Arsha", "false");
            SomSQL.setSql(DataBase.Table.PlayerData, "UUID", uuid, "Map", getLastCity().getId());
            SomSQL.setSql(DataBase.Table.PlayerData, "UUID", uuid, "Location", new UnsetLocation(getLastCity().getGlobalInstance(false).getSpawnLocation()).toString());
        } else if (WorldManager.isActive(getWorld())) {
            SomSQL.setSql(DataBase.Table.PlayerData, "UUID", uuid, "Arsha", String.valueOf(isArsha()));
            SomSQL.setSql(DataBase.Table.PlayerData, "UUID", uuid, "Map", getMap().getId());
            SomSQL.setSql(DataBase.Table.PlayerData, "UUID", uuid, "Location", getLocation().asUnsetLocation().toString());
        }
    }
}
