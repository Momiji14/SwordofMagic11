package SwordofMagic11.Entity;

import SwordofMagic11.AttributeType;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Component.SomTask;
import SwordofMagic11.Component.WorldManager;
import SwordofMagic11.Custom.CustomLocation;
import SwordofMagic11.DataBase.MapDataLoader;
import SwordofMagic11.Enemy.EnemyData;
import SwordofMagic11.Item.SomEquip;
import SwordofMagic11.Map.MapData;
import SwordofMagic11.Pet.PetEntity;
import SwordofMagic11.Pet.SomPet;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Player.Setting.PlayerSetting;
import SwordofMagic11.SomCore;
import SwordofMagic11.StatusType;
import SwordofMagic11.TaskOwner;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import static SwordofMagic11.Component.Function.MinMax;
import static SwordofMagic11.Component.Function.scale;
import static SwordofMagic11.DataBase.DataBase.PercentValue;

public abstract class SomEntity implements TaskOwner {

    public static boolean is(Entity entity) {
        return entity.hasMetadata("SomEntity");
    }

    public static SomEntity instance(Entity entity) {
        for (MetadataValue metadataValue : entity.getMetadata("SomEntity")) {
            if (metadataValue.getOwningPlugin() == SomCore.plugin()) {
                return (SomEntity) metadataValue.value();
            }
        }
        return null;
    }

    private final HashMap<StatusType, Double> finalStatus = new HashMap<>();
    private final HashMap<StatusType, Double> baseStatus = new HashMap<>();
    private final HashMap<StatusType, Double> normalStatus = new HashMap<>();
    private final HashMap<AttributeType, Integer> finalAttribute = new HashMap<>();
    private final HashMap<AttributeType, Integer> baseAttribute = new HashMap<>();
    private final HashMap<StatusType, Double> overrideStatus = new HashMap<>();
    private final HashMap<StatusType, Double> limitStatus = new HashMap<>();
    private final ConcurrentHashMap<String, SomEffect> effectTable = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Integer> timer = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Runnable> timerEndTask = new ConcurrentHashMap<>();
    private final LivingEntity entity;
    private double health = 0;
    private double mana = 0;
    private double fixedMaxHealth;
    private SomEntity lastAttacker;
    private boolean isInvincible = false;
    private boolean isDeath = false;
    private boolean ignoreCrowdControl = false;
    private double damageOverride;

    public SomEntity(LivingEntity entity) {
        this.entity = entity;

        SomTask.asyncTimer(() -> {
            if (!effectTable.isEmpty()) {
                for (SomEffect effect : effectTable.values()) {
                    effect.removeTime(5);
                    if (effect.getTime() <= 0) {
                        removeEffect(effect.getId());
                        String logText = effect.getDisplay() + (effect.getPriority() != 0 ? "§b[" + scale(effect.getPriority()) + "]" : "");
                        if (effect.isLog() && effect.getOwner() instanceof PlayerData playerData) {
                            if (playerData.setting().is(PlayerSetting.BooleanEnum.BuffLog)) {
                                playerData.sendMessage("§c≫§f" + logText + " §e[" + getName() + "]");
                            }
                        }
                        if (effect.isLog() && effect.getOwner() != this && this instanceof PlayerData playerData) {
                            if (playerData.setting().is(PlayerSetting.BooleanEnum.BuffLog)) {
                                playerData.sendMessage("§c≪§f" + logText + " §e[" + effect.getOwner().getName() + "]");
                            }
                        }
                    }
                }
            }
            if (!timer.isEmpty()) {
                timer.forEach((id, time) -> timer.merge(id, -5, Integer::sum));
                timer.entrySet().removeIf(entry -> {
                    if (entry.getValue() <= 0) {
                        if (timerEndTask.containsKey(entry.getKey())) {
                            timerEndTask.get(entry.getKey()).run();
                        }
                        return true;
                    } else return false;
                });
            }
        }, 5, this);
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public String getUUID() {
        return entity.getUniqueId().toString();
    }

    public World getWorld() {
        return entity.getWorld();
    }

    public CustomLocation getLocation() {
        return new CustomLocation(entity.getLocation());
    }

    public CustomLocation getEyeLocation() {
        return new CustomLocation(entity.getEyeLocation());
    }

    public CustomLocation getHipsLocation() {
        CustomLocation location = new CustomLocation(getWorld(), entity.getBoundingBox().getCenter());
        location.setDirection(getDirection());
        return location;
        //return new CustomLocation(entity.getLocation().add(0, 1, 0));
    }

    public CustomLocation getHandLocation() {
        CustomLocation location = getLocation();
        return location.addY(0.9).right(0.35).frontHorizon(0.5);
    }

    public Vector getDirection() {
        return entity.getLocation().getDirection();
    }

    public float yaw() {
        return entity.getLocation().getYaw();
    }

    public int yawInt() {
        return (int) entity.getLocation().getYaw();
    }

    public float pitch() {
        return entity.getLocation().getPitch();
    }

    public HashMap<StatusType, Double> getFinalStatus() {
        return finalStatus;
    }

    public double getStatus(StatusType statusType) {
        return finalStatus.getOrDefault(statusType, 0.0);
    }

    public HashMap<StatusType, Double> getBaseStatus() {
        return baseStatus;
    }

    public void setBaseStatus(HashMap<StatusType, Double> baseStatus) {
        this.baseStatus.clear();
        this.baseStatus.putAll(baseStatus);
    }

    public double getNormalStatus(StatusType statusType) {
        return normalStatus.getOrDefault(statusType, 0.0);
    }

    public HashMap<StatusType, Double> getNormalStatus() {
        return normalStatus;
    }

    public HashMap<AttributeType, Integer> getFinalAttribute() {
        return finalAttribute;
    }

    public HashMap<AttributeType, Integer> getBaseAttribute() {
        return baseAttribute;
    }

    public int getBaseAttribute(AttributeType attr) {
        return baseAttribute.getOrDefault(attr, 0);
    }

    public void setBaseStatus(StatusType type, double value) {
        baseStatus.put(type, value);
    }

    public void setBaseAttribute(AttributeType attr, int value) {
        baseAttribute.put(attr, value);
    }

    /**
     * ATK/MAT/SAT/SPTのうち一番高いステータスのStatusTypeを返す
     *
     * @param status 評価するステータス
     * @return  デフォルトはATK
     */
    public StatusType getHighestAttackStatusType(HashMap<StatusType, Double> status) {
        double maxValue = 0;
        StatusType result = StatusType.ATK;
        for (StatusType type : status.keySet()) {
            if (type == StatusType.ATK || type == StatusType.MAT || type == StatusType.SAT || type == StatusType.SPT) {
                double value = status.get(type);
                if (value > maxValue) {
                    maxValue = value;
                    result = type;
                }
            }
        }
        return result;
    }

    /**
     * 最後に攻撃されたSomEntity
     * ※いない場合は null
     */
    public SomEntity lastAttacker() {
        return lastAttacker;
    }

    public void lastAttacker(SomEntity entity) {
        lastAttacker = entity;
    }


    /**
     * バフを付与
     *
     * @param effect バフのインスタンス
     */
    public void addEffect(SomEffect effect) {
        if (effectTable.containsKey(effect.getId())) {
            if (effect.getPriority() < effectTable.get(effect.getId()).getPriority()) {
                return;
            }
        }
        effectTable.put(effect.getId(), effect.clone());
        if (effect.isUpdate()) statusUpdate();
        String logText = effect.getDisplay() + (effect.getPriority() != 0 ? "§b[" + scale(effect.getPriority()) + "]" : "");
        if (effect.isLog() && effect.getOwner() instanceof PlayerData playerData) {
            if (playerData.setting().is(PlayerSetting.BooleanEnum.BuffLog)) {
                playerData.sendMessage("§a≪§f" + logText + " §e[" + getName() + "]");
            }
        }
        if (effect.isLog() && effect.getOwner() != this && this instanceof PlayerData playerData) {
            if (playerData.setting().is(PlayerSetting.BooleanEnum.BuffLog)) {
                playerData.sendMessage("§a≫§f" + logText + " §e[" + effect.getOwner().getName() + "]");
            }
        }
        if (effect.hasApplyRunnable()) {
            effect.getApplyRunnable().accept(this);
        }
    }

    /**
     * バフを削除
     *
     * @param id バフのID
     */
    public void removeEffect(String id) {
        if (effectTable.containsKey(id)) {
            SomEffect effect = effectTable.get(id);
            effectTable.remove(id);
            if (effect.isUpdate()) statusUpdate();
            if (effect.hasRemoveRunnable()) {
                effect.getRemoveRunnable().accept(this);
            }
        }
    }

    public void removeEffectAll() {
        for (SomEffect effect : effectTable.values()) {
            if (effect.hasRemoveRunnable()) effect.getRemoveRunnable().accept(this);
        }
        effectTable.clear();
        statusUpdate();
    }

    /**
     * バフ付与確認
     *
     * @param id バフのID
     */
    public boolean hasEffect(String id) {
        return effectTable.containsKey(id);
    }

    public SomEffect getEffect(String id) {
        return effectTable.get(id);
    }

    public Collection<SomEffect> getEffects() {
        return effectTable.values();
    }

    /**
     * ステータスの再計算
     */
    public void statusUpdate() {
        HashMap<AttributeType, Integer> attribute = new HashMap<>(this.baseAttribute);
        for (AttributeType attr : AttributeType.values()) {
            if (!attribute.containsKey(attr)) {
                attribute.put(attr, 0);
            }
        }
        finalAttribute.putAll(attribute);

        HashMap<StatusType, Double> status = new HashMap<>(this.baseStatus);
        HashMap<StatusType, Double> statusMultiply = new HashMap<>();
        if (overrideStatus.isEmpty()) {
            for (AttributeType attr : AttributeType.values()) {
                attr.getPerStatus().forEach((statusType, value) -> status.merge(statusType, value * finalAttribute.getOrDefault(attr, 0), Double::sum));
            }
            for (StatusType type : StatusType.values()) {
                if (!status.containsKey(type)) {
                    status.put(type, 0.0);
                }
            }
            if (this instanceof PlayerData playerData) {
                for (SomEquip somEquip : playerData.getEquipment().values()) {
                    somEquip.getStatus().forEach((statusType, value) -> status.merge(statusType, value, Double::sum));
                }
                playerData.getTotalCapsuleStatus().forEach((statusType, value) -> statusMultiply.merge(statusType, value, Double::sum));
                playerData.memorialMenu().getMemorialStatus().forEach((statusType, value) -> statusMultiply.merge(statusType, value, Double::sum));
            } else if (this instanceof EnemyData enemyData) {
                double multiply = enemyData.rank().healthMultiply() * enemyData.getMobData().getRank().healthMultiply() * (1 + getLevel() * 0.1) * (1 + (getMap().getMinBalance() - 1) * PercentValue);
                status.put(StatusType.MaxHealth, status.getOrDefault(StatusType.MaxHealth, 0.0) * multiply);
                if (fixedMaxHealth != 0.0) status.put(StatusType.MaxHealth, fixedMaxHealth);
            } else if (this instanceof PetEntity petEntity) {
                SomPet somPet = petEntity.instance();
                for (SomEquip somEquip : somPet.getEquipment().values()) {
                    somEquip.getStatus().forEach((statusType, value) -> status.merge(statusType, value, Double::sum));
                }
                if (somPet.getPlus() > 0) for (StatusType type : StatusType.values()) {
                    statusMultiply.merge(type, somPet.getPlus() * 0.01, Double::sum);
                }

            }
            limitStatus.forEach((statusType, value) -> {
                if (status.containsKey(statusType)) {
                    status.put(statusType, Math.min(value, status.get(statusType)));
                }
            });
        } else {
            status.putAll(overrideStatus);
        }

        normalStatus.putAll(status);
        HashMap<StatusType, Double> effectStatus = new HashMap<>();
        for (SomEffect effect : effectTable.values()) {
            effect.getStatus().forEach((statusType, value) -> effectStatus.merge(statusType, value, Double::sum));
            effect.getStatusMultiply().forEach((statusType, value) -> statusMultiply.merge(statusType, value, Double::sum));
        }
        statusMultiply.forEach((statusType, value) -> status.merge(statusType, status.getOrDefault(statusType, 0.0) * value, Double::sum));
        effectStatus.forEach((statusType, value) -> status.merge(statusType, value, Double::sum));
        finalStatus.putAll(status);
        SomTask.sync(() -> {
            if (entity instanceof Player player) {
                player.setWalkSpeed((float) (getStatus(StatusType.Movement) / 1000));
            } else {
                entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(getStatus(StatusType.Movement) / 1000);
            }
        });
    }

    public void setOverrideStatus(HashMap<StatusType, Double> overrideStatus) {
        this.overrideStatus.putAll(overrideStatus);
        statusUpdate();
    }

    public void resetOverrideStatus() {
        overrideStatus.clear();
        statusUpdate();
    }

    public void setLimitStatus(HashMap<StatusType, Double> limitStatus) {
        this.limitStatus.putAll(limitStatus);
        statusUpdate();
    }

    public void resetLimitStatus() {
        limitStatus.clear();
        statusUpdate();
    }

    public void setFixedMaxHealth(double value) {
        fixedMaxHealth = Math.max(value, 0);
        statusUpdate();
    }

    public double getFixedMaxHealth() {
        return fixedMaxHealth;
    }

    public void setHealth(double value) {
        health = MinMax(value, 0, finalStatus.getOrDefault(StatusType.MaxHealth, 0.0));
    }

    public double getHealth() {
        return health;
    }

    public void addHealth(double value) {
        setHealth(health + value);
    }

    public void removeHealth(double value, SomEntity attacker) {
        setHealth(health - value);
        if (health <= 0) death(attacker);
    }

    public double healthPercent() {
        return health / getStatus(StatusType.MaxHealth);
    }

    public void setMana(double value) {
        mana = MinMax(value, 0, finalStatus.getOrDefault(StatusType.MaxMana, 0.0));
    }

    public double getMana() {
        return mana;
    }

    public double manaPercent() {
        return mana / getStatus(StatusType.MaxMana);
    }

    public void addMana(double value) {
        setMana(mana + value);
    }

    public void removeMana(double value) {
        setMana(mana - value);
    }

    public boolean isArsha() {
        return WorldManager.isArsha(getWorld());
    }

    public MapData getMap() {
        return MapDataLoader.getMapData(WorldManager.ID(getWorld()));
    }

    /**
     * スローを付与 (バニラ鈍足ポーション)
     *
     * @param level    Potionのレベル
     * @param duration 効果時間 (tick)
     */
    public void slow(int level, int duration) {
        addPotionEffect(new PotionEffect(PotionEffectType.SLOW, duration, level, false, false));
    }

    /**
     * 盲目を付与 (バニラ盲目ポーション)
     *
     * @param level    Potionのレベル
     * @param duration 効果時間 (tick)
     */
    public void blind(int level, int duration) {
        addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, duration, level, false, false));
    }

    /**
     * ポーション効果を除去
     *
     * @param type 削除する効果
     */
    public void removePotionEffect(PotionEffectType type) {
        SomTask.sync(() -> entity.removePotionEffect(type));
    }

    /**
     * ポーション効果を除去
     *
     * @param effect 追加する効果
     */
    public void addPotionEffect(PotionEffect effect) {
        SomTask.sync(() -> entity.addPotionEffect(effect));
    }

    /**
     * タイマーの残り時間
     *
     * @param id タイマーのId
     * @return 残り時間
     */
    public int timer(String id) {
        return timer.getOrDefault(id, 0);
    }

    /**
     * 汎用のタイマー
     * ※5tickで更新されます
     *
     * @param id   タイマーのId
     * @param time 時間 (tick)
     */
    public void timer(String id, int time) {
        if (!timer.containsKey(id) || timer.get(id) < time) {
            timer.put(id, time);
        }
    }

    /**
     * タイマーの所持確認
     *
     * @param id タイマーのID
     */
    public boolean hasTimer(String id) {
        return timer.containsKey(id);
    }

    public void resetTimer(String id) {
        timer.remove(id);
    }

    /**
     * 汎用のタイマー
     * ※5tickで更新されます
     *
     * @param id       タイマーのId
     * @param runnable 実行内容
     */
    public void timerEndTask(String id, Runnable runnable) {
        timerEndTask.put(id, runnable);
    }

    /**
     * サイレンスを付与
     * ※プレイヤ－の場合はスキルの詠唱を阻害する
     *
     * @param time 効果時間
     */
    public void silence(int time, SomEntity attacker) {
        timer("Silence", time);
        if (this == attacker) return;
        if (attacker instanceof PlayerData playerData) {
            playerData.sendMessage("§a≫§fサイレンス §e[" + getName() + "]");
        }
        if (this instanceof PlayerData playerData) {
            playerData.sendMessage("§c≪§fサイレンス §c[" + attacker.getName() + "]");
        }
    }

    public boolean isSilence() {
        return hasTimer("Silence");
    }

    public void resetSilence() {
        if (isSilence()) {
            resetTimer("Silence");
            if (this instanceof PlayerData playerData) {
                playerData.sendMessage("§c≫§fサイレンス");
            }
        }
    }

    public void heal() {
        setHealth(getStatus(StatusType.MaxHealth));
        setMana(getStatus(StatusType.MaxMana));
    }

    public boolean isInInstance() {
        return WorldManager.isInstance(getWorld());
    }

    public void invinciblePvP(int time) {
        timer("InvinciblePvP", time);
    }

    public boolean isInvinciblePvP() {
        return hasTimer("InvinciblePvP");
    }

    public void invincible(boolean bool) {
        isInvincible = bool;
    }

    public boolean isInvincible() {
        return isInvincible;
    }

    public boolean hasDamageOverride() {
        return damageOverride != 0.0;
    }

    public double damageOverride() {
        return damageOverride;
    }

    public void damageOverride(double damage) {
        damageOverride = damage;
    }

    public void ignoreCrowdControl(boolean bool) {
        ignoreCrowdControl = bool;
    }

    public void teleport(Location location) {
        teleport(location, null, null);
    }

    public void teleport(Location location, Vector vector) {
        teleport(location, vector, null);
    }

    public void teleport(Location location, Vector vector, SomSound sound) {
        if (!ignoreCrowdControl) SomTask.sync(() -> {
            if (this instanceof PlayerData playerData) {
                playerData.resetAFK();
                playerData.getNamePlate().remove();
            }
            for (Entity passenger : entity.getPassengers()) {
                entity.removePassenger(passenger);
            }
            entity.teleport(location);
            if (vector != null) entity.setVelocity(vector);
            if (sound != null) sound.radius(location);
        });
    }

    public Vector getVelocity() {
        return entity.getVelocity();
    }

    public void setVelocity(Vector velocity) {
        if (!ignoreCrowdControl) {
            entity.setVelocity(velocity);
        }
    }

    public abstract int getLevel();

    public abstract void hurt();

    public abstract String getName();

    /**
     * 攻撃対象を取得
     *
     * @return 対象のリスト
     */
    public abstract Collection<SomEntity> enemies();

    /**
     * 対象が攻撃対象かを調べる
     *
     * @param entity 対象のSomEntity
     */
    public abstract boolean isEnemy(SomEntity entity);

    /**
     * 味方を取得
     *
     * @return 味方のリスト
     */
    public abstract Collection<SomEntity> allies();

    /**
     * 対象が味方かを調べる
     *
     * @param entity 対象のSomEntity
     */
    public abstract boolean isAlly(SomEntity entity);

    /**
     * 死亡状態の味方を取得
     *
     * @return 死亡状態の味方のリスト
     */
    public abstract Collection<SomEntity> alliesIsDeath();

    /**
     * 対象が死亡状態の味方かを調べる
     *
     * @param entity 対象のSomEntity
     */
    public abstract boolean isAllyIsDeath(SomEntity entity);

    public synchronized void death(SomEntity killer) {
        if (!isDeath) {
            isDeath = true;
            deathProcess(killer);
        }
    }

    public boolean isDeath() {
        return isDeath;
    }

    public void setDeath(boolean death) {
        isDeath = death;
    }

    protected abstract void deathProcess(SomEntity killer);

    public abstract void onMakeDamage(SomEntity victim, Damage.Type type, double multiply, boolean isCritical);

    public abstract void onTakeDamage(SomEntity attacker, Damage.Type type, double multiply, boolean isCritical);
}
