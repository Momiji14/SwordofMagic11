package SwordofMagic11.Component;

import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class CustomVector extends Vector {

    public CustomVector() {
        super();
    }

    public CustomVector(double x, double y, double z) {
        super(x, y, z);
    }

    public Vector to(Location to, boolean horizontal) {
        Vector vector = to.toVector().subtract(this);
        if (horizontal) vector.setY(0);
        return vector;
    }

    public Vector from(Location from, boolean horizontal) {
        Vector vector = this.subtract(from.toVector());
        if (horizontal) vector.setY(0);
        return vector;
    }

    public @NotNull Vector3f toVector3f() {
        return new Vector3f((float) x, (float) y, (float) z);
    }

    public Quaternionf toQuaternion() {
        double yaw=x*(Math.PI/180);
        double roll=y*(Math.PI/180);
        double pitch=z*(Math.PI/180);
        double t0 = Math.cos(yaw * 0.5);
        double t1 = Math.sin(yaw * 0.5);
        double t2 = Math.cos(roll * 0.5);
        double t3 = Math.sin(roll * 0.5);
        double t4 = Math.cos(pitch * 0.5);
        double t5 = Math.sin(pitch * 0.5);
        return new Quaternionf(t0 * t2 * t5 + t1 * t3 * t4,-(t0 * t2 * t4 + t1 * t3 * t5),-(t0 * t3 * t4 - t1 * t2 * t5),-(t1 * t2 * t4 - t0 * t3 * t5));
    }
}
