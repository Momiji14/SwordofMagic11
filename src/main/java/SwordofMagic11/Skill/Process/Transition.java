package SwordofMagic11.Skill.Process;

import SwordofMagic11.Component.SomParticle;
import SwordofMagic11.Component.SomRay;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Component.SomTask;
import SwordofMagic11.Custom.CustomLocation;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Skill.ParamType;
import SwordofMagic11.Skill.Parameter;
import SwordofMagic11.Skill.SkillData;
import SwordofMagic11.Skill.SomSkill;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;

public class Transition extends SomSkill {
    public Transition(PlayerData owner, SkillData skillData) {
        super(owner, skillData);
    }

    @Override
    public boolean active() {
        SomParticle particle = new SomParticle(Particle.FIREWORK, owner).setSpeed(0.35f);
        SomSound.LowHeal.radius(owner);
        if (owner.hasSkill("TransitionTeleport")) {
            Parameter parameter = owner.getSkillParam("TransitionTeleport");
            double range = parameter.getParam(ParamType.Range);
            SomRay ray = SomRay.rayLocationBlock(owner.getHipsLocation(), range, true);
            CustomLocation location = ray.getOriginPosition();
            location.add(owner.getDirection().multiply(-0.5));
            particle.line(owner.getHandLocation(), location);
            particle.random(location);
            owner.teleport(location, owner.getDirection(), SomSound.Teleport);
            SomSound.Teleport.radius(owner);
        } else {
            Entity entity = owner.getEntity();
            SomTask.skillTaskCount(() -> {
                particle.setVector(owner.getDirection());
                particle.random(owner.getHipsLocation(), 5);
                entity.setVelocity(owner.getDirection().multiply(1.35));
            }, 1, 15, owner);
        }
        return true;
    }
}
