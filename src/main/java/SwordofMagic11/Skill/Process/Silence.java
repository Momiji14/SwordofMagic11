package SwordofMagic11.Skill.Process;

import SwordofMagic11.Component.SomParticle;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Entity.Damage;
import SwordofMagic11.Entity.SearchEntity;
import SwordofMagic11.Entity.SomEntity;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Skill.ParamType;
import SwordofMagic11.Skill.Parameter;
import SwordofMagic11.Skill.SkillData;
import SwordofMagic11.Skill.SomSkill;
import org.bukkit.Color;

public class Silence extends SomSkill {
    public Silence(PlayerData owner, SkillData skillData) {
        super(owner, skillData);
    }

    @Override
    public boolean active() {
        Parameter parameter = getParam();
        double damage = parameter.getParam(ParamType.Damage);
        double range = parameter.getParam(ParamType.Range);
        double angle = parameter.getParam(ParamType.Angle);
        int time = parameter.getParamInt(ParamType.Time);
        SomParticle particle = new SomParticle(Color.RED, owner);
        particle.fanShaped(owner.getLocation(), range, angle);
        SomSound.Sword.radius(owner);
        for (SomEntity entity : SearchEntity.fanShapedSomEntity(owner.playerEnemies(), owner.getLocation(), range, angle)) {
            Damage.makeDamage(owner, entity, Damage.Type.Physics, damage);
            entity.silence(time, owner);
        }
        return true;
    }
}
