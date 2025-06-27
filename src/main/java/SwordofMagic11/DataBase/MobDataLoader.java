package SwordofMagic11.DataBase;

import SwordofMagic11.AttributeType;
import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.Enemy.MobData;
import SwordofMagic11.Item.CapsuleData;
import SwordofMagic11.Item.Material.MaterialData;
import SwordofMagic11.Player.Achievement.AchievementData;
import SwordofMagic11.Player.Memorial.MemorialData;
import SwordofMagic11.StatusType;
import me.libraryaddict.disguise.disguisetypes.*;
import me.libraryaddict.disguise.disguisetypes.watchers.*;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.joml.Vector3f;

import java.io.File;
import java.util.*;

import static SwordofMagic11.Component.Function.ceil;
import static SwordofMagic11.Component.Function.loreText;
import static SwordofMagic11.SomCore.Log;

public class MobDataLoader {

    public static final double MemorialPercent = 0.00025;

    private static final HashMap<String, MobData> mobDataList = new HashMap<>();
    private static final List<MobData> list = new ArrayList<>();

    public static MobData getMobData(String id) {
        if (!mobDataList.containsKey(id)) Log("§c存在しないMobが参照されました -> " + id);
        return mobDataList.get(id);
    }

    public static List<MobData> getMobDataList() {
        return list;
    }

    public static List<String> getComplete() {
        List<String> complete = new ArrayList<>();
        for (MobData mobData : getMobDataList()) {
            complete.add(mobData.getId());
        }
        return complete;
    }

    public static void load() {
        for (File file : DataBase.dumpFile(new File(DataBase.Path, "MobData"))) {
            try {
                FileConfiguration data = YamlConfiguration.loadConfiguration(file);
                String id = DataBase.fileId(file);
                MobData mobData = new MobData();
                mobData.setId(id);
                mobData.setDisplay(data.getString("Display"));
                mobData.setEntityType(EntityType.valueOf(data.getString("Type")));
                mobData.setSize(data.getInt("Size", 1));
                mobData.setRank(MobData.Rank.valueOf(data.getString("Rank", "Normal")));
                mobData.setHostile(data.getBoolean("Hostile", false));
                mobData.setLevelHide(data.getBoolean("LevelHide", false));
                mobData.setObject(data.getBoolean("Object", false));
                mobData.setPenetration(data.getDouble("Penetration", 0));
                mobData.setDefenseBattle(data.getBoolean("DefenseBattle", true));
                if (data.isSet("CustomClass")) mobData.setCustomClass(data.getString("CustomClass"));
                if (data.isSet("Disguise.Type")) {
                    DisguiseType type = DisguiseType.valueOf(data.getString("Disguise.Type"));
                    if (type.isMob()) {
                        MobDisguise disguise = new MobDisguise(type);
                        switch (disguise.getType()) {
                            case OCELOT -> {
                                CatWatcher customWatcher = new CatWatcher(disguise);
                                customWatcher.setType(Cat.Type.valueOf(data.getString("Disguise.CatType", "BLACK")));
                                disguise.setWatcher(customWatcher);
                            }
                            case FOX -> {
                                FoxWatcher customWatcher = new FoxWatcher(disguise);
                                customWatcher.setType(Fox.Type.valueOf(data.getString("Disguise.FoxType", "RED")));
                                disguise.setWatcher(customWatcher);
                            }
                            case SLIME -> {
                                SlimeWatcher customWatcher = new SlimeWatcher(disguise);
                                customWatcher.setSize(data.getInt("Disguise.Size", 1));
                                disguise.setWatcher(customWatcher);
                            }
                            case PHANTOM -> {
                                PhantomWatcher customWatcher = new PhantomWatcher(disguise);
                                customWatcher.setSize(data.getInt("Disguise.Size", 1));
                                disguise.setWatcher(customWatcher);
                            }
                            case SHEEP -> {
                                SheepWatcher customWatcher = new SheepWatcher(disguise);
                                customWatcher.setColor(DyeColor.valueOf(data.getString("Disguise.Color", "WHITE")));
                                customWatcher.setSheared(data.getBoolean("Sheared", false));
                                customWatcher.setRainbowWool(data.getBoolean("Disguise.Rainbow", false));
                                disguise.setWatcher(customWatcher);
                            }
                            case HORSE -> {
                                HorseWatcher customWatcher = new HorseWatcher(disguise);
                                customWatcher.setColor(Horse.Color.valueOf(data.getString("BROWN", "BROWN")));
                                customWatcher.setStyle(Horse.Style.valueOf(data.getString("Style", "NONE")));
                                disguise.setWatcher(customWatcher);
                            }
                            case RABBIT -> {
//                                RabbitWatcher customWatcher = new RabbitWatcher(disguise);
//                                customWatcher.setType(RabbitType.valueOf(data.getString("Disguise.RabbitType", "WHITE")));
//                                disguise.setWatcher(customWatcher);
                            }
                            case ZOMBIE_VILLAGER -> {
                                ZombieVillagerWatcher customWatcher = new ZombieVillagerWatcher(disguise);
                                customWatcher.setBiome(Villager.Type.valueOf(data.getString("Disguise.VillagerType", "PLAINS")));
                                customWatcher.setProfession(Villager.Profession.valueOf(data.getString("Profession", "ARMORER")));
                                disguise.setWatcher(customWatcher);
                            }
                            case SNOWMAN -> {
                                SnowmanWatcher customWatcher = new SnowmanWatcher(disguise);
                                customWatcher.setDerp(data.getBoolean("Disguise.Derp", false));
                                disguise.setWatcher(customWatcher);
                            }
                            case IRON_GOLEM -> {
                                IronGolemWatcher customWatcher = new IronGolemWatcher(disguise);
                                customWatcher.setCracks(GolemCrack.valueOf(data.getString("Disguise.Crack", "HEALTH_100")));
                                disguise.setWatcher(customWatcher);
                            }
                        }
                        mobData.setDisguise(disguise);
                    } else if (type.isMisc()) {
                        MiscDisguise disguise = new MiscDisguise(type);
                        switch (type) {
                            case BLOCK_DISPLAY -> {
                                BlockDisplayWatcher customWatcher = new BlockDisplayWatcher(disguise);
                                customWatcher.setBlock(Material.valueOf(data.getString("Disguise.Block")).createBlockData());
                                String[] scale = data.getString("Disguise.Scale", "1.0,1.0,1.0").split(",");
                                customWatcher.setScale(new Vector3f(Float.parseFloat(scale[0]), Float.parseFloat(scale[1]), Float.parseFloat(scale[2])));
                                disguise.setWatcher(customWatcher);
                            }
                            case FALLING_BLOCK -> {
                                FallingBlockWatcher customWatcher = new FallingBlockWatcher(disguise);
                                customWatcher.setBlock(new CustomItemStack(Material.valueOf(data.getString("Disguise.Block", "STONE"))));
                                disguise.setWatcher(customWatcher);
                            }
                        }
                        mobData.setDisguise(disguise);
                    }
                    FlagWatcher watcher = mobData.getDisguise().getWatcher();
                    if (data.isSet("Disguise.OffsetY")) {
                        watcher.setYModifier((float) data.getDouble("Disguise.OffsetY"));
                    }
                    if (data.isSet("Disguise.PitchLock")) {
                        watcher.setPitchLock((float) data.getDouble("Disguise.PitchLock"));
                    }
                    if (data.isSet("Disguise.Glowing")) {
                        watcher.setGlowing(data.getBoolean("Disguise.Glowing"));
                    }
                    if (data.isSet("Disguise.MainHand")) {
                        String[] split = data.getString("Disguise.MainHand").split(":");
                        Material material = Material.valueOf(split[0]);
                        ItemStack item = ItemStack.of(material);
                        //if (split.length == 2) item.setCustomModelData(Integer.parseUnsignedInt(split[1]));
                        watcher.setItemInMainHand(item);
                    }
                    if (data.isSet("Disguise.OffHand")) {
                        String[] split = data.getString("Disguise.OffHand").split(":");
                        Material material = Material.valueOf(split[0]);
                        ItemStack item = ItemStack.of(material);
                        //if (split.length == 2) item.setCustomModelData(Integer.parseUnsignedInt(split[1]));
                        watcher.setItemInOffHand(item);
                    }
                    if (data.isSet("Disguise.Head")) {
                        String[] split = data.getString("Disguise.Head").split(":");
                        Material material = Material.valueOf(split[0]);
                        ItemStack item = ItemStack.of(material);
                        if (split.length == 2) {
                            String[] color = split[1].split(",");
                            CustomItemStack.setLeatherArmorColor(item, Color.fromRGB(Integer.parseUnsignedInt(color[0]), Integer.parseUnsignedInt(color[1]), Integer.parseUnsignedInt(color[2])));
                        }
                        watcher.setHelmet(item);
                    }
                    if (data.isSet("Disguise.Chest")) {
                        String[] split = data.getString("Disguise.Chest").split(":");
                        Material material = Material.valueOf(split[0]);
                        ItemStack item = ItemStack.of(material);
                        if (split.length == 2) {
                            String[] color = split[1].split(",");
                            CustomItemStack.setLeatherArmorColor(item, Color.fromRGB(Integer.parseUnsignedInt(color[0]), Integer.parseUnsignedInt(color[1]), Integer.parseUnsignedInt(color[2])));
                        }
                        watcher.setChestplate(item);
                    }
                    if (data.isSet("Disguise.Legs")) {
                        String[] split = data.getString("Disguise.Legs").split(":");
                        Material material = Material.valueOf(split[0]);
                        ItemStack item = ItemStack.of(material);
                        if (split.length == 2) {
                            String[] color = split[1].split(",");
                            CustomItemStack.setLeatherArmorColor(item, Color.fromRGB(Integer.parseUnsignedInt(color[0]), Integer.parseUnsignedInt(color[1]), Integer.parseUnsignedInt(color[2])));
                        }
                        watcher.setLeggings(item);
                    }
                    if (data.isSet("Disguise.Boots")) {
                        String[] split = data.getString("Disguise.Boots").split(":");
                        Material material = Material.valueOf(split[0]);
                        ItemStack item = ItemStack.of(material);
                        if (split.length == 2) {
                            String[] color = split[1].split(",");
                            CustomItemStack.setLeatherArmorColor(item, Color.fromRGB(Integer.parseUnsignedInt(color[0]), Integer.parseUnsignedInt(color[1]), Integer.parseUnsignedInt(color[2])));
                        }
                        watcher.setBoots(item);
                    }
                    if (data.isSet("Disguise.Glowing")) {
                        watcher.setGlowing(data.getBoolean("Disguise.Glowing"));
                    }
                    if (data.isSet("Disguise.GlowColor")) {
                        watcher.setGlowColor(ChatColor.valueOf(data.getString("Disguise.GlowColor")));
                    }
                    mobData.getDisguise().setWatcher(watcher);
                }
                if (data.isSet("Collision.Size")) mobData.setCollisionSize(data.getDouble("Collision.Size"));
                if (data.isSet("Collision.SizeY")) mobData.setCollisionSize(data.getDouble("Collision.SizeY"));
                for (AttributeType attr : AttributeType.values()) {
                    if (data.isSet("AttributeRatio." + attr.getNick())) {
                        mobData.setAttributeRatio(attr, data.getDouble("AttributeRatio." + attr.getNick()));
                    }
                }
                if (mobData.getAttributeRatio().isEmpty()) {
                    mobData.setAttributeRatio(AttributeType.Strength, 0.25);
                    mobData.setAttributeRatio(AttributeType.Intelligence, 0.25);
                    mobData.setAttributeRatio(AttributeType.Dexterity, 0.1);
                    mobData.setAttributeRatio(AttributeType.Agility, 0.25);
                    mobData.setAttributeRatio(AttributeType.Spirit, 0.18);
                    mobData.setAttributeRatio(AttributeType.Vitality, 0.18);
                }
                for (String str : data.getStringList("MaterialDrop")) {
                    String[] dropData = str.split(" ");
                    String[] amount = dropData[2].contains("-") ? dropData[2].split("-") : new String[]{dropData[2], dropData[2]};
                    double percent = Double.parseDouble(dropData[1]);
                    int minAmount = Integer.parseUnsignedInt(amount[0]);
                    int maxAmount = Integer.parseUnsignedInt(amount[1]);
                    MaterialData material = MaterialDataLoader.getMaterialData(dropData[0]);
                    if (material.getRawSell() == -1) {
                        int sell = ceil(mobData.getRank().sellMultiply() / (((minAmount + maxAmount)/2.0) * percent));
                        material.setSell(sell);
                    }
                    mobData.addMaterialDrop(new MobData.MaterialDrop(material, percent, minAmount, maxAmount));
                }
                for (String str : data.getStringList("CapsuleDrop")) {
                    String[] dropData = str.split(" ");
                    mobData.setCapsuleDrop(dropData[0], Double.parseDouble(dropData[1]));
                }
                if (data.isSet("Memorial.Icon")) {
                    Collection<String> statusList = data.getStringList("Memorial.List");
                    MemorialData memorialData = new MemorialData();
                    Material icon = Material.valueOf(data.getString("Memorial.Icon", "BARRIER"));
                    memorialData.setId(id);
                    memorialData.setIcon(icon);
                    memorialData.setDisplay(mobData.getDisplay());
                    memorialData.setRank(mobData.getRank());
                    memorialData.setPoint((int) (mobData.getRank().memorialPoint() * data.getDouble("Memorial.PointMultiply", 1.0)));
                    for (String statusType : statusList) {
                        memorialData.addStatus(StatusType.valueOf(statusType));
                    }
                    MaterialData material = MemorialDataLoader.register(memorialData, 1000, data.getBoolean("Memorial.Gacha", true));
                    mobData.addMaterialDrop(new MobData.MaterialDrop(material, MemorialPercent, 1, 1));
                    mobData.setMemorial(memorialData);

                    CapsuleData capsuleData = new CapsuleData();
                    capsuleData.setId(id + "ソール");
                    capsuleData.setDisplay(id + "ソール");
                    capsuleData.setIcon(icon);
                    capsuleData.setGroup("ソール");
                    capsuleData.setPercent(0.1);
                    for (String statusType : statusList) {
                        capsuleData.setStatus(StatusType.valueOf(statusType), mobData.getRank().capsuleMultiply() / statusList.size());
                    }
                    mobData.setCapsuleDrop(capsuleData, mobData.getRank().soulCapsuleDrop());
                    CapsuleDataLoader.register(capsuleData);

                    for (int count : new int[]{10000, 50000, 100000}) {
                        AchievementData achievementData = new AchievementData();
                        achievementData.setId("EnemyKill_" + count + "_" + mobData.getId());
                        achievementData.setDisplay(mobData.getId() + " [" + count + "体討伐]");
                        achievementData.setIcon(icon);
                        achievementData.setLore(loreText(mobData.getDisplay() + "を§e" + count + "体§a倒す"));
                        String line = "%EnemyKillCount%の" + mobData.getDisplay();
                        switch (count) {
                            case 10000 -> achievementData.setLine(Collections.singletonList("§e" + line));
                            case 50000 -> {
                                achievementData.addLine("§e" + line, 10);
                                achievementData.addLine("§b" + line, 10);
                            }
                            case 100000 -> {
                                for (int i = 0; i < 5; i++) {
                                    achievementData.addLine("§e§l" + line, 2);
                                    achievementData.addLine("§b§l§n" + line, 2);
                                }
                            }
                        }

                        achievementData.addReplace((playerData, text) -> text.replace("%EnemyKillCount%", String.valueOf(playerData.statistics().enemyKill(mobData.getId()))));
                        achievementData.setPredicate(playerData -> playerData.statistics().enemyKill(id) >= count);
                        AchievementDataLoader.register(achievementData);
                    }
                }
                if (mobData.getRank() == MobData.Rank.Boss) CapsuleDataLoader.BossCapsuleDrop.forEach(mobData::setCapsuleDrop);
                mobDataList.put(id, mobData);
                list.add(mobData);
            } catch (Exception e) {
                DataBase.error(file, e);
            }
        }
        list.sort(Comparator.comparing(MobData::getId));
        Log("§a[MobDataLoader]§b" + mobDataList.size() + "種をロードしました");
    }
}
