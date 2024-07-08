package SwordofMagic11.Map;

import SwordofMagic11.Command.Developer.MobClear;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Component.SomTask;
import SwordofMagic11.Component.WorldManager;
import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.Enemy.SpawnerData;
import SwordofMagic11.Item.Material.MaterialData;
import SwordofMagic11.Item.SomEquip;
import SwordofMagic11.Player.Memorial.MemorialData;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Player.SomParty;
import SwordofMagic11.SomCore;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TextDisplay;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

import static SwordofMagic11.Component.Function.*;
import static SwordofMagic11.Player.Setting.PlayerSetting.BooleanEnum.BossModeForceDisable;
import static SwordofMagic11.SomCore.Log;

public class MapData {
    public static double ArshaMultiply = 0.25;
    public static double ArshaRisk = 0.25;
    public static double ArshaEnemyRisk = 0.1;

    private String id;
    private String display;
    private Material icon;
    private int minLevel;
    private int minBalance;
    private int maxBalance;
    private Type type = Type.Field;
    private String region;
    private boolean commandBlock = false;
    private boolean levelHide = false;
    private final HashMap<String, NpcData> npc = new HashMap<>();
    private final HashMap<String, WarpGate> gate = new HashMap<>();
    private final HashMap<String, SpawnerData> spawner = new HashMap<>();
    private RoomData roomData;
    private MemorialData memorial;
    private GatheringData miningData;
    private GatheringData collectData;
    private GatheringData fishingData;
    private World globalInstance;
    private World arshaInstance;
    private final HashMap<SomParty, World> privateInstance = new HashMap<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public Material getIcon() {
        return icon;
    }

    public void setIcon(Material icon) {
        this.icon = icon;
    }

    public int getMinLevel() {
        return minLevel;
    }

    public void setMinLevel(int minLevel) {
        this.minLevel = minLevel;
    }

    public int getMinBalance() {
        return minBalance;
    }

    public void setMinBalance(int minBalance) {
        this.minBalance = minBalance;
    }

    public int getMaxBalance() {
        return maxBalance;
    }

    public void setMaxBalance(int maxBalance) {
        this.maxBalance = maxBalance;
    }

    public boolean isCity() {
        return type == Type.City;
    }

    public boolean isRoom() {
        return type == Type.Room;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public boolean isCommandBlock() {
        return commandBlock;
    }

    public void setCommandBlock(boolean commandBlock) {
        this.commandBlock = commandBlock;
    }

    public boolean isLevelHide() {
        return levelHide;
    }

    public void setLevelHide(boolean levelHide) {
        this.levelHide = levelHide;
    }

    public Collection<NpcData> getNpc() {
        return npc.values();
    }

    public void addNpc(NpcData npcData) {
        npc.put(npcData.getId(), npcData);
    }

    public Collection<WarpGate> getWarpGate() {
        return gate.values();
    }

    public WarpGate getWarpGate(String id) {
        return gate.get(id);
    }

    public void addWarpGate(WarpGate warpGate) {
        gate.put(warpGate.getId(), warpGate);
    }

    public Collection<SpawnerData> getSpawner() {
        return spawner.values();
    }

    public void addSpawner(SpawnerData spawnerData) {
        spawner.put(spawnerData.getId(), spawnerData);
    }

    public RoomData getRoomData() {
        return roomData;
    }

    public void setRoomData(RoomData roomData) {
        this.roomData = roomData;
    }

    public MemorialData getMemorial() {
        return memorial;
    }

    public void setMemorial(MemorialData memorial) {
        this.memorial = memorial;
    }

    public GatheringData getMiningData() {
        return miningData;
    }

    public void setMiningData(GatheringData miningData) {
        this.miningData = miningData;
    }

    public GatheringData getCollectData() {
        return collectData;
    }

    public void setCollectData(GatheringData collectData) {
        this.collectData = collectData;
    }

    public GatheringData getFishingData() {
        return fishingData;
    }

    public void setFishingData(GatheringData fishingData) {
        this.fishingData = fishingData;
    }

    public World createInstance(String suffix) {
        World world = WorldManager.createInstance(id, suffix);
        getSpawner().forEach(spawner -> spawner.addWorld(world));
        getWarpGate().forEach(gate -> gate.addWorld(world));
        if (isRoom()) roomData.addWorld(world);
        return world;
    }

    public World getGlobalInstance(boolean arsha) {
        if (arsha && !isCity()) {
            arshaInstance = getInstance(arshaInstance, "PvPIns");
            WorldManager.instance(arshaInstance).setArsha(true);
            return arshaInstance;
        } else {
            globalInstance = getInstance(globalInstance, "Global");
            return globalInstance;
        }
    }

    public World getPrivateInstance(SomParty party) {
        World world = getInstance(privateInstance.get(party), "Private-" + UUID.randomUUID());
        WorldManager.instance(world).setPrivate(party);
        privateInstance.put(party, world);
        return world;
    }

    public synchronized World getInstance(World world, String suffix) {
        if (world == null || WorldManager.isUnload(world)) {
            world = createInstance(suffix);
        }
        if (WorldManager.isInitialize(world)) {
            WorldManager.instance(world).setInitialize(false);
            intiInstance(world);
        }
        return world;
    }

    public synchronized void intiInstance(World instance) {
        SomTask.syncDelay(() -> {
            MobClear.clearForce(instance);
            getNpc().forEach(npcData -> npcData.spawn(instance));
            getWarpGate().forEach(gate -> gatePlate(instance, gate));
        }, 20);
    }

    public void gatePlate(World instance, WarpGate warpGate) {
        TextDisplay gatePlate = (TextDisplay) instance.spawnEntity(warpGate.getFromLocation().as(instance).addY(2.5), EntityType.TEXT_DISPLAY);
        gatePlate.getPersistentDataContainer().set(SomCore.SomParticle, PersistentDataType.BOOLEAN, true);
        gatePlate.setPersistent(false);
        gatePlate.setBillboard(Display.Billboard.VERTICAL);
        gatePlate.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
        MapData nextMap = warpGate.getToMap();
        gatePlate.text(Component.text(
                "§e" + nextMap.getDisplay() + "\n" +
                        "§a推奨人数 " + nextMap.getMinBalance() + "~" + nextMap.getMaxBalance() + "人" + "\n" +
                        "§e推奨Lv" + (nextMap.isLevelHide() ? "???" : (nextMap.getMinLevel() + "~"))
        ));
    }

    public boolean isInvalidNPC(World instance) {
        if (npc.isEmpty()) return false;
        for (Entity entity : instance.getEntities()) {
            if (entity.getScoreboardTags().contains("NPC")) {
                return false;
            }
        }
        return true;
    }

    public void enter(PlayerData playerData, boolean isArsha) {
        playerData.resetDPS();
        playerData.timer("WarpGateCoolTime", 50);
        playerData.sendTitle((!isCity() && isArsha ? "§c" : "§e") + display, "§e推奨Lv" + (isLevelHide() ? "???" : (minLevel + "~")) + "  §a推奨人数 " + minBalance + "~" + maxBalance + "人", 10, 40, 10);
        playerData.updateEquipView(isArsha);
        SomSound.Teleport.radius(playerData);
        if (isCity()) {
            playerData.setting().resetPvPMode();
            playerData.heal();
            playerData.teleportMenu().active(this);
            playerData.setLastCity(this);
        }
    }

    public boolean warpGateUse(PlayerData playerData) {
        for (WarpGate warpGate : playerData.getMap().getWarpGate()) {
            if (warpGate.getFromLocation().distance(playerData.getLocation()) < warpGate.getRadius()) {
                if (playerData.getMap().isCity() && playerData.getEquip(SomEquip.Slot.MainHand) == null) {
                    playerData.sendMessage("§dゲート§aを利用するには§e武器§aを§e装備§aしてください", SomSound.Nope);
                    return true;
                }
                if (warpGate.hasReqMaterial()) {
                    List<String> message = new ArrayList<>();
                    message.add(decoText("ゲート使用条件"));
                    boolean check = true;
                    for (MaterialData material : warpGate.getReqMaterial()) {
                        if (playerData.hasMaterial(material, 1)) {
                            message.add("§b✓ §f" + material.getDisplay());
                        } else {
                            message.add("§c× §f" + material.getDisplay());
                            check = false;
                        }
                    }
                    if (!check) {
                        playerData.sendMessage(message, SomSound.Nope);
                        return true;
                    }
                }
                MapData nextMap = warpGate.getToMap();
                boolean isBossTimeAttack = nextMap.isRoom() && !nextMap.getRoomData().getLimitStatus().isEmpty();
                if (playerData.getMap().isCity() && playerData.classes().getLevel() >= 20) {
                    playerData.instanceMenu().open(warpGate);
                } else if (!playerData.setting().is(BossModeForceDisable) && isBossTimeAttack) {
                    playerData.bossModeMenu().open(warpGate);
                } else {
                    SomTask.syncDelay(() -> {
                        if (!nextMap.isRoom() && playerData.hasTimer("WarpGateCoolTime")) {
                            playerData.sendMessage("§cゲートクールタイム中です (残り" + scale(playerData.timer("WarpGateCoolTime")/20.0, 2) + "秒)", SomSound.Nope);
                            return;
                        }
                        World instance;
                        boolean arsha = playerData.isArsha();
                        if (nextMap.isCity()) {
                            if (playerData.hasPvPAttackTime()) {
                                playerData.sendMessage("§cPvPダメージ与えた場合一定時間街には入れません (残り" + scale(playerData.pvpAttackTime()/20.0, 2) + "秒)", SomSound.Nope);
                                return;
                            }
                            instance = nextMap.getGlobalInstance(false);
                            arsha = false;
                        } else if (playerData.isInPrivateIns()) {
                            instance = nextMap.getPrivateInstance(playerData.getParty());
                        } else {
                            instance = nextMap.getGlobalInstance(arsha);
                        }
                        mapEnter(playerData, instance, warpGate, arsha);
                    }, 1);
                }
                return true;
            }
        }
        return false;
    }

    public static void mapEnter(PlayerData playerData, World instance, WarpGate warpGate, boolean arsha) {
        MapData nextMap = warpGate.getToMap();
        playerData.teleport(warpGate.getToLocation().as(instance));
        nextMap.enter(playerData, arsha);
        SomSound.Teleport.radius(playerData);
    }

    public static World getInstanceAtPlayer(PlayerData playerData, MapData nextMap) {
        if (playerData.isInPrivateIns()) {
            return nextMap.getPrivateInstance(playerData.getParty());
        } else {
            return nextMap.getGlobalInstance(playerData.isArsha());
        }
    }

    public void deletePrivateInstance(SomParty party) {
        privateInstance.remove(party);
    }

    public CustomItemStack viewItem() {
        int mel = getTeleportMel();
        CustomItemStack item = new CustomItemStack(icon);
        item.setDisplay(display);
        item.addLore(decoLore("推奨レベル") + minLevel);
        item.addLore(decoLore("必要メル") + mel);
        item.setCustomData("MapData", id);
        item.setCustomData("TeleportMel", mel);
        return item;
    }

    public int getTeleportMel() {
        return minLevel * 5;
    }

    public enum Type {
        Field,
        City,
        Room,
    }
}
