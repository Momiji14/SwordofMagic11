package SwordofMagic11.Skill.Process;

import SwordofMagic11.Component.SomParticle;
import SwordofMagic11.Component.SomRay;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Custom.CustomLocation;
import SwordofMagic11.Entity.SomEntity;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Skill.ParamType;
import SwordofMagic11.Skill.Parameter;
import SwordofMagic11.Skill.SkillData;
import SwordofMagic11.Skill.SomSkill;
import org.bukkit.Particle;

public class SlaveChain extends SomSkill {
    public SlaveChain(PlayerData owner, SkillData skillData) {
        super(owner, skillData);
    }

    @Override
    public boolean active() {
        Parameter parameter = getParam();
        double range = parameter.getParam(ParamType.Range);
        SomParticle particle = new SomParticle(Particle.CRIT, owner);
        SomRay ray = SomRay.rayLocationEntity(owner, range, 1, owner.enemies(), true);
        CustomLocation location = SomRay.rayLocationBlock(owner.getEyeLocation().pitch(0), 2, true).getOriginPosition().back(0.2);
        particle.line(owner.getHandLocation(), ray.getOriginPosition());
        SomSound.Bow.radius(owner);
        for (SomEntity entity : ray.getHitEntities()) {
            entity.teleport(location);
        }
        return true;
    }
}
