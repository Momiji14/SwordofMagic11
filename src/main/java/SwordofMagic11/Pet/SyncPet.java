package SwordofMagic11.Pet;

import SwordofMagic11.AttributeType;
import SwordofMagic11.Component.SomSQL;
import SwordofMagic11.DataBase.DataBase;
import SwordofMagic11.DataBase.MobDataLoader;
import SwordofMagic11.Enemy.MobData;
import SwordofMagic11.Item.SomEquip;
import SwordofMagic11.Item.SyncItem;
import SwordofMagic11.Player.PlayerData;
import com.github.jasync.sql.db.RowData;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static SwordofMagic11.SomCore.Log;

public class SyncPet {

    private static final ConcurrentHashMap<String, SomPet> petDataBase = new ConcurrentHashMap<>();

    public static List<SomPet> getList(PlayerData playerData) {
        List<SomPet> list = new ArrayList<>();
        for (SomPet pet : petDataBase.values()) {
            if (pet.getOwner() == playerData) {
                list.add(pet);
            }
        }
        return list;
    }

    public static List<SomPet> getList(PlayerData playerData, PetState state) {
        List<SomPet> list = getList(playerData);
        list.removeIf(pet -> pet.getState() != state);
        return list;
    }

    public static boolean hasSomPet(String uuid) {
        return petDataBase.containsKey(uuid) || SomSQL.exists(DataBase.Table.PetCage, "UUID", uuid);
    }

    public static SomPet register(MobData mobData, PlayerData playerData) {
        if (mobData == null) {
            Log("MobData is null", true);
            return null;
        }
        SomPet pet = new SomPet(UUID.randomUUID().toString(), mobData);
        pet.updateOwner(playerData);
        pet.updateName(mobData.getDisplay());
        pet.updateState(PetState.Cage);
        SomSQL.setSql(DataBase.Table.PetCage, "UUID", pet.getUUID(), "MobData", mobData.getId());
        pet.randomInit();
        petDataBase.put(pet.getUUID(), pet);
        return pet;
    }


    public static void updateCache(String uuid) {
        if (SomSQL.exists(DataBase.Table.PetCage, "UUID", uuid)) {
            RowData objects = SomSQL.getSql(DataBase.Table.PetCage, "UUID", uuid, "*");
            String mobDataId = objects.getString("MobData");
            if (MobDataLoader.getComplete().contains(mobDataId)) {
                SomPet pet = new SomPet(uuid, MobDataLoader.getMobData(mobDataId));
                pet.setName(objects.getString("Name"));
                pet.setState(PetState.valueOf(objects.getString("State")));
                String task = objects.getString("Task");
                if (task != null) pet.setTask(PetTaskType.valueOf(task));
                pet.setOwner(PlayerData.get(Bukkit.getPlayer(UUID.fromString(objects.getString("Owner")))));
                if (SomSQL.exists(DataBase.Table.PetAttributeMultiply, "UUID", uuid)) {
                    for (AttributeType attr : AttributeType.values()) {
                        pet.setRawAttributeMultiply(attr, SomSQL.getDouble(DataBase.Table.PetAttributeMultiply, "UUID", uuid, attr.toString()));
                    }
                } else {
                    pet.randomAttributeMultiply();
                }
                if (SomSQL.exists(DataBase.Table.PetAttribute, "UUID", uuid)) {
                    for (AttributeType attr : AttributeType.values()) {
                        pet.setRawAttribute(attr, SomSQL.getInt(DataBase.Table.PetAttribute, "UUID", uuid, attr.toString()));
                    }
                } else {
                    pet.randomAttribute();
                }
                if (SomSQL.exists(DataBase.Table.PetEquipment, "UUID", uuid)) {
                    for (RowData rowData : SomSQL.getSqlList(DataBase.Table.PetEquipment, "UUID", uuid, "EquipUUID")) {
                        SomEquip equip = (SomEquip) SyncItem.getSomItem(rowData.getString("EquipUUID"));
                        pet.setRawEquipment(equip);
                    }
                }
                petDataBase.put(uuid, pet);
            } else {
                delete(objects.getString("UUID"));
                Log("存在しないPetIDが参照されました " + uuid + ":" + mobDataId, true);
            }
        } else {
            Log("DataBaseに存在しないペットが参照されました " + uuid, true);
        }
    }

    public static SomPet getSomPet(String uuid) {
        if (!petDataBase.containsKey(uuid)) {
            updateCache(uuid);
        }
        return petDataBase.get(uuid);
    }

    public static void delete(String uuid) {
        petDataBase.remove(uuid);
        SomSQL.delete(DataBase.Table.PetCage, "UUID", uuid);
    }

}
