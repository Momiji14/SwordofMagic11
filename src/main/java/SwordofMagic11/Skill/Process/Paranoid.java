package SwordofMagic11.Skill.Process;

import SwordofMagic11.Component.SomParticle;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Component.SomTask;
import SwordofMagic11.Entity.SearchEntity;
import SwordofMagic11.Entity.SomEntity;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Skill.ParamType;
import SwordofMagic11.Skill.Parameter;
import SwordofMagic11.Skill.SkillData;
import SwordofMagic11.Skill.SomSkill;
import org.bukkit.Particle;

import static SwordofMagic11.Component.Function.randomFloat;

public class Paranoid extends SomSkill {
    public Paranoid(PlayerData owner, SkillData skillData) {
        super(owner, skillData);
    }

    @Override
    public boolean active() {
        Parameter parameter = getParam();
        double radius = parameter.getParam(ParamType.Radius);
        int interval = parameter.getParamInt(ParamType.Interval);
        int count = parameter.getParamInt(ParamType.Count);
        SomParticle particle = new SomParticle(Particle.ENCHANT, owner).setRandomVector().setSpeed(0.1f);
        SomTask.skillTaskCount(() -> {
            particle.circle(owner.getHipsLocation(), radius);
            SomSound.LowHeal.radius(owner);
            for (SomEntity entity : SearchEntity.nearSomEntity(owner.enemies(), owner.getLocation(), radius)) {
                entity.teleport(entity.getLocation().yaw(randomFloat(-180, 180)).pitch(randomFloat(-90, 90)));
            }
        }, interval, count, owner);
        return true;
    }
}
