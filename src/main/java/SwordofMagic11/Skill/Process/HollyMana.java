package SwordofMagic11.Skill.Process;

import SwordofMagic11.Component.SomParticle;
import SwordofMagic11.Component.SomRay;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Entity.Damage;
import SwordofMagic11.Entity.SomEntity;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Skill.ParamType;
import SwordofMagic11.Skill.Parameter;
import SwordofMagic11.Skill.SkillData;
import SwordofMagic11.Skill.SomSkill;
import org.bukkit.Color;

public class HollyMana extends SomSkill {
    public HollyMana(PlayerData owner, SkillData skillData) {
        super(owner, skillData);
    }

    @Override
    public boolean active() {
        Parameter parameter = getParam();
        double mana = parameter.getParam(ParamType.Heal);
        double range = parameter.getParam(ParamType.Range);
        SomParticle particle = new SomParticle(Color.AQUA, owner).setSpeed(0.2f).setRandomVector();
        SomEntity target = owner;
        SomRay ray = SomRay.rayLocationEntity(owner, range, 1, owner.allies(), false);
        if (ray.isHitEntity()) {
            target = ray.getHitEntity();
            particle.line(owner.getHandLocation(), target.getHipsLocation());
        }
        particle.random(target.getHipsLocation());
        SomSound.Heal.radius(target);
        Damage.makeMana(owner, target, mana);
        return true;
    }
}
