package SwordofMagic11.Skill.Process;

import SwordofMagic11.Component.SomParticle;
import SwordofMagic11.Component.SomRay;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Custom.CustomLocation;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Skill.ParamType;
import SwordofMagic11.Skill.Parameter;
import SwordofMagic11.Skill.SkillData;
import SwordofMagic11.Skill.SomSkill;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

public class SilenceShot extends SomSkill {
    public SilenceShot(PlayerData owner, SkillData skillData) {
        super(owner, skillData);
    }

    @Override
    public boolean active() {
        Parameter parameter = getParam();
        double range = parameter.getParam(ParamType.Range);
        int time = parameter.getParamInt(ParamType.Time);
        SomParticle particle = new SomParticle(Particle.CRIT, owner).setSpeed(0.35f);
        SomRay ray = SomRay.rayLocationEntity(owner, range, 1, owner.playerEnemies(), true);
        CustomLocation center = ray.getOriginPosition();
        center.add(owner.getDirection().multiply(-1));
        particle.line(owner.getHandLocation(), center);
        SomSound.Bow.radius(owner);
        if (ray.isHitEntity()) {
            particle.line(center, ray.getHitEntity().getHipsLocation());
            ray.getHitEntity().setVelocity(new Vector());
            ray.getHitEntity().silence(time, owner);
        }
        return true;
    }
}
