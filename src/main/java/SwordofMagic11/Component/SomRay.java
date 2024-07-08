package SwordofMagic11.Component;

import SwordofMagic11.Custom.CustomLocation;
import SwordofMagic11.Enemy.EnemyData;
import SwordofMagic11.Entity.SomEntity;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;

import java.util.*;

public class SomRay {
    private final List<SomEntity> HitEntity = new ArrayList<>();
    private final HashMap<SomEntity, Boolean> HeadShot = new HashMap<>();
    private Location HitPosition = null;
    private Block HitBlock = null;
    private BlockFace HitBlockFace = null;

    public boolean isHitEntity() {
        return !HitEntity.isEmpty();
    }

    public boolean isHitBlock() {
        return HitBlock != null;
    }

    public boolean isHeadShot() {
        return HeadShot.get(HitEntity.get(0));
    }

    public boolean isHeadShot(SomEntity entity) {
        return HeadShot.get(entity);
    }

    public Block getHitBlock() {
        return HitBlock;
    }

    public BlockFace getHitBlockFace() {
        return HitBlockFace;
    }

    public SomEntity getHitEntity() {
        return HitEntity.size() > 0 ? HitEntity.get(0) : null;
    }

    public List<SomEntity> getHitEntities() {
        return HitEntity;
    }

    public CustomLocation getHitPosition() {
        if (isHitEntity()) return new CustomLocation(HitEntity.get(HitEntity.size() - 1).getEyeLocation());
        return getOriginPosition();
    }

    public CustomLocation getOriginPosition() {
        return new CustomLocation(HitPosition);
    }

    /**
     * BlockのみのRayCast
     *
     * @param loc      始点座標
     * @param distance 光線の長さ
     * @param ignore   貫通するBlockを無視 (基本true)
     */
    public static SomRay rayLocationBlock(CustomLocation loc, double distance, boolean ignore) {
        loc = loc.clone();
        World world = loc.getWorld();
        RayTraceResult rayData = world.rayTraceBlocks(loc, loc.getDirection(), distance, FluidCollisionMode.NEVER, ignore);
        SomRay ray = new SomRay();
        if (rayData == null) {
            ray.HitPosition = loc.add(loc.getDirection().multiply(distance));
        } else {
            ray.HitPosition = rayData.getHitPosition().toLocation(world);
            ray.HitPosition.setDirection(loc.getDirection());
            if (rayData.getHitBlock() != null) {
                ray.HitBlock = rayData.getHitBlock();
                ray.HitBlockFace = rayData.getHitBlockFace();
            }
        }
        return ray;
    }

    /**
     * BlockのみのRayCast
     *
     * @param entity   始点のSomEntity
     * @param distance 光線の長さ
     * @param ignore   貫通するBlockを無視 (基本true)
     */
    public static SomRay rayLocationBlock(SomEntity entity, double distance, boolean ignore) {
        if (entity instanceof EnemyData enemyData) {
            if (enemyData.hasTarget()) {
                CustomLocation location = enemyData.getTarget().getLocation();
                CustomLocation eyeLocation = enemyData.getEyeLocation().clone();
                eyeLocation.setDirection(location.toVector().subtract(eyeLocation.toVector()));
                return rayLocationBlock(eyeLocation, distance, ignore);
            }
        }
        return rayLocationBlock(entity.getEyeLocation(), distance, ignore);
    }

    /**
     * BlockとEntityのRayCast
     *
     * @param from      始点座標
     * @param to        終点座標
     * @param size      光線の太さ
     * @param targets   対象のSomEntity
     * @param penetrate 貫通
     */
    public static SomRay rayLocationEntity(CustomLocation from, CustomLocation to, double size, Collection<SomEntity> targets, boolean penetrate) {
        double distance = from.distance(to);
        from.setDirection(to.toVector().subtract(from.toVector()));
        return rayLocationEntity(from, distance, size, targets, penetrate);
    }

    /**
     * BlockとEntityのRayCast
     *
     * @param loc       始点座標
     * @param distance  光線の長さ
     * @param size      光線の太さ
     * @param targets   対象のSomEntity
     * @param penetrate 貫通
     */
    public static SomRay rayLocationEntity(CustomLocation loc, double distance, double size, Collection<SomEntity> targets, boolean penetrate) {
        World world = loc.getWorld();
        SomRay ray = new SomRay();
        Location lastLocation = rayLocationBlock(loc, distance, true).getOriginPosition();
        distance = loc.distance(lastLocation);
        List<SomEntity> enemyList = new ArrayList<>(targets);
        enemyList.sort(Comparator.comparingDouble(enemy -> enemy.getLocation().distance(loc)));
        for (SomEntity entity : enemyList) {
            BoundingBox box = entity.getEntity().getBoundingBox().expand(size);
            if (entity instanceof EnemyData enemyData) {
                box.expand(enemyData.getMobData().getCollisionSize());
                box.expand(0, enemyData.getMobData().getCollisionSizeY(), 0);
            }
            RayTraceResult rayData = box.rayTrace(loc.toVector(), loc.getDirection(), distance);
            if (rayData != null) {
                ray.HitPosition = rayData.getHitPosition().toLocation(world);
                ray.HitPosition.setDirection(loc.getDirection());
                ray.HitEntity.add(entity);
                ray.HeadShot.put(entity, entity.getEyeLocation().getY() <= ray.HitPosition.getY());
                if (!penetrate) {
                    return ray;
                }
            }
        }
        ray.HitPosition = lastLocation;
        return ray;
    }

    /**
     * BlockとEntityのRayCast
     * ※敵対中のEnemyDataの場合TargetにAutoAim
     *
     * @param entity    始点のSomEntity
     * @param distance  光線の長さ
     * @param size      光線の太さ
     * @param targets   対象のSomEntity
     * @param penetrate 貫通
     */
    public static SomRay rayLocationEntity(SomEntity entity, double distance, double size, Collection<SomEntity> targets, boolean penetrate) {
        if (entity instanceof EnemyData enemyData) {
            if (enemyData.hasTarget()) {
                CustomLocation location = enemyData.getTarget().getLocation();
                CustomLocation eyeLocation = enemyData.getEyeLocation().clone();
                eyeLocation.setDirection(location.toVector().subtract(eyeLocation.toVector()));
                return rayLocationEntity(eyeLocation, distance, size, targets, penetrate);
            }
        }
        return rayLocationEntity(entity.getEyeLocation(), distance, size, targets, penetrate);
    }
}
