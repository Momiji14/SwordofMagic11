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

public class HomingArrow extends SomSkill {
    public HomingArrow(PlayerData owner, SkillData skillData) {
        super(owner, skillData);
    }

    @Override
    public boolean active() {
        Parameter parameter = getParam();
        double damage = parameter.getParam(ParamType.Damage);
        double range = parameter.getParam(ParamType.Range);
        double angle = parameter.getParam(ParamType.Angle);
        SomParticle particle = new SomParticle(Particle.CRIT, owner);
        int i = 0;
        particle.fanShaped(owner.getHipsLocation(), range, angle);
        for (SomEntity entity : SearchEntity.fanShapedSomEntity(owner.enemies(), owner.getLocation(), range, angle)) {
            SomTask.asyncDelay(() -> {
                particle.line(owner.getHandLocation(), entity.getHipsLocation());
                Damage.makeDamage(owner, entity, Damage.Type.Shoot, damage);
                SomSound.Bow.radius(owner);
            }, i * 3);
            i++;
        }
        return true;
    }
}
