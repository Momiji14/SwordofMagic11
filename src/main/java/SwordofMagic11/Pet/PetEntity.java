package SwordofMagic11.Pet;

import SwordofMagic11.AttributeType;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Component.SomTask;
import SwordofMagic11.Custom.CustomLocation;
import SwordofMagic11.Entity.Damage;
import SwordofMagic11.Entity.SearchEntity;
import SwordofMagic11.Entity.SomEntity;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.SomCore;
import SwordofMagic11.StatusType;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Mob;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import static SwordofMagic11.Component.Function.scale;
import static SwordofMagic11.Enemy.EnemyData.hurtSound;

public class PetEntity extends SomEntity {
    private final SomPet pet;
    private final Mob mob;
    private SomEntity target;
    private CustomLocation lastTargetLocation;
    private CustomLocation overrideLocation = null;
    public double searchRadius;
    private State state;
    public PetEntity(SomPet pet) {
        super((Mob) pet.getOwner().getWorld().spawnEntity(pet.getOwner().getLocation(), pet.getMobData().getEntityType(), false));
        this.pet = pet;
        pet.setState(PetState.Summon);
        setBaseStatus(StatusType.MaxHealth, 150);
        setBaseStatus(StatusType.ATK, 50);
        setBaseStatus(StatusType.MAT, 50);
        setBaseStatus(StatusType.SAT, 50);
        setBaseStatus(StatusType.DEF, 25);
        setBaseStatus(StatusType.MDF, 25);
        setBaseStatus(StatusType.SDF, 25);
        setBaseStatus(StatusType.Movement, 300);

        mob = (Mob) getEntity();

        mob.setCustomNameVisible(true);
        mob.setMetadata("SomEntity", new FixedMetadataValue(SomCore.plugin(), this));
        mob.getPersistentDataContainer().set(SomCore.SomEntityKey, PersistentDataType.BOOLEAN, true);

        if (pet.getMobData().hasDisguise()) {
            Disguise disguise = pet.getMobData().getDisguise().clone();
            disguise.setEntity(mob);
            disguise.startDisguise();
        }
        updateAttribute();
        heal();

        SomTask.asyncTimer(() -> {
            if (overrideLocation != null) {
                if (getLocation().distance(overrideLocation) < 1) {
                    resetOverrideLocation();
                } else {
                    SomTask.sync(() -> mob.getPathfinder().moveTo(overrideLocation));
                }
            } else {
                if (target == null) {
                    if (state == State.Auto) {
                        List<SomEntity> entry = SearchEntity.nearSomEntity(getOwner().enemies(), getOwner().getLocation(), searchRadius);
                        if (!entry.isEmpty()) {
                            entry.sort(Comparator.comparingDouble(enemy -> enemy.getLocation().distance(getLocation())));
                            target = entry.get(0);
                        }
                    }
                }
                if (target != null) {
                    if (!target.isDeath()) {
                        if (getLocation().distance(target.getLocation()) < 2) {
                            Damage.makeDamage(this, target, Damage.Type.Physics, 1);
                        } else if (lastTargetLocation == null || lastTargetLocation.distance(target.getLocation()) > 1.5){
                            SomTask.sync(() -> mob.getPathfinder().moveTo(target.getLocation()));
                        }
                    } else {
                        target = null;
                    }
                }
            }
            if (getOwner().isAFK()) {
                delete();
            }
            updateNamePlate();
        }, 10, this);
        SomTask.registerEndTask(this, this::delete);
    }

    public void updateNamePlate() {
        mob.customName(Component.text("§e" + pet.getName() + " Lv" + pet.getLevel(PetLevelType.Combat) + " §c[" + scale(healthPercent()*100, 2) + "%]"));
    }

    public void updateAttribute() {
        for (AttributeType attr : AttributeType.values()) {
            setBaseAttribute(attr, (int) (pet.getLevel(PetLevelType.Combat) * pet.getAttributeMultiply(attr)));
        }
        statusUpdate();
    }

    public SomPet instance() {
        return pet;
    }

    public PlayerData getOwner() {
        return pet.getOwner();
    }

    public void setTarget(SomEntity target) {
        this.target = target;
    }

    public void setOverrideLocation(CustomLocation overrideLocation) {
        this.overrideLocation = overrideLocation;
    }

    public void resetOverrideLocation() {
        overrideLocation = null;
    }

    public void setSearchRadius(double searchRadius) {
        this.searchRadius = searchRadius;
    }

    public void setState(State state) {
        this.state = state;
    }

    @Override
    public int getLevel() {
        return pet.getLevel(PetLevelType.Combat);
    }

    @Override
    public void hurt() {
        hurtSound(pet.getMobData(), getLocation());
    }

    @Override
    public String getName() {
        return pet.getName();
    }

    @Override
    public Collection<SomEntity> enemies() {
        return null;
    }

    @Override
    public boolean isEnemy(SomEntity entity) {
        return false;
    }

    @Override
    public Collection<SomEntity> allies() {
        return null;
    }

    @Override
    public boolean isAlly(SomEntity entity) {
        return false;
    }

    @Override
    public Collection<SomEntity> alliesIsDeath() {
        return null;
    }

    @Override
    public boolean isAllyIsDeath(SomEntity entity) {
        return false;
    }

    @Override
    protected void deathProcess(SomEntity killer) {

    }

    @Override
    public void onMakeDamage(SomEntity victim, Damage.Type type, double multiply, boolean isCritical) {

    }

    @Override
    public void onTakeDamage(SomEntity attacker, Damage.Type type, double multiply, boolean isCritical) {

    }

    @Override
    public boolean isValid() {
        return mob.isValid() && !isDeath() && mob.getWorld().getPlayers().contains(getOwner().getPlayer());
    }

    public void delete() {
        SomTask.sync(() -> {
            mob.remove();
            setDeath(true);
        });
        pet.setState(PetState.Cage);
        getOwner().sendMessage(pet.colorName() + "§aが§eケージ§aに戻りました", SomSound.Tick);
    }

    public enum State {
        Auto,
        Select,
    }
}
