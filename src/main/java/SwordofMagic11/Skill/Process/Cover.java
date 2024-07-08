package SwordofMagic11.Skill.Process;

import SwordofMagic11.Component.SomParticle;
import SwordofMagic11.Component.SomRay;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Entity.SomEffect;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Skill.ParamType;
import SwordofMagic11.Skill.Parameter;
import SwordofMagic11.Skill.SkillData;
import SwordofMagic11.Skill.SomSkill;
import org.bukkit.Particle;

public class Cover extends SomSkill {
    public Cover(PlayerData owner, SkillData skillData) {
        super(owner, skillData);
    }

    @Override
    public boolean active() {
        Parameter parameter = getParam();
        double range = parameter.getParamInt(ParamType.Range);
        int time = parameter.getParamInt(ParamType.Time);
        SomParticle particle = new SomParticle(Particle.ENCHANTMENT_TABLE, owner).setSpeed(0.2f).setRandomVector();
        SomRay ray = SomRay.rayLocationEntity(owner, range, 1, owner.allies(), false);
        if (ray.isHitEntity()) {
            SomEffect effect = new SomEffect(this, time);
            ray.getHitEntity().addEffect(effect);
            particle.random(ray.getHitEntity().getHipsLocation());
            particle.line(owner.getHandLocation(), ray.getHitEntity().getHipsLocation());
            SomSound.Heal.radius(ray.getHitEntity());
        }
        return true;
    }
}
