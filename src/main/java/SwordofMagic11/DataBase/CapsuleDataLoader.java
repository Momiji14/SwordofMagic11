package SwordofMagic11.DataBase;

import SwordofMagic11.Item.CapsuleData;
import SwordofMagic11.Item.SomItem;
import SwordofMagic11.StatusType;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Boss;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;
import java.util.*;

import static SwordofMagic11.SomCore.Log;

public class CapsuleDataLoader {

    public static final HashMap<CapsuleData, Double> DefaultCapsuleDrop = new HashMap<>();
    public static final HashMap<CapsuleData, Double> BossCapsuleDrop = new HashMap<>();
    private static final HashMap<String, CapsuleData> capsuleDataList = new HashMap<>();
    private static final List<CapsuleData> list = new ArrayList<>();

    @NonNull
    public static CapsuleData getCapsuleData(String id) {
        if (!capsuleDataList.containsKey(id)) {
            Log("§c存在しないCapsuleDataが参照されました -> " + id);
            throw new RuntimeException("§c存在しないCapsuleDataが参照されました -> " + id);
        }
        return capsuleDataList.get(id);
    }

    public static List<CapsuleData> getCapsuleDataList() {
        return list;
    }

    public static List<String> getComplete() {
        List<String> complete = new ArrayList<>();
        for (CapsuleData capsuleData : getCapsuleDataList()) {
            complete.add(capsuleData.getId());
        }
        return complete;
    }

    public static void load() {
        for (File file : DataBase.dumpFile(new File(DataBase.Path, "CapsuleData"))) {
            try {
                FileConfiguration data = YamlConfiguration.loadConfiguration(file);
                String id = DataBase.fileId(file);
                CapsuleData capsuleData = new CapsuleData();
                capsuleData.setId(id);
                capsuleData.setDisplay(data.getString("Display"));
                capsuleData.setIcon(Material.valueOf(data.getString("Icon")));
                capsuleData.setGroup(data.getString("Group"));
                capsuleData.setPercent(data.getDouble("Percent"));
                for (int i = 1; i <= 2; i++) {
                    if (data.isSet("Recipe-" + i)) {
                        HashMap<String, Integer> recipe = new HashMap<>();
                        for (String recipeData : data.getStringList("Recipe-" + i)) {
                            String[] split = recipeData.split("x");
                            recipe.put(split[0], Integer.parseUnsignedInt(split[1]));
                        }
                        capsuleData.addRecipe(recipe);
                    }
                }
                if (data.isSet("Color")) {
                    capsuleData.setColor(Color.fromRGB(data.getInt("Color.r"), data.getInt("Color.g"), data.getInt("Color.b")));
                }
                for (StatusType statusType : StatusType.values()) {
                    if (data.isSet(statusType.toString())) {
                        capsuleData.setStatus(statusType, data.getDouble(statusType.toString()));
                    }
                }
                register(capsuleData);
            } catch (Exception e) {
                DataBase.error(file, e);
            }
        }

        FileConfiguration defaultCapsuleDrop = YamlConfiguration.loadConfiguration(new File(DataBase.Path, "CapsuleDrop.yml"));
        for (String capsuleDrop : defaultCapsuleDrop.getStringList("CapsuleDrop")) {
            String[] dropData = capsuleDrop.split(" ");
            DefaultCapsuleDrop.put(getCapsuleData(dropData[0]), Double.parseDouble(dropData[1]));
        }

        for (String capsuleDrop : defaultCapsuleDrop.getStringList("BossDrop")) {
            String[] dropData = capsuleDrop.split(" ");
            BossCapsuleDrop.put(getCapsuleData(dropData[0]), Double.parseDouble(dropData[1]));
        }


        Log("§a[CapsuleDataLoader]§b" + capsuleDataList.size() + "個をロードしました");
    }

    public static void register(CapsuleData capsuleData) {
        capsuleDataList.put(capsuleData.getId(), capsuleData);
        list.add(capsuleData);
    }
}
