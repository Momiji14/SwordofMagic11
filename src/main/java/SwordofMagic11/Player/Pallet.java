package SwordofMagic11.Player;

import SwordofMagic11.Component.SomJson;
import SwordofMagic11.Component.SomSQL;
import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.DataBase.DataBase;
import SwordofMagic11.DataBase.MaterialDataLoader;
import SwordofMagic11.DataBase.SkillDataLoader;
import SwordofMagic11.Item.Material.MaterialData;
import SwordofMagic11.Item.Material.UseAbleMaterial;
import SwordofMagic11.Skill.SomSkill;
import org.bukkit.Material;

public interface Pallet {

    static CustomItemStack nonPallet(int i) {
        return new CustomItemStack(Material.IRON_BARS).setNonDecoDisplay("§f[スロット" + (i + 1) + "]").setCustomData("NonPallet", i);
    }

    static void load(PlayerData playerData) {
        String[] key = new String[]{"UUID", "ClassType"};
        for (ClassType classType : ClassType.values()) {
            String[] value = new String[]{playerData.getUUID(), classType.toString()};
            Pallet[] pallet = playerData.classes().getPallet(classType);
            if (SomSQL.exists(DataBase.Table.Pallet, key, value, "Pallet")) {
                SomJson json = new SomJson(SomSQL.getString(DataBase.Table.Pallet, key, value, "Pallet"));
                for (int i = 0; i < pallet.length; i++) {
                    pallet[i] = fromJson(json.getSomJson("Pallet-" + i), playerData);
                }
            }
        }
    }

    static void save(PlayerData playerData) {
        String[] key = new String[]{"UUID", "ClassType"};
        for (ClassType classType : ClassType.values()) {
            String[] value = new String[]{playerData.getUUID(), classType.toString()};
            Pallet[] pallet = playerData.classes().getPallet(classType);
            SomJson json = new SomJson();
            for (int i = 0; i < pallet.length; i++) {
                if (pallet[i] != null) {
                    json.set("Pallet-" + i, pallet[i].toJson());
                }
            }
            if (!json.isEmpty()) {
                SomSQL.setSql(DataBase.Table.Pallet, key, value, "Pallet", json.toString());
            }
        }
    }

    CustomItemStack viewItem();

    String getId();

    void use(PlayerData playerData);

    default SomJson toJson() {
        SomJson json = new SomJson();
        String type;
        if (this instanceof SomSkill) {
            type = "SomSkill";
        } else if (this instanceof UseAbleMaterial) {
            type = "UseAbleMaterial";
        } else {
            type = "";
        }
        json.set("Type", type);
        json.set("Id", getId());
        return json;
    }

    static Pallet fromJson(SomJson json, PlayerData playerData) {
        String id = json.getString("Id");
        String type = json.getString("Type");
        switch (type) {
            case "SomSkill" -> {
                if (!SkillDataLoader.getSkillData(id).isPassive()) {
                    return playerData.skillManager().instance(id);
                }
            }
            case "UseAbleMaterial" -> {
                if (MaterialDataLoader.getComplete().contains(id)) {
                    return (Pallet) MaterialDataLoader.getMaterialData(id);
                }
            }
        }
        return null;
    }
}
