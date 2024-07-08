package SwordofMagic11.Skill.Process;

import SwordofMagic11.Component.SomParticle;
import SwordofMagic11.Component.SomRay;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Entity.SomEffect;
import SwordofMagic11.Entity.SomEntity;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Skill.ParamType;
import SwordofMagic11.Skill.Parameter;
import SwordofMagic11.Skill.SkillData;
import SwordofMagic11.Skill.SomSkill;
import SwordofMagic11.StatusType;
import org.bukkit.Color;

public class Moreturk extends SomSkill {

    public static SomEffect createEffect(SomEntity owner, int time) {
        return new SomEffect("Moreturk", "アターク", time, owner);
    }

    public Moreturk(PlayerData owner, SkillData skillData) {
        super(owner, skillData);
    }

    @Override
    public boolean active() {
        Parameter parameter = getParam();
        double value = parameter.getParam(ParamType.Value) * owner.getStatus(StatusType.SPT) + parameter.getParamInt(ParamType.ValueInt);
        double range = parameter.getParam(ParamType.Range);
        int time = parameter.getParamInt(ParamType.Time);
        SomEffect effect = createEffect(owner, time).setPriority(value);
        effect.setStatusAttack(value);
        SomParticle particle = new SomParticle(Color.RED, owner).setSpeed(0.2f).setRandomVector();
        SomEntity target = owner;
        SomRay ray = SomRay.rayLocationEntity(owner, range, 1, owner.allies(), false);
        if (ray.isHitEntity()) {
            target = ray.getHitEntity();
            particle.line(owner.getHandLocation(), target.getHipsLocation());
        }
        particle.random(target.getHipsLocation());
        SomSound.Heal.radius(target);
        target.addEffect(effect);
        return true;
    }
}
