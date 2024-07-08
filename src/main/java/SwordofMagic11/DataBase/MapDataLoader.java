package SwordofMagic11.DataBase;

import SwordofMagic11.Component.WorldManager;
import SwordofMagic11.Custom.UnsetLocation;
import SwordofMagic11.Enemy.SpawnerData;
import SwordofMagic11.Item.SomItem;
import SwordofMagic11.Item.TeleportCrystal;
import SwordofMagic11.Map.*;
import SwordofMagic11.Player.Achievement.AchievementData;
import SwordofMagic11.StatusType;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Villager;
import org.bukkit.util.Vector;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;
import java.util.*;

import static SwordofMagic11.Component.Function.loreText;
import static SwordofMagic11.SomCore.Log;

public class MapDataLoader {


    private static final HashMap<String, MapData> mapDataList = new HashMap<>();
    private static final List<MapData> cities = new ArrayList<>();
    private static final List<MapData> rooms = new ArrayList<>();


    @NonNull
    public static MapData getMapData(String id) {
        if (!mapDataList.containsKey(id)) {
            Log("§c存在しないMapDataが参照されました -> " + id);
            throw new RuntimeException("§c存在しないMapDataが参照されました -> " + id);
        }
        return mapDataList.get(id);
    }

    public static Collection<MapData> getMapDataList() {
        return mapDataList.values();
    }

    public static List<String> getComplete() {
        List<String> complete = new ArrayList<>();
        for (MapData mapData : getMapDataList()) {
            complete.add(mapData.getId());
        }
        return complete;
    }

    public static List<MapData> getCities() {
        return cities;
    }

    public static List<MapData> getRooms() {
        return rooms;
    }

    public static void load() {
        int npcDataCount = 0;
        int warpGateCount = 0;
        int spawnerDataCount = 0;
        FileConfiguration defaultCapsuleDrop = YamlConfiguration.loadConfiguration(new File(DataBase.Path, "CapsuleDrop.yml"));
        FileConfiguration gathering = YamlConfiguration.loadConfiguration(new File(DataBase.Path, "Gathering.yml"));
        for (File file : DataBase.dumpFile(new File(DataBase.Path, "MapData"))) {
            try {
                FileConfiguration data = YamlConfiguration.loadConfiguration(file);
                String id = DataBase.fileId(file);
                MapData mapData = new MapData();
                mapData.setId(id);
                mapData.setDisplay(data.getString("Display"));
                mapData.setIcon(Material.valueOf(data.getString("Icon")));
                mapData.setMinBalance(data.getInt("MinBalance", 1));
                mapData.setMaxBalance(data.getInt("MaxBalance", 3));
                mapData.setType(MapData.Type.valueOf(data.getString("Type", "Field")));
                mapData.setRegion(data.getString("Region", "未設定"));
                mapData.setCommandBlock(data.getBoolean("CMDBlock", false));
                mapData.setLevelHide(data.getBoolean("LevelHide", false));

                World world = WorldManager.getContainer(id);
                if (data.isSet("Buyer.x")) {
                    NpcData npcData = new NpcData();
                    npcData.setId("Buyer");
                    npcData.setDisplay("買取屋");
                    npcData.setType(Villager.Type.TAIGA);
                    npcData.setProfession(Villager.Profession.FARMER);
                    npcData.setLocation(new UnsetLocation(data, "Buyer"));
                    npcData.setParam(Collections.singletonList("Buyer:Buyer"));
                    mapData.addNpc(npcData);
                }

                if (data.isSet("Smith.x")) {
                    NpcData npcData = new NpcData();
                    npcData.setId("Smith");
                    npcData.setDisplay("鍛冶屋");
                    npcData.setType(Villager.Type.TAIGA);
                    npcData.setProfession(Villager.Profession.ARMORER);
                    npcData.setLocation(new UnsetLocation(data, "Smith"));
                    npcData.setParam(Collections.singletonList("Smith:Smith"));
                    mapData.addNpc(npcData);
                }

                if (data.isSet("Classes.x")) {
                    NpcData npcData = new NpcData();
                    npcData.setId("Classes");
                    npcData.setDisplay("転職屋");
                    npcData.setType(Villager.Type.PLAINS);
                    npcData.setProfession(Villager.Profession.CLERIC);
                    npcData.setLocation(new UnsetLocation(data, "Classes"));
                    npcData.setParam(Collections.singletonList("Classes:Classes"));
                    mapData.addNpc(npcData);
                }

                if (data.isSet("Npc")) {
                    for (String npc : data.getStringList("Npc")) {
                        NpcData npcData = new NpcData();
                        npcData.setId(npc);
                        npcData.setDisplay(npc);
                        npcData.setType(Villager.Type.valueOf(data.getString(npc + ".Type")));
                        npcData.setProfession(Villager.Profession.valueOf(data.getString(npc + ".Profession")));
                        npcData.setLocation(new UnsetLocation(data, npc + ".Location"));
                        npcData.setParam(data.getStringList(npc + ".Param"));
                        mapData.addNpc(npcData);
                        npcDataCount++;
                    }
                }

                if (data.isSet("Gates")) {
                    for (String gate : data.getStringList("Gates")) {
                        WarpGate warpGate = new WarpGate();
                        warpGate.setId(gate);
                        warpGate.setFromLocation(new UnsetLocation(data, gate + ".From"));
                        warpGate.setRadius(data.getDouble(gate + ".Radius"));
                        warpGate.addWorld(world);
                        if (data.isSet(gate + ".ReqMaterial")) {
                            List<String> materialIds = data.getStringList(gate + ".ReqMaterial");
                            warpGate.setReqMaterial(materialIds);
                            MaterialDataLoader.registerNonUseSlot(materialIds);
                        }
                        warpGate.start();
                        mapData.addWarpGate(warpGate);
                        warpGateCount++;
                    }
                }

                if (data.isSet("Spawners")) {
                    for (String spawner : data.getStringList("Spawners")) {
                        SpawnerData spawnerData = new SpawnerData();
                        spawnerData.setId(spawner);
                        spawnerData.setMobData(MobDataLoader.getMobData(data.getString(spawner + ".MobData")));
                        spawnerData.setMinLevel(data.getInt(spawner + ".MinLevel"));
                        spawnerData.setMaxLevel(data.getInt(spawner + ".MaxLevel"));
                        spawnerData.setRadius(data.getInt(spawner + ".Radius"));
                        spawnerData.setMaxEnemy(data.getInt(spawner + ".MaxEnemy"));
                        spawnerData.setCoolTime(data.getInt(spawner + ".CoolTime"));
                        double x = data.getDouble(spawner + ".Location.x");
                        double y = data.getDouble(spawner + ".Location.y");
                        double z = data.getDouble(spawner + ".Location.z");
                        spawnerData.setLocation(new Vector(x, y, z));
                        spawnerData.addWorld(world);
                        spawnerData.start();
                        mapData.addSpawner(spawnerData);
                        spawnerDataCount++;
                    }
                }

                if (data.isSet("Room.MobData")) {
                    RoomData roomData = new RoomData(mapData);
                    roomData.setMobData(MobDataLoader.getMobData(data.getString("Room.MobData")));
                    roomData.setLevel(data.getInt("Room.Level"));
                    roomData.setSpawn(new UnsetLocation(data, "Room.Spawn"));
                    roomData.setExit(new UnsetLocation(data, "Room.Exit"));
                    HashMap<StatusType, Double> limitStatus = new HashMap<>();
                    for (StatusType statusType : StatusType.values()) {
                        String key = "LimitStatus." + statusType;
                        if (data.isSet(key)) {
                            limitStatus.put(statusType, data.getDouble(key));
                        }
                    }
                    roomData.setLimitStatus(limitStatus);
                    roomData.start();
                    mapData.setRoomData(roomData);
                    mapData.setType(MapData.Type.Room);
                    mapData.setMinLevel(roomData.getLevel());
                    rooms.add(mapData);
                }

                if (data.isSet("MinLevel")) {
                    mapData.setMinLevel(data.getInt("MinLevel"));
                } else if (!mapData.getSpawner().isEmpty()) {
                    int minLevel = Integer.MAX_VALUE;
                    for (SpawnerData spawnerData : mapData.getSpawner()) {
                        if (spawnerData.getMinLevel() < minLevel) {
                            minLevel = spawnerData.getMinLevel();
                        }
                    }
                    mapData.setMinLevel(minLevel);
                }

                if (gathering.isSet(mapData.getRegion() + ".Memorial")) {
                    mapData.setMemorial(MemorialDataLoader.getMemorialData(gathering.getString(mapData.getRegion() + ".Memorial")));
                }
                for (String type : new ArrayList<String>() {{
                    add("Mining");
                    add("Collect");
                    add("Fishing");
                }}) {
                    if (gathering.isSet(mapData.getRegion())) {
                        GatheringData gatheringData = new GatheringData();
                        gatheringData.setReqPower(gathering.getInt(mapData.getRegion() + "." + type + ".ReqPower"));
                        for (String block : gathering.getStringList(mapData.getRegion() + "." + type + ".Block")) {
                            gatheringData.addBlock(Material.valueOf(block));
                        }
                        for (String drop : gathering.getStringList(mapData.getRegion() + "." + type + ".Drop")) {
                            String[] split = drop.split(" ");
                            gatheringData.setDrop(split[0], Double.parseDouble(split[1]));
                        }
                        List<String> capsuleDrop = new ArrayList<>(defaultCapsuleDrop.getStringList("CapsuleDrop"));
                        capsuleDrop.addAll(gathering.getStringList(mapData.getRegion() + "." + type + ".CapsuleDrop"));
                        for (String str : capsuleDrop) {
                            String[] dropData = str.split(" ");
                            gatheringData.setCapsuleDrop(dropData[0], Double.parseDouble(dropData[1]));
                        }
                        switch (type) {
                            case "Mining" -> mapData.setMiningData(gatheringData);
                            case "Collect" -> mapData.setCollectData(gatheringData);
                            case "Fishing" -> mapData.setFishingData(gatheringData);
                        }
                    }
                }

                if (mapData.isCity()) {
                    TeleportCrystal crystal = new TeleportCrystal();
                    crystal.setId("TeleportCrystal_" + mapData.getId());
                    crystal.setDisplay(mapData.getDisplay() + "の転移結晶");
                    crystal.setIcon(Material.ENDER_EYE);
                    crystal.setCategory(SomItem.Category.TeleportCrystal);
                    crystal.setSell(10);
                    crystal.setCity(mapData);
                    ItemDataLoader.register(crystal);
                }

                mapDataList.put(id, mapData);
            } catch (Exception e) {
                DataBase.error(file, e);
            }
        }
        for (File file : DataBase.dumpFile(new File(DataBase.Path, "MapData"))) {
            try {
                FileConfiguration data = YamlConfiguration.loadConfiguration(file);
                String id = DataBase.fileId(file);
                MapData mapData = getMapData(id);
                if (data.isSet("Gates")) {
                    for (String gate : data.getStringList("Gates")) {
                        MapData toMap = getMapData(data.getString(gate + ".ToMap"));
                        WarpGate warpGate = mapData.getWarpGate(gate);
                        warpGate.setToMap(toMap);
                        if (data.isSet(gate + ".To.x")) {
                            UnsetLocation enter = new UnsetLocation(data, gate + ".To");
                            warpGate.setToLocation(enter);
                            if (toMap.isRoom()) {
                                toMap.getRoomData().setEnter(enter);
                            }
                        } else {
                            warpGate.setToLocation(toMap.getWarpGate(data.getString(gate + ".ToGate")).getFromLocation());
                        }
                        if (warpGate.getToLocation() == null) {
                            Log("§a[WarpGateLoader]§c" + mapData.getId() + "." + warpGate.getId() + "のToLocationが設定されていません");
                        }
                    }
                }
                if (data.isSet("Room.ExitMap")) {
                    mapData.getRoomData().setExitMap(getMapData(data.getString("Room.ExitMap")));
                }
            } catch (Exception e) {
                DataBase.error(file, e);
            }
        }
        for (MapData mapData : MapDataLoader.getMapDataList()) {
            if (mapData.isCity()) {
                cities.add(mapData);
            }
        }
        cities.sort(Comparator.comparing(MapData::getMinLevel));
        Log("§a[MapDataLoader]§b" + mapDataList.size() + "個をロードしました");
        Log("§a[NpcDataLoader]§b" + npcDataCount + "個をロードしました");
        Log("§a[WarpGateLoader]§b" + warpGateCount + "個をロードしました");
        Log("§a[SpawnerDataLoader]§b" + spawnerDataCount + "個をロードしました");
    }
}
