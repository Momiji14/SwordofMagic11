package SwordofMagic11.Skill.Process;

import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Skill.SkillData;
import SwordofMagic11.Skill.SkillManager;
import SwordofMagic11.Skill.SomSkill;

public class MultiCastBall extends SomSkill {
    public MultiCastBall(PlayerData owner, SkillData skillData) {
        super(owner, skillData);
    }

    @Override
    public boolean active() {
        SkillManager skillManager = owner.skillManager();
        skillManager.instance("FireBall").instantCast(owner);
        skillManager.instance("IceBall").instantCast(owner);
        return true;
    }
}
