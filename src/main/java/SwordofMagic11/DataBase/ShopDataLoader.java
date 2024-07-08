package SwordofMagic11.DataBase;

import SwordofMagic11.Item.SomEquip;
import SwordofMagic11.Item.SomItem;
import SwordofMagic11.Player.Shop.ShopData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import static SwordofMagic11.Component.Function.ceil;
import static SwordofMagic11.SomCore.Log;

public class ShopDataLoader {

    public static double ArmorBuyMultiply = 0.65;

    private static final HashMap<String, ShopData> shopDataList = new HashMap<>();

    @NonNull
    public static ShopData getShopData(String id) {
        if (!shopDataList.containsKey(id)) {
            Log("§c存在しないShopDataが参照されました -> " + id);
            throw new RuntimeException("§c存在しないShopDataが参照されました -> " + id);
        }
        return shopDataList.get(id);
    }

    public static Collection<ShopData> getShopDataList() {
        return shopDataList.values();
    }

    public static List<String> getComplete() {
        List<String> complete = new ArrayList<>();
        for (ShopData shopData : getShopDataList()) {
            complete.add(shopData.getDisplay());
        }
        return complete;
    }

    public static void load() {
        for (File file : DataBase.dumpFile(new File(DataBase.Path, "ShopData"))) {
            try {
                FileConfiguration data = YamlConfiguration.loadConfiguration(file);
                String id = DataBase.fileId(file);
                ShopData shopData = new ShopData();
                shopData.setDisplay(data.getString("Display"));
                int slot = 0;
                for (String str : data.getStringList("ShopData")) {
                    String[] split = str.split(" ");
                    String[] split2 = split[0].split("x");
                    String name = split2[0];
                    int mel = Integer.parseUnsignedInt(split[1]);
                    if (Integer.parseUnsignedInt(split[2]) == -1) {
                        slot++;
                    } else {
                        slot = Integer.parseUnsignedInt(split[2]);
                    }
                    List<ShopData.Recipe> materialRecipes = new ArrayList<>();
                    List<ShopData.Recipe> weaponRecipes = new ArrayList<>();
                    List<ShopData.Recipe> armorRecipes = new ArrayList<>();
                    for (int i = 3; i < split.length; i++) {
                        split2 = split[i].split("x");
                        String[] matId = split2[0].split("@");
                        if (matId.length == 2) {
                            if (matId[1].contains("素材化武器")) {
                                MaterialDataLoader.registerMaterialize(MaterialDataLoader.Type.Weapon, matId[1].replace("素材化武器", ""));
                            } else if (matId[1].contains("素材化防具")) {
                                MaterialDataLoader.registerMaterialize(MaterialDataLoader.Type.Armor, matId[1].replace("素材化防具", ""));
                            }
                            switch (matId[0]) {
                                case "Weapon" -> weaponRecipes.add(new ShopData.Recipe(matId[1], Integer.parseUnsignedInt(split2[1])));
                                case "Armor" -> armorRecipes.add(new ShopData.Recipe(matId[1], Integer.parseUnsignedInt(split2[1])));
                            }
                        } else {
                            materialRecipes.add(new ShopData.Recipe(matId[0], Integer.parseUnsignedInt(split2[1])));
                        }
                    }
                    if (name.contains("Series@")) {
                        for (SomItem item : ItemDataLoader.getSeries(name.replace("Series@", ""))) {
                            SomEquip equip = (SomEquip) item;
                            List<ShopData.Recipe> recipes = new ArrayList<>(materialRecipes);
                            if (equip.isWeapon()) {
                                recipes.addAll(weaponRecipes);
                                shopData.addContainer(new ShopData.Container(item.getId(), mel, recipes, slot));
                            }
                            if (equip.isArmor()) {
                                recipes.addAll(armorRecipes);
                                shopData.addContainer(new ShopData.Container(item.getId(), ceil(mel * ArmorBuyMultiply), recipes, slot));
                            }
                            slot++;
                        }
                        slot--;
                    } else {
                        shopData.addContainer(new ShopData.Container(name, mel, materialRecipes, slot));
                    }
                }
                shopDataList.put(id, shopData);
            } catch (Exception e) {
                DataBase.error(file, e);
            }
        }

        Log("§a[ShopDataLoader]§b" + shopDataList.size() + "個をロードしました");
    }
}
