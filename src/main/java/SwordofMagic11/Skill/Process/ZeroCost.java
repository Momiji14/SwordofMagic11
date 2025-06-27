package SwordofMagic11.Skill.Process;

import SwordofMagic11.Component.SomParticle;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Entity.SomEffect;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Skill.ParamType;
import SwordofMagic11.Skill.Parameter;
import SwordofMagic11.Skill.SkillData;
import SwordofMagic11.Skill.SomSkill;
import SwordofMagic11.StatusType;
import org.bukkit.Particle;

public class ZeroCost extends SomSkill {
    public ZeroCost(PlayerData owner, SkillData skillData) {
        super(owner, skillData);
    }

    @Override
    public boolean active() {
        Parameter parameter = getParam();
        int time = parameter.getParamInt(ParamType.Time);
        SomParticle particle = new SomParticle(Particle.ENCHANT, owner).setSpeed(0.35f);
        particle.sphere(owner.getHipsLocation(), 1);
        SomSound.LowHeal.radius(owner);
        SomEffect effect = new SomEffect(this, time);
        if (owner.hasSkill("ZeroCostExtend")) {
            Parameter parameter2 = owner.getSkillParam("ZeroCostExtend");
            double value = parameter2.getParam(ParamType.Value);
            effect.setStatusMultiply(StatusType.ManaRegen, value);
        }
        owner.addEffect(effect);
        return true;
    }
}
