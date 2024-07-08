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

import java.util.Collection;
import java.util.HashSet;

import static SwordofMagic11.Component.Function.randomDouble;

public class FireBall extends SomSkill {
    public FireBall(PlayerData owner, SkillData skillData) {
        super(owner, skillData);
    }

    @Override
    public boolean active() {
        Parameter parameter = getParam();
        double damage = parameter.getParam(ParamType.Damage);
        double damage2 = parameter.getParam(ParamType.Damage2);
        double damage3 = parameter.getParam(ParamType.Damage3);
        int count = parameter.getParamInt(ParamType.Count);
        int interval = parameter.getParamInt(ParamType.Interval);
        double range = parameter.getParam(ParamType.Range);
        double radius = parameter.getParam(ParamType.Radius);
        double percent = parameter.getParam(ParamType.Percent);
        SomParticle particle = new SomParticle(Particle.FLAME, owner);
        SomRay ray = SomRay.rayLocationEntity(owner, range, 0.5, owner.enemies(), false);
        CustomLocation center = ray.getOriginPosition();
        if (ray.isHitEntity()) center.add(owner.getDirection().multiply(radius / 2));
        particle.line(owner.getHandLocation(), center);
        particle.sphere(center, radius);
        SomSound.Fire.radius(owner);
        Collection<SomEntity> enemies = SearchEntity.nearSomEntity(owner.enemies(), center, radius);
        Collection<SomEntity> fired = new HashSet<>();
        for (SomEntity enemy : enemies) {
            if (randomDouble() < percent) {
                fired.add(enemy);
            }
        }
        if (ray.isHitEntity()) {
            Damage.makeDamage(owner, ray.getHitEntity(), Damage.Type.Magic, damage);
            enemies.remove(ray.getHitEntity());
        }
        for (SomEntity enemy : enemies) {
            Damage.makeDamage(owner, enemy, Damage.Type.Magic, damage2);
        }
        SomTask.skillTaskCount(() -> {
            for (SomEntity enemy : fired) {
                Damage.makeDamage(owner, enemy, Damage.Type.Magic, damage3);
            }
        }, interval, count, owner);
        return true;
    }
}
