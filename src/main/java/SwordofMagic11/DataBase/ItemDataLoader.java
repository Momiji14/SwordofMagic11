package SwordofMagic11.DataBase;

import SwordofMagic11.Item.SomEquip;
import SwordofMagic11.Item.SomItem;
import SwordofMagic11.Item.VoteBox;
import SwordofMagic11.Player.Shop.EldShop;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;
import java.util.*;

import static SwordofMagic11.SomCore.Log;

public class ItemDataLoader {


    private static final HashMap<String, SomItem> itemDataList = new HashMap<>();
    private static final HashMap<String, List<SomItem>> seriesItemList = new HashMap<>();
    private static final List<SomItem> list = new ArrayList<>();

    @NonNull
    public static SomItem getItemData(String id) {
        if (!itemDataList.containsKey(id)) {
            Log("§c存在しないItemDataが参照されました -> " + id);
            throw new RuntimeException("§c存在しないItemDataが参照されました -> " + id);
        }
        return itemDataList.get(id).clone();
    }

    public static Collection<SomItem> getItemDataList() {
        return itemDataList.values();
    }

    public static Collection<SomItem> getSeries(String series) {
        if (!seriesItemList.containsKey(series)) {
            Log("§c存在しないSeriesが参照されました -> " + series);
            throw new RuntimeException("§c存在しないSeriesが参照されました -> " + series);
        }
        return seriesItemList.get(series);
    }

    public static List<String> getComplete() {
        List<String> complete = new ArrayList<>();
        for (SomItem item : getItemDataList()) {
            complete.add(item.getId());
        }
        return complete;
    }

    public static void load() {
        for (File file : DataBase.dumpFile(new File(DataBase.Path, "ItemData"))) {
            try {
                FileConfiguration data = YamlConfiguration.loadConfiguration(file);
                String id = DataBase.fileId(file);
                SomItem.Category category = SomItem.Category.valueOf(data.getString("Category"));
                SomItem item = new SomItem();
                item.setSell(data.getInt("Sell", 1));
                switch (category) {
                    case Equip, Tool -> {
                        SomEquip equip = new SomEquip();
                        SomEquip.Category equipCategory = SomEquip.Category.valueOf(data.getString("Equip.Category"));
                        equip.setCategory(category);
                        equip.setIcon(equipCategory.getIcon());
                        equip.setColor(equipCategory.getColor());
                        equip.setPower(data.getDouble("Power"));
                        equip.setEquipCategory(equipCategory);
                        equip.setReqLevel(data.getInt("ReqLevel", 1));
                        equip.setSell(100);
                        item = equip;
                    }
                }
                item.setId(id);
                item.setDisplay(data.getString("Display"));
                if (item.getIcon() == null) {
                    item.setIcon(Material.valueOf(data.getString("Icon")));
                }
                register(item);
            } catch (Exception e) {
                DataBase.error(file, e);
            }
        }

        FileConfiguration data = YamlConfiguration.loadConfiguration(new File(DataBase.Path, "Series.yml"));
        Set<String> BossList = new HashSet<>(data.getStringList("Boss"));
        for (String weapon : data.getStringList("Series")) {
            String[] split = weapon.split(" ");
            String name = split[0];
            String suffixType = split[1];
            double power = Double.parseDouble(split[2]);
            int reqLevel = Integer.parseUnsignedInt(split[3]);
            List<SomItem> list = new ArrayList<>();
            List<SomEquip.Category> categories = new ArrayList<>(SomEquip.Category.Weapons());
            if (split.length >= 5) {
                categories.addAll(SomEquip.Category.Armors(SomEquip.Slot.valueOf(split[4])));
            }
            boolean isBoss = BossList.contains(name);
            for (SomEquip.Category category : categories) {
                SomEquip seriesData = new SomEquip();
                seriesData.setId(name + (category.isWeapon() ? category : category.armorType()));
                seriesData.setCategory(SomItem.Category.Equip);
                seriesData.setIcon(category.getIcon());
                seriesData.setColor(category.getColor());
                seriesData.setDisplay(name + category.getSuffix(suffixType));
                seriesData.setPower(power);
                seriesData.setEquipCategory(category);
                seriesData.setSeries(name);
                seriesData.setReqLevel(reqLevel);
                seriesData.setSell(100);
                if (isBoss) seriesData.setCapsuleSlot(5);
                list.add(seriesData);
                register(seriesData);
            }
            seriesItemList.put(name, list);
        }

        for (String tool : data.getStringList("Tool")) {
            String[] split = tool.split(" ");
            String name = split[0];
            String suffixType = split[1];
            double power = Double.parseDouble(split[2]);
            for (SomEquip.Category category : SomEquip.Category.Tools()) {
                SomEquip toolData = new SomEquip();
                toolData.setId(name + category);
                toolData.setCategory(SomItem.Category.Equip);
                toolData.setIcon(category.getIcon());
                toolData.setColor(category.getColor());
                toolData.setDisplay(name + category.getSuffix(suffixType));
                toolData.setPower(power);
                toolData.setEquipCategory(category);
                toolData.setSell(100);
                register(toolData);
            }
        }

        register(new VoteBox());
        EldShop.donationItemRegister();

        list.sort(Comparator.comparing(SomItem::getId));
        Log("§a[ItemDataLoader]§b" + itemDataList.size() + "個をロードしました");
    }

    public static void register(SomItem item) {
        itemDataList.put(item.getId(), item);
        list.add(item);
    }
}
