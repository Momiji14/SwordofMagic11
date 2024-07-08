package SwordofMagic11.Pet;

import SwordofMagic11.Component.SomSQL;
import SwordofMagic11.DataBase.DataBase;
import SwordofMagic11.DataBase.MaterialDataLoader;
import SwordofMagic11.Player.PlayerData;
import com.github.jasync.sql.db.RowData;

import java.util.concurrent.ConcurrentHashMap;

public interface PetMenuCapsuleStorage {

    String[] Key = new String[]{"UUID", "Capsule"};

    PlayerData playerData();
    ConcurrentHashMap<String, Integer> capsuleStorage();

    private String[] value(String capsule) {
        return new String[]{playerData().getUUID(), capsule};
    }

    default void loadCapsule() {
        for (RowData objects : SomSQL.getSqlList(DataBase.Table.PetMenuCapsuleStorage, "UUID", playerData().getUUID(), "*")) {
            String capsule = objects.getString("Capsule");
            capsuleStorage().put(capsule, SomSQL.getInt(DataBase.Table.PetMenuCapsuleStorage, Key, value(capsule), "Amount"));
        }
    }

    default int getCapsule(String capsule) {
        return capsuleStorage().getOrDefault(capsule, 0);
    }

    default void setCapsule(String capsule, int amount) {
        if (amount > 0) {
            capsuleStorage().put(capsule, amount);
            SomSQL.setSql(DataBase.Table.PetMenuCapsuleStorage, Key, value(capsule), "Amount", amount);
        } else {
            deleteCapsule(capsule);
        }
    }

    default void addCapsule(String capsule, int amount) {
        setCapsule(capsule, getCapsule(capsule) + amount);
    }

    default void removeCapsule(String capsule, int amount) {
        setCapsule(capsule, getCapsule(capsule) - amount);
    }

    default void deleteCapsule(String capsule) {
        capsuleStorage().remove(capsule);
        SomSQL.delete(DataBase.Table.PetMenuCapsuleStorage, Key, value(capsule));
    }

    default void deleteCapsuleAll() {
        capsuleStorage().clear();
        SomSQL.delete(DataBase.Table.PetMenuCapsuleStorage, "UUID", playerData().getUUID());
    }

    default int sizeCapsule() {
        return capsuleStorage().size();
    }
}
