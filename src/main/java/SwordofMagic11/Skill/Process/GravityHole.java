package SwordofMagic11.Skill.Process;

import SwordofMagic11.Component.SomParticle;
import SwordofMagic11.Component.SomRay;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Component.SomTask;
import SwordofMagic11.Custom.CustomLocation;
import SwordofMagic11.Enemy.EnemyData;
import SwordofMagic11.Entity.SearchEntity;
import SwordofMagic11.Entity.SomEffect;
import SwordofMagic11.Entity.SomEntity;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Skill.ParamType;
import SwordofMagic11.Skill.Parameter;
import SwordofMagic11.Skill.SkillData;
import SwordofMagic11.Skill.SomSkill;
import SwordofMagic11.StatusType;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

import static SwordofMagic11.Component.SomParticle.VectorUp;

public class GravityHole extends SomSkill {
    public GravityHole(PlayerData owner, SkillData skillData) {
        super(owner, skillData);
    }

    @Override
    public boolean active() {
        Parameter parameter = getParam();
        double range = parameter.getParam(ParamType.Range);
        double radius = parameter.getParam(ParamType.Radius);
        double value = parameter.getParam(ParamType.Value);
        int interval = parameter.getParamInt(ParamType.Interval);
        int count = parameter.getParamInt(ParamType.Count);
        int time = parameter.getParamInt(ParamType.Time);
        SomParticle particle = new SomParticle(Particle.SPELL_WITCH, owner).setSpeed(0.35f);
        SomRay ray = SomRay.rayLocationBlock(owner, range, true);
        CustomLocation center = ray.getOriginPosition().addY(0.5);
        center.add(owner.getDirection().multiply(-1));
        particle.line(owner.getHandLocation(), center);
        SomSound.Rod.radius(owner);
        SomEffect effect = new SomEffect(this, time);
        effect.setStatusMultiply(StatusType.HealthRegen, value);
        effect.setStatusMultiply(StatusType.ManaRegen, value);
        effect.setLog(false);
        SomTask.skillTaskCount(() -> {
            particle.sphere(center, radius);
            for (SomEntity entity : SearchEntity.nearSomEntity(owner.enemies(), center, radius)) {
                Vector vector = center.toVector().subtract(entity.getLocation().toVector()).add(VectorUp).normalize().multiply(0.3);
                entity.setVelocity(entity.getVelocity().add(vector));
                entity.addEffect(effect);
                if (entity instanceof EnemyData enemyData) enemyData.addHate(owner, 0.01);
            }
        }, interval, count, owner);
        return true;
    }
}
