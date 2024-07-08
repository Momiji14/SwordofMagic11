package SwordofMagic11.Skill.Process;

import SwordofMagic11.Component.SomParticle;
import SwordofMagic11.Component.SomRay;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Component.SomTask;
import SwordofMagic11.Custom.CustomLocation;
import SwordofMagic11.Entity.Damage;
import SwordofMagic11.Entity.SearchEntity;
import SwordofMagic11.Entity.SomEntity;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Skill.ParamType;
import SwordofMagic11.Skill.Parameter;
import SwordofMagic11.Skill.SkillData;
import SwordofMagic11.Skill.SomSkill;
import org.bukkit.Particle;

import static SwordofMagic11.Component.Function.randomDouble;

public class FlameFloor extends SomSkill {
    public FlameFloor(PlayerData owner, SkillData skillData) {
        super(owner, skillData);
    }

    @Override
    public boolean active() {
        Parameter parameter = getParam();
        double damage = parameter.getParam(ParamType.Damage);
        double damage2 = parameter.getParam(ParamType.Damage2);
        int count = parameter.getParamInt(ParamType.Count);
        int interval = parameter.getParamInt(ParamType.Interval);
        double range = parameter.getParam(ParamType.Range);
        double radius = parameter.getParam(ParamType.Radius);
        double percent = parameter.getParam(ParamType.Percent);
        SomParticle particle = new SomParticle(Particle.FLAME, owner);
        SomParticle particle2 = new SomParticle(Particle.FLAME, owner).setLower(radius);
        SomParticle particle3 = new SomParticle(Particle.EXPLOSION_NORMAL, owner).setLower(radius);
        SomRay ray = SomRay.rayLocationBlock(owner, range, true);
        CustomLocation center = ray.getOriginPosition().back(0.1).lower();
        particle.line(owner.getHandLocation(), center);
        SomSound.Fire.radius(owner);
        SomTask.skillTaskCount(() -> {
            particle2.circleFill(center, radius);
            double finalDamage;
            if (randomDouble() < percent) {
                SomSound.Explode.radius(center);
                particle3.circleFill(center, radius, 10 + radius * 2);
                finalDamage = damage2;
            } else {
                finalDamage = damage;
            }
            for (SomEntity entity : SearchEntity.nearSomEntity(owner.enemies(), center, radius)) {
                Damage.makeDamage(owner, entity, Damage.Type.Magic, finalDamage);
            }
        }, interval, count, owner);
        return true;
    }
}
