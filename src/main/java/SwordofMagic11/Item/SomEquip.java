package SwordofMagic11.Item;

import SwordofMagic11.DataBase.CapsuleDataLoader;
import SwordofMagic11.DataBase.DataBase;
import SwordofMagic11.StatusType;
import org.bukkit.Color;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static SwordofMagic11.Component.Function.numberColor;
import static SwordofMagic11.Component.Function.randomDouble;

public class SomEquip extends SomItem implements Cloneable {
    private double power = 1;
    private Double exp = null;
    private Integer trance = null;
    private Integer plus = null;
    private Integer levelDown = null;
    private int capsuleSlot = 4;
    private List<CapsuleData> capsules = null;
    private int reqLevel;
    private Category category;
    private String series;

    public String getColorDisplay() {
        return numberColor(trance) + getDisplay();
    }

    public double getPower() {
        return power;
    }

    public void setPower(double power) {
        this.power = power;
    }

    public double getExp() {
        if (exp == null) exp = getMetaDataDouble(DataBase.Table.EquipmentExp, 0.0);
        return exp;
    }

    public void setExp(double exp) {
        this.exp = exp;
        setMetaData("Exp", exp);
    }

    public void addExp(double exp) {
        setExp(getExp() + exp);
    }

    public int getTrance() {
        if (trance == null) trance = getMetaDataInt(DataBase.Table.EquipmentTrance, 0);
        return trance;
    }

    public void setTrance(int trance) {
        this.trance = trance;
        setMetaData("Trance", trance);
    }

    public void addTrance(int trance) {
        setTrance(getTrance() + trance);
    }

    public int getPlus() {
        if (plus == null) plus = getMetaDataInt(DataBase.Table.EquipmentPlus, 0);
        return plus;
    }

    public void setPlus(int plus) {
        this.plus = plus;
        setMetaData("Plus", plus);
    }

    public void addPlus(int plus) {
        setPlus(getPlus() + plus);
    }

    public int getLevelDown() {
        if (levelDown == null) levelDown = getMetaDataInt(DataBase.Table.EquipmentLevelDown, 0);
        return levelDown;
    }

    public void setLevelDown(int levelDown) {
        this.levelDown = levelDown;
        setMetaData("LevelDown", levelDown);
    }

    public void addLevelDown(int levelDown) {
        setLevelDown(getLevelDown() + levelDown);
    }

    public int getCapsuleSlot() {
        return capsuleSlot;
    }

    public void setCapsuleSlot(int capsuleSlot) {
        this.capsuleSlot = capsuleSlot;
    }

    public List<CapsuleData> getCapsules() {
        if (capsules == null) {
            capsules = new ArrayList<>();
            String data = getMetaData(DataBase.Table.EquipmentCapsule, "None");
            if (!data.equals("None")) for (String id : data.split(",")) {
                capsules.add(CapsuleDataLoader.getCapsuleData(id));
            }
        }
        return capsules;
    }

    public void addCapsule(CapsuleData capsuleData) {
        capsules.add(capsuleData);
        updateCapsule();
    }

    public void removeCapsule(CapsuleData capsuleData) {
        capsules.remove(capsuleData);
        updateCapsule();
    }

    public void updateCapsule() {
        if (capsules.isEmpty()) {
            deleteMetaData("Capsule");
        } else {
            StringBuilder text = new StringBuilder(capsules.get(0).getId());
            for (int i = 1; i < capsules.size(); i++) {
                text.append(",").append(capsules.get(i).getId());
            }
            setMetaData("Capsule", text.toString());
        }
    }

    public int getReqLevel() {
        return reqLevel - getLevelDown();
    }

    public int getRawReqLevel() {
        return reqLevel;
    }

    public void setReqLevel(int reqLevel) {
        this.reqLevel = reqLevel;
    }

    public Category getEquipCategory() {
        return category;
    }

    public void setEquipCategory(Category category) {
        this.category = category;
    }

    public String getSeries() {
        return series;
    }

    public void setSeries(String series) {
        this.series = series;
    }

    public boolean hasSeries() {
        return series != null;
    }

    public Slot getSlot() {
        return category.slot;
    }

    public HashMap<StatusType, Double> getStatus() {
        HashMap<StatusType, Double> status = new HashMap<>();
        category.baseStatus.forEach((type, value) -> status.put(type, value(value)));
        return status;
    }

    public double getBaseStatus(StatusType statusType) {
        return base(category.baseStatus.getOrDefault(statusType, 0.0));
    }

    public double getStatus(StatusType statusType) {
        return value(category.baseStatus.getOrDefault(statusType, 0.0));
    }

    public double base(double value) {
        return value * power;
    }

    public double value(double value) {
        return base(value) * (1 + getExp()) * (1 + getTrance() * 0.05) * (1 + getPlus() * 0.025);
    }

    private static final double ExpLimit = 1.25;
    public void chanceExp() {
        if (getExp() < ExpLimit) {
            double percent =  1 / (getExp() * 100);
            if (getExp() >= 1.0) percent /= Math.pow(getExp() + 0.7, 2);
            if (randomDouble() < percent) {
                addExp(0.0001);
            }
        }
        if (getExp() > ExpLimit) setExp(ExpLimit);
    }

    public SomEquip copy(SomEquip equip) {
        equip.setExp(getExp());
        equip.setPlus(getPlus());
        equip.setTrance(getTrance());
        equip.setLevelDown(getLevelDown());
        equip.capsules = new ArrayList<>();
        getCapsules().forEach(equip::addCapsule);
        return equip;
    }

    public boolean isWeapon() {
        return category.isWeapon();
    }
    public boolean isTool() {
        return category.isTool();
    }
    public boolean isArmor() {
        return category.isArmor();
    }

    @Override
    public SomEquip clone() {
        SomEquip equip = (SomEquip) super.clone();
        equip.setSync(false);
        return equip;
    }

    public enum Slot {
        MainHand("武器"),
        //OffHand("サブ"),
        Head("兜鎧"),
        Chest("胸鎧"),
        Legs("脚鎧"),
        Foot("靴鎧"),
        ;

        private final String display;

        Slot(String display) {
            this.display = display;
        }

        public String getDisplay() {
            return display;
        }
    }

    public enum Category {
        Dagger(Material.STONE_SWORD, "短剣", "ダガー", Slot.MainHand),
        Sword(Material.IRON_SWORD, "片手剣", "ソード", Slot.MainHand),
        Rod(Material.BLAZE_ROD, "杖", "ロッド", Slot.MainHand),
        Bow(Material.BOW, "弓", "ボウ", Slot.MainHand),
        Mace(Material.STONE_SHOVEL, "鈍器", "メイス", Slot.MainHand),

        MiningTool(Material.IRON_PICKAXE, "採掘道具", "マイニングツール", Slot.MainHand),
        CollectTool(Material.SHEARS, "採集道具", "コレクトツール", Slot.MainHand),
        FishingTool(Material.FISHING_ROD, "漁獲道具", "フィッシングツール", Slot.MainHand),
        CraftTool(Material.CRAFTING_TABLE, "制作道具", "クラフトツール", Slot.MainHand),

        Helmet(Material.LEATHER_HELMET, Color.RED, "鉄帽", "ヘルメット", Slot.Head),
        Armor(Material.LEATHER_CHESTPLATE, Color.RED, "胸鎧", "アーマー", Slot.Chest),
        Leggings(Material.LEATHER_LEGGINGS, Color.RED, "佩楯", "レギンス", Slot.Legs),
        Shoes(Material.LEATHER_BOOTS, Color.RED, "鉄靴", "シューズ", Slot.Foot),

        Corsage(Material.LEATHER_HELMET, Color.PURPLE, "頭飾り", "コサージ", Slot.Head),
        Robe(Material.LEATHER_CHESTPLATE, Color.PURPLE, "法服", "ローブ", Slot.Chest),
        Loin(Material.LEATHER_LEGGINGS, Color.PURPLE, "腰巻", "ロース", Slot.Legs),
        Boots(Material.LEATHER_BOOTS, Color.PURPLE, "靴", "ブーツ", Slot.Foot),

        Cap(Material.LEATHER_HELMET, Color.YELLOW, "帽子", "キャップ", Slot.Head),
        Cloak(Material.LEATHER_CHESTPLATE, Color.YELLOW, "外套", "マント", Slot.Chest),
        Belt(Material.LEATHER_LEGGINGS, Color.YELLOW, "腰当", "ベルト", Slot.Legs),
        Greaves(Material.LEATHER_BOOTS, Color.YELLOW, "脛当", "グリーブ", Slot.Foot),
        ;

        private final String display;
        private final Material icon;
        private final Color color;
        private final String suffix;
        private final Slot slot;
        private final HashMap<StatusType, Double> baseStatus = new HashMap<>();

        Category(Material icon, String display, String suffix, Slot slot) {
            this.display = display;
            this.icon = icon;
            color = null;
            this.suffix = suffix;
            this.slot = slot;
        }

        Category(Material icon, Color color, String display, String suffix, Slot slot) {
            this.display = display;
            this.icon = icon;
            this.color = color;
            this.suffix = suffix;
            this.slot = slot;
        }

        public String getDisplay() {
            return display;
        }

        public Material getIcon() {
            return icon;
        }

        public Color getColor() {
            return color;
        }

        public String getSuffixA() {
            return suffix;
        }

        public String getSuffixB() {
            return display;
        }

        public String getSuffixC() {
            return "の" + display;
        }

        public String getSuffix(String type) {
            switch (type) {
                case "A" -> {
                    return getSuffixA();
                }
                case "B" -> {
                    return getSuffixB();
                }
                case "C" -> {
                    return getSuffixC();
                }
                default -> {
                    return type;
                }
            }
        }

        public Slot getSlot() {
            return slot;
        }

        public boolean isWeapon() {
            return Category.Weapons().contains(this);
        }
        public boolean isTool() {
            return Category.Tools().contains(this);
        }
        public boolean isArmor() {
            return Category.Armors().contains(this);
        }

        public boolean isPhysics() {
            return Category.PhysicsArmors().contains(this);
        }

        public boolean isMagic() {
            return Category.MagicArmors().contains(this);
        }

        public boolean isCritical() {
            return Category.CriticalArmors().contains(this);
        }

        public String armorType() {
            if (isPhysics()) {
                return "Physics";
            } else if (isMagic()) {
                return "Magic";
            } else if (isCritical()) {
                return "Critical";
            } else return "Error";
        }

        public static void load() {
            for (Category category : Category.values()) {
                switch (category) {
                    case Dagger -> {
                        category.baseStatus.put(StatusType.ATK, 28.0);
                        category.baseStatus.put(StatusType.CriticalRate, 7.0);
                        category.baseStatus.put(StatusType.CriticalDamage, 7.0);
                    }
                    case Sword -> {
                        category.baseStatus.put(StatusType.ATK, 32.0);
                        category.baseStatus.put(StatusType.CriticalRate, 1.75);
                        category.baseStatus.put(StatusType.CriticalDamage, 1.75);
                    }
                    case Rod -> {
                        category.baseStatus.put(StatusType.MAT, 30.0);
                        category.baseStatus.put(StatusType.CriticalRate, 7.0);
                        category.baseStatus.put(StatusType.CriticalDamage, 3.5);
                    }
                    case Bow -> {
                        category.baseStatus.put(StatusType.SAT, 30.0);
                        category.baseStatus.put(StatusType.CriticalRate, 3.5);
                        category.baseStatus.put(StatusType.CriticalDamage, 3.5);
                    }
                    case Mace -> {
                        category.baseStatus.put(StatusType.SPT, 30.0);
                        category.baseStatus.put(StatusType.MAT, 30.0);
                        category.baseStatus.put(StatusType.CriticalRate, 1.75);
                        category.baseStatus.put(StatusType.CriticalDamage, 1.75);
                    }

                    case MiningTool, CollectTool, FishingTool, CraftTool -> category.baseStatus.put(StatusType.GatheringPower, 100.0);

                    case Helmet, Armor, Leggings, Shoes -> {
                        category.baseStatus.put(StatusType.MaxHealth, 40.0);
                        category.baseStatus.put(StatusType.HealthRegen, 0.5);
                        category.baseStatus.put(StatusType.DEF, 6.75);
                        category.baseStatus.put(StatusType.MDF, 6.75);
                        category.baseStatus.put(StatusType.SDF, 6.75);
                        category.baseStatus.put(StatusType.CriticalResist, 3.75);
                    }

                    case Corsage, Robe, Loin, Boots -> {
                        category.baseStatus.put(StatusType.MaxHealth, 25.0);
                        category.baseStatus.put(StatusType.MaxMana, 12.0);
                        category.baseStatus.put(StatusType.ManaRegen, 0.5);
                        category.baseStatus.put(StatusType.DEF, 5.0);
                        category.baseStatus.put(StatusType.MDF, 7.0);
                        category.baseStatus.put(StatusType.SDF, 5.0);
                        category.baseStatus.put(StatusType.CriticalResist, 1.2);
                    }

                    case Cap, Cloak, Belt, Greaves -> {
                        category.baseStatus.put(StatusType.MaxHealth, 30.0);
                        category.baseStatus.put(StatusType.ManaRegen, 0.1);
                        category.baseStatus.put(StatusType.DEF, 4.5);
                        category.baseStatus.put(StatusType.SDF, 4.5);
                        category.baseStatus.put(StatusType.MDF, 4.5);
                        category.baseStatus.put(StatusType.CriticalRate, 4.25);
                        category.baseStatus.put(StatusType.CriticalDamage, 1.5);
                        category.baseStatus.put(StatusType.CriticalResist, 1.4);
                    }
                }
            }
        }

        public static List<Category> Weapons() {
            return List.of(new Category[]{Dagger, Sword, Rod, Bow, Mace});
        }

        public static List<Category> Tools() {
            return List.of(new Category[]{MiningTool, CollectTool, FishingTool, CraftTool});
        }

        public static List<Category> Armors() {
            List<Category> armors = new ArrayList<>();
            armors.addAll(PhysicsArmors());
            armors.addAll(MagicArmors());
            armors.addAll(CriticalArmors());
            return armors;
        }

        public static List<Category> PhysicsArmors() {
            return List.of(new Category[]{Helmet, Armor, Leggings, Shoes});
        }

        public static List<Category> MagicArmors() {
            return List.of(new Category[]{Corsage, Robe, Loin, Boots});
        }
        public static List<Category> CriticalArmors() {
            return List.of(new Category[]{Cap, Cloak, Belt, Greaves});
        }

        public static List<Category> Head() {
            return List.of(new Category[]{Helmet, Corsage, Cap});
        }

        public static List<Category> Chest() {
            return List.of(new Category[]{Armor, Robe, Cloak});
        }

        public static List<Category> Legs() {
            return List.of(new Category[]{Leggings, Loin, Belt});
        }

        public static List<Category> Foot() {
            return List.of(new Category[]{Shoes, Boots, Greaves});
        }

        public static List<Category> Armors(SomEquip.Slot equipSlot) {
            switch (equipSlot) {
                case Head -> {
                    return Head();
                }
                case Chest -> {
                    return Chest();
                }
                case Legs -> {
                    return Legs();
                }
                case Foot -> {
                    return Foot();
                }
            }
            throw new RuntimeException();
        }
    }
}
