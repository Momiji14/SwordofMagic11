package SwordofMagic11.Player.Storage;

import SwordofMagic11.Item.CapsuleData;
import SwordofMagic11.Player.PlayerData;

public interface CapsuleStorage {

    PlayerData playerData();

    String getUUID();

    default int getCapsule(CapsuleData capsule) {
        return getCapsule(capsule.getId());
    }

    default int getCapsule(String capsule) {
        return playerData().capsuleMenu().get(capsule);
    }

    default void addCapsule(CapsuleData capsule, int amount) {
        addCapsule(capsule.getId(), amount);
    }

    default void addCapsule(String capsule, int amount) {
        playerData().capsuleMenu().add(capsule, amount);
    }

    default void removeCapsule(CapsuleData capsule, int amount) {
        removeCapsule(capsule.getId(), amount);
    }

    default void removeCapsule(String capsule, int amount) {
        playerData().capsuleMenu().remove(capsule, amount);
    }

    default boolean hasCapsule(CapsuleData capsule, int amount) {
        return hasCapsule(capsule.getId(), amount);
    }

    default boolean hasCapsule(String capsule, int amount) {
        if (amount < 0) return false;
        return getCapsule(capsule) >= amount;
    }
}
