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

public class Prominence extends SomSkill {
    public Prominence(PlayerData owner, SkillData skillData) {
        super(owner, skillData);
    }

    @Override
    public boolean active() {
        Parameter parameter = getParam();
        double damage = parameter.getParam(ParamType.Damage);
        double radius = parameter.getParam(ParamType.Radius);
        int interval = parameter.getParamInt(ParamType.Interval);
        int count = parameter.getParamInt(ParamType.Count);
        SomParticle particle = new SomParticle(Particle.FLAME, owner).setRandomVector().setSpeed(0.1f);
        SomTask.skillTaskCount(() -> {
            particle.circle(owner.getHipsLocation(), radius);
            SomSound.Fire.radius(owner);
            for (SomEntity entity : SearchEntity.nearSomEntity(owner.enemies(), owner.getLocation(), radius)) {
                Damage.makeDamage(owner, entity, Damage.Type.Magic, damage);
            }
        }, interval, count, owner);
        return true;
    }
}
