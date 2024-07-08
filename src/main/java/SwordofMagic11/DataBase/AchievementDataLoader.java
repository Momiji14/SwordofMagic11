package SwordofMagic11.DataBase;

import SwordofMagic11.Map.RoomData;
import SwordofMagic11.Player.Achievement.AchievementData;
import SwordofMagic11.Player.ClassType;
import SwordofMagic11.Player.Gathering.GatheringMenu;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Player.Statistics.Statistics;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;

import static SwordofMagic11.Component.Function.decoSeparator;
import static SwordofMagic11.Component.Function.loreText;
import static SwordofMagic11.SomCore.Log;

public class AchievementDataLoader {


    private static final HashMap<String, AchievementData> achievementDataList = new HashMap<>();
    private static final List<AchievementData> list = new ArrayList<>();

    @NonNull
    public static AchievementData getAchievementData(String id) {
        if (!achievementDataList.containsKey(id)) {
            Log("§c存在しないAchievementDataが参照されました -> " + id);
            throw new RuntimeException("§c存在しないAchievementDataが参照されました -> " + id);
        }
        return achievementDataList.get(id);
    }

    public static int size() {
        return achievementDataList.size();
    }

    public static List<AchievementData> getAchievementDataList() {
        return list;
    }

    public static List<String> getComplete() {
        List<String> complete = new ArrayList<>();
        for (AchievementData achievementData : getAchievementDataList()) {
            complete.add(achievementData.getId());
        }
        return complete;
    }

    public static void load() {
        for (File file : DataBase.dumpFile(new File(DataBase.Path, "AchievementData"))) {
            try {
                FileConfiguration data = YamlConfiguration.loadConfiguration(file);
                String id = DataBase.fileId(file);
                AchievementData achievementData = new AchievementData();
                achievementData.setId(id);
                achievementData.setDisplay(data.getString("Display"));
                achievementData.setIcon(Material.valueOf(data.getString("Icon")));
                if (data.isSet("Color")) {
                    achievementData.setColor(Color.fromRGB(data.getInt("Color.r"), data.getInt("Color.g"), data.getInt("Color.b")));
                }
                if (data.isSet("Lore")) achievementData.setLore(loreText(data.getStringList("Lore")));
                achievementData.setType(AchievementData.Type.valueOf(data.getString("Predicate.Type")));
                switch (achievementData.getType()) {
                    case None -> {
                        achievementData.setPredicate(playerData -> false);
                        achievementData.setLore(new ArrayList<>());
                    }
                    case Custom -> {
                        Class<?> preficateClass = Class.forName("SwordofMagic11.Player.Achievement.Custom." + data.getString("Predicate.Custom"));
                        Constructor<?> constructor = preficateClass.getConstructor();
                        achievementData.setPredicate((Predicate<PlayerData>) constructor.newInstance());
                    }
                    case ClassLevel -> {
                        ClassType classType = ClassType.valueOf(data.getString("Predicate.Class"));
                        int level = data.getInt("Predicate.Level");
                        achievementData.setPredicate(playerData -> playerData.classes().getLevel(classType) >= level);
                        achievementData.setLore(loreText(classType.getColorDisplay() + "§aが§eLv" + level + "§aになると§e獲得§a出来ます"));
                    }
                    case GatheringLevel -> {
                        GatheringMenu.Type type = GatheringMenu.Type.valueOf(data.getString("Predicate.Gathering"));
                        int level = data.getInt("Predicate.Level");
                        achievementData.setPredicate(playerData -> playerData.gatheringMenu().getLevel(type) >= level);
                        achievementData.setLore(loreText("§e" + type.getDisplay() + "§aが§eLv" + level + "§aになると§e獲得§a出来ます"));
                    }
                    case Statistics -> {
                        String sign = data.getString("Predicate.Sign");
                        if (data.isSet("Predicate.DoubleEnum")) {
                            double value = data.getDouble("Predicate.Value");
                            Statistics.DoubleEnum doubleEnum = Statistics.DoubleEnum.valueOf(data.getString("Predicate.DoubleEnum"));
                            switch (sign) {
                                case "More" -> {
                                    achievementData.setPredicate(playerData -> playerData.statistics().get(doubleEnum) >= value);
                                    achievementData.setLore(loreText("§e" + doubleEnum.getDisplay() + "§aが§e" + value + "以上§aで§e獲得§a出来ます"));
                                }
                                case "Less" -> {
                                    achievementData.setPredicate(playerData -> playerData.statistics().get(doubleEnum) <= value);
                                    achievementData.setLore(loreText("§e" + doubleEnum.getDisplay() + "§aが§e" + value + "以下§aで§e獲得§a出来ます"));
                                }
                            }
                        } else if (data.isSet("Predicate.IntEnum")) {
                            int value = data.getInt("Predicate.Value");
                            Statistics.IntEnum intEnum = Statistics.IntEnum.valueOf(data.getString("Predicate.IntEnum"));
                            switch (sign) {
                                case "More" -> {
                                    achievementData.setPredicate(playerData -> playerData.statistics().get(intEnum) >= value);
                                    achievementData.setLore(loreText("§e" + intEnum.getDisplay() + "§aが§e" + value + "以上§aで§e獲得§a出来ます"));
                                }
                                case "Less" -> {
                                    achievementData.setPredicate(playerData -> playerData.statistics().get(intEnum) <= value);
                                    achievementData.setLore(loreText("§e" + intEnum.getDisplay() + "§aが§e" + value + "以下§aで§e獲得§a出来ます"));
                                }
                            }
                        }
                    }
                    case Achievement -> {
                        List<String> list = data.getStringList("Predicate.Achievement");
                        achievementData.setPredicate(playerData -> {
                            for (String achievementId : list) {
                                if (!playerData.achievementMenu().has(achievementId)) {
                                    return false;
                                }
                            }
                            return true;
                        });
                        List<String> lore = new ArrayList<>();
                        lore.add("§a特定の§e称号§aを所持していると獲得できます");
                        lore.add(decoSeparator("必要称号"));
                        for (String achievementId : list) {
                            lore.add("§7・§e" + achievementId);
                        }
                        achievementData.setLore(lore);
                    }
                    case Eld -> {
                        int eld = data.getInt("Predicate.Eld");
                        achievementData.setPredicate(playerData -> playerData.donation().getTotalDonation() >= eld);
                        achievementData.setLore(loreText("§d合計寄付§aを§e" + eld + "以上§aで§e獲得§a出来ます"));
                    }
                    case BossTimeAttack -> {
                        String mapID = data.getString("Predicate.MapID");
                        int time = data.getInt("Predicate.Time");
                        achievementData.setPredicate(playerData -> {
                            if (playerData.bossTimeAttackMenu().hasRecord(mapID)) {
                                return time > playerData.bossTimeAttackMenu().getRecord(mapID).time();
                            }
                            return false;
                        });
                        achievementData.setLore(loreText("§cTAモード§aで§e" + mapID + "§aを§e" + time + "秒以内§aに§eクリア§aする"));
                    }
                }
                achievementData.setLine(data.getStringList("Line"));
                if (achievementData.getLore() == null) {
                    achievementData.setLore(loreText("§c説明文未設定"));
                    Log("§a[AchievementDataLoader]§c" + achievementData.getId() + "の説明文が設定されていません");
                }
                register(achievementData);
            } catch (Exception e) {
                DataBase.error(file, e);
            }
        }

        //list.sort(Comparator.comparing(AchievementData::getId));
        Log("§a[AchievementDataLoader]§b" + achievementDataList.size() + "個をロードしました");
    }

    public static void register(AchievementData achievementData) {
        achievementDataList.put(achievementData.getId(), achievementData);
        list.add(achievementData);
    }
}
