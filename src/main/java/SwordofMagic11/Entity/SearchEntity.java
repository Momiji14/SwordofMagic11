package SwordofMagic11.Entity;

import SwordofMagic11.Custom.CustomLocation;
import SwordofMagic11.Enemy.EnemyData;
import SwordofMagic11.Player.PlayerData;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import static SwordofMagic11.Component.Function.angle;

public interface SearchEntity {

    /**
     * 周囲のPlayerを検索 (距離 )
     *
     * @param location 検索の中心
     * @param distance 検索範囲
     * @return 見つかったPlayer
     */
    static List<PlayerData> nearPlayer(Location location, double distance) {
        return nearPlayer(PlayerData.getPlayerList(location.getWorld()), location, distance);
    }

    /**
     * 周囲のPlayerを検索 (距離 )
     *
     * @param targets  検索対象のPlayer
     * @param location 検索の中心
     * @param distance 検索範囲
     * @return 見つかったPlayer
     */
    static List<PlayerData> nearPlayer(Collection<PlayerData> targets, Location location, double distance) {
        List<PlayerData> list = new ArrayList<>();
        for (PlayerData playerData : targets) {
            if (playerData.getLocation().distance(location) <= distance) {
                list.add(playerData);
            }
        }
        return list;
    }

    /**
     * 周囲のPlayerを検索 (距離 )
     *
     * @param targets  検索対象のPlayer
     * @param location 検索の中心
     * @param distance 検索範囲
     * @return 見つかったPlayer
     */
    static List<PlayerData> nearPlayerXZ(Collection<PlayerData> targets, Location location, double distance) {
        List<PlayerData> list = new ArrayList<>();
        for (PlayerData playerData : targets) {
            if (playerData.getLocation().distanceXZ(location) <= distance) {
                list.add(playerData);
            }
        }
        return list;
    }

    /**
     * 周囲のPlayerを検索 (距離 )
     *
     * @param targets  検索対象のPlayer
     * @param location 検索の中心
     * @param distance 検索範囲
     */
    static boolean isInPlayerXZ(Collection<PlayerData> targets, Location location, double distance) {
        for (PlayerData playerData : targets) {
            if (playerData.getLocation().distanceXZ(location) <= distance) {
                return true;
            }
        }
        return false;
    }

    /**
     * 周囲のPlayerを検索 (距離 )
     *
     * @param location 検索の中心
     * @param distance 検索範囲
     * @return 見つかったPlayer
     */
    static List<PlayerData> nearPlayerNoAFK(Location location, double distance) {
        Collection<PlayerData> players = PlayerData.getPlayerList(location.getWorld());
        players.removeIf(PlayerData::isAFK);
        return nearPlayer(players, location, distance);
    }

    /**
     * 周囲のPlayerを検索 (距離 )
     *
     * @param location 検索の中心
     * @param distance 検索範囲
     * @return 見つかったPlayer
     */
    static List<PlayerData> nearPlayerXZNoAFK(Location location, double distance) {
        Collection<PlayerData> players = PlayerData.getPlayerList(location.getWorld());
        players.removeIf(PlayerData::isAFK);
        return nearPlayerXZ(players, location, distance);
    }

    /**
     * 周囲のPlayerを検索 (距離 )
     *
     * @param location 検索の中心
     * @param distance 検索範囲
     */
    static boolean isInPlayerXZNoAFK(Location location, double distance) {
        Collection<PlayerData> players = PlayerData.getPlayerList(location.getWorld());
        players.removeIf(PlayerData::isAFK);
        return isInPlayerXZ(players, location, distance);
    }

    /**
     * 周囲のSomEntityを検索 (距離 )
     *
     * @param location 検索の中心
     * @param radius   検索範囲
     * @return 見つかったSomEntity
     */
    static List<SomEntity> nearSomEntity(CustomLocation location, double radius) {
        return nearSomEntity(EnemyData.asSomEntities(), location, radius);
    }

    /**
     * 周囲のSomEntityを検索 (距離 )
     *
     * @param targets  検索対象のSomEntity
     * @param location 検索の中心
     * @param radius   検索範囲
     * @return 見つかったSomEntity
     */
    static List<SomEntity> nearSomEntity(Collection<SomEntity> targets, CustomLocation location, double radius) {
        List<SomEntity> list = new ArrayList<>();
        for (SomEntity entity : targets) {
            LivingEntity livingEntity = entity.getEntity();
            if (livingEntity != null && location.getWorld() == livingEntity.getWorld()) {
                Location target = livingEntity.getLocation();
                if (location.distanceXZ(target) <= radius && Math.abs(location.getY() - target.getY()) < radius + 2) {
                    list.add(entity);
                }
            }
        }
        return list;
    }

    /**
     * 周囲のSomEntityを検索 (距離 )
     *
     * @param targets  検索対象のSomEntity
     * @param location 検索の中心
     * @param radius   検索範囲
     * @return 見つかったSomEntity
     */
    static List<SomEntity> nearXZSomEntity(Collection<SomEntity> targets, CustomLocation location, double radius) {
        List<SomEntity> list = new ArrayList<>();
        for (SomEntity entity : targets) {
            LivingEntity livingEntity = entity.getEntity();
            if (livingEntity != null && location.getWorld() == livingEntity.getWorld()) {
                Location target = livingEntity.getLocation();
                if (location.distanceXZ(target) <= radius) {
                    list.add(entity);
                }
            }
        }
        return list;
    }

    /**
     * 周囲のSomEntityを検索 (距離 )
     *
     * @param targets  検索対象のSomEntity
     * @param location 検索の中心
     * @param radius   検索範囲
     * @return 見つかったSomEntity
     */
    static List<SomEntity> nearXZSomEntity(Collection<SomEntity> targets, CustomLocation location, double radius, double radiusY) {
        return nearXZSomEntity(targets, location, radius, radiusY, radiusY);
    }

    /**
     * 周囲のSomEntityを検索 (距離 )
     *
     * @param targets  検索対象のSomEntity
     * @param location 検索の中心
     * @param radius   検索範囲
     * @return 見つかったSomEntity
     */
    static List<SomEntity> nearXZSomEntity(Collection<SomEntity> targets, CustomLocation location, double radius, double radiusUpperY, double radiusLowerY) {
        List<SomEntity> list = new ArrayList<>();
        for (SomEntity entity : targets) {
            LivingEntity livingEntity = entity.getEntity();
            if (livingEntity != null && location.getWorld() == livingEntity.getWorld()) {
                Location target = livingEntity.getLocation();
                if (location.y() + radiusUpperY > target.y() && location.y() + radiusLowerY < target.y()) {
                    if (location.distanceXZ(target) <= radius) {
                        list.add(entity);
                    }
                }
            }
        }
        return list;
    }

    /**
     * 周囲のSomEntityを検索 (距離Min～距離Max )
     *
     * @param targets   検索対象のSomEntity
     * @param location  検索の中心
     * @param minRadius 検索範囲Min
     * @param maxRadius 検索範囲Max
     * @return 見つかったSomEntity
     */
    static List<SomEntity> nearSomEntity(Collection<SomEntity> targets, CustomLocation location, double minRadius, double maxRadius) {
        List<SomEntity> list = nearSomEntity(targets, location, maxRadius);
        list.removeIf(entity -> entity.getLocation().distance(location) < minRadius);
        return list;
    }

    /**
     * 周囲のSomEntityを検索 (平面 ※Yは∞ )
     *
     * @param targets   検索対象のSomEntity
     * @param location  平面の頂点
     * @param location2 平面の頂点
     * @return 見つかったSomEntity
     */
    static List<SomEntity> squareXZSomEntity(Collection<SomEntity> targets, CustomLocation location, CustomLocation location2) {
        double x = Math.min(location.x(), location2.x());
        double x2 = Math.max(location.x(), location2.x());
        double z = Math.min(location.z(), location2.z());
        double z2 = Math.max(location.z(), location2.z());
        List<SomEntity> list = new ArrayList<>();
        for (SomEntity entity : targets) {
            CustomLocation loc = entity.getLocation();
            if (x <= loc.x() && loc.x() <= x2 && z <= loc.z() && loc.z() <= z2) {
                list.add(entity);
            }
        }
        return list;
    }

    /**
     * 周囲のSomEntityを検索 (立方体 )
     *
     * @param targets   検索対象のSomEntity
     * @param location  立方体の頂点
     * @param location2 立方体の頂点
     * @return 見つかったSomEntity
     */
    static List<SomEntity> squareSomEntity(Collection<SomEntity> targets, CustomLocation location, CustomLocation location2) {
        List<SomEntity> list = squareXZSomEntity(targets, location, location2);
        double y = Math.min(location.y(), location2.y());
        double y2 = Math.max(location.y(), location2.y());
        list.removeIf(entity -> y > entity.getLocation().y() || entity.getLocation().y() > y2);
        return list;
    }


    static List<SomEntity> nearestSomEntity(Collection<SomEntity> targets, CustomLocation location, double radius) {
        List<SomEntity> entityList = nearSomEntity(targets, location, radius);
        entityList.sort(Comparator.comparingDouble(enemy -> enemy.getLocation().distance(location)));
        return entityList;
    }

    /**
     * 周囲のSomEntityを検索 (扇型 )
     *
     * @param targets  検索対象のSomEntity
     * @param location 基準位置
     * @param range    長さ
     * @param angle    角度
     * @return 見つかったSomEntity
     */
    static List<SomEntity> fanShapedSomEntity(Collection<SomEntity> targets, CustomLocation location, double range, double angle) {
        List<SomEntity> list = new ArrayList<>();
        for (SomEntity entity : targets) {
            if (location.distanceXZ(entity.getLocation()) < range) {
                Location location2 = entity.getLocation();
                int Angle = angle(location.getDirection());
                int Angle2 = angle(location.toVector(), location2.toVector());
                if (Math.abs(Angle % 360 - Angle2 % 360) < angle / 2) {
                    list.add(entity);
                }
            }
        }
        return list;
    }

    /**
     * 周囲のSomEntityを検索 (長方形 )
     *
     * @param targets  検索対象のSomEntity
     * @param location 基準位置
     * @param range    長さ
     * @param width    横幅
     * @return 見つかったSomEntity
     */
    static List<SomEntity> rectangleSomEntity(Collection<SomEntity> targets, CustomLocation location, double range, double width) {
        final double posX0 = -width / 2;
        final double posX1 = width / 2;
        final double posZ1 = 0;
        double angle = angle(location.getDirection());
        List<SomEntity> list = new ArrayList<>();
        for (SomEntity entity : targets) {
            double distance = location.distanceXZ(entity.getLocation());
            double posAngle = angle(location.toVector(), entity.getLocation().toVector()) - angle;
            double posX = distance * Math.sin(posAngle * (Math.PI / 180));
            double posZ = distance * Math.cos(posAngle * (Math.PI / 180));
            if (posX0 <= posX && posX <= posX1 && posZ1 <= posZ && posZ <= range) {
                list.add(entity);
            }
        }
        return list;
    }

    /**
     * 基準位置から一番遠いSomEntityを取得
     *
     * @param targets  検索対象のSomEntity
     * @param location 基準位置
     * @return
     */
    static SomEntity farthestSomEntity(Collection<SomEntity> targets, CustomLocation location) {
        SomEntity target = null;
        double maxDistance = 0;

        for (SomEntity entity : targets) {
            double distance = location.distance(entity.getLocation());
            if (distance > maxDistance) {
                distance = maxDistance;
                target = entity;
            }
        }
        return target;
    }
}
