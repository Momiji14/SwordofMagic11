package SwordofMagic11.Player;

import SwordofMagic11.Component.SomSQL;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.DataBase.DataBase;
import SwordofMagic11.SomCore;

import java.time.LocalDateTime;

import static SwordofMagic11.DataBase.DataBase.DateFormat;

public class Donation {

    public static final double InstanceBoosterMultiply = 1.0;

    private final PlayerData playerData;
    private final String uuid;

    private int instanceBooster = 0;

    public Donation(PlayerData playerData) {
        this.playerData = playerData;
        uuid = playerData.uuid;
        if (SomSQL.exists(DataBase.Table.PlayerDonation, "UUID", uuid)) {
            SomSQL.setSql(DataBase.Table.PlayerDonation, "UUID", uuid, "Username", playerData.getName());
        }
        if (SomSQL.exists(DataBase.Table.PlayerDonation, "UUID", uuid, "InstanceBooster")) {
            instanceBooster = SomSQL.getInt(DataBase.Table.PlayerDonation, "UUID", uuid, "InstanceBooster");
        }
    }

    public int getEld() {
        if (SomSQL.exists(DataBase.Table.PlayerDonation, "UUID", uuid, "Eld")) {
            return SomSQL.getInt(DataBase.Table.PlayerDonation, "UUID", uuid, "Eld");
        } else return 0;
    }

    public void setEld(int eld) {
        SomSQL.setSql(DataBase.Table.PlayerDonation, "UUID", uuid, "Eld", eld);
    }

    public void addEld(int eld) {
        addTotalDonation(eld);
        setEld(getEld() + eld);
        playerData.sendMessage("§4[+]§d" + eld + "エルド", SomSound.Level);
    }

    public void removeEld(int eld) {
        setEld(getEld() - eld);
    }

    public boolean hasEld(int eld) {
        return getEld() >= eld;
    }

    public int getTotalDonation() {
        if (SomSQL.exists(DataBase.Table.PlayerDonation, "UUID", uuid, "TotalDonation")) {
            return SomSQL.getInt(DataBase.Table.PlayerDonation, "UUID", uuid, "TotalDonation");
        } else return 0;
    }

    public void setTotalDonation(int totalDonation) {
        SomSQL.setSql(DataBase.Table.PlayerDonation, "UUID", uuid, "TotalDonation", totalDonation);
    }

    public void addTotalDonation(int totalDonation) {
        setTotalDonation(getTotalDonation() + totalDonation);
    }

    public boolean hasPrivateInsRight() {
        if (SomSQL.exists(DataBase.Table.PlayerDonation, "UUID", uuid, "PrivateInstance")) {
            String date = SomSQL.getString(DataBase.Table.PlayerDonation, "UUID", uuid, "PrivateInstance");
            if (!date.isEmpty()) {
                return LocalDateTime.parse(date, DateFormat).isAfter(LocalDateTime.now());
            }
        }
        return false;
    }

    public String getPrivateInsText() {
        if (SomSQL.exists(DataBase.Table.PlayerDonation, "UUID", uuid, "PrivateInstance")) {
            String date = SomSQL.getString(DataBase.Table.PlayerDonation, "UUID", uuid, "PrivateInstance");
            if (!date.isEmpty()) {
                if (LocalDateTime.parse(date, DateFormat).isAfter(LocalDateTime.now())) {
                    return "§b" + date;
                } else {
                    return "§c期限切れ (" + date + ")";
                }
            }
        }
        return "§c未所持";
    }

    public void addPrivateIns(long hours) {
        LocalDateTime nextTime = LocalDateTime.now();
        if (SomSQL.exists(DataBase.Table.PlayerDonation, "UUID", uuid, "PrivateInstance")) {
            String date = SomSQL.getString(DataBase.Table.PlayerDonation, "UUID", uuid, "PrivateInstance");
            if (!date.isEmpty() && nextTime.isBefore(LocalDateTime.parse(date, DateFormat))) {
                nextTime = LocalDateTime.parse(date, DateFormat);
            }
        }
        SomSQL.setSql(DataBase.Table.PlayerDonation, "UUID", uuid, "PrivateInstance", nextTime.plusHours(hours).format(DateFormat));
    }

    public void addInsBoost(int second) {
        instanceBooster += second;
        SomSQL.setSql(DataBase.Table.PlayerDonation, "UUID", uuid, "InstanceBooster", instanceBooster);
    }

    public void removeInsBoost() {
        instanceBooster--;
        if (instanceBooster == 0) {
            playerData.sendMessage("§dインスタンスブースター§aの§e効果§aが切れました", SomSound.Tick);
        }
        SomSQL.setSql(DataBase.Table.PlayerDonation, "UUID", uuid, "InstanceBooster", instanceBooster);
    }

    public boolean hasInsBoost() {
        return instanceBooster > 0 && !playerData.isAFK();
    }

    public int getInsBoost() {
        return instanceBooster;
    }

    public String getInsBoostText() {
        if (hasInsBoost()) {
            return "§b" + instanceBooster + "秒";
        } else {
            return "§c無効";
        }
    }

    private Integer additionMaterialStorageSize;

    public int additionMaterialStorageSize() {
        if (additionMaterialStorageSize == null) {
            if (SomSQL.exists(DataBase.Table.PlayerDonation, "UUID", playerData.getUUID(), "MaterialStorageSize")) {
                additionMaterialStorageSize = SomSQL.getInt(DataBase.Table.PlayerDonation, "UUID", playerData.getUUID(), "MaterialStorageSize");
            } else additionMaterialStorageSize = 0;
        }
        return additionMaterialStorageSize;
    }

    public void addMaterialStorageSize(int addition) {
        additionMaterialStorageSize = additionMaterialStorageSize() + addition;
        SomSQL.setSql(DataBase.Table.PlayerDonation, "UUID", playerData.getUUID(), "MaterialStorageSize", additionMaterialStorageSize);
    }

    private Integer additionCapsuleStorageSize;

    public int additionCapsuleStorageSize() {
        if (additionCapsuleStorageSize == null) {
            if (SomSQL.exists(DataBase.Table.PlayerDonation, "UUID", playerData.getUUID(), "CapsuleStorageSize")) {
                additionCapsuleStorageSize = SomSQL.getInt(DataBase.Table.PlayerDonation, "UUID", playerData.getUUID(), "CapsuleStorageSize");
            } else additionCapsuleStorageSize = 0;
        }
        return additionCapsuleStorageSize;
    }

    public void addCapsuleStorageSize(int addition) {
        additionCapsuleStorageSize = additionCapsuleStorageSize() + addition;
        SomSQL.setSql(DataBase.Table.PlayerDonation, "UUID", playerData.getUUID(), "CapsuleStorageSize", additionCapsuleStorageSize);
    }

    private Integer additionPetCageSize;

    public int additionPetCageSize() {
        if (additionPetCageSize == null) {
            if (SomSQL.exists(DataBase.Table.PlayerDonation, "UUID", playerData.getUUID(), "PetCageSize")) {
                additionPetCageSize = SomSQL.getInt(DataBase.Table.PlayerDonation, "UUID", playerData.getUUID(), "PetCageSize");
            } else additionPetCageSize = 0;
        }
        return additionPetCageSize;
    }

    public void addPetCageSize(int addition) {
        additionPetCageSize = additionPetCageSize() + addition;
        SomSQL.setSql(DataBase.Table.PlayerDonation, "UUID", playerData.getUUID(), "PetCageSize", additionPetCageSize);
    }

    private Integer additionPalletStorageSize;

    public int additionPalletStorageSize() {
        if (additionPalletStorageSize == null) {
            if (SomSQL.exists(DataBase.Table.PlayerDonation, "UUID", playerData.getUUID(), "PalletStorageSize")) {
                additionPalletStorageSize = SomSQL.getInt(DataBase.Table.PlayerDonation, "UUID", playerData.getUUID(), "PalletStorageSize");
            } else additionPalletStorageSize = 0;
        }
        return additionPalletStorageSize;
    }

    public void addPalletStorageSize(int addition) {
        additionPalletStorageSize = additionPalletStorageSize() + addition;
        SomSQL.setSql(DataBase.Table.PlayerDonation, "UUID", playerData.getUUID(), "PalletStorageSize", additionPalletStorageSize);
    }
}
