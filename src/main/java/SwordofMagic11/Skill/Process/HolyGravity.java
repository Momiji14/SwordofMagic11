package SwordofMagic11.Skill.Process;

import SwordofMagic11.Component.SomParticle;
import SwordofMagic11.Component.SomRay;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Component.SomTask;
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
import org.bukkit.util.Vector;

public class HolyGravity extends SomSkill {
    public HolyGravity(PlayerData owner, SkillData skillData) {
        super(owner, skillData);
    }

    @Override
    public boolean active() {
        Parameter parameter = getParam();
        double range = parameter.getParam(ParamType.Range);
        double radius = parameter.getParam(ParamType.Radius);
        int interval = parameter.getParamInt(ParamType.Interval);
        int count = parameter.getParamInt(ParamType.Count);
        SomParticle particle = new SomParticle(Particle.FIREWORK, owner).setSpeed(1.0f).setVectorDown();
        SomRay ray = SomRay.rayLocationBlock(owner, range, true);
        CustomLocation center = ray.getOriginPosition().addY(0.5);
        center.add(owner.getDirection().multiply(-1));
        center.addY(8);
        particle.line(owner.getHandLocation(), center);
        SomSound.Mace.radius(owner);
        SomTask.skillTaskCount(() -> {
            particle.circle(center, radius);
            for (SomEntity entity : SearchEntity.nearSomEntity(owner.enemies(), center, radius)) {
                double y = 1/(1+center.distance(entity.getLocation())*0.05);
                Vector vector = entity.getVelocity();
                entity.setVelocity(vector.setY(vector.getY()-y));
                if (entity instanceof EnemyData enemyData) enemyData.addHate(owner, 0.01);
            }
        }, interval, count, owner);
        return true;
    }
}
