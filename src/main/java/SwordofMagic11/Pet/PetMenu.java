package SwordofMagic11.Pet;

import SwordofMagic11.AttributeType;
import SwordofMagic11.Component.SomSQL;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Component.SomTask;
import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.DataBase.CapsuleDataLoader;
import SwordofMagic11.DataBase.DataBase;
import SwordofMagic11.DataBase.MaterialDataLoader;
import SwordofMagic11.Item.CapsuleData;
import SwordofMagic11.Item.Material.MaterialData;
import SwordofMagic11.Item.SomEquip;
import SwordofMagic11.Map.Gathering;
import SwordofMagic11.Map.GatheringData;
import SwordofMagic11.Player.Gathering.GatheringMenu;
import SwordofMagic11.Player.Menu.GUIManager;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Player.Setting.PlayerSetting;
import SwordofMagic11.StatusType;
import com.github.jasync.sql.db.RowData;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static SwordofMagic11.Component.Function.*;
import static SwordofMagic11.DataBase.DataBase.*;
import static SwordofMagic11.Map.Gathering.BaseMax;
import static SwordofMagic11.SomCore.Log;

public class PetMenu extends PetSelect implements PetMenuMaterialStorage, PetMenuCapsuleStorage {

    public static final int OfflineMaxTime = 8;

    public static final int TaskSlot = 3;
    private static final CustomItemStack nonTask = new CustomItemStack(Material.BARRIER).setNonDecoDisplay("§c未割当");

    public CustomItemStack icon() {
        CustomItemStack item = new CustomItemStack(Material.NOTE_BLOCK);
        item.setDisplay("ペットケージ");
        item.addLore("§aペットに関するとはこちらから行えます");
        item.addLore("§c※未完成");
        item.setCustomData("Menu", "PetMenu");
        return item;
    }

    private final PetEditor petEditor;
    private final ConcurrentHashMap<String, Integer> materialStorage = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Integer> capsuleStorage = new ConcurrentHashMap<>();

    private int scroll = 0;
    public PetMenu(PlayerData playerData) {
        super(playerData, "ペットメニュー", 3);
        petEditor = new PetEditor(playerData);

        loadCapsule();
        loadMaterial();
    }

    public void load() {
        Duration difference = Duration.between(playerData.lastOnlineTime(), LocalDateTime.now());

        for (RowData objects : SomSQL.getSqlList(DataBase.Table.PetCage, "Owner", playerData.getUUID(), "*")) {
            SyncPet.updateCache(objects.getString("UUID"));
        }

        for (SomPet somPet : getSummonList()) {
            somPet.setState(PetState.Cage);
            somPet.addStamina((int) (difference.toSeconds()/10));
        }

        if (!getTaskList().isEmpty()) {
            SomTask.asyncDelay(() -> {
                if (difference.toMinutes() > 0) {
                    long minute = Math.min(difference.toMinutes(), 60L * OfflineMaxTime);
                    playerData.sendMessage("§bオフライン時間分(" + minute + "分)§aの§eペットタスク§aを§e再生§aしています\n§c※完了前にログアウトすると失います", SomSound.Tick);
                    SomTask.async(() -> {
                        for (int i = 0; i < minute; i++) {
                            taskTick(false);
                        }
                        playerData.sendMessage("§eペットタスク再生§aが§b完了§aしました", SomSound.Tick);
                        taskTimer();
                    });
                } else {
                    taskTimer();
                }
            }, 50);
        } else {
            taskTimer();
        }
    }

    public CustomItemStack CageIcon() {
        CustomItemStack item = new CustomItemStack(Material.NOTE_BLOCK);
        item.setDisplay("ペットケージ");
        item.addLore(decoLore("ペット枠") + size() + "/" + playerData.petCageSize());
        return item;
    }

    public int size() {
        return getAllList().size();
    }

    public List<SomPet> getAllList() {
        return SyncPet.getList(playerData);
    }

    public List<SomPet> getCageList() {
        return SyncPet.getList(playerData, PetState.Cage);
    }

    public List<SomPet> getSummonList() {
        return SyncPet.getList(playerData, PetState.Summon);
    }

    public List<SomPet> getTaskList() {
        return SyncPet.getList(playerData, PetState.Task);
    }

    public List<SomPet> getViewList() {
        List<SomPet> list = getSummonList();
        list.addAll(getCageList());
        return list;
    }

    public List<PetEntity> entityInsList() {
        List<PetEntity> petEntities = new ArrayList<>();
        for (SomPet somPet : getSummonList()) {
            if (somPet.isSummon()) {
                petEntities.add(somPet.getEntityIns());
            }
        }
        return petEntities;
    }

    @Override
    public ConcurrentHashMap<String, Integer> materialStorage() {
        return materialStorage;
    }

    @Override
    public ConcurrentHashMap<String, Integer> capsuleStorage() {
        return capsuleStorage;
    }

    public CustomItemStack MaterialStorageIcon() {
        CustomItemStack icon = new CustomItemStack(Material.CHEST);
        icon.setDisplay("ペット素材倉庫");
        if (materialStorage.isEmpty()) {
            icon.addLore("§7・収集物なし");
        } else {
            boolean isShort = false;
            int viewSize = 0;
            for (Map.Entry<String, Integer> entry : materialStorage.entrySet()) {
                if (playerData.setting().is(PlayerSetting.BooleanEnum.StorageViewShort) && viewSize > ContainerViewSize) {
                    isShort = true;
                    break;
                }
                String materialId = entry.getKey();
                int amount = entry.getValue();
                MaterialData material = MaterialDataLoader.getMaterialData(materialId);
                icon.addLore("§7・§f" + material.getDisplay() + "§ax" + amount);
                viewSize++;
            }
            if (isShort) {
                icon.addLore("§7その他..." + (materialStorage.size() - ContainerViewSize) + "種");
            }
        }
        icon.setCustomData("PetMenu", "MaterialStorage");
        return icon;
    }

    public CustomItemStack CapsuleStorageIcon() {
        CustomItemStack icon = new CustomItemStack(Material.ENDER_CHEST);
        icon.setDisplay("ペットカプセル倉庫");
        if (capsuleStorage.isEmpty()) {
            icon.addLore("§7・収集物なし");
        } else {
            boolean isShort = false;
            int viewSize = 0;
            for (Map.Entry<String, Integer> entry : capsuleStorage.entrySet()) {
                if (playerData.setting().is(PlayerSetting.BooleanEnum.StorageViewShort) && viewSize > ContainerViewSize) {
                    isShort = true;
                    break;
                }
                String capsuleId = entry.getKey();
                int amount = entry.getValue();
                CapsuleData capsule = CapsuleDataLoader.getCapsuleData(capsuleId);
                icon.addLore("§7・§f" + capsule.getDisplay() + "§ax" + amount);
                viewSize++;
            }
            if (isShort) {
                icon.addLore("§7その他..." + (capsuleStorage.size() - ContainerViewSize) + "種");
            }
        }
        icon.setCustomData("PetMenu", "CapsuleStorage");
        return icon;
    }

    @Override
    public void updateContainer() {
        updateContainer(playerData.petMenu().getViewList());
    }

    @Override
    public void updateUpperContainer() {
        setItem(0, CageIcon());
        setItem(1, MaterialStorageIcon());
        setItem(2, CapsuleStorageIcon());

        for (int i = 18; i < 27; i++) {
            setItem(i, ItemFlame);
        }
        setItem(5, ItemFlame);
        setItem(14, ItemFlame);

        int slot = 15;
        for (SomPet somPet : getTaskList()) {
            PetTaskType type = somPet.getTask();
            setItem(slot-9, type.viewItem(somPet));
            setItem(slot, somPet.viewItem());
            slot++;
        }

        for (int i = 0; i < TaskSlot-getTaskList().size(); i++) {
            setItem(slot-9, nonTask);
            slot++;
        }
    }

    @Override
    public void topClick(int slot, ItemStack clickedItem, ClickType clickType) {
        if (CustomItemStack.hasCustomData(clickedItem, "UpScroll")) {
            scroll = scrollUp(scroll);
            update();
        } else if (CustomItemStack.hasCustomData(clickedItem, "DownScroll")) {
            scroll = scrollDown(getCageList().size(), 3, scroll);
            update();
        } else if (CustomItemStack.hasCustomData(clickedItem, "Pet")) {
            SomPet pet = SyncPet.getSomPet(CustomItemStack.getCustomData(clickedItem, "Pet"));
            switch (pet.getState()) {
                case Cage -> petEditor.open(pet);
                case Task -> {
                    pet.updateState(PetState.Cage);
                    pet.updateTask(null);
                    playerData.sendMessage(pet.colorName() + "§aが§eケージ§aに戻りました", SomSound.Tick);
                    update();
                }
            }
        } else if (CustomItemStack.hasCustomData(clickedItem, "TaskOwner")) {
            SomPet pet = SyncPet.getSomPet(CustomItemStack.getCustomData(clickedItem, "TaskOwner"));
            pet.updateTask(pet.getTask().next());
            playerData.sendMessage(pet.colorName() + "§aの§e作業内容§aを§e" + pet.getTask().getDisplay() + "§aに変更しました", SomSound.Tick);
            update();
        } else if (CustomItemStack.hasCustomData(clickedItem, "PetMenu")) {
            switch (CustomItemStack.getCustomData(clickedItem, "PetMenu")) {
                case "MaterialStorage" -> {
                    if (!materialStorage.isEmpty()) {
                        materialStorage.forEach((id, amount) -> {
                            playerData.addMaterial(id, amount);
                            MaterialData material = MaterialDataLoader.getMaterialData(id);
                            String color = material.isRare() ? "§f" : "§e";
                            playerData.sendMessage("§b[+]" + color + material.getDisplay() + "§ax" + amount);
                        });
                        deleteMaterialAll();
                        playerData.sendMessage("§eペット素材倉庫§aの§e収集物§aを§b回収§aしました", SomSound.Tick);
                        update();
                    } else {
                        playerData.sendMessage("§eペット素材倉庫§aに§e収集物§aがありません", SomSound.Nope);
                    }
                }
                case "CapsuleStorage" -> {
                    if (!capsuleStorage.isEmpty()) {
                        capsuleStorage.forEach((id, amount) -> {
                            playerData.addCapsule(id, amount);
                            CapsuleData capsule = CapsuleDataLoader.getCapsuleData(id);
                            playerData.sendMessage("§b[+]" + capsule.getDisplay() + "§ax" + amount);
                        });
                        deleteCapsuleAll();
                        playerData.sendMessage("§eペットカプセル倉庫§aの§e収集物§aを§b回収§aしました", SomSound.Tick);
                        update();
                    } else {
                        playerData.sendMessage("§eペットカプセル倉庫§aに§e収集物§aがありません", SomSound.Nope);
                    }
                }
            }
        }
    }

    @Override
    public void bottomClick(int slot, ItemStack clickedItem, ClickType clickType) {

    }

    @Override
    public void close() {

    }

    public void taskTimer() {
        int timer = 20*60;
        SomTask.asyncTimer(() -> taskTick(true), timer, timer, playerData);
    }

    public void taskTick(boolean active) {
        for (SomPet somPet : getTaskList()) {
            GatheringData gatheringData;
            switch (somPet.getTask()) {
                case Mining -> gatheringData = playerData.getMap().getMiningData();
                case Collect -> gatheringData = playerData.getMap().getCollectData();
                case Fishing -> gatheringData = playerData.getMap().getFishingData();
                default -> gatheringData = null;
            }
            if (gatheringData != null) gatheringTaskTick(somPet, PetLevelType.valueOf(somPet.getTask().toString()), gatheringData);
        }
    }

    public void gatheringTaskTick(SomPet somPet, PetLevelType levelType, GatheringData gatheringData) {
        double reqPower = gatheringData.getReqPower();
        double toolPower = somPet.hasEquip(SomEquip.Slot.MainHand) ? somPet.getEquip(SomEquip.Slot.MainHand).getStatus(StatusType.GatheringPower) * 0.15 : 0;
        double power = (somPet.getAttributeMultiply(AttributeType.Dexterity) * somPet.getLevel(levelType) * (1 + somPet.getPlus() * 0.01)) + toolPower;
        double multiply = power / reqPower;
        if (reqPower <= 0 || multiply <= 0) return;
        somPet.addExp(levelType, reqPower);
        for (Map.Entry<MaterialData, Double> entry : gatheringData.getDrop().entrySet()) {
            double percent = entry.getValue() * multiply;
            MaterialData material = entry.getKey();
            int amount = 0;
            while (percent > 1) {
                amount++;
                percent--;
            }
            if (randomDouble() < percent) {
                amount++;
            }
            if (amount > 0) {
                amount = Math.min(amount, BaseMax);
                addMaterial(material.getId(), amount);
            }
        }

        double grinderPercent = Math.min(0.025, 0.001 * multiply);
        if (randomDouble() < grinderPercent) {
            int max = ceil(reqPower * 0.01);
            int amount = max > 1 ? randomInt(1, max) : 1;
            addMaterial("グラインダー", amount);
        }

        for (Map.Entry<CapsuleData, Double> entry : gatheringData.getCapsuleDrop().entrySet()) {
            CapsuleData capsuleData = entry.getKey();
            double percent = Math.min(0.1, entry.getValue() * multiply);
            if (randomDouble() < percent) addCapsule(capsuleData.getId(), 1);
        }
    }
}
