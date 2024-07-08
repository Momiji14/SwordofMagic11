package SwordofMagic11.Skill.Process;

import SwordofMagic11.Component.SomRay;
import SwordofMagic11.Component.SomSQL;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Component.SomTask;
import SwordofMagic11.Entity.SomEntity;
import SwordofMagic11.Pet.PetEntity;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Skill.ParamType;
import SwordofMagic11.Skill.Parameter;
import SwordofMagic11.Skill.SkillData;
import SwordofMagic11.Skill.SomSkill;
import org.bukkit.entity.Entity;

public class PetLeap extends SomSkill {
    public PetLeap(PlayerData owner, SkillData skillData) {
        super(owner, skillData);
    }

    @Override
    public boolean active() {
        Parameter parameter = getParam();
        double value = parameter.getParam(ParamType.Value);
        if (owner.getPlayer().isInsideVehicle()) {
            Entity entity = owner.getPlayer().getVehicle();
            if (entity != null && SomEntity.is(entity) && SomEntity.instance(entity) instanceof PetEntity petEntity && petEntity.getOwner() == owner) {
                SomTask.skillTaskCount(() -> petEntity.setVelocity(owner.getDirection().multiply(value)), 1, 8, owner);
                SomSound.Push.radius(owner);
                return true;
            }
        }
        owner.sendMessage("§c騎乗していません", SomSound.Nope);
        return true;
    }
}
