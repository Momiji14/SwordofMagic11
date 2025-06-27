package SwordofMagic11.Skill.Process;

import SwordofMagic11.Component.SomParticle;
import SwordofMagic11.Component.SomRay;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Custom.CustomLocation;
import SwordofMagic11.Enemy.EnemyData;
import SwordofMagic11.Entity.SearchEntity;
import SwordofMagic11.Entity.SomEntity;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Skill.ParamType;
import SwordofMagic11.Skill.Parameter;
import SwordofMagic11.Skill.SkillData;
import SwordofMagic11.Skill.SomSkill;
import org.bukkit.Particle;

public class ChainHook extends SomSkill {
    public ChainHook(PlayerData owner, SkillData skillData) {
        super(owner, skillData);
    }

    @Override
    public boolean active() {
        Parameter parameter = getParam();
        double range = parameter.getParam(ParamType.Range);
        double radius = parameter.getParam(ParamType.Radius);
        int time = parameter.getParamInt(ParamType.Time);
        SomParticle particle = new SomParticle(Particle.CRIT, owner).setSpeed(0.35f);
        SomRay ray = SomRay.rayLocationBlock(owner, range, true);
        CustomLocation center = ray.getOriginPosition();
        center.add(owner.getDirection().multiply(-1));
        particle.line(owner.getHandLocation(), center);
        SomSound.Bow.radius(owner);
        for (SomEntity entity : SearchEntity.nearSomEntity(owner.enemies(), center, radius)) {
            particle.line(center, entity.getHipsLocation());
            entity.teleport(center.clone().setDirection(entity.getDirection()));
            entity.SLOWNESS(2, time);
            if (entity instanceof EnemyData enemyData) enemyData.addHate(owner, 0.01);
        }
        return true;
    }
}
