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

import static SwordofMagic11.Component.Function.VectorFromYaw;

public class DaggerAxle extends SomSkill {
    public DaggerAxle(PlayerData owner, SkillData skillData) {
        super(owner, skillData);
    }

    @Override
    public boolean active() {
        Parameter parameter = getParam();
        double damage = parameter.getParam(ParamType.Damage);
        double range = parameter.getParam(ParamType.Range);
        SomParticle particle = new SomParticle(Particle.FIREWORK, owner);
        SomRay ray = SomRay.rayLocationEntity(owner.getHipsLocation().pitch(0), range, 1, owner.enemies(), true);
        particle.line(owner.getHipsLocation(), ray.getOriginPosition(), 1);
        owner.teleport(ray.getOriginPosition().add(owner.getDirection().multiply(-0.5)), VectorFromYaw(owner.yaw()));
        SomSound.Blade.radius(owner);
        for (SomEntity entity : ray.getHitEntities()) {
            Damage.makeDamage(owner, entity, Damage.Type.Physics, damage);
        }
        return true;
    }
}
