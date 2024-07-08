package SwordofMagic11.Skill.Process;

import SwordofMagic11.Component.SomParticle;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Component.SomTask;
import SwordofMagic11.Entity.Damage;
import SwordofMagic11.Entity.SearchEntity;
import SwordofMagic11.Entity.SomEntity;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Skill.ParamType;
import SwordofMagic11.Skill.Parameter;
import SwordofMagic11.Skill.SkillData;
import SwordofMagic11.Skill.SomSkill;
import org.bukkit.Particle;

public class WhirlWind extends SomSkill {
    public WhirlWind(PlayerData owner, SkillData skillData) {
        super(owner, skillData);
    }

    @Override
    public boolean active() {
        Parameter parameter = getParam();
        double damage = parameter.getParam(ParamType.Damage);
        double radius = parameter.getParam(ParamType.Radius);
        int interval = parameter.getParamInt(ParamType.Interval);
        int count = parameter.getParamInt(ParamType.Count);
        SomParticle particle = new SomParticle(Particle.SWEEP_ATTACK, owner);
        SomTask.skillTaskCount(() -> {
            particle.circleFill(owner.getHipsLocation(), radius, 3);
            SomSound.Sword.radius(owner);
            for (SomEntity entity : SearchEntity.nearSomEntity(owner.enemies(), owner.getLocation(), radius)) {
                Damage.makeDamage(owner, entity, Damage.Type.Physics, damage);
            }
        }, interval, count, owner);
        return true;
    }
}
