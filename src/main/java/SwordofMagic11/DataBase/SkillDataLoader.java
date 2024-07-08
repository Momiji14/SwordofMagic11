package SwordofMagic11.DataBase;

import SwordofMagic11.Skill.Parameter;
import SwordofMagic11.Skill.SkillData;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import static SwordofMagic11.SomCore.Log;

public class SkillDataLoader {


    private static final HashMap<String, SkillData> skillDataList = new HashMap<>();
    private static final List<SkillData> list = new ArrayList<>();

    @NonNull
    public static SkillData getSkillData(String id) {
        if (!skillDataList.containsKey(id)) {
            Log("§c存在しないSkillDataが参照されました -> " + id);
            throw new RuntimeException("§c存在しないSkillDataが参照されました -> " + id);
        }
        return skillDataList.get(id);
    }

    public static List<SkillData> getSkillDataList() {
        return list;
    }

    public static List<String> getComplete() {
        List<String> complete = new ArrayList<>();
        for (SkillData skillData : getSkillDataList()) {
            complete.add(skillData.getId());
        }
        return complete;
    }

    public static void load() {
        for (File file : DataBase.dumpFile(new File(DataBase.Path, "SkillData"))) {
            try {
                FileConfiguration data = YamlConfiguration.loadConfiguration(file);
                String id = DataBase.fileId(file);
                SkillData skillData = new SkillData();
                skillData.setId(id);
                skillData.setDisplay(data.getString("Display"));
                try {
                    skillData.setIcon(Material.valueOf(data.getString("Icon")));
                } catch (Exception e) {
                    skillData.setIcon(Material.BARRIER);
                    Log("§b" + id + "§aの§eIcon§aが間違っています §c" + data.getString("Icon"));
                }
                if (data.isSet("Color.r")) {
                    skillData.setColor(Color.fromRGB(data.getInt("Color.r"), data.getInt("Color.g"), data.getInt("Color.b")));
                }
                skillData.setLore(data.getStringList("Lore"));
                if (data.isSet("Addition")) skillData.setAddition(data.getStringList("Addition"));
                skillData.setPassive(data.getBoolean("Passive", false));
                skillData.setMaxLevel(data.getInt("MaxLevel"));
                skillData.setMaxStack(data.getInt("MaxStack"));
                for (String text : data.getStringList("ReqLevel")) {
                    String[] split = text.split(":");
                    skillData.setReqLevel(split[0], Integer.parseUnsignedInt(split[1]));
                }
                Parameter baseParam = Parameter.fromFile(data);
                skillData.setParam(1, baseParam);

                Parameter levelParam = baseParam.clone();
                for (int i = 1; i < skillData.getMaxLevel(); i++) {
                    String key = "Increase_Lv" + i;
                    Parameter increase = Parameter.fromFile(data, data.isSet(key) ? key + "." : "Increase.");
                    levelParam.increase(increase);
                    skillData.setParam(i + 1, levelParam);
                    levelParam = levelParam.clone();
                }

                skillDataList.put(id, skillData);
                list.add(skillData);
            } catch (Exception e) {
                DataBase.error(file, e);
            }
        }

        list.sort(Comparator.comparing(SkillData::getId));
        Log("§a[SkillDataLoader]§b" + skillDataList.size() + "個をロードしました");
    }
}
