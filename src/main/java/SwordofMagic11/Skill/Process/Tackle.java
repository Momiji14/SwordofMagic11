package SwordofMagic11.Skill.Process;

import SwordofMagic11.Component.SomParticle;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Entity.Damage;
import SwordofMagic11.Entity.SearchEntity;
import SwordofMagic11.Entity.SomEntity;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Skill.ParamType;
import SwordofMagic11.Skill.Parameter;
import SwordofMagic11.Skill.SkillData;
import SwordofMagic11.Skill.SomSkill;
import org.bukkit.Color;
import org.bukkit.util.Vector;

import static SwordofMagic11.Component.Function.VectorFromYawPitch;

public class Tackle extends SomSkill {
    public Tackle(PlayerData owner, SkillData skillData) {
        super(owner, skillData);
    }

    @Override
    public boolean active() {
        Parameter parameter = getParam();
        double damage = parameter.getParam(ParamType.Damage);
        double range = parameter.getParam(ParamType.Range);
        double width = parameter.getParam(ParamType.Width);
        int time = parameter.getParamInt(ParamType.Time);
        SomParticle particle = new SomParticle(Color.RED, owner);
        particle.rectangle(owner.getLocation(), range, width);
        SomSound.Sword.radius(owner);
        Vector vector = VectorFromYawPitch(owner.yaw(), -20).multiply(1.5);
        owner.strafeCoolDown();
        owner.setVelocity(vector);
        for (SomEntity entity : SearchEntity.rectangleSomEntity(owner.enemies(), owner.getLocation(), range, width)) {
            Damage.makeDamage(owner, entity, Damage.Type.Physics, damage);
            entity.setVelocity(vector);
            entity.SLOWNESS(2, time);
        }
        return true;
    }
}
