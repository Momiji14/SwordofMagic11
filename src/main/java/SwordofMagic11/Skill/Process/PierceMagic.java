package SwordofMagic11.Skill.Process;

import SwordofMagic11.Component.SomParticle;
import SwordofMagic11.Component.SomRay;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Entity.Damage;
import SwordofMagic11.Entity.SomEntity;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Skill.ParamType;
import SwordofMagic11.Skill.Parameter;
import SwordofMagic11.Skill.SkillData;
import SwordofMagic11.Skill.SomSkill;
import org.bukkit.Particle;

public class PierceMagic extends SomSkill {
    public PierceMagic(PlayerData owner, SkillData skillData) {
        super(owner, skillData);
    }

    @Override
    public boolean active() {
        Parameter parameter = getParam();
        double damage = parameter.getParam(ParamType.Damage);
        double range = parameter.getParam(ParamType.Range);
        double radius = parameter.getParam(ParamType.Radius);
        SomParticle particle = new SomParticle(Particle.CRIT, owner);
        SomRay ray = SomRay.rayLocationEntity(owner, range, radius, owner.enemies(), true);
        particle.line(owner.getHandLocation(), ray.getOriginPosition());
        SomSound.Rod.radius(owner);
        for (SomEntity entity : ray.getHitEntities()) {
            Damage.makeDamage(owner, entity, Damage.Type.Magic, damage);
        }
        return true;
    }
}
