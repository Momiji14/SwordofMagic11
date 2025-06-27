package SwordofMagic11.Player;

import SwordofMagic11.Component.SomParticle;
import SwordofMagic11.Custom.CustomLocation;
import SwordofMagic11.Entity.SomEntity;
import org.bukkit.Color;
import org.bukkit.Particle;

public enum KillParticle {
    Normal,
    Eva,
    ;

    public void view(SomEntity entity, PlayerData playerData) {
        switch (this) {
            case Normal -> {
                SomParticle particle = new SomParticle(Particle.FIREWORK, playerData);
                particle.setSpeed(0.15f).setRandomVector();
                particle.random(entity.getHipsLocation());
            }
            case Eva -> {
                SomParticle particle = new SomParticle(Particle.FIREWORK, Color.RED, playerData);
                particle.setSpeed(0.15f).setRandomVector();
                particle.line(entity.getLocation(), entity.getLocation().addY(5));
                CustomLocation location = entity.getLocation().addY(3.5).left(1.5);
                particle.line(location, location.right(3));
            }
        }
    }
}
