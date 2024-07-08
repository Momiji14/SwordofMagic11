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
import java.util.Set;

public class Lightning extends SomSkill {
    public Lightning(PlayerData owner, SkillData skillData) {
        super(owner, skillData);
    }
    private final SomParticle particle = new SomParticle(Particle.END_ROD, owner);

    @Override
    public boolean active() {
        Parameter parameter = getParam();
        double damage = parameter.getParam(ParamType.Damage);
        double range = parameter.getParam(ParamType.Range);
        double radius = parameter.getParam(ParamType.Radius);
        SomRay ray = SomRay.rayLocationEntity(owner, range, 0.5, owner.enemies(), false);
        particle.line(owner.getHandLocation(), ray.getOriginPosition());
        if (ray.isHitEntity()) {
            chain(ray.getHitPosition(), damage, radius, new HashSet<>());
        }
        return true;
    }

    public void chain(CustomLocation location, double damage, double radius, Set<SomEntity> hits) {
        Collection<SomEntity> entities = owner.enemies();
        entities.removeAll(hits);
        for (SomEntity entity : SearchEntity.nearestSomEntity(entities, location, radius)) {
            Damage.makeDamage(owner, entity, Damage.Type.Magic, damage);
            hits.add(entity);
            particle.line(location, entity.getHipsLocation());
            SomSound.Hit.play(owner);
            SomTask.asyncDelay(() -> chain(entity.getHipsLocation(), damage, radius, hits), 2);
            break;
        }
    }
}
