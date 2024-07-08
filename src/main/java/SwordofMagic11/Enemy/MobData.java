package SwordofMagic11.Enemy;

import SwordofMagic11.AttributeType;
import SwordofMagic11.DataBase.CapsuleDataLoader;
import SwordofMagic11.Item.CapsuleData;
import SwordofMagic11.Item.Material.MaterialData;
import SwordofMagic11.Player.Memorial.MemorialData;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static SwordofMagic11.Component.Function.randomInt;

public class MobData {
    private String id;
    private String display;
    private EntityType entityType;
    private Disguise disguise;
    private Rank rank = Rank.Normal;
    private final HashMap<AttributeType, Double> attributeRatio = new HashMap<>();
    private final Set<MaterialDrop> materialDrop = new HashSet<>();
    private final HashMap<CapsuleData, Double> capsuleDrop = new HashMap<>();
    private SwordofMagic11.Player.Memorial.MemorialData memorial;
    private boolean hostile = false;
    private boolean levelHide = false;
    private boolean object = false;
    private boolean defenseBattle = true;
    private String customClass;

    private int size;
    private double collisionSize = 0;
    private double collisionSizeY = 0;
    private double penetration = 0;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    public void setDisguise(Disguise disguise) {
        this.disguise = disguise;
    }

    public Disguise getDisguise() {
        return disguise;
    }

    public boolean hasDisguise() {
        return disguise != null;
    }

    public Rank getRank() {
        return rank;
    }

    public void setRank(Rank rank) {
        this.rank = rank;
    }

    public HashMap<AttributeType, Double> getAttributeRatio() {
        return attributeRatio;
    }

    public void setAttributeRatio(AttributeType attr, double value) {
        attributeRatio.put(attr, value);
    }

    public Set<MaterialDrop> getMaterialDrop() {
        return materialDrop;
    }

    public void addMaterialDrop(MaterialDrop materialDrop) {
        this.materialDrop.add(materialDrop);
    }

    public HashMap<CapsuleData, Double> getCapsuleDrop() {
        return capsuleDrop;
    }

    public void setCapsuleDrop(String capsule, double percent) {
        setCapsuleDrop(CapsuleDataLoader.getCapsuleData(capsule), percent);
    }

    public void setCapsuleDrop(CapsuleData capsule, double percent) {
        capsuleDrop.put(capsule, percent);
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public double getCollisionSize() {
        return collisionSize;
    }

    public void setCollisionSize(double collisionSize) {
        this.collisionSize = collisionSize;
    }

    public double getCollisionSizeY() {
        return collisionSizeY;
    }

    public void setCollisionSizeY(double collisionSizeY) {
        this.collisionSizeY = collisionSizeY;
    }

    public double getPenetration() {
        return penetration;
    }

    public void setPenetration(double penetration) {
        this.penetration = penetration;
    }

    public MemorialData getMemorial() {
        return memorial;
    }

    public void setMemorial(MemorialData memorial) {
        this.memorial = memorial;
    }

    public boolean hasMemorial() {
        return memorial != null;
    }

    public boolean isHostile() {
        return hostile;
    }

    public void setHostile(boolean hostile) {
        this.hostile = hostile;
    }

    public boolean isLevelHide() {
        return levelHide;
    }

    public void setLevelHide(boolean levelHide) {
        this.levelHide = levelHide;
    }

    public boolean isObject() {
        return object;
    }

    public void setObject(boolean object) {
        this.object = object;
    }

    public boolean isDefenseBattle() {
        return defenseBattle;
    }

    public void setDefenseBattle(boolean defenseBattle) {
        this.defenseBattle = defenseBattle;
    }

    public String getCustomClass() {
        return customClass;
    }

    public void setCustomClass(String customClass) {
        this.customClass = customClass;
    }

    public boolean hasCustomClass() {
        return customClass != null;
    }

    public record MaterialDrop(MaterialData material, double percent, int minAmount, int maxAmount) {
        private static final Random random = new Random();

        public int drop(double multiply) {
            double percent = percent() * multiply;
            int amount = 0;
            while (percent >= 1) {
                percent--;
                if (minAmount != maxAmount) {
                    amount += randomInt(minAmount, maxAmount);
                } else amount += minAmount;
            }
            if (percent > 0) {
                if (random.nextDouble() < percent) {
                    if (minAmount != maxAmount) {
                        amount += randomInt(minAmount, maxAmount);
                    } else amount += minAmount;
                }
            }
            return amount;
        }
    }

    public enum Rank {
        Normal(1.0, 1.0, 1.0, 1.0, 0.06, 0.001, 0, 0.05, 50),
        Middle(20.0, 20.0, 10.0, 2.0, 0.09, 0.0025, 0.5, 0.5, 500),
        Boss(50.0, 60.0, 100.0, 5.0, 0.18, 0.005, 1.0, 0.75, 2000),
        RaidBoss(200.0, 200.0, 300.0, 20.0, 0.32, 0.01, 2.0, 1.0, 10000),
        ;

        private final double healthMultiply;
        private final double dropMultiply;
        private final double sellMultiply;
        private final double memorialMultiply;
        private final double capsuleMultiply;
        private final double soulCapsuleDrop;
        private final double dropMel;
        private final double capsuleLimit;
        private final int memorialPoint;

        Rank(double healthMultiply, double dropMultiply, double sellMultiply, double memorialMultiply, double capsuleMultiply, double soulCapsuleDrop, double dropMel, double capsuleLimit, int memorialPoint) {
            this.healthMultiply = healthMultiply;
            this.dropMultiply = dropMultiply;
            this.sellMultiply = sellMultiply;
            this.memorialMultiply = memorialMultiply;
            this.capsuleMultiply = capsuleMultiply;
            this.soulCapsuleDrop = soulCapsuleDrop;
            this.dropMel = dropMel;
            this.capsuleLimit = capsuleLimit;
            this.memorialPoint = memorialPoint;
        }

        public double healthMultiply() {
            return healthMultiply;
        }

        public double dropMultiply() {
            return dropMultiply;
        }

        public double sellMultiply() {
            return sellMultiply;
        }

        public double memorialMultiply() {
            return memorialMultiply;
        }

        public double capsuleMultiply() {
            return capsuleMultiply;
        }

        public double soulCapsuleDrop() {
            return soulCapsuleDrop;
        }

        public double dropMel() {
            return dropMel;
        }

        public double capsuleLimit() {
            return capsuleLimit;
        }

        public int memorialPoint() {
            return memorialPoint;
        }

        public boolean isBossBar() {
            return this == Boss;
        }
    }
}
