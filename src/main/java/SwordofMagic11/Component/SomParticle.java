package SwordofMagic11.Component;

import SwordofMagic11.Custom.CustomLocation;
import SwordofMagic11.Enemy.EnemyData;
import SwordofMagic11.Entity.SearchEntity;
import SwordofMagic11.Entity.SomEntity;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Player.Setting.PlayerSetting;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.*;

import static SwordofMagic11.Component.Function.VectorFromYaw;
import static SwordofMagic11.Component.Function.VectorFromYawPitch;

public class SomParticle {

    public static final Vector VectorUp = new Vector(0, 1, 0);
    public static final Vector VectorDown = new Vector(0, -1, 0);

    public static Vector VectorUp(double y) {
        return new Vector(0, y, 0);
    }

    private final Particle particle;
    private Particle.DustOptions options;
    private final SomEntity owner;
    private Vector vector = new Vector();
    private VectorType vectorType = VectorType.Normal;
    private float speed = 0f;
    private double lower = 0;
    private double offsetY = 0;
    private final Random random = new Random();

    public enum VectorType {
        Normal,
        Random,
        Shrink,
        Expand,
    }

    /**
     * パーティクル
     *
     * @param particle パーティクルの種類
     * @param owner    パーティクルの所有者
     */
    public SomParticle(Particle particle, SomEntity owner) {
        this.particle = particle;
        this.owner = owner;
    }

    /**
     * パーティクル
     *
     * @param color レッドストーンの色指定
     * @param owner パーティクルの所有者
     */
    public SomParticle(Color color, SomEntity owner) {
        this.particle = Particle.REDSTONE;
        options = new Particle.DustOptions(color, 1);
        this.owner = owner;
    }

    /**
     * パーティクル
     *
     * @param particle パーティクルの種類
     * @param color    色指定
     * @param owner    パーティクルの所有者
     */
    public SomParticle(Particle particle, Color color, SomEntity owner) {
        this.particle = particle;
        options = new Particle.DustOptions(color, 1);
        this.owner = owner;
    }

    /**
     * パーティクルの速度を指定
     *
     * @param speed 速度
     */
    public SomParticle setSpeed(float speed) {
        this.speed = speed;
        return this;
    }

    /**
     * パーティクルの表示位置をLowerにします
     */
    public SomParticle setLower() {
        this.lower = 64;
        return this;
    }

    /**
     * パーティクルの表示位置をLowerにします
     */
    public SomParticle setLower(double max) {
        this.lower = max;
        return this;
    }

    /**
     * パーティクルの表示位置のY軸をオフセットします
     *
     * @param offsetY オフセット Y軸
     */
    public void setOffsetY(double offsetY) {
        this.offsetY = offsetY;
    }

    /**
     * パーティクルのVectorを指定
     */
    public SomParticle setVector(Vector vector) {
        this.vector = vector;
        return this;
    }

    /**
     * パーティクルのVectorを指定
     *
     * @param multiply 倍率
     */
    public SomParticle setVector(Vector vector, double multiply) {
        this.vector = vector.multiply(multiply);
        return this;
    }

    /**
     * パーティクルのVectorTypeをRandomに指定
     */
    public SomParticle setRandomVector() {
        return setVectorType(VectorType.Random);
    }

    /**
     * パーティクルのVectorTypeをShrinkに指定
     * ※基準座標方向にVectorを指定
     */
    public SomParticle setShrink() {
        return setVectorType(VectorType.Shrink);
    }

    /**
     * パーティクルのVectorTypeをExpandに指定
     * ※基準座標方向と反対にVectorを指定
     */
    public SomParticle setExpand() {
        return setVectorType(VectorType.Expand);
    }

    /**
     * パーティクルのVectorTypeを指定
     */
    public SomParticle setVectorType(VectorType vectorType) {
        this.vectorType = vectorType;
        return this;
    }

    /**
     * パーティクルのVectorを上向きに指定
     */
    public SomParticle setVectorUp() {
        this.vector = VectorUp;
        return this;
    }

    /**
     * パーティクルのVectorを下向きに指定
     */
    public SomParticle setVectorDown() {
        this.vector = VectorDown;
        return this;
    }

    /**
     * パーティクルを1つだけ表示
     *
     * @param center 表示する座標
     */
    public void spawn(CustomLocation center) {
        spawn(Collections.singleton(new Vector()), center);
    }

    /**
     * パーティクルを表示
     *
     * @param locations 表示する座標 (基準座標との差)
     * @param center    基準座標
     */
    public void spawn(Collection<Vector> locations, CustomLocation center) {
        spawn(SearchEntity.nearPlayerNoAFK(center, 64), locations, center);
    }

    public record LocationVector(CustomLocation location, Vector vector) {}
    /**
     * パーティクルを表示
     *
     * @param viewers   パーティクルを表示するプレイヤー
     * @param locations 表示する座標 (基準座標との差)
     * @param center    基準座標
     */
    public void spawn(Collection<PlayerData> viewers, Collection<Vector> locations, CustomLocation center) {
        viewers.removeIf(playerData -> playerData.getWorld() != center.getWorld());
        if (viewers.isEmpty()) return;
        List<LocationVector> locationVectors = new ArrayList<>();
        for (Vector locationVector : locations) {
            CustomLocation location = center.clone();
            location.add(locationVector);
            if (lower >= 1) {
                for (int i = 0; i < 4; i++) {
                    if (location.isSolid()) {
                        location.addY(1);
                    } else break;
                }
                location.lower(lower + 4);
            }
            if (offsetY != 0) location.addY(offsetY);

            if (location.isSolid()) {
                continue;
            }
            Vector vector = this.vector;
            switch (vectorType) {
                case Random -> vector = new Vector(random.nextDouble(-1, 1), random.nextDouble(-1, 1), random.nextDouble(-1, 1));
                case Shrink -> vector = center.toVector().subtract(location.toVector());
                case Expand -> vector = location.toVector().subtract(center.toVector()).normalize();
            }
            locationVectors.add(new LocationVector(location, vector));
        }
        for (PlayerData playerData : viewers) {
            if (playerData.getLocation().distance(center) < playerData.setting().get(PlayerSetting.DoubleEnum.ParticleViewDistance)) {
                Particle particle = this.particle;
                Particle.DustOptions options = this.options;
                Player player = playerData.getPlayer();
                double viewPercent;
                if (owner == playerData) {
                    viewPercent = playerData.setting().get(PlayerSetting.DoubleEnum.PlayerParticle);
                } else if (owner instanceof PlayerData) {
                    if (playerData.isEnemy(owner)) {
                        viewPercent = playerData.setting().get(PlayerSetting.DoubleEnum.PlayerEnemyParticle);
                    } else {
                        viewPercent = playerData.setting().get(PlayerSetting.DoubleEnum.OtherParticle);
                    }
                } else if (owner instanceof EnemyData) {
                    viewPercent = playerData.setting().get(PlayerSetting.DoubleEnum.EnemyParticle);
                } else {
                    viewPercent = 1;
                }
                if (viewPercent == 0.0) continue;

                if (playerData.isBE()) {
                    switch (particle) {
                        case FIREWORKS_SPARK -> {
                            particle = Particle.REDSTONE;
                            options = new Particle.DustOptions(Color.WHITE, 1);
                        }
                    }
                }

                for (LocationVector locationVector : locationVectors) {
                    if (viewPercent == 1 || random.nextDouble() < viewPercent) {
                        Vector vector = locationVector.vector;
                        if (options == null) {
                            player.spawnParticle(particle, locationVector.location, 0, vector.getX(), vector.getY(), vector.getZ(), speed);
                        } else {
                            player.spawnParticle(particle, locationVector.location, 0, vector.getX(), vector.getY(), vector.getZ(), speed, options);
                        }
                    }
                }
            }
        }
    }

    /**
     * パーティクルをランダム座標表示 (半径は1)
     *
     * @param center 基準座標
     */
    public void random(CustomLocation center) {
        random(center, 20);
    }

    /**
     * パーティクルをランダム座標表示 (半径は1)
     *
     * @param center  基準座標
     * @param density 密度
     */
    public void random(CustomLocation center, int density) {
        random(center, 1, density);
    }

    /**
     * パーティクルをランダム座標表示
     *
     * @param center 基準座標
     * @param radius 半径
     */
    public void random(CustomLocation center, double radius) {
        random(center, radius, 20);
    }

    /**
     * パーティクルをランダム座標表示
     *
     * @param center  基準座標
     * @param radius  半径
     * @param density 密度
     */
    public void random(CustomLocation center, double radius, int density) {
        Collection<Vector> locations = new ArrayList<>();
        for (int i = 0; i < density; i++) {
            locations.add(new Vector(random.nextDouble(-radius, radius), random.nextDouble(-radius, radius), random.nextDouble(-radius, radius)));
        }
        spawn(locations, center);
    }

    /**
     * パーティクルを球体で表示
     *
     * @param center 基準座標
     * @param radius 半径
     */
    public void sphere(CustomLocation center, double radius) {
        sphere(center, radius, 12 + radius * 6);
    }

    /**
     * パーティクルを球体で表示
     *
     * @param center  基準座標
     * @param radius  半径
     * @param density 密度
     */
    public void sphere(CustomLocation center, double radius, double density) {
        List<Vector> locations = sphereData(radius, density);
        spawn(locations, center);
    }

    public List<Vector> sphereData(double radius, double density) {
        List<Vector> locations = new ArrayList<>();
        density = 1 / density;
        double pi = 2 * Math.PI;
        for (double i = 0; i < pi; i += density) {
            locations.add(VectorFromYawPitch(random.nextFloat() * 360 - 180, random.nextFloat() * 180 - 90).multiply(radius));
        }
        return locations;
    }

    /**
     * パーティクルを半球で表示
     *
     * @param center 基準座標
     * @param radius 半径
     */
    public void sphereHalf(CustomLocation center, double radius) {
        sphereHalf(center, radius, 12 + radius * 6);
    }

    /**
     * パーティクルを半球で表示
     *
     * @param center  基準座標
     * @param radius  半径
     * @param density 密度
     */
    public void sphereHalf(CustomLocation center, double radius, double density) {
        List<Vector> locations = sphereHalfData(radius, density);
        spawn(locations, center);
    }

    public List<Vector> sphereHalfData(double radius, double density) {
        List<Vector> locations = new ArrayList<>();
        density = 1 / density;
        double pi = Math.PI;
        for (double i = 0; i < pi; i += density) {
            locations.add(Function.VectorFromYawPitch(random.nextFloat() * 360 - 180, random.nextFloat() * -90).multiply(radius));
        }
        return locations;
    }

    /**
     * パーティクルを円で表示
     *
     * @param center 基準座標
     * @param radius 半径
     */
    public void circle(CustomLocation center, double radius) {
        circle(center, radius, 12 + radius * 6);
    }

    /**
     * パーティクルを円で表示
     *
     * @param center  基準座標
     * @param radius  半径
     * @param density 密度
     */
    public void circle(CustomLocation center, double radius, double density) {
        List<Vector> locations = circleData(radius, density);
        spawn(locations, center);
    }

    public List<Vector> circleData(double radius, double density) {
        List<Vector> locations = new ArrayList<>();
        density = 1 / density;
        double pi = 2 * Math.PI;
        double offset = random.nextDouble(pi);
        for (double i = 0; i < pi; i += density) {
            double x = Math.cos(i + offset) * radius;
            double z = Math.sin(i + offset) * radius;
            locations.add(new Vector(x, 0, z));
        }
        return locations;
    }

    /**
     * パーティクルを円で内側も表示
     *
     * @param center 基準座標
     * @param radius 半径
     */
    public void circleFill(CustomLocation center, double radius) {
        circleFill(center, radius, 10 + radius * 5);
    }

    /**
     * パーティクルを円で内側も表示
     *
     * @param center  基準座標
     * @param radius  半径
     * @param density 密度
     */
    public void circleFill(CustomLocation center, double radius, double density) {
        List<Vector> locations = circleFillData(radius, density);
        spawn(locations, center);
    }

    public List<Vector> circleFillData(double radius, double density) {
        List<Vector> locations = new ArrayList<>();
        density = 1 / density / (radius / Math.PI);
        double pi = 2 * Math.PI;
        double offset = random.nextDouble(pi);
        for (double i = 0; i < pi; i += density) {
            double x = Math.cos(i + offset) * random.nextDouble(radius);
            double z = Math.sin(i + offset) * random.nextDouble(radius);
            locations.add(new Vector(x, 0, z));
        }
        return locations;
    }

    /**
     * パーティクルをRain系で表示
     *
     * @param center 基準座標
     * @param radius 半径
     * @param y      落下距離
     */
    public void circleRain(CustomLocation center, double radius, double y) {
        for (Vector vector : circleFillData(radius, 0.5)) {
            CustomLocation to = new CustomLocation(center);
            to.add(vector);
            CustomLocation from = to.clone().addY(y);
            line(from, to, 0, 5);
        }
    }

    /**
     * パーティクルを直線で表示
     *
     * @param pivot 基準座標
     * @param range 長さ
     */
    public void line(CustomLocation pivot, double range) {
        line(pivot, range, 0);
    }

    /**
     * パーティクルを直線で表示
     *
     * @param from 始点座標
     * @param to   終点座標
     */
    public void line(CustomLocation from, CustomLocation to) {
        line(from, to, 0);
    }

    /**
     * パーティクルを直線で表示
     *
     * @param from  始点座標
     * @param to    終点座標
     * @param width 太さ
     */
    public void line(CustomLocation from, CustomLocation to, double width) {
        line(from, to, width, 10 + width * 10);
    }

    /**
     * パーティクルを直線で表示
     *
     * @param pivot 基準座標
     * @param range 長さ
     * @param width 太さ
     */
    public void line(CustomLocation pivot, double range, double width) {
        line(pivot, range, width, 10 + width * 10);
    }

    /**
     * パーティクルを直線で表示
     *
     * @param from    始点座標
     * @param to      終点座標
     * @param width   太さ
     * @param density 密度
     */
    public void line(CustomLocation from, CustomLocation to, double width, double density) {
        if (from.getWorld() == to.getWorld()) {
            spawn(lineData(from, to, width, density), from);
        }
    }

    /**
     * パーティクルを直線で表示
     *
     * @param pivot   基準座標
     * @param range   長さ
     * @param width   太さ
     * @param density 密度
     */
    public void line(CustomLocation pivot, double range, double width, double density) {
        spawn(lineData(pivot, range, width, density), pivot);
    }

    public List<Vector> lineData(CustomLocation pivot, double range, double width, double density) {
        return lineData(pivot, pivot.clone().add(pivot.getDirection().multiply(range)), width, density);
    }

    public List<Vector> lineData(CustomLocation from, Location to, double width, double density) {
        density = 1 / density;
        Vector vector = to.toVector().subtract(from.toVector()).normalize().multiply(density);
        double distance = from.distance(to);
        List<Vector> locations = new ArrayList<>();
        Vector location = new Vector();
        for (double i = 0; i < distance; i += density) {
            location.add(vector);
            if (width > 0) {
                locations.add(location.clone().add(Vector.getRandom().multiply(width * random.nextDouble())));
            } else {
                locations.add(location.clone());
            }
        }
        return locations;
    }

    /**
     * パーティクルを扇型で表示
     *
     * @param pivot 基準位置
     * @param range 長さ
     * @param angle 角度
     */
    public void fanShaped(CustomLocation pivot, double range, double angle) {
        fanShaped(pivot, range, angle, 10);
    }

    /**
     * パーティクルを扇型で表示
     *
     * @param pivot   基準位置
     * @param range   長さ
     * @param angle   角度
     * @param density 密度
     */
    public void fanShaped(CustomLocation pivot, double range, double angle, double density) {
        List<Vector> locations = new ArrayList<>();
        density = 1 / density;
        pivot = pivot.clone();
        Vector right = new Vector();
        Vector left = new Vector();
        Vector rightInc = VectorFromYaw(pivot.getYaw() + angle / 2).multiply(density);
        Vector leftInc = VectorFromYaw(pivot.getYaw() - angle / 2).multiply(density);
        for (double i = 0; i < range; i += density) {
            locations.add(right.clone());
            locations.add(left.clone());
            right.add(rightInc);
            left.add(leftInc);
        }
        double yaw = pivot.getYaw() - angle / 2;
        for (double i = 0; i < angle; i += density * Math.PI * 2) {
            locations.add(VectorFromYaw(yaw + i).multiply(range));
        }
        spawn(locations, pivot);
    }

    /**
     * パーティクルを扇型で表示 (内側も)
     *
     * @param pivot   基準位置
     * @param range   長さ
     * @param angle   角度
     * @param density 密度
     */
    public void fanShapedFill(CustomLocation pivot, double range, double angle, double density) {
        List<Vector> locations = new ArrayList<>();
        density = 1 / density;
        pivot = pivot.clone();
        pivot.setPitch(0);
        pivot.setYaw((float) (pivot.getYaw() - angle / 2));
        for (double i = 0; i < angle; i += density) {
            pivot.setYaw((float) (pivot.getYaw() + density));
            locations.addAll(lineData(pivot, range, 0, density));
        }
        spawn(locations, pivot);
    }

    /**
     * パーティクルを長方形で表示
     *
     * @param pivot 基準位置
     * @param range 長さ
     * @param width 横幅
     */
    public void rectangle(CustomLocation pivot, double range, double width) {
        rectangle(pivot, range, width, 10);
    }

    /**
     * パーティクルを長方形で表示
     *
     * @param pivot   基準位置
     * @param range   長さ
     * @param width   横幅
     * @param density 密度
     */
    public void rectangle(CustomLocation pivot, double range, double width, double density) {
        List<Vector> locations = new ArrayList<>();
        density = 1 / density;
        Vector rangeVector = VectorFromYaw(pivot.getYaw());
        Vector widthVector = VectorFromYaw(pivot.getYaw() + 90);
        for (double i = 0; i < range; i += density) {
            locations.add(widthVector.clone().multiply(width / 2).add(rangeVector.clone().multiply(i)));
            locations.add(widthVector.clone().multiply(-width / 2).add(rangeVector.clone().multiply(i)));
        }
        Vector rangedVector = rangeVector.clone().multiply(range);
        for (double i = -width / 2; i < width / 2; i += density) {
            locations.add(widthVector.clone().multiply(i));
            locations.add(widthVector.clone().multiply(i).add(rangedVector));
        }
        spawn(locations, pivot);
    }

    /**
     * パーティクルを四角形で表示
     *
     * @param location  始点座標
     * @param location2 終点座標
     * @param width     太さ
     */
    public void square(CustomLocation location, CustomLocation location2, double width) {
        square(location, location2, width, 10);
    }

    /**
     * パーティクルを四角形で表示
     *
     * @param location  始点座標
     * @param location2 終点座標
     * @param width     太さ
     * @param density   密度
     */
    public void square(CustomLocation location, CustomLocation location2, double width, double density) {
        double x = Math.min(location.x(), location2.x());
        double x2 = Math.max(location.x(), location2.x());
        double y = Math.min(location.y(), location2.y());
        double y2 = Math.max(location.y(), location2.y());
        double yA = (y + y2) / 2;
        double z = Math.min(location.z(), location2.z());
        double z2 = Math.max(location.z(), location2.z());
        World world = location.getWorld();
        CustomLocation locA = new CustomLocation(world, x, y, z);
        CustomLocation locB = new CustomLocation(world, x2, yA, z);
        CustomLocation locC = new CustomLocation(world, x2, y2, z2);
        CustomLocation locD = new CustomLocation(world, x, yA, z2);
        line(locA, locB, width, density);
        line(locB, locC, width, density);
        line(locC, locD, width, density);
        line(locD, locA, width, density);
    }
}
