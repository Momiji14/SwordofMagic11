package SwordofMagic11.Pet;

import SwordofMagic11.AttributeType;
import SwordofMagic11.Component.SomSQL;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Component.SomTask;
import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.DataBase.DataBase;
import SwordofMagic11.Enemy.MobData;
import SwordofMagic11.Item.SomEquip;
import SwordofMagic11.Item.SyncItem;
import SwordofMagic11.Player.Classes;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Player.Setting.PlayerSetting;
import SwordofMagic11.StatusType;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;

import static SwordofMagic11.Component.Function.*;
import static SwordofMagic11.Player.Classes.MaxLevel;

public class SomPet {
    public static final int Sell = 1000;

    private final String[] keyLevel = new String[]{"UUID", "Type"};
    private String[] valueLevel(PetLevelType type) {
        return new String[]{uuid, type.toString()};
    }

    public static double ReqExp(int level) {
        int tier;
        if (level < 50) {
            tier = 0;
        } else if (level < 100) {
            tier = 1;
        } else if (level < 200) {
            tier = 2;
        } else if (level < MaxLevel) {
            tier = 3;
        } else {
            tier = Integer.MAX_VALUE;
        }
        return Classes.ReqExp(level, tier);
    }

    private final String uuid;
    private PlayerData owner;
    private final MobData mobData;
    private String name;
    private PetState state;
    private PetTaskType task;
    private final HashMap<PetLevelType, Integer> level = new HashMap<>();
    private final HashMap<PetLevelType, Double> exp = new HashMap<>();
    private final HashMap<AttributeType, Integer> attribute = new HashMap<>();
    private final HashMap<AttributeType, Double> attributeMultiply = new HashMap<>();
    private int stamina;
    protected final HashMap<SomEquip.Slot, SomEquip> equipment = new HashMap<>();
    private Integer plus;
    private PetEntity entity;

    public SomPet(String uuid, MobData mobData) {
        this.uuid = uuid;
        this.mobData = mobData;
    }

    public void randomInit() {
        randomAttributeMultiply();
        randomAttribute();
    }

    public void randomAttributeMultiply() {
        double min = 0;
        double max = 0;
        switch (mobData.getRank()) {
            case Normal -> {
                min = 0.75;
                max = 1.25;
            }
            case Middle -> {
                min = 1.5;
                max = 2.5;
            }
            case Boss -> {
                min = 4.5;
                max = 7.5;
            }
        }
        for (AttributeType attr : AttributeType.values()) {
            setAttributeMultiply(attr, randomDouble(min, max));
        }
    }

    public void randomAttribute() {
        int min = 0;
        int max = 0;
        switch (mobData.getRank()) {
            case Normal -> {
                min = 5;
                max = 25;
            }
            case Middle -> {
                min = 15;
                max = 35;
            }
            case Boss -> {
                min = 50;
                max = 100;
            }
        }
        for (AttributeType attr : AttributeType.values()) {
            setAttribute(attr, randomInt(min, max));
        }
    }

    public void summon() {
        if (entity != null) entity.death(entity);
        SomTask.sync(() -> entity = new PetEntity(this));
    }

    public boolean isSummon() {
        return entity != null;
    }

    public PetEntity getEntityIns() {
        return entity;
    }

    public String getUUID() {
        return uuid;
    }

    public MobData getMobData() {
        return mobData;
    }

    public String colorName() {
        return "§f" + name;
    }

    public PlayerData getOwner() {
        return owner;
    }

    public void setOwner(PlayerData owner) {
        this.owner = owner;
    }

    public void updateOwner(PlayerData playerData) {
        setOwner(playerData);
        SomSQL.setSql(DataBase.Table.PetCage, "UUID", uuid, "Owner", playerData.getUUID());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void updateName(String name) {
        setName(name);
        SomSQL.setSql(DataBase.Table.PetCage, "UUID", uuid, "Name", name);
    }

    public PetState getState() {
        return state;
    }

    public void setState(PetState state) {
        this.state = state;
    }

    public void updateState(PetState state) {
        setState(state);
        SomSQL.setSql(DataBase.Table.PetCage, "UUID", uuid, "State", state.toString());
    }

    public PetTaskType getTask() {
        return task;
    }

    public void setTask(PetTaskType task) {
        this.task = task;
    }

    public void updateTask(PetTaskType task) {
        setTask(task);
        SomSQL.setSql(DataBase.Table.PetCage, "UUID", uuid, "Task", task != null ? task.toString() : null);
    }

    public HashMap<SomEquip.Slot, SomEquip> getEquipment() {
        return equipment;
    }

    public int getAttribute(AttributeType attr) {
        if (!attribute.containsKey(attr)) {
            if (SomSQL.exists(DataBase.Table.PetAttribute, "UUID", uuid, attr.toString())) {
                setRawAttribute(attr, SomSQL.getInt(DataBase.Table.PetAttribute, "UUID", uuid, attr.toString()));
            } else {
                setRawAttribute(attr, 0);
            }
        }
        return attribute.get(attr);
    }

    public void setAttribute(AttributeType attr, int value) {
        setRawAttribute(attr, value);
        SomSQL.setSql(DataBase.Table.PetAttribute, "UUID", uuid, attr.toString(), value);
    }

    public void setRawAttribute(AttributeType attr, int value) {
        attribute.put(attr, value);
    }

    public double getAttributeMultiply(AttributeType attr) {
        if (!attributeMultiply.containsKey(attr)) {
            if (SomSQL.exists(DataBase.Table.PetAttributeMultiply, "UUID", uuid, attr.toString())) {
                setRawAttributeMultiply(attr, SomSQL.getDouble(DataBase.Table.PetAttributeMultiply, "UUID", uuid, attr.toString()));
            } else {
                setRawAttributeMultiply(attr, 0.0);
            }
        }
        return attributeMultiply.get(attr);
    }

    public void setAttributeMultiply(AttributeType attr, double value) {
        setRawAttributeMultiply(attr, value);
        SomSQL.setSql(DataBase.Table.PetAttributeMultiply, "UUID", uuid, attr.toString(), (float) value);
    }

    public void setRawAttributeMultiply(AttributeType attr, double value) {
        attributeMultiply.put(attr, value);
    }

    public int getStamina() {
        return stamina;
    }

    public void setStamina(int stamina) {
        setRawStamina(Math.min(stamina, getMaxStamina()));
        SomSQL.setSql(DataBase.Table.PetCage, "UUID", uuid, "Stamina", stamina);
    }

    public void setRawStamina(int stamina) {
        this.stamina = stamina;
    }

    public void addStamina(int stamina) {
        setStamina(getStamina() + stamina);
    }

    public int getMaxStamina() {
        return (int) (getAttributeMultiply(AttributeType.Vitality) * 100);
    }

    public int getLevel(PetLevelType type) {
        if (!level.containsKey(type)) {
            if (SomSQL.exists(DataBase.Table.PetLevel, keyLevel, valueLevel(type), "Level")) {
                level.put(type, SomSQL.getInt(DataBase.Table.PetLevel, keyLevel, valueLevel(type), "Level"));
            } else {
                level.put(type, 1);
            }
        }
        return level.get(type);
    }

    public void setLevel(PetLevelType type, int level) {
        this.level.put(type, level);
        SomSQL.setSql(DataBase.Table.PetLevel, keyLevel, valueLevel(type), "Level", level);
    }

    public void addLevel(PetLevelType type, int level) {
        setLevel(type, getLevel(type) + level);
        String text = colorName() + "§aの§e" + type.getDisplay() + "レベル§aが§eLv" + getLevel(type) + "§aになりました";
        owner.sendMessage(text, SomSound.Level);
        if (MaxLevel <= getLevel(type)) setExp(type, 0);

        if (isSummon() && type == PetLevelType.Combat) {
            getEntityIns().updateAttribute();
        }
    }

    public double getExp(PetLevelType type) {
        if (!exp.containsKey(type)) {
            if (SomSQL.exists(DataBase.Table.PetLevel, keyLevel, valueLevel(type), "Exp")) {
                exp.put(type, SomSQL.getDouble(DataBase.Table.PetLevel, keyLevel, valueLevel(type), "Exp"));
            } else {
                exp.put(type, 0d);
            }
        }
        return exp.get(type);
    }

    public double getExpPercent(PetLevelType type) {
        return getExp(type) / ReqExp(getLevel(type));
    }

    public void setExp(PetLevelType type, double exp) {
        this.exp.put(type, exp);
        SomSQL.setSql(DataBase.Table.PetLevel, keyLevel, valueLevel(type), "Exp", exp);
    }

    private double expBuffer = 0;
    private BukkitTask bufferTask;
    public synchronized void addExp(PetLevelType type, double exp) {
        setExp(type, getExp(type) + exp);
        expBuffer += exp;
        if (bufferTask == null) {
            bufferTask = SomTask.asyncDelay(() -> {
                double currentExp = getExp(type) + expBuffer;
                if (getLevel(type) >= MaxLevel) {
                    double reqExp = ReqExp(getLevel(type));
                    if (currentExp >= reqExp) {
                        currentExp = reqExp-1;
                    }
                } else {
                    int addLevel = 0;
                    while (currentExp >= ReqExp(getLevel(type) + addLevel)) {
                        currentExp -= ReqExp(getLevel(type) + addLevel);
                        addLevel++;
                    }
                    if (addLevel >= 1) {
                        addLevel(type, addLevel);
                    }
                }
                setExp(type, currentExp);
                bufferTask = null;
                expBuffer = 0;
            }, 5);
        }
        if (owner.setting().is(PlayerSetting.BooleanEnum.ExpLog)) {
            owner.sendMessage("§a[+]" + colorName() + " §a+" + scale(exp, 2));
        }
    }

    public SomEquip getEquip(SomEquip.Slot slot) {
        return equipment.get(slot);
    }

    public boolean hasEquip(SomEquip.Slot slot) {
        return equipment.containsKey(slot);
    }

    public void setEquip(SomEquip.Slot slot, SomEquip equip) {
        if (equip != null && equip.getReqLevel() > getLevel(PetLevelType.Combat)) {
            owner.sendMessage("§eペット§aが§c装備条件§aを満たしていません", SomSound.Nope);
            return;
        }
        if (equipment.containsKey(slot)) {
            equipment.get(slot).setState(SyncItem.State.ItemInventory);
        }
        String[] key = new String[]{"UUID", "EquipSlot"};
        String[] value = new String[]{uuid, slot.toString()};
        if (equip != null) {
            equipment.put(slot, equip);
            equip.setState(SyncItem.State.Equipped);
            SomSQL.setSql(DataBase.Table.PetEquipment, key, value, "EquipUUID", equip.getUUID());
        } else {
            equipment.remove(slot);
            SomSQL.delete(DataBase.Table.PetEquipment, key, value);
        }
        owner.updateInventory();
        if (isSummon()) getEntityIns().statusUpdate();
    }

    public void setRawEquipment(SomEquip equip) {
        equipment.put(equip.getSlot(), equip);
    }

    public int getPlus() {
        if (plus == null) {
            if (SomSQL.exists(DataBase.Table.PetCage, "UUID", uuid, "Plus")) {
                plus = SomSQL.getInt(DataBase.Table.PetCage, "UUID", uuid, "Plus");
            } else {
                plus = 0;
            }
        }
        return plus;
    }

    public void setPlus(int plus) {
        this.plus = plus;
        SomSQL.setSql(DataBase.Table.PetCage, "UUID", uuid, "Plus", plus);
    }

    public void addPlus(int plus) {
        setPlus(getPlus() + plus);
    }

    public int getKillCount() {
        return SomSQL.getInt(DataBase.Table.PetCage, "UUID", uuid, "KillCount");
    }

    public void setKillCount(int killCount) {
        SomSQL.setSql(DataBase.Table.PetCage, "UUID", uuid, "KillCount", killCount);
    }

    public void addKillCount(int killCount) {
        setKillCount(getKillCount() + killCount);
    }

    public AttributeType additionAttributeType() {
        int ordinal = mobData.getId().hashCode() % AttributeType.values().length;
        return AttributeType.values()[ordinal];
    }

    public CustomItemStack viewItem() {
        CustomItemStack item = new CustomItemStack(mobData.getMemorial().getIcon());
        String suffix = getPlus() > 0 ? " §e[+" + getPlus() + "]" : "";
        item.setDisplay(colorName() + suffix);
        for (PetLevelType type : PetLevelType.values()) {
            item.addLore("§7・§e" + type.getDisplay() + " §eLv" + getLevel(type) + " §a" + scale(getExpPercent(type)*100, 2) + "%");
        }

        if (isSummon()) {
            PetEntity petEntity = getEntityIns();
            item.addSeparator("ステータス");
            for (StatusType statusType : StatusType.values()) {
                item.addLore(decoLore(statusType.getDisplay()) + scale(petEntity.getStatus(statusType), 1));
            }
            item.addSeparator("アトリビュート");
            for (AttributeType attr : AttributeType.values()) {
                item.addLore(decoLore(attr.getDisplay()) + petEntity.getBaseAttribute().getOrDefault(attr, 0) + " (" + scale(getAttributeMultiply(attr)*100, 3) + "%)");
            }
        } else {
            item.addSeparator("アトリビュート倍率");
            for (AttributeType attr : AttributeType.values()) {
                item.addLore(decoLore(attr.getDisplay()) + scale(getAttributeMultiply(attr)*100, 3) + "%");
            }
        }
        item.addSeparator("装備状況");
        for (SomEquip.Slot slot : SomEquip.Slot.values()) {
            if (hasEquip(slot)) {
                SomEquip equip = getEquip(slot);
                item.addLore(decoLore(slot.getDisplay()) + equip.viewText().getFirst());
            } else {
                item.addLore(decoLore(slot.getDisplay()) + "§7未装備");
            }
        }

        item.setCustomData("Pet", uuid);
        return item;
    }
}
