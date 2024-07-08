package SwordofMagic11.Player;

import SwordofMagic11.Component.SomParticle;
import SwordofMagic11.Component.SomRay;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Component.SomTask;
import SwordofMagic11.Custom.CustomLocation;
import SwordofMagic11.Entity.Damage;
import SwordofMagic11.Entity.SearchEntity;
import SwordofMagic11.Entity.SomEntity;
import SwordofMagic11.Item.SomEquip;
import SwordofMagic11.Skill.ParamType;
import SwordofMagic11.Skill.Parameter;
import org.bukkit.Particle;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;

import static SwordofMagic11.Component.Function.randomDouble;

public interface NormalAttack {
    HashMap<PlayerData, BukkitTask> coolTime = new HashMap<>();
    HashMap<SomEquip.Category, Double> range = new HashMap<>() {{
        put(SomEquip.Category.Dagger, 4.0);
        put(SomEquip.Category.Sword, 5.0);
        put(SomEquip.Category.Rod, 15.0);
        put(SomEquip.Category.Bow, 20.0);
        put(SomEquip.Category.Mace, 10.0);
    }};

    HashMap<SomEquip.Category, Integer> tick = new HashMap<>() {{
        put(SomEquip.Category.Dagger, 10);
        put(SomEquip.Category.Sword, 12);
        put(SomEquip.Category.Rod, 15);
        put(SomEquip.Category.Bow, 18);
        put(SomEquip.Category.Mace, 15);
    }};

    PlayerData playerData();

    default void normalAttack() {
        normalAttack(null);
    }

    default int getCoolTime(SomEquip.Category category) {
        PlayerData playerData = playerData();
        int tick = this.tick.get(category);
        switch (category) {
            case Sword -> {
                if (playerData.hasSkill("WideAttack")) {
                    Parameter parameter = playerData.getSkillParam("WideAttack");
                    tick += parameter.getCoolTime();
                }
            }
            case Dagger -> {
                if (playerData.hasSkill("QuickStrike")) {
                    Parameter parameter = playerData.getSkillParam("QuickStrike");
                    tick += parameter.getCoolTime();
                }
            }
            case Bow -> {
                if (playerData.hasSkill("QuickFire")) {
                    Parameter parameter = playerData.getSkillParam("QuickFire");
                    tick += parameter.getCoolTime();
                }
            }
            case Rod, Mace -> {
                if (playerData.hasSkill("DiffusionTech")) {
                    Parameter parameter = playerData.getSkillParam("DiffusionTech");
                    tick += parameter.getCoolTime();
                }
            }
        }
        return tick;
    }

    default void normalAttack(SomEntity target) {
        SomTask.async(() -> {
            PlayerData playerData = playerData();
            if (!coolTime.containsKey(playerData) && playerData.hasEquip(SomEquip.Slot.MainHand)) {
                SomEquip.Category category = playerData.getEquip(SomEquip.Slot.MainHand).getEquipCategory();
                if (category.isWeapon()) {
                    coolTime.put(playerData, SomTask.asyncDelay(() -> coolTime.remove(playerData), getCoolTime(category)));
                    CustomLocation position;
                    SomEntity entity = target;
                    if (entity == null) {
                        SomRay ray = SomRay.rayLocationEntity(playerData, range.get(category), 0.5, playerData.enemies(), false);
                        position = ray.getOriginPosition();
                        if (ray.isHitEntity()) entity = ray.getHitEntity();
                    } else {
                        position = entity.getEyeLocation();
                    }
                    double addition = 0;
                    if (playerData.hasSkill("BasicTraining")) {
                        Parameter parameter = playerData.getSkillParam("BasicTraining");
                        addition += parameter.getParam(ParamType.Damage);
                    }
                    if (playerData.hasSkill("AdventurerSecret")) {
                        Parameter parameter = playerData.getSkillParam("AdventurerSecret");
                        addition += parameter.getParam(ParamType.Damage);
                    }
                    switch (category) {
                        case Dagger -> {
                            SomSound.Dagger.play(playerData);
                            if (entity != null) {
                                Damage.makeDamage(playerData, entity, Damage.Type.Physics, 1 + addition);
                            }
                        }
                        case Sword -> {
                            SomSound.Sword.play(playerData);
                            if (playerData.hasSkill("WideAttack")) {
                                Parameter parameter = playerData.getSkillParam("WideAttack");
                                double damage = 1 + parameter.getParam(ParamType.Damage) + addition;
                                double range = parameter.getParam(ParamType.Range);
                                double angle = parameter.getParam(ParamType.Angle);
                                SomParticle particle = new SomParticle(Particle.CRIT, playerData);
                                particle.fanShaped(playerData.getLocation(), range, angle);
                                for (SomEntity enemy : SearchEntity.fanShapedSomEntity(playerData.enemies(), playerData.getLocation(), range, angle)) {
                                    Damage.makeDamage(playerData, enemy, Damage.Type.Physics, damage);
                                }
                            } else if (entity != null) {
                                Damage.makeDamage(playerData, entity, Damage.Type.Physics, 1 + addition);
                            }
                        }
                        case Rod -> {
                            SomParticle particle = new SomParticle(Particle.CRIT_MAGIC, playerData);
                            if (playerData.hasSkill("DiffusionTech")) {
                                Parameter parameter = playerData.getSkillParam("DiffusionTech");
                                double damage = (1 + addition) * parameter.getParam(ParamType.Damage);
                                CustomLocation pivot = playerData.getEyeLocation().left(1);
                                CustomLocation pivot2 = playerData.getHandLocation().left(1);
                                SomSound.Rod.play(playerData);
                                for (int i = 0; i < 3; i++) {
                                    SomRay ray = SomRay.rayLocationEntity(pivot, range.get(category), 0.5, playerData.enemies(), false);
                                    particle.line(pivot2, ray.getOriginPosition());
                                    if (ray.isHitEntity()) {
                                        Damage.makeDamage(playerData, ray.getHitEntity(), Damage.Type.Magic, damage);
                                    }
                                    pivot.right(1);
                                    pivot2.right(1);
                                }
                            } else {
                                particle.line(playerData.getHandLocation(), position);
                                SomSound.Rod.play(playerData);
                                if (entity != null) {
                                    Damage.makeDamage(playerData, entity, Damage.Type.Magic, 1 + addition);
                                }
                            }
                        }
                        case Bow -> {
                            SomParticle particle = new SomParticle(Particle.CRIT, playerData);
                            particle.line(playerData.getHandLocation(), position);
                            SomSound.Bow.play(playerData);
                            double damage = 1 + addition;
                            if (entity != null) {
                                Damage.makeDamage(playerData, entity, Damage.Type.Shoot, damage);
                            }
                            if (playerData.hasSkill("QuickFireExtend")) {
                                Parameter parameter = playerData.getSkillParam("QuickFireExtend");
                                double percent = parameter.getParam(ParamType.Percent);
                                if (randomDouble() > percent) {
                                    SomTask.asyncDelay(() -> {
                                        SomRay ray = SomRay.rayLocationEntity(playerData, range.get(category), 0.5, playerData.enemies(), false);
                                        particle.line(playerData.getHandLocation(), ray.getOriginPosition());
                                        SomSound.Bow.play(playerData);
                                        if (ray.isHitEntity()) {
                                            Damage.makeDamage(playerData, ray.getHitEntity(), Damage.Type.Shoot, damage);
                                        }
                                    }, 4);
                                }
                            }
                        }
                        case Mace -> {
                            SomParticle particle = new SomParticle(Particle.CRIT_MAGIC, playerData);
                            particle.line(playerData.getHandLocation(), position);
                            SomSound.Mace.play(playerData);
                            if (playerData.hasSkill("DiffusionTech")) {
                                Parameter parameter = playerData.getSkillParam("DiffusionTech");
                                double damage = (1 + addition) * parameter.getParam(ParamType.Damage);
                                CustomLocation pivot = playerData.getEyeLocation().left(1);
                                CustomLocation pivot2 = playerData.getHandLocation().left(1);
                                SomSound.Rod.play(playerData);
                                for (int i = 0; i < 3; i++) {
                                    SomRay ray = SomRay.rayLocationEntity(pivot, range.get(category), 0.5, playerData.enemies(), false);
                                    particle.line(pivot2, ray.getOriginPosition());
                                    if (ray.isHitEntity()) {
                                        Damage.makeDamage(playerData, ray.getHitEntity(), Damage.Type.Magic, damage);
                                    }
                                    pivot.right(1);
                                    pivot2.right(1);
                                }
                            } else if (entity != null) {
                                Damage.makeDamage(playerData, entity, Damage.Type.Magic, 1 + addition);
                            }
                        }
                    }
                }
            }
        });
    }
}
