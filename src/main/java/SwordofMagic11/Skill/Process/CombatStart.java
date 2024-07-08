package SwordofMagic11.Skill.Process;

import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Skill.SkillData;
import SwordofMagic11.Skill.SkillManager;
import SwordofMagic11.Skill.SomSkill;

public class CombatStart extends SomSkill {
    public CombatStart(PlayerData owner, SkillData skillData) {
        super(owner, skillData);
    }

    @Override
    public boolean active() {
        SkillManager skillManager = owner.skillManager();
        skillManager.instance("Concentration").instantCast(owner);
        skillManager.instance("CriticalDamage").instantCast(owner);
        skillManager.instance("Offensive").instantCast(owner);
        return true;
    }
}
