package SwordofMagic11.Skill.Process;

import SwordofMagic11.Component.SomParticle;
import SwordofMagic11.Component.SomRay;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Entity.SomEntity;
import SwordofMagic11.Pet.PetEntity;
import SwordofMagic11.Pet.SomPet;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Skill.ParamType;
import SwordofMagic11.Skill.Parameter;
import SwordofMagic11.Skill.SkillData;
import SwordofMagic11.Skill.SomSkill;
import org.bukkit.Color;

public class SelectTarget extends SomSkill {
    public SelectTarget(PlayerData owner, SkillData skillData) {
        super(owner, skillData);
    }

    @Override
    public boolean active() {
        Parameter parameter = getParam();
        double range = parameter.getParam(ParamType.Range);
        SomParticle particle = new SomParticle(Color.RED, owner);
        SomRay ray = SomRay.rayLocationEntity(owner, range, 1, owner.enemies(), false);
        if (ray.isHitEntity()) {
            SomEntity target = ray.getHitEntity();
            for (SomPet somPet : owner.petMenu().getSummonList()) {
                PetEntity petEntity = somPet.getEntityIns();
                particle.line(owner.getHandLocation(), petEntity.getHipsLocation());
                particle.line(petEntity.getHipsLocation(), target.getHipsLocation());
                petEntity.setState(PetEntity.State.Select);
                petEntity.setTarget(target);
            }
            SomSound.Tick.play(owner);
        }
        return true;
    }
}
