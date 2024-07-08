package SwordofMagic11.Skill.Process;

import SwordofMagic11.Component.SomParticle;
import SwordofMagic11.Component.SomRay;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Component.SomTask;
import SwordofMagic11.Entity.Damage;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Skill.ParamType;
import SwordofMagic11.Skill.Parameter;
import SwordofMagic11.Skill.SkillData;
import SwordofMagic11.Skill.SomSkill;
import org.bukkit.Particle;

public class Icicle extends SomSkill {
    public Icicle(PlayerData owner, SkillData skillData) {
        super(owner, skillData);
    }

    @Override
    public boolean active() {
        Parameter parameter = getParam();
        double damage = parameter.getParam(ParamType.Damage);
        double range = parameter.getParam(ParamType.Range);
        int count = parameter.getParamInt(ParamType.Count);
        int interval = parameter.getParamInt(ParamType.Interval);
        SomParticle particle = new SomParticle(Particle.FIREWORKS_SPARK, owner);
        SomTask.skillTaskCount(() -> {
            SomRay ray = SomRay.rayLocationEntity(owner, range, 0.5, owner.enemies(), false);
            particle.line(owner.getHandLocation(), ray.getOriginPosition());
            if (ray.isHitEntity()) {
                particle.random(ray.getHitEntity().getHipsLocation());
                Damage.makeDamage(owner, ray.getHitEntity(), Damage.Type.Magic, damage);
            } else {
                particle.random(ray.getOriginPosition());
            }
            SomSound.Glass.radius(owner);
        }, interval, count, owner);
        return true;
    }
}
