package SwordofMagic11.Skill.Process;

import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Skill.SkillData;
import SwordofMagic11.Skill.SkillManager;
import SwordofMagic11.Skill.SomSkill;

public class MultiCastFloor extends SomSkill {
    public MultiCastFloor(PlayerData owner, SkillData skillData) {
        super(owner, skillData);
    }

    @Override
    public boolean active() {
        SkillManager skillManager = owner.skillManager();
        skillManager.instance("FlameFloor").instantCast(owner);
        skillManager.instance("FrostFloor").instantCast(owner);
        return true;
    }
}
