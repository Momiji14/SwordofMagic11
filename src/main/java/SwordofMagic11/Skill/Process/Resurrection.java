package SwordofMagic11.Skill.Process;

import SwordofMagic11.Component.SomParticle;
import SwordofMagic11.Component.SomRay;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Skill.ParamType;
import SwordofMagic11.Skill.Parameter;
import SwordofMagic11.Skill.SkillData;
import SwordofMagic11.Skill.SomSkill;
import org.bukkit.Particle;

public class Resurrection extends SomSkill {
    public Resurrection(PlayerData owner, SkillData skillData) {
        super(owner, skillData);
    }

    @Override
    public boolean active() {
        Parameter parameter = getParam();
        double range = parameter.getParam(ParamType.Range);
        SomParticle particle = new SomParticle(Particle.FIREWORK, owner).setSpeed(0.2f).setRandomVector();
        SomRay ray = SomRay.rayLocationEntity(owner, range, 1, owner.alliesIsDeath(), false);
        if (ray.isHitEntity() && ray.getHitEntity() instanceof PlayerData playerData) {
            particle.line(owner.getHandLocation(), playerData.getHipsLocation());
            particle.random(playerData.getHipsLocation());
            playerData.resurrection(owner);
            SomSound.Heal.radius(playerData);
        } else {
            owner.sendMessage("§c蘇生対象がいません", SomSound.Nope);
            owner.addMana(getManaCost());
            setCurrentCoolTime(1);
        }
        return true;
    }
}
