package SwordofMagic11.Skill.Process;

import SwordofMagic11.Component.SomParticle;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.DataBase.SkillDataLoader;
import SwordofMagic11.Enemy.EnemyData;
import SwordofMagic11.Entity.SearchEntity;
import SwordofMagic11.Entity.SomEffect;
import SwordofMagic11.Entity.SomEntity;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Skill.ParamType;
import SwordofMagic11.Skill.Parameter;
import SwordofMagic11.Skill.SkillData;
import SwordofMagic11.Skill.SomSkill;
import SwordofMagic11.StatusType;
import org.bukkit.Particle;

import java.util.HashSet;
import java.util.Set;

public class NearAura extends SomSkill {
    public NearAura(PlayerData owner, SkillData skillData) {
        super(owner, skillData);
    }

    @Override
    public boolean active() {
        Parameter parameter = getParam();
        double hate = parameter.getParam(ParamType.Hate);
        double radius = parameter.getParam(ParamType.Radius);
        SomParticle particle = new SomParticle(Particle.VILLAGER_ANGRY, owner).setSpeed(0.2f);
        SomSound.Horn.radius(owner);
        Set<EnemyData> enemyList = new HashSet<>();
        for (SomEntity entity : SearchEntity.nearSomEntity(owner.enemies(), owner.getLocation(), radius)) {
            if (entity instanceof EnemyData enemyData) {
                enemyData.addHate(owner, owner.getStatus(StatusType.MaxHealth) * hate);
                enemyData.addTakeDamage(owner, 0);
                particle.random(entity.getEyeLocation(), 5);
                enemyList.add(enemyData);
            }
        }
        if (owner.hasSkill("NearAuraSpeedUp")) {
            if (owner.getMap().getId().equals("DefenseBattle")) {
                owner.addMana(owner.getStatus(StatusType.ManaRegen) * enemyList.size());
            } else {
                Parameter parameter2 = owner.getSkillParam("NearAuraSpeedUp");
                double value = parameter2.getParam(ParamType.Value);
                int time = parameter2.getParamInt(ParamType.Time);
                SomEffect effect = new SomEffect(SkillDataLoader.getSkillData("NearAuraSpeedUp"), time, owner);
                effect.setLog(false);
                effect.setStatusMultiply(StatusType.Movement, value);
                for (EnemyData enemyData : enemyList) {
                    enemyData.addEffect(effect);
                }
            }
        }
        return true;
    }
}
