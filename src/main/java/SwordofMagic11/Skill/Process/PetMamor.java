package SwordofMagic11.Skill.Process;

import SwordofMagic11.Component.SomParticle;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Entity.SomEffect;
import SwordofMagic11.Pet.PetEntity;
import SwordofMagic11.Pet.SomPet;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Skill.ParamType;
import SwordofMagic11.Skill.Parameter;
import SwordofMagic11.Skill.SkillData;
import SwordofMagic11.Skill.SomSkill;
import org.bukkit.Color;

public class PetMamor extends SomSkill {
    public PetMamor(PlayerData owner, SkillData skillData) {
        super(owner, skillData);
    }

    @Override
    public boolean active() {
        Parameter param = getParam();
        double percent = param.getParam(ParamType.Percent);
        int time = param.getParamInt(ParamType.Time);
        SomParticle particle = new SomParticle(Color.BLUE, owner);
        SomEffect effect = new SomEffect(this, time);
        effect.setStatusMultiplyDefense(percent);
        for (SomPet somPet : owner.petMenu().getSummonList()) {
            PetEntity petEntity = somPet.getEntityIns();
            particle.line(owner.getHandLocation(), petEntity.getHipsLocation());
            petEntity.addEffect(effect);
        }
        SomSound.Tick.play(owner);
        return true;
    }
}
