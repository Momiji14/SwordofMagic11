package SwordofMagic11.Custom;

import SwordofMagic11.Component.SomRay;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import static SwordofMagic11.Component.Function.VectorFromYaw;
import static SwordofMagic11.Component.Function.randomDouble;

public class CustomLocation extends Location implements Cloneable {
    public CustomLocation(World world, double x, double y, double z) {
        super(world, x, y, z);
    }

    public CustomLocation(World world, double x, double y, double z, float yaw, float pitch) {
        super(world, x, y, z, yaw, pitch);
    }

    public CustomLocation(Location location) {
        super(location.getWorld(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

    public CustomLocation(World world, Vector vector) {
        super(world, vector.getX(), vector.getY(), vector.getZ());
    }

    public void setLocation(Location location) {
        setWorld(location.getWorld());
        setX(location.x());
        setY(location.y());
        setZ(location.z());
        setYaw(location.getYaw());
        setPitch(location.getPitch());
    }

    @Override
    public double distance(@NotNull Location location) {
        double x = x() - location.x();
        double y = y() - location.y();
        double z = z() - location.z();
        return Math.sqrt((x * x) + (y * y) + (z * z));
    }

    public double distanceXZ(Location location) {
        double x = x() - location.x();
        double z = z() - location.z();
        return Math.sqrt((x * x) + (z * z));
    }

    public CustomLocation addY(double y) {
        add(0, y, 0);
        return this;
    }

    public CustomLocation addXZ(double x, double z) {
        add(x, 0, z);
        return this;
    }

    public CustomLocation left(double value) {
        add(VectorFromYaw(getYaw() - 90).multiply(value));
        return this;
    }

    public CustomLocation right(double value) {
        add(VectorFromYaw(getYaw() + 90).multiply(value));
        return this;
    }

    public CustomLocation front(double value) {
        add(getDirection().multiply(value));
        return this;
    }

    public CustomLocation frontHorizon(double value) {
        add(VectorFromYaw(getYaw()).multiply(value));
        return this;
    }

    public CustomLocation back(double value) {
        add(getDirection().multiply(-value));
        return this;
    }
    public CustomLocation pitch(double pitch) {
        setPitch((float) pitch);
        return this;
    }

    public CustomLocation yaw(double yaw) {
        setYaw((float) yaw);
        return this;
    }

    public CustomLocation addPitch(double pitch) {
        setPitch((float) (getPitch() + pitch));
        return this;
    }

    public CustomLocation addYaw(double yaw) {
        setYaw((float) (getYaw() + yaw));
        return this;
    }

    public CustomLocation lower() {
        return lower(128);
    }

    public CustomLocation lower(double y) {
        CustomLocation loc = this.clone().addY(1);
        loc.setPitch(90);
        SomRay somRay = SomRay.rayLocationBlock(loc, y, true);
        setLocation(somRay.getHitPosition().addY(0.1));
        return this;
    }

    public CustomLocation look(Location location) {
        setDirection(location.toVector().subtract(toVector()));
        return this;
    }

    public CustomLocation randomRadiusLocation(double radius) {
        double x;
        double z;
        while (true) {
            x = randomDouble(-1, 1);
            z = randomDouble(-1, 1);
            double sqr = (x * x) + (z * z);
            if (sqr < 1) break;
        }
        return new CustomLocation(this).addXZ(x * radius, z * radius);
    }

    public CustomLocation randomSquareLocation(double length) {
        return new CustomLocation(this).addXZ(randomDouble(0, length), randomDouble(0, length));
    }

    public CustomLocation randomSquareLocation(CustomLocation location){
        double x;
        double z;

        if(this.x() == location.x()) x = this.x();
        else x = randomDouble(Math.min(location.x(), this.x()), Math.max(location.x(), this.x()));

        if(this.z() == location.z()) z = this.z();
        else z = randomDouble(Math.min(location.z(), this.z()), Math.max(location.z(), this.z()));

        return new CustomLocation(this.getWorld(), x, this.y(), z);
    }

    @Override
    public boolean isBlock() {
        return getBlock().getType().isBlock();
    }

    public boolean isSolid() {
        return getBlock().getType().isSolid();
    }

    public boolean isSpawnAble() {
        return !isSolid() && clone().addY(-1).isSolid();
    }

    public UnsetLocation asUnsetLocation() {
        return new UnsetLocation(getX(), getY(), getZ(), getYaw(), getPitch());
    }

    @Override
    public @NotNull CustomLocation clone() {
        return (CustomLocation) super.clone();
    }
}
