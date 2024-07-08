package SwordofMagic11.Skill.Process;

import SwordofMagic11.Component.SomParticle;
import SwordofMagic11.Component.SomRay;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Component.SomTask;
import SwordofMagic11.Custom.CustomLocation;
import SwordofMagic11.Entity.Damage;
import SwordofMagic11.Entity.SearchEntity;
import SwordofMagic11.Entity.SomEntity;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Skill.ParamType;
import SwordofMagic11.Skill.Parameter;
import SwordofMagic11.Skill.SkillData;
import SwordofMagic11.Skill.SomSkill;
import SwordofMagic11.StatusType;
import org.bukkit.Color;

import java.util.Collection;

public class PoisonBomb extends SomSkill {
    public PoisonBomb(PlayerData owner, SkillData skillData) {
        super(owner, skillData);
    }

    @Override
    public boolean active() {
        Parameter parameter = getParam();
        double damage = parameter.getParam(ParamType.Damage);
        int count = parameter.getParamInt(ParamType.Count);
        int interval = parameter.getParamInt(ParamType.Interval);
        double range = parameter.getParam(ParamType.Range);
        double radius = parameter.getParam(ParamType.Radius);
        SomParticle particle = new SomParticle(Color.GREEN, owner).setLower();
        SomRay ray = SomRay.rayLocationBlock(owner, range, true);
        CustomLocation center = ray.getOriginPosition().back(0.1).lower();
        SomSound.Glass.radius(owner);
        Damage.Type damageType = owner.getStatus(StatusType.ATK) > owner.getStatus(StatusType.SAT) ? Damage.Type.Physics : Damage.Type.Shoot;
        SomTask.skillTaskCount(() -> {
            particle.circleFill(center, radius);
            for (SomEntity entity : SearchEntity.nearSomEntity(owner.enemies(), center, radius)) {
                Damage.makeDamage(owner, entity, damageType, damage);
            }
        }, interval, count, owner);
        return true;
    }
}
