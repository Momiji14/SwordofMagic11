package SwordofMagic11.Player;

public interface SizeManager {
    PlayerData playerData();

    default int materialStorageSize() {
        return 100 + playerData().donation().additionMaterialStorageSize();
    }

    default int capsuleStorageSize() {
        return 100 + playerData().donation().additionCapsuleStorageSize();
    }

    default int petCageSize() {
        return 25 + playerData().donation().additionPetCageSize();
    }

    default int palletStorageSize() {
        return playerData().donation().additionPalletStorageSize();
    }
}
