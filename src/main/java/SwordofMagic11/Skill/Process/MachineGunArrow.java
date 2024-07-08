package SwordofMagic11.Skill.Process;

import SwordofMagic11.Component.SomParticle;
import SwordofMagic11.Component.SomRay;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Component.SomTask;
import SwordofMagic11.Entity.Damage;
import SwordofMagic11.Entity.SomEntity;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Skill.ParamType;
import SwordofMagic11.Skill.Parameter;
import SwordofMagic11.Skill.SkillData;
import SwordofMagic11.Skill.SomSkill;
import org.bukkit.Particle;

public class MachineGunArrow extends SomSkill {
    public MachineGunArrow(PlayerData owner, SkillData skillData) {
        super(owner, skillData);
    }

    private SomEntity lastHit;
    private int combo = 0;

    @Override
    public boolean active() {
        Parameter parameter = getParam();
        double damage = parameter.getParam(ParamType.Damage);
        double range = parameter.getParam(ParamType.Range);
        int count = parameter.getParamInt(ParamType.Count);
        int interval = parameter.getParamInt(ParamType.Interval);
        SomParticle particle = new SomParticle(Particle.CRIT, owner);
        SomTask.skillTaskCount(() -> {
            SomRay ray = SomRay.rayLocationEntity(owner, range, 0.5, owner.enemies(), false);
            particle.line(owner.getHandLocation(), ray.getOriginPosition());
            if (ray.isHitEntity()) {
                SomEntity target = ray.getHitEntity();
                particle.random(ray.getHitEntity().getHipsLocation());
                if (owner.hasSkill("MachineGunArrowCombo")) {
                    Parameter parameter2 = owner.getSkillParam("MachineGunArrowCombo");
                    double damage2 = parameter2.getParam(ParamType.Damage);
                    if (lastHit != null && target.getUUID().equals(lastHit.getUUID())) {
                        combo++;
                    } else {
                        lastHit = target;
                        combo = 1;
                    }
                    Damage.makeDamage(owner, target, Damage.Type.Shoot, damage + (combo * damage2));
                } else {
                    Damage.makeDamage(owner, target, Damage.Type.Shoot, damage);
                }
            } else {
                particle.random(ray.getOriginPosition());
            }
            SomSound.Bow.radius(owner);
        }, interval, count, () -> {
            if (owner.hasSkill("MachineGunArrowCombo")) {
                if (combo == count) {
                    Parameter param2 = owner.getSkillParam("MachineGunArrowCombo");
                    Damage.makeDamage(owner, lastHit, Damage.Type.Shoot, param2.getParam(ParamType.Damage2));
                    SomSound.Strong.radius(owner);
                }
                combo = 0;
                lastHit = null;
            }
        }, owner);
        return true;
    }
}
