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

public class Smite extends SomSkill {
    public Smite(PlayerData owner, SkillData skillData) {
        super(owner, skillData);
    }

    @Override
    public boolean active() {
        Parameter parameter = getParam();
        double damage = parameter.getParam(ParamType.Damage);
        double range = parameter.getParam(ParamType.Range);
        double radius = parameter.getParam(ParamType.Radius);
        SomParticle particle = new SomParticle(Particle.FIREWORK, owner);
        CustomLocation location = SomRay.rayLocationBlock(owner, range, true).getOriginPosition().lower();
        particle.circle(location, radius);
        SomSound.Mace.radius(owner);
        for (SomEntity entity : SearchEntity.nearSomEntity(owner.enemies(), location, radius)) {
            Damage.makeDamage(owner, entity, Damage.Type.Magic, damage);
        }
        return true;
    }
}
