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

import static SwordofMagic11.Entity.Damage.BuffMultiply;
import static SwordofMagic11.Entity.Damage.PvPMultiply;

public class Distract extends SomSkill {

    public static SomEffect createEffect(SomEntity owner, int time) {
        return new SomEffect("Distract", "ディストラクト", time, owner);
    }

    public Distract(PlayerData owner, SkillData skillData) {
        super(owner, skillData);
    }

    @Override
    public boolean active() {
        Parameter parameter = getParam();
        double value = parameter.getParam(ParamType.Value) * owner.getStatus(StatusType.CriticalRate);
        double range = parameter.getParam(ParamType.Range);
        int time = parameter.getParamInt(ParamType.Time);
        SomParticle particle = new SomParticle(Color.ORANGE, owner).setSpeed(0.2f).setRandomVector();
        SomRay ray = SomRay.rayLocationEntity(owner, range, 1, owner.enemies(), false);
        if (ray.isHitEntity()) {
            if (ray.getHitEntity() instanceof PlayerData playerData && playerData.isPvPMode()) {
                value *= BuffMultiply;
            }
            SomEffect effect = createEffect(owner, time).setPriority(-value);
            effect.setStatus(StatusType.CriticalResist, value);
            particle.line(owner.getHandLocation(), ray.getHitEntity().getHipsLocation());
            particle.random(ray.getHitEntity().getHipsLocation());
            SomSound.Glass.radius(ray.getHitEntity());
            ray.getHitEntity().addEffect(effect);
        } else {
            owner.sendMessage("§c対象がいません", SomSound.Nope);
            owner.addMana(getManaCost());
            setCurrentCoolTime(1);
        }
        return true;
    }
}
