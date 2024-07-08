package SwordofMagic11.DataBase;

import SwordofMagic11.Item.SomEquip;
import SwordofMagic11.Player.Gathering.CraftData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import static SwordofMagic11.Item.Material.PotionMaterial.MaxPotionLevel;
import static SwordofMagic11.Item.Material.PotionMaterial.Suffix;
import static SwordofMagic11.SomCore.Log;

public class CraftDataLoader {

    private static final HashMap<String, CraftData> craftDataList = new HashMap<>();
    private static final List<CraftData> list = new ArrayList<>();

    @NonNull
    public static CraftData getCraftData(String id) {
        if (!craftDataList.containsKey(id)) {
            Log("§c存在しないCraftDataが参照されました -> " + id);
            throw new RuntimeException("§c存在しないCraftDataが参照されました -> " + id);
        }
        return craftDataList.get(id);
    }

    public static List<CraftData> getCraftDataList() {
        return list;
    }

    public static void load() {
        for (File file : DataBase.dumpFile(new File(DataBase.Path, "CraftData"))) {
            try {
                FileConfiguration data = YamlConfiguration.loadConfiguration(file);
                String id = DataBase.fileId(file);
                String[] resultData = data.getString("Result").split("x");
                CraftData.Result result;
                if (MaterialDataLoader.getComplete().contains(resultData[0])) {
                    result = new CraftData.Result(MaterialDataLoader.getMaterialData(resultData[0]), Integer.parseUnsignedInt(resultData[1]));
                } else if (ItemDataLoader.getComplete().contains(resultData[0])) {
                    result = new CraftData.Result(ItemDataLoader.getItemData(resultData[0]));
                } else throw new RuntimeException("存在しないMaterialおよびSomItemです");
                double cost = data.getDouble("Cost");
                double exp = data.getDouble("Exp");
                List<CraftData.Pack> recipe = new ArrayList<>();
                for (String recipeData : data.getStringList("Recipe")) {
                    String[] split = recipeData.split("x");
                    recipe.add(new CraftData.Pack(MaterialDataLoader.getMaterialData(split[0]), Integer.parseUnsignedInt(split[1])));
                }
                CraftData craftData = new CraftData(id, result, recipe, cost, exp);

                craftDataList.put(id, craftData);
                list.add(craftData);
            } catch (Exception e) {
                DataBase.error(file, e);
            }
        }

        FileConfiguration data = YamlConfiguration.loadConfiguration(new File(DataBase.Path, "Series.yml"));
        for (String tool : data.getStringList("Tool")) {
            String[] split = tool.split(" ");
            String name = split[0];
            if (split.length > 3) {
                for (SomEquip.Category category : SomEquip.Category.Tools()) {
                    SomEquip equipTool = (SomEquip) ItemDataLoader.getItemData(name + category);
                    CraftData.Result result = new CraftData.Result(equipTool);
                    List<CraftData.Pack> recipe = new ArrayList<>();
                    for (int i = 3; i < split.length; i++) {
                        String[] recipeData = split[i].split("x");
                        recipe.add(new CraftData.Pack(MaterialDataLoader.getMaterialData(recipeData[0]), Integer.parseUnsignedInt(recipeData[1])));
                    }
                    double cost = 100 + Math.pow(equipTool.getPower(), 6);
                    CraftData craftData = new CraftData(name + category, result, recipe, cost, cost * 5);
                    craftDataList.put(craftData.id(), craftData);
                    list.add(craftData);
                }
            }
        }

        for (int i = 2; i <= MaxPotionLevel; i++) {
            double cost = 10 + Math.pow(i, 4);

            for (MaterialDataLoader.Potion potion : MaterialDataLoader.Potion.values()) {
                CraftData.Result result = new CraftData.Result(MaterialDataLoader.getMaterialData(potion.id() + Suffix[i-1]), 1);
                List<CraftData.Pack> recipe = new ArrayList<>();
                recipe.add(new CraftData.Pack(MaterialDataLoader.getMaterialData(potion.id() + Suffix[i-2]), 1));
                String suffix;
                switch (potion) {
                    case Heal, Mana -> suffix = String.valueOf(i);
                    case Attack, Defence -> suffix = Suffix[i-1];
                    default -> suffix = null;
                }
                recipe.add(new CraftData.Pack(MaterialDataLoader.getMaterialData(potion.craft() + suffix), 10));
                CraftData craftData = new CraftData(potion.id() + Suffix[i-1], result, recipe, cost, cost * 5);
                craftDataList.put(craftData.id(), craftData);
                list.add(craftData);
            }
        }

        list.sort(Comparator.comparing(CraftData::id));
        list.sort(Comparator.comparing(craftData -> {
            switch (craftData.result().type()) {
                case Item -> {
                    if (craftData.result().somItem() instanceof SomEquip equip) {
                        return equip.getPower();
                    }
                    return -1.0;
                }
                case Material -> {
                    return 1.0;
                }
                default -> {
                    return 0.0;
                }
            }
        }));
        Log("§a[CraftDataLoader]§b" + craftDataList.size() + "個をロードしました");
    }
}
