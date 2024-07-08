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
import org.bukkit.Particle;

import java.util.Collection;

public class HolyLight extends SomSkill {
    public HolyLight(PlayerData owner, SkillData skillData) {
        super(owner, skillData);
    }

    @Override
    public boolean active() {
        Parameter parameter = getParam();
        double damage = parameter.getParam(ParamType.Damage);
        double range = parameter.getParam(ParamType.Range);
        double radius = parameter.getParam(ParamType.Radius);
        SomParticle particle = new SomParticle(Particle.END_ROD, owner).setSpeed(0.15f).setShrink();
        SomRay ray = SomRay.rayLocationEntity(owner, range, 0.5, owner.enemies(), false);
        CustomLocation center = ray.getOriginPosition();
        if (ray.isHitEntity()) center.add(owner.getDirection().multiply(radius / 2));
        SomSound.Mace.radius(owner);
        SomTask.skillTaskCount(() -> particle.sphere(center, radius / 2), 3, 3, () -> SomTask.asyncDelay(() -> {
            particle.setRandomVector().sphere(center, radius);
            particle.setExpand().setSpeed(0.25f).sphere(center, 0.5);
            Collection<SomEntity> enemies = SearchEntity.nearSomEntity(owner.enemies(), center, radius);
            SomSound.Light.radius(center);
            for (SomEntity enemy : enemies) {
                Damage.makeDamage(owner, enemy, Damage.Type.Magic, damage);
            }
        }, 5), owner);
        return true;
    }
}
