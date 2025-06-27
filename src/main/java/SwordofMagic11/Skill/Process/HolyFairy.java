package SwordofMagic11.Skill.Process;

import SwordofMagic11.Component.SomParticle;
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
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

public class HolyFairy extends SomSkill {
    public HolyFairy(PlayerData owner, SkillData skillData) {
        super(owner, skillData);
    }

    private BukkitTask task;

    @Override
    public boolean active() {
        if (task == null || task.isCancelled()) {
            Parameter parameter = getParam();
            double damage = parameter.getParam(ParamType.Damage);
            double radius = parameter.getParam(ParamType.Radius);
            int interval = parameter.getParamInt(ParamType.Interval);
            double manaCost = getManaCost();
            SomParticle particle = new SomParticle(Particle.FIREWORK, owner);
            SomParticle particle2 = new SomParticle(Particle.END_ROD, owner).setShrink().setSpeed(0.075f);
            task = SomTask.skillTaskTimer(() -> {
                if (owner.getMana() >= manaCost) {
                    CustomLocation pivot = owner.getLocation().addY(2.5).right(1.5);
                    particle2.sphere(pivot, 0.5);
                    List<SomEntity> entities = SearchEntity.nearestSomEntity(owner.enemies(), owner.getLocation(), radius);
                    if (!entities.isEmpty()) {
                        SomEntity entity = entities.get(0);
                        particle.line(pivot, entity.getHipsLocation());
                        owner.removeMana(manaCost);
                        Damage.makeDamage(owner, entity, Damage.Type.Magic, damage);
                        SomSound.Mace.radius(pivot);
                    }
                }
            }, interval, owner);
            owner.sendMessage("§e聖霊§aを召喚しました", SomSound.Heal);
        } else {
            stop();
        }
        return true;
    }

    public void stop() {
        task.cancel();
        task = null;
        owner.sendMessage("§e聖霊§aを還しました", SomSound.Tick);
    }
}
