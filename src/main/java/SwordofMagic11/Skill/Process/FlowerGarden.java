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

public class FlowerGarden extends SomSkill {
    public FlowerGarden(PlayerData owner, SkillData skillData) {
        super(owner, skillData);
    }

    private BukkitTask task;

    @Override
    public boolean active() {
        if (task == null || task.isCancelled()) {
            Parameter parameter = getParam();
            double heal = parameter.getParam(ParamType.Heal);
            double radius = parameter.getParam(ParamType.Radius);
            int interval = parameter.getParamInt(ParamType.Interval);
            double manaCost = getManaCost();
            SomParticle particle = new SomParticle(Particle.HAPPY_VILLAGER, owner).setLower();
            CustomLocation pivot = owner.getLocation().lower().addY(5);
            task = SomTask.skillTaskTimer(() -> {
                if (owner.getMana() >= manaCost) {
                    particle.circleFill(pivot, radius);
                    List<SomEntity> entities = SearchEntity.nearSomEntity(owner.alliesWithMe(), pivot, radius);
                    entities.removeIf(entity -> entity.healthPercent() >= 1);
                    owner.removeMana(manaCost * (1 + entities.size()));
                    if (!entities.isEmpty()) {
                        SomSound.Heal.radius(pivot);
                        for (SomEntity entity : entities) {
                            Damage.makeHeal(owner, entity, heal);
                        }
                    }
                }
            }, interval, owner);
            owner.sendMessage("§e癒しの花園§aを生み出しました", SomSound.Heal);
        } else {
            stop();
        }
        return true;
    }

    public void stop() {
        task.cancel();
        task = null;
        owner.sendMessage("§e癒しの花園§aが枯れました", SomSound.Tick);
    }
}
