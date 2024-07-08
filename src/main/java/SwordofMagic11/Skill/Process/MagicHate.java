package SwordofMagic11.Skill.Process;

import SwordofMagic11.Component.SomParticle;
import SwordofMagic11.Component.SomRay;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Enemy.EnemyData;
import SwordofMagic11.Entity.SearchEntity;
import SwordofMagic11.Entity.SomEntity;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Skill.ParamType;
import SwordofMagic11.Skill.Parameter;
import SwordofMagic11.Skill.SkillData;
import SwordofMagic11.Skill.SomSkill;
import SwordofMagic11.StatusType;
import org.bukkit.Particle;

public class MagicHate extends SomSkill {
    public MagicHate(PlayerData owner, SkillData skillData) {
        super(owner, skillData);
    }

    @Override
    public boolean active() {
        Parameter parameter = getParam();
        double range = parameter.getParam(ParamType.Range);
        double radius = parameter.getParam(ParamType.Radius);
        SomParticle particle = new SomParticle(Particle.CRIT_MAGIC, owner);
        SomRay ray = SomRay.rayLocationBlock(owner, range, true);
        particle.line(owner.getHandLocation(), ray.getOriginPosition());
        particle.sphere(ray.getOriginPosition(), radius, 360);
        SomSound.Rod.radius(owner);
        for (SomEntity entity : SearchEntity.nearSomEntity(owner.enemies(), ray.getOriginPosition(), radius)) {
            if (entity instanceof EnemyData enemyData) {
                enemyData.addHate(owner, owner.getStatus(StatusType.MaxMana));
            }
        }
        return true;
    }
}
