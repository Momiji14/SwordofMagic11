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
import org.bukkit.Color;

public class HardSkin extends SomSkill {
    public HardSkin(PlayerData owner, SkillData skillData) {
        super(owner, skillData);
    }

    @Override
    public boolean active() {
        Parameter parameter = getParam();
        double value = parameter.getParam(ParamType.Value);
        double value2 = parameter.getParam(ParamType.Value2);
        int time = parameter.getParamInt(ParamType.Time);
        SomParticle particle = new SomParticle(Color.BLUE, owner).setSpeed(0.35f);
        particle.sphere(owner.getHipsLocation(), 1);
        SomSound.LowHeal.radius(owner);
        SomEffect effect = new SomEffect(this, time);
        effect.setStatusMultiplyDefense(value);
        effect.setStatusMultiply(StatusType.CriticalResist, value);
        effect.setStatusMultiplyAttack(value2);
        owner.addEffect(effect);
        return true;
    }
}
