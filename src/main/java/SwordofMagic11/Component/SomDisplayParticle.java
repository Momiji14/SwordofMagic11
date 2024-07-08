package SwordofMagic11.Component;

import org.bukkit.Location;
import org.bukkit.entity.Display;

import java.util.ArrayList;
import java.util.List;

public abstract class SomDisplayParticle {

    private final List<Animation> animation = new ArrayList<>();
    private Display.Billboard billboard;

    public void setBillboard(Display.Billboard billboard) {
        this.billboard = billboard;
    }

    public void addAnimation(Animation animation) {
        this.animation.add(animation);
    }

    public abstract Display create(Location location);

    public void spawn(Location location) {
        Display object = create(location);
        object.setBillboard(billboard);

        int delay = 0;
        for (Animation animation : animation) {
            SomTask.asyncDelay(() -> {
                object.setInterpolationDuration(animation.time);
                object.setInterpolationDelay(-1);
                if (animation.transformation != null) {
                    object.setTransformation(animation.transformation);
                }
            }, delay);
            delay += animation.time;
        }
        SomTask.syncDelay(object::remove, delay);
    }

    public record Animation(CustomTransformation transformation, int time) {
        public static Animation Delay(int time) {
            return new Animation(null, time);
        }
    }
}
