package SwordofMagic11.Component;

import org.bukkit.Location;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

public class CustomTransformation extends Transformation {

    public CustomTransformation() {
        super(new Vector3f(), new AxisAngle4f(), new Vector3f(1f, 1f, 1f), new AxisAngle4f());
    }

    public CustomTransformation setOffset(double x, double y, double z) {
        getTranslation().set(x, y, z);
        return this;
    }

    public CustomTransformation setOffset(Vector vector) {
        setOffset(vector.getX(), vector.getY(), vector.getZ());
        return this;
    }

    public CustomTransformation setScale(double x, double y, double z) {
        getScale().set(x, y, z);
        return this;
    }

    public CustomTransformation setScale(Vector vector) {
        getScale().set(vector.toVector3f());
        return this;
    }

    public CustomTransformation setLeftRotation(double x, double y, double z) {
        getLeftRotation().set(new CustomVector(x, y, z).toQuaternion());
        return this;
    }

    public CustomTransformation setLeftRotation(Location location, double angle) {
        setLeftRotation(location.getYaw(), location.getPitch(), angle);
        return this;
    }

    public CustomTransformation setLeftRotation(Location location) {
        setLeftRotation(location, 0);
        return this;
    }

    public CustomTransformation setLeftRotation(Vector vector) {
        return setLeftRotation(vector.getX(), vector.getY(), vector.getZ());
    }

    public CustomTransformation setRightRotation(double x, double y, double z) {
        getRightRotation().set(new CustomVector(x, y, z).toQuaternion());
        return this;
    }

    public CustomTransformation setRightRotation(Vector vector) {
        return setRightRotation(vector.getX(), vector.getY(), vector.getZ());
    }


    @Override
    public CustomTransformation clone() {
        CustomTransformation clone = new CustomTransformation();
        clone.getTranslation().set(getTranslation());
        clone.getScale().set(getScale());
        clone.getLeftRotation().set(getLeftRotation());
        clone.getRightRotation().set(getRightRotation());
        return clone;
    }
}
