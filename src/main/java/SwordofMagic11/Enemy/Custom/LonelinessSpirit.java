package SwordofMagic11.Enemy.Custom;

import SwordofMagic11.Component.Function;
import SwordofMagic11.Component.SomParticle;
import SwordofMagic11.Component.SomTask;
import SwordofMagic11.Custom.CustomLocation;
import SwordofMagic11.Enemy.MobData;
import SwordofMagic11.Entity.Damage;
import SwordofMagic11.Entity.SearchEntity;
import SwordofMagic11.Entity.SomEntity;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;

import java.util.Collection;

public class LonelinessSpirit extends CustomData {

    public LonelinessSpirit(MobData mobData, int level, Location location) {
        super(mobData, level, location);
    }

    @Override
    public void tick() {
        if(!hasTimer("Slash")) Slash();
        if(!hasTimer("WithMe") && healthPercent() <= 0.75) WithMe();
        if(!hasTimer("Snuggle") && healthPercent() <= 0.6) Snuggle();
        if(!ToFasterFlg && healthPercent() <= 0.35) ToFaster();

    }

    private int SlashCT = 4;
    private int WithMeCT = 10;
    private int SnuggleCT = 12;
    private boolean ToFasterFlg = false;

    private int SlashRadius = 5;
    public void Slash() {
        SomParticle atkParticle = new SomParticle(Particle.SWEEP_ATTACK, this);

        atkParticle.circle(getLocation(), 5);
        for(SomEntity target : SearchEntity.nearSomEntity(enemies(), getLocation(), SlashRadius)){
            Damage.makeDamage(this, target, Damage.Type.Physics, 6);
        }

        timer("Slash", SlashCT*20);
    }

    private int WithMeRadius = 4;
    public void WithMe() {

        SomParticle preParticle = new SomParticle(Color.RED, this);
        SomParticle atkParticle = new SomParticle(Color.PURPLE, this);

        Collection<SomEntity> enemies = enemies();
        if(!enemies.isEmpty()){
            CustomLocation targetLocation = Function.randomGet(enemies()).getLocation();

            preParticle.circle(targetLocation, WithMeRadius);

            SomTask.asyncDelay(() -> {
                atkParticle.circle(targetLocation, WithMeRadius);
                for(SomEntity target : SearchEntity.nearSomEntity(enemies(), targetLocation, WithMeRadius)){
                    target.teleport(getLocation());
                    target.slow(4, 20);
                }
            }, 20);

            timer("WithMe", WithMeCT*20);
        }

    }

    private int SnuggleDelay = 20;
    public void Snuggle() {

        SomParticle preParticle = new SomParticle(Color.ORANGE, this);

        Collection<SomEntity> enemies = enemies();
        if(!enemies.isEmpty()){
            CustomLocation targetLocation = Function.randomGet(enemies()).getLocation();

            preParticle.circle(targetLocation, 0.5);
            SomTask.asyncDelay(() -> teleport(targetLocation), SnuggleDelay);

            timer("Snuggle", SnuggleCT*20);
        }

    }

    public void ToFaster() {
        SlashCT /= 2;

        WithMeCT /= 2;
        WithMeRadius += 3;

        SnuggleDelay /= 2;
        ToFasterFlg = true;
    }

}
