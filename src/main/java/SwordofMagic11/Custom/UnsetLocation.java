package SwordofMagic11.Custom;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.util.Vector;

import static SwordofMagic11.Component.Function.scale;

public class UnsetLocation {
    private double x;
    private double y;
    private double z;
    private double yaw = 0.0;
    private double pitch = 0.0;

    public UnsetLocation(double x, double y, double z) {
        x(x);
        y(y);
        z(z);
    }

    public UnsetLocation(double x, double y, double z, float yaw, float pitch) {
        x(x);
        y(y);
        z(z);
        yaw(yaw);
        pitch(pitch);
    }

    public UnsetLocation(Location location) {
        x(location.x());
        y(location.y());
        z(location.z());
        yaw(location.getYaw());
        pitch(location.getPitch());
    }

    public UnsetLocation(FileConfiguration data, String prefix) {
        x(data.getDouble(prefix + ".x"));
        y(data.getDouble(prefix + ".y"));
        z(data.getDouble(prefix + ".z"));
        if (data.isSet(prefix + ".yaw")) {
            yaw(data.getDouble(prefix + ".yaw"));
        }
        if (data.isSet(prefix + ".pitch")) {
            pitch(data.getDouble(prefix + ".pitch"));
        }
    }

    public UnsetLocation(String data) {
        String[] split = data.split(",");
        x(Double.parseDouble(split[0]));
        y(Double.parseDouble(split[1]));
        z(Double.parseDouble(split[2]));
        if (split.length > 4) {
            yaw(Double.parseDouble(split[3]));
            pitch(Double.parseDouble(split[4]));
        }
    }

    public double x() {
        return x;
    }

    public void x(double x) {
        this.x = x;
    }

    public double y() {
        return y;
    }

    public void y(double y) {
        this.y = y;
    }

    public double z() {
        return z;
    }

    public void z(double z) {
        this.z = z;
    }

    public double yaw() {
        return yaw;
    }

    public void yaw(double yaw) {
        this.yaw = yaw;
    }

    public double pitch() {
        return pitch;
    }

    public void pitch(double pitch) {
        this.pitch = pitch;
    }

    public CustomLocation as(World world) {
        return new CustomLocation(world, x, y, z, (float) yaw, (float) pitch);
    }

    public Vector toVector() {
        return new Vector(x, y, z);
    }

    public double distance(Location location) {
        return distance(location.toVector());
    }

    public double distance(UnsetLocation location) {
        return distance(location.toVector());
    }

    public double distance(Vector vector) {
        return toVector().distance(vector);
    }

    @Override
    public String toString() {
        return scale(x, 2) + "," + scale(y, 2) + "," + scale(z, 2) + "," + scale(yaw, 2) + "," + scale(pitch, 2);
    }
}
