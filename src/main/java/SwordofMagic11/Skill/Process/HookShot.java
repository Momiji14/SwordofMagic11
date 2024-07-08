package SwordofMagic11.Skill.Process;

import SwordofMagic11.Component.SomParticle;
import SwordofMagic11.Component.SomRay;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Skill.ParamType;
import SwordofMagic11.Skill.Parameter;
import SwordofMagic11.Skill.SkillData;
import SwordofMagic11.Skill.SomSkill;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;

import static SwordofMagic11.Component.SomParticle.VectorUp;

public class HookShot extends SomSkill {
    public HookShot(PlayerData owner, SkillData skillData) {
        super(owner, skillData);
    }

    @Override
    public boolean active() {
        Parameter parameter = getParam();
        double range = parameter.getParam(ParamType.Range);
        SomParticle particle = new SomParticle(Particle.CRIT, owner).setSpeed(0.35f).setVector(owner.getDirection());
        SomRay ray = SomRay.rayLocationBlock(owner, range, true);
        if (ray.isHitBlock()) {
            SomSound.Bow.radius(owner);
            particle.line(owner.getHandLocation(), ray.getOriginPosition());
            Entity entity = owner.getEntity();
            entity.setVelocity(owner.getDirection().multiply(1.5).add(VectorUp));
        } else {
            owner.sendMessage("§cフックが刺さりませんでした", SomSound.Nope);
        }
        return true;
    }
}
