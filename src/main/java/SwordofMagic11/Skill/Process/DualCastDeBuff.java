package SwordofMagic11.Skill.Process;

import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Skill.SkillData;
import SwordofMagic11.Skill.SkillManager;
import SwordofMagic11.Skill.SomSkill;

public class DualCastDeBuff extends SomSkill {
    public DualCastDeBuff(PlayerData owner, SkillData skillData) {
        super(owner, skillData);
    }

    @Override
    public boolean active() {
        SkillManager skillManager = owner.skillManager();
        skillManager.instance("Jelun").instantCast(owner);
        skillManager.instance("Crack").instantCast(owner);
        return true;
    }
}
