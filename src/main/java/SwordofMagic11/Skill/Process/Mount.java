package SwordofMagic11.Skill.Process;

import SwordofMagic11.Component.SomParticle;
import SwordofMagic11.Component.SomRay;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Component.SomTask;
import SwordofMagic11.Entity.SomEntity;
import SwordofMagic11.Pet.PetEntity;
import SwordofMagic11.Pet.SomPet;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Skill.ParamType;
import SwordofMagic11.Skill.Parameter;
import SwordofMagic11.Skill.SkillData;
import SwordofMagic11.Skill.SomSkill;
import org.bukkit.Color;

import java.util.ArrayList;
import java.util.List;

public class Mount extends SomSkill {
    public Mount(PlayerData owner, SkillData skillData) {
        super(owner, skillData);
    }

    @Override
    public boolean active() {
        Parameter parameter = getParam();
        double range = parameter.getParam(ParamType.Range);
        List<SomEntity> petList = new ArrayList<>();
        for (SomPet somPet : owner.petMenu().getSummonList()) {
            petList.add(somPet.getEntityIns());
        }
        SomRay ray = SomRay.rayLocationEntity(owner, range, 1, petList, false);
        if (ray.isHitEntity()) {
            SomEntity target = ray.getHitEntity();
            if (target instanceof PetEntity petEntity && petEntity.getOwner() == owner) {
                SomTask.sync(() -> petEntity.getEntity().addPassenger(owner.getPlayer()));
                SomSound.Equip.play(owner);
                return true;
            }
        }
        owner.sendMessage("§c対象が見つかりませんでした", SomSound.Nope);
        return true;
    }
}
