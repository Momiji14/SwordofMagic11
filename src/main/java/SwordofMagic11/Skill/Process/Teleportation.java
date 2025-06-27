package SwordofMagic11.Skill.Process;

import SwordofMagic11.Component.SomParticle;
import SwordofMagic11.Component.SomRay;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Custom.CustomLocation;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Skill.ParamType;
import SwordofMagic11.Skill.Parameter;
import SwordofMagic11.Skill.SkillData;
import SwordofMagic11.Skill.SomSkill;
import org.bukkit.Particle;

public class Teleportation extends SomSkill {
    public Teleportation(PlayerData owner, SkillData skillData) {
        super(owner, skillData);
    }

    @Override
    public boolean active() {
        Parameter parameter = getParam();
        double range = parameter.getParam(ParamType.Range);
        SomParticle particle = new SomParticle(Particle.WITCH, owner);
        SomRay ray = SomRay.rayLocationBlock(owner.getHipsLocation(), range, true);
        CustomLocation location = ray.getOriginPosition();
        location.add(owner.getDirection().multiply(-0.5));
        particle.line(owner.getHandLocation(), location);
        particle.random(location);
        owner.teleport(location, owner.getDirection(), SomSound.Teleport);
        SomSound.Teleport.radius(owner);
        return true;
    }
}
