package SwordofMagic11.Skill.Process;

import SwordofMagic11.Component.SomParticle;
import SwordofMagic11.Component.SomRay;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Custom.CustomLocation;
import SwordofMagic11.Entity.SearchEntity;
import SwordofMagic11.Entity.SomEntity;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Skill.ParamType;
import SwordofMagic11.Skill.Parameter;
import SwordofMagic11.Skill.SkillData;
import SwordofMagic11.Skill.SomSkill;
import org.bukkit.Particle;

import java.util.Collection;

public class MagicSilence extends SomSkill {
    public MagicSilence(PlayerData owner, SkillData skillData) {
        super(owner, skillData);
    }

    @Override
    public boolean active() {
        Parameter parameter = getParam();
        double range = parameter.getParam(ParamType.Range);
        double radius = parameter.getParam(ParamType.Radius);
        int time = parameter.getParamInt(ParamType.Time);
        SomParticle particle = new SomParticle(Particle.CRIT, owner);
        SomRay ray = SomRay.rayLocationEntity(owner, range, 2, owner.enemies(), false);
        CustomLocation center = ray.getOriginPosition();
        if (ray.isHitEntity()) center.add(owner.getDirection().multiply(radius / 2));
        particle.line(owner.getHandLocation(), ray.getOriginPosition());
        particle.sphere(center, radius);
        SomSound.Rod.radius(owner);
        Collection<SomEntity> enemies = SearchEntity.nearSomEntity(owner.playerEnemies(), center, radius);
        for (SomEntity enemy : enemies) {
            enemy.silence(time, owner);
        }
        return true;
    }
}
