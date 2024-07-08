package SwordofMagic11.Skill.Process;

import SwordofMagic11.Component.SomParticle;
import SwordofMagic11.Component.SomRay;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Component.SomTask;
import SwordofMagic11.DataBase.SkillDataLoader;
import SwordofMagic11.Entity.Damage;
import SwordofMagic11.Entity.SomEffect;
import SwordofMagic11.Entity.SomEntity;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Skill.ParamType;
import SwordofMagic11.Skill.Parameter;
import SwordofMagic11.Skill.SkillData;
import SwordofMagic11.Skill.SomSkill;
import SwordofMagic11.StatusType;
import org.bukkit.Particle;

public class DaggerRendan extends SomSkill {
    public DaggerRendan(PlayerData owner, SkillData skillData) {
        super(owner, skillData);
    }

    private boolean daggerRendanCritical = true;
    private SomEntity lastHitEntity;

    @Override
    public boolean active() {
        Parameter parameter = getParam();
        double damage = parameter.getParam(ParamType.Damage);
        double range = parameter.getParam(ParamType.Range);
        int count = parameter.getParamInt(ParamType.Count);
        int interval = parameter.getParamInt(ParamType.Interval);
        SomParticle particle = new SomParticle(Particle.SWEEP_ATTACK, owner);
        lastHitEntity = null;
        daggerRendanCritical = true;
        SomTask.skillTaskCount(() -> {
            SomRay ray = SomRay.rayLocationEntity(owner, range, 0.3, owner.enemies(), false);
            if (ray.isHitEntity()) {
                if (lastHitEntity == null) {
                    lastHitEntity = ray.getHitEntity();
                } else if (lastHitEntity != ray.getHitEntity()) {
                    daggerRendanCritical = false;
                }
                particle.random(ray.getHitEntity().getHipsLocation());
                Damage.makeDamage(owner, ray.getHitEntity(), Damage.Type.Physics, damage);
            } else {
                daggerRendanCritical = false;
                particle.random(ray.getOriginPosition());
            }
            SomSound.Sword.radius(owner);
        }, interval, count, () -> {
            if (daggerRendanCritical && owner.hasSkill("DaggerRendanCritical")) {
                Parameter parameter2 = owner.getSkillParam("DaggerRendanCritical");
                double percent = parameter2.getParam(ParamType.Percent);
                int time = parameter2.getParamInt(ParamType.Time);
                SomEffect effect = new SomEffect(SkillDataLoader.getSkillData("DaggerRendanCritical"), time, owner);
                effect.setStatusMultiply(StatusType.CriticalRate, percent);
                owner.addEffect(effect);
                SomSound.LowHeal.radius(owner);
            }
        }, owner);
        return true;
    }
}
