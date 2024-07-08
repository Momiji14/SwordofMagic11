package SwordofMagic11.Skill.Process;

import SwordofMagic11.Component.SomParticle;
import SwordofMagic11.Component.SomRay;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Custom.CustomLocation;
import SwordofMagic11.Pet.PetEntity;
import SwordofMagic11.Pet.SomPet;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Skill.ParamType;
import SwordofMagic11.Skill.Parameter;
import SwordofMagic11.Skill.SkillData;
import SwordofMagic11.Skill.SomSkill;
import org.bukkit.Color;

public class Locate extends SomSkill {
    public Locate(PlayerData owner, SkillData skillData) {
        super(owner, skillData);
    }

    @Override
    public boolean active() {
        SomParticle particle = new SomParticle(Color.YELLOW, owner);
        SomRay ray = SomRay.rayLocationBlock(owner, 32, true);
        CustomLocation location = ray.getOriginPosition().lower();
        for (SomPet somPet : owner.petMenu().getSummonList()) {
            PetEntity petEntity = somPet.getEntityIns();
            particle.line(owner.getHandLocation(), petEntity.getHipsLocation());
            particle.line(petEntity.getHipsLocation(), location);
            petEntity.setOverrideLocation(location);
        }
        SomSound.Tick.play(owner);
        return true;
    }
}
