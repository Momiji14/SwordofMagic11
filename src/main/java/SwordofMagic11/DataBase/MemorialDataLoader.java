package SwordofMagic11.DataBase;

import SwordofMagic11.Enemy.MobData;
import SwordofMagic11.Item.Material.MaterialData;
import SwordofMagic11.Item.Material.MemorialMaterial;
import SwordofMagic11.Player.Memorial.MemorialData;
import SwordofMagic11.StatusType;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import static SwordofMagic11.SomCore.Log;

public class MemorialDataLoader {


    private static final HashMap<String, MemorialData> memorialDataList = new HashMap<>();
    private static final List<MemorialData> list = new ArrayList<>();
    private static final List<MemorialMaterial> gachaList = new ArrayList<>();

    public static MemorialData getMemorialData(String id) {
        if (!memorialDataList.containsKey(id)) {
            Log("§c存在しないMemorialDataが参照されました -> " + id);
            throw new RuntimeException("§c存在しないMemorialDataが参照されました -> " + id);
        }
        return memorialDataList.get(id);
    }

    public static List<MemorialData> getMemorialDataList() {
        return list;
    }

    public static List<String> getComplete() {
        List<String> complete = new ArrayList<>();
        for (MemorialData memorialData : getMemorialDataList()) {
            complete.add(memorialData.getId());
        }
        return complete;
    }

    public static List<MemorialMaterial> getGachaList() {
        return gachaList;
    }

    public static int size() {
        return memorialDataList.size();
    }

    public static void load() {
        for (File file : DataBase.dumpFile(new File(DataBase.Path, "MemorialData"))) {
            try {
                FileConfiguration data = YamlConfiguration.loadConfiguration(file);
                String id = DataBase.fileId(file);
                SwordofMagic11.Player.Memorial.MemorialData memorialData = new SwordofMagic11.Player.Memorial.MemorialData();
                memorialData.setId(id);
                memorialData.setIcon(Material.valueOf(data.getString("Icon", "BARRIER")));
                memorialData.setDisplay(data.getString("Display"));
                memorialData.setRank(MobData.Rank.valueOf(data.getString("Rank", "Normal")));
                memorialData.setPoint(data.getInt("Point"));
                for (String statusType : data.getStringList("List")) {
                    memorialData.addStatus(StatusType.valueOf(statusType));
                }
                register(memorialData, data.getInt("Sell", 0), data.getBoolean("Gacha", true));
            } catch (Exception e) {
                DataBase.error(file, e);
            }
        }

        list.sort(Comparator.comparing(MemorialData::getId));
        Log("§a[MemorialDataLoader]§b" + memorialDataList.size() + "個をロードしました");
    }

    public static MaterialData register(MemorialData memorial, int sell, boolean gacha) {
        MemorialMaterial materialData = new MemorialMaterial(memorial);
        materialData.setId(memorial.getId() + "Memorial");
        materialData.setIcon(memorial.getIcon());
        materialData.setDisplay(memorial.getDisplay() + "のメモリアル");
        materialData.setRare(true);
        materialData.setSell(sell);
        MaterialDataLoader.register(materialData);
        MaterialDataLoader.registerNonUseSlot(materialData.getId());
        memorialDataList.put(memorial.getId(), memorial);
        list.add(memorial);
        if (gacha) gachaList.add(materialData);
        return materialData;
    }
}
