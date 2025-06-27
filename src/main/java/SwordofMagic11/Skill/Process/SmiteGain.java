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

public class SmiteGain extends SomSkill {
    public SmiteGain(PlayerData owner, SkillData skillData) {
        super(owner, skillData);
    }

    @Override
    public boolean active() {
        Parameter parameter = getParam();
        double damage = parameter.getParam(ParamType.Damage);
        double damage2 = parameter.getParam(ParamType.Damage2);
        double range = parameter.getParam(ParamType.Range);
        double radius = parameter.getParam(ParamType.Radius);
        double radius2 = parameter.getParam(ParamType.Radius2) + radius;
        SomParticle particle = new SomParticle(Particle.FIREWORK, owner);
        CustomLocation location = SomRay.rayLocationBlock(owner, range, true).getOriginPosition().lower();
        particle.circle(location, radius);
        particle.circle(location, radius2);
        SomSound.Mace.radius(owner);
        Collection<SomEntity> in = SearchEntity.nearSomEntity(owner.enemies(), location, radius);
        Collection<SomEntity> out = SearchEntity.nearSomEntity(owner.enemies(), location, radius2);
        out.removeAll(in);
        for (SomEntity entity : in) {
            Damage.makeDamage(owner, entity, Damage.Type.Magic, damage);
        }
        for (SomEntity entity : out) {
            Damage.makeDamage(owner, entity, Damage.Type.Magic, damage2);
        }
        return true;
    }
}
