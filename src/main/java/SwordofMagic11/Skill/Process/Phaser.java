package SwordofMagic11.Skill.Process;

import SwordofMagic11.Component.SomParticle;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Component.SomTask;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Skill.SkillData;
import SwordofMagic11.Skill.SomSkill;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public class Phaser extends SomSkill {
    public Phaser(PlayerData owner, SkillData skillData) {
        super(owner, skillData);
    }

    @Override
    public boolean active() {
        SomParticle particle = new SomParticle(Particle.WITCH, owner).setSpeed(0.35f).setVector(owner.getDirection());
        SomSound.Push.radius(owner);
        Entity entity = owner.getEntity();
        entity.setGravity(false);
        SomTask.asyncCount(() -> {
            Vector vector = owner.getDirection().multiply(1.5);
            vector.setY(vector.getY() * 0.4);
            entity.setVelocity(vector);
        }, 1, 3, owner);
        SomTask.asyncCount(() -> particle.random(owner.getHipsLocation(), 10), 1, 20, () -> entity.setGravity(true), owner);
        return true;
    }
}
