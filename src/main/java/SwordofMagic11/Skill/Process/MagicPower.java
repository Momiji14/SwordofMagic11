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

public class MagicPower extends SomSkill {
    public MagicPower(PlayerData owner, SkillData skillData) {
        super(owner, skillData);
    }

    @Override
    public boolean active() {
        Parameter parameter = getParam();
        double percent = parameter.getParam(ParamType.Percent);
        int time = parameter.getParamInt(ParamType.Time);
        SomParticle particle = new SomParticle(Color.PURPLE, owner).setSpeed(0.35f);
        particle.sphere(owner.getHipsLocation(), 1);
        SomSound.LowHeal.radius(owner);
        SomEffect effect = new SomEffect(this, time);
        effect.setStatusMultiply(StatusType.MAT, percent);
        owner.addEffect(effect);
        return true;
    }
}
