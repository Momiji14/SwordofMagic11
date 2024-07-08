package SwordofMagic11.Skill.Process;

import SwordofMagic11.Pet.SomPet;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Skill.SkillData;
import SwordofMagic11.Skill.SomSkill;

public class Cage extends SomSkill {
    public Cage(PlayerData owner, SkillData skillData) {
        super(owner, skillData);
    }

    @Override
    public boolean active() {
        for (SomPet somPet : owner.petMenu().getSummonList()) {
            somPet.getEntityIns().setDeath(true);
        }
        return true;
    }
}
