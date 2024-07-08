package SwordofMagic11.Skill.Process;

import SwordofMagic11.Component.SomParticle;
import SwordofMagic11.Component.SomRay;
import SwordofMagic11.Component.SomSound;
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

import java.util.Collection;

import static SwordofMagic11.Component.Function.randomDouble;

public class IceBall extends SomSkill {
    public IceBall(PlayerData owner, SkillData skillData) {
        super(owner, skillData);
    }

    @Override
    public boolean active() {
        Parameter parameter = getParam();
        double damage = parameter.getParam(ParamType.Damage);
        double damage2 = parameter.getParam(ParamType.Damage2);
        int time = parameter.getParamInt(ParamType.Time);
        double range = parameter.getParam(ParamType.Range);
        double radius = parameter.getParam(ParamType.Radius);
        double percent = parameter.getParam(ParamType.Percent);
        SomParticle particle = new SomParticle(Particle.SNOWBALL, owner);
        SomRay ray = SomRay.rayLocationEntity(owner, range, 0.5, owner.enemies(), false);
        CustomLocation center = ray.getOriginPosition();
        if (ray.isHitEntity()) center.add(owner.getDirection().multiply(radius / 2));
        particle.line(owner.getHandLocation(), center);
        particle.sphere(center, radius);
        SomSound.Ice.radius(owner);
        Collection<SomEntity> enemies = SearchEntity.nearSomEntity(owner.enemies(), center, radius);
        for (SomEntity enemy : enemies) {
            if (randomDouble() < percent) {
                enemy.slow(2, time);
            }
        }
        if (ray.isHitEntity()) {
            Damage.makeDamage(owner, ray.getHitEntity(), Damage.Type.Magic, damage);
            enemies.remove(ray.getHitEntity());
        }
        for (SomEntity enemy : enemies) {
            Damage.makeDamage(owner, enemy, Damage.Type.Magic, damage2);
        }
        return true;
    }
}
