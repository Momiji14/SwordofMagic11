package SwordofMagic11.Player.Storage;

import SwordofMagic11.Item.Material.MaterialData;
import SwordofMagic11.Player.PlayerData;

public interface MaterialStorage {

    PlayerData playerData();

    String getUUID();

    default int getMaterial(MaterialData material) {
        return getMaterial(material.getId());
    }

    default int getMaterial(String material) {
        return playerData().materialMenu().get(material);
    }

    default void addMaterial(MaterialData material, int amount) {
        addMaterial(material.getId(), amount);
    }

    default void addMaterial(String material, int amount) {
        playerData().materialMenu().add(material, amount);
    }

    default void removeMaterial(MaterialData material, int amount) {
        removeMaterial(material.getId(), amount);
    }

    default void removeMaterial(String material, int amount) {
        playerData().materialMenu().remove(material, amount);
    }

    default boolean hasMaterial(MaterialData material, int amount) {
        return hasMaterial(material.getId(), amount);
    }

    default boolean hasMaterial(String material, int amount) {
        if (amount < 0) return false;
        return getMaterial(material) >= amount;
    }
}
