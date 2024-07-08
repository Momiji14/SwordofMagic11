package SwordofMagic11.Pet;

import SwordofMagic11.Component.SomSQL;
import SwordofMagic11.DataBase.DataBase;
import SwordofMagic11.DataBase.MaterialDataLoader;
import SwordofMagic11.Player.PlayerData;
import com.github.jasync.sql.db.RowData;

import java.util.concurrent.ConcurrentHashMap;

public interface PetMenuMaterialStorage {

    String[] Key = new String[]{"UUID", "MaterialID"};

    PlayerData playerData();
    ConcurrentHashMap<String, Integer> materialStorage();

    private String[] value(String materialID) {
        return new String[]{playerData().getUUID(), materialID};
    }

    default void loadMaterial() {
        for (RowData objects : SomSQL.getSqlList(DataBase.Table.PetMenuMaterialStorage, "UUID", playerData().getUUID(), "*")) {
            String material = objects.getString("MaterialID");
            materialStorage().put(material, SomSQL.getInt(DataBase.Table.PetMenuMaterialStorage, Key, value(material), "Amount"));
        }
    }

    default int getMaterial(String material) {
        return materialStorage().getOrDefault(material, 0);
    }

    default void setMaterial(String material, int amount) {
        if (amount > 0) {
            materialStorage().put(material, amount);
            SomSQL.setSql(DataBase.Table.PetMenuMaterialStorage, Key, value(material), "Amount", amount);
        } else {
            deleteMaterial(material);
        }
    }

    default void addMaterial(String material, int amount) {
        setMaterial(material, getMaterial(material) + amount);
    }

    default void removeMaterial(String material, int amount) {
        setMaterial(material, getMaterial(material) - amount);
    }

    default void deleteMaterial(String material) {
        materialStorage().remove(material);
        SomSQL.delete(DataBase.Table.PetMenuMaterialStorage, Key, value(material));
    }

    default void deleteMaterialAll() {
        materialStorage().clear();
        SomSQL.delete(DataBase.Table.PetMenuMaterialStorage, "UUID", playerData().getUUID());
    }

    default int sizeMaterial() {
        int size = materialStorage().size();
        for (String id : MaterialDataLoader.getNonUseSlotList()) {
            if (materialStorage().containsKey(id)) {
                size--;
            }
        }
        return size;
    }
}
