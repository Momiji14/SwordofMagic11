package SwordofMagic11.Skill.Process;

import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Skill.SkillData;
import SwordofMagic11.Skill.SkillManager;
import SwordofMagic11.Skill.SomSkill;

public class MultiCastBuff extends SomSkill {
    public MultiCastBuff(PlayerData owner, SkillData skillData) {
        super(owner, skillData);
    }

    @Override
    public boolean active() {
        SkillManager skillManager = owner.skillManager();
        skillManager.instance("ZeroCost").instantCast(owner);
        skillManager.instance("MagicPower").instantCast(owner);
        skillManager.instance("LimitRelease").instantCast(owner);
        return true;
    }
}
