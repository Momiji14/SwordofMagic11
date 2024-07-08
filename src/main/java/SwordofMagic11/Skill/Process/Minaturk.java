package SwordofMagic11.Skill.Process;

import SwordofMagic11.Component.SomParticle;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Entity.SearchEntity;
import SwordofMagic11.Entity.SomEffect;
import SwordofMagic11.Entity.SomEntity;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Skill.ParamType;
import SwordofMagic11.Skill.Parameter;
import SwordofMagic11.Skill.SkillData;
import SwordofMagic11.Skill.SomSkill;
import SwordofMagic11.StatusType;
import org.bukkit.Color;

public class Minaturk extends SomSkill {

    public Minaturk(PlayerData owner, SkillData skillData) {
        super(owner, skillData);
    }

    @Override
    public boolean active() {
        Parameter parameter = getParam();
        double value = parameter.getParam(ParamType.Value) * owner.getStatus(StatusType.SPT) + parameter.getParamInt(ParamType.ValueInt);
        double radius = parameter.getParam(ParamType.Radius);
        int time = parameter.getParamInt(ParamType.Time);
        SomEffect effect = Moreturk.createEffect(owner, time).setPriority(value);
        effect.setStatusAttack(value);
        SomParticle particle = new SomParticle(Color.RED, owner).setSpeed(0.2f).setRandomVector();
        for (SomEntity entity : SearchEntity.nearSomEntity(owner.alliesWithMe(), owner.getLocation(), radius)) {
            particle.line(owner.getHandLocation(), entity.getHipsLocation());
            particle.random(entity.getHipsLocation());
            entity.addEffect(effect);
            SomSound.Heal.play(entity);
        }
        return true;
    }
}
