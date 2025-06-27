package SwordofMagic11.Skill.Process;

import SwordofMagic11.Component.SomParticle;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Skill.SkillData;
import SwordofMagic11.Skill.SomSkill;
import org.bukkit.Particle;

public class HighJump extends SomSkill {
    public HighJump(PlayerData owner, SkillData skillData) {
        super(owner, skillData);
    }

    @Override
    public boolean active() {
        SomParticle particle = new SomParticle(Particle.EXPLOSION, owner).setSpeed(0.2f).setRandomVector();
        particle.circleFill(owner.getLocation(), 1);
        SomSound.Push.radius(owner);
        owner.setVelocity(owner.getDirection().setY(2));
        return true;
    }
}
