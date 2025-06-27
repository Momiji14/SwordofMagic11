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

public class FrostFloor extends SomSkill {
    public FrostFloor(PlayerData owner, SkillData skillData) {
        super(owner, skillData);
    }

    @Override
    public boolean active() {
        Parameter parameter = getParam();
        double damage = parameter.getParam(ParamType.Damage);
        int time = parameter.getParamInt(ParamType.Time);
        int count = parameter.getParamInt(ParamType.Count);
        int interval = parameter.getParamInt(ParamType.Interval);
        double range = parameter.getParam(ParamType.Range);
        double radius = parameter.getParam(ParamType.Radius);
        double percent = parameter.getParam(ParamType.Percent);
        SomParticle particle = new SomParticle(Particle.ITEM_SNOWBALL, owner);
        SomParticle particle2 = new SomParticle(Particle.ITEM_SNOWBALL, owner).setLower(radius);
        SomRay ray = SomRay.rayLocationBlock(owner, range, true);
        CustomLocation center = ray.getOriginPosition().back(0.1).lower();
        particle.line(owner.getHandLocation(), center);
        SomSound.Rod.radius(owner);
        SomTask.skillTaskCount(() -> {
            particle2.circleFill(center, radius);
            for (SomEntity entity : SearchEntity.nearSomEntity(owner.enemies(), center, radius)) {
                if (randomDouble() < percent) {
                    entity.SLOWNESS(2, time);
                }
                Damage.makeDamage(owner, entity, Damage.Type.Magic, damage);
            }
        }, interval, count, owner);
        return true;
    }
}
