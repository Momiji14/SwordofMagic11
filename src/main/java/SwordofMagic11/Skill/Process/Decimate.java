package SwordofMagic11.Skill.Process;

import SwordofMagic11.Component.SomParticle;
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
import org.bukkit.Color;

public class Decimate extends SomSkill {
    public Decimate(PlayerData owner, SkillData skillData) {
        super(owner, skillData);
    }

    @Override
    public boolean active() {
        Parameter parameter = getParam();
        double damage = parameter.getParam(ParamType.Damage);
        double radius = parameter.getParam(ParamType.Radius);
        int time = parameter.getParamInt(ParamType.Time);
        SomParticle particle = new SomParticle(Color.RED, owner);
        CustomLocation pivot = owner.getLocation().frontHorizon(radius/2);
        particle.circle(pivot, radius);
        SomSound.Sword.radius(owner);
        for (SomEntity entity : SearchEntity.nearXZSomEntity(owner.enemies(), pivot, radius)) {
            Damage.makeDamage(owner, entity, Damage.Type.Physics, damage);
            entity.slow(2, time);
        }
        return true;
    }
}
