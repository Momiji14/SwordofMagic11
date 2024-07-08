package SwordofMagic11.Skill.Process;

import SwordofMagic11.Component.SomParticle;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Component.SomTask;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Skill.SkillData;
import SwordofMagic11.Skill.SomSkill;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;

public class Blipper extends SomSkill {
    public Blipper(PlayerData owner, SkillData skillData) {
        super(owner, skillData);
    }

    @Override
    public boolean active() {
        if (owner.hasSkill("Transition")) return false;
        SomParticle particle = new SomParticle(Particle.END_ROD, owner).setSpeed(0.35f);
        SomSound.LowHeal.radius(owner);
        Entity entity = owner.getEntity();
        SomTask.skillTaskCount(() -> {
            particle.setVector(owner.getDirection());
            particle.random(owner.getHipsLocation(), 5);
            entity.setVelocity(owner.getDirection().multiply(0.5));
        }, 1, 20, owner);
        return true;
    }
}
