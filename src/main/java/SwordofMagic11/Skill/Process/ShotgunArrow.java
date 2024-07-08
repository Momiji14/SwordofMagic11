package SwordofMagic11.Skill.Process;

import SwordofMagic11.Component.SomParticle;
import SwordofMagic11.Component.SomRay;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Custom.CustomLocation;
import SwordofMagic11.Entity.Damage;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Skill.ParamType;
import SwordofMagic11.Skill.Parameter;
import SwordofMagic11.Skill.SkillData;
import SwordofMagic11.Skill.SomSkill;
import org.bukkit.Particle;

import static SwordofMagic11.Component.Function.randomDouble;

public class ShotgunArrow extends SomSkill {
    public ShotgunArrow(PlayerData owner, SkillData skillData) {
        super(owner, skillData);
    }

    @Override
    public boolean active() {
        Parameter parameter = getParam();
        double damage = parameter.getParam(ParamType.Damage);
        double range = parameter.getParam(ParamType.Range);
        double angle = parameter.getParam(ParamType.Angle);
        int count = parameter.getParamInt(ParamType.Count);
        SomParticle particle = new SomParticle(Particle.CRIT, owner);
        for (int i = 0; i < count; i++) {
            CustomLocation pivot = owner.getEyeLocation().addYaw(randomDouble(-angle, angle)).addPitch(randomDouble(-angle, angle));
            SomRay ray = SomRay.rayLocationEntity(pivot, range, 0.25, owner.enemies(), false);
            particle.line(owner.getHandLocation(), ray.getOriginPosition());
            if (ray.isHitEntity()) {
                Damage.makeDamage(owner, ray.getHitEntity(), Damage.Type.Shoot, damage);
            }
            SomSound.Bow.radius(owner);
        }
        return true;
    }
}
