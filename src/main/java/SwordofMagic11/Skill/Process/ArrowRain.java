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
import org.bukkit.Color;
import org.bukkit.Particle;

public class ArrowRain extends SomSkill {
    public ArrowRain(PlayerData owner, SkillData skillData) {
        super(owner, skillData);
    }

    @Override
    public boolean active() {
        Parameter parameter = getParam();
        double damage = parameter.getParam(ParamType.Damage);
        int count = parameter.getParamInt(ParamType.Count);
        int interval = parameter.getParamInt(ParamType.Interval);
        double range = parameter.getParam(ParamType.Range);
        double radius = parameter.getParam(ParamType.Radius);
        SomParticle particle = new SomParticle(Color.RED, owner);
        SomParticle particle2 = new SomParticle(Particle.CRIT, owner);
        SomRay ray = SomRay.rayLocationBlock(owner, range, true);
        CustomLocation center = ray.getOriginPosition().back(0.1).lower();
        particle2.line(owner.getHandLocation(), center);
        SomSound.Bow.radius(owner);
        SomTask.skillTaskCount(() -> {
            particle.circle(center, radius);
            particle2.circleRain(center, radius, 10);
            SomSound.Bow.radius(center);
            for (SomEntity entity : SearchEntity.nearXZSomEntity(owner.enemies(), center, radius, 10, -3)) {
                Damage.makeDamage(owner, entity, Damage.Type.Shoot, damage);
            }
        }, interval, count, owner);
        return true;
    }
}
