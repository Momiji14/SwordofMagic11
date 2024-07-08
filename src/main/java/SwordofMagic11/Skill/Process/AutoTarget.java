package SwordofMagic11.Skill.Process;

import SwordofMagic11.Component.SomParticle;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Pet.PetEntity;
import SwordofMagic11.Pet.SomPet;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Skill.ParamType;
import SwordofMagic11.Skill.Parameter;
import SwordofMagic11.Skill.SkillData;
import SwordofMagic11.Skill.SomSkill;
import org.bukkit.Color;

public class AutoTarget extends SomSkill {
    public AutoTarget(PlayerData owner, SkillData skillData) {
        super(owner, skillData);
    }

    @Override
    public boolean active() {
        Parameter parameter = getParam();
        double radius = parameter.getParam(ParamType.Radius);
        SomParticle particle = new SomParticle(Color.RED, owner);
        for (SomPet somPet : owner.petMenu().getSummonList()) {
            PetEntity petEntity = somPet.getEntityIns();
            particle.line(owner.getHandLocation(), petEntity.getHipsLocation());
            particle.circle(owner.getLocation(), radius);
            petEntity.setSearchRadius(radius);
            petEntity.setState(PetEntity.State.Auto);
        }
        SomSound.Tick.play(owner);
        return true;
    }
}
