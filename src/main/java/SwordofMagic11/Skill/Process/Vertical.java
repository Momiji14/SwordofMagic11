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

public class Vertical extends SomSkill {
    public Vertical(PlayerData owner, SkillData skillData) {
        super(owner, skillData);
    }

    @Override
    public boolean active() {
        Parameter parameter = getParam();
        double damage = parameter.getParam(ParamType.Damage);
        double range = parameter.getParam(ParamType.Range);
        double width = parameter.getParam(ParamType.Width);
        SomParticle particle = new SomParticle(Color.RED, owner);
        particle.rectangle(owner.getLocation(), range, width);
        SomSound.Sword.radius(owner);
        for (SomEntity entity : SearchEntity.rectangleSomEntity(owner.enemies(), owner.getLocation(), range, width)) {
            Damage.makeDamage(owner, entity, Damage.Type.Physics, damage);
        }
        return true;
    }
}
