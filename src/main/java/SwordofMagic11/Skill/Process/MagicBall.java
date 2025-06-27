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

public class MagicBall extends SomSkill {
    public MagicBall(PlayerData owner, SkillData skillData) {
        super(owner, skillData);
    }

    @Override
    public boolean active() {
        Parameter parameter = getParam();
        double damage = parameter.getParam(ParamType.Damage);
        double damage2 = parameter.getParam(ParamType.Damage2);
        double range = parameter.getParam(ParamType.Range);
        double radius = parameter.getParam(ParamType.Radius);
        SomParticle particle = new SomParticle(Particle.CRIT, owner);
        SomRay ray = SomRay.rayLocationEntity(owner, range, 0.5, owner.enemies(), false);
        CustomLocation center = ray.getOriginPosition();
        if (ray.isHitEntity()) center.add(owner.getDirection().multiply(radius / 2));
        particle.line(owner.getHandLocation(), ray.getOriginPosition());
        particle.sphere(center, radius);
        SomSound.Rod.radius(owner);
        Collection<SomEntity> enemies = SearchEntity.nearSomEntity(owner.enemies(), center, radius);
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
