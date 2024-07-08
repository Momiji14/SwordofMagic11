package SwordofMagic11.Enemy.Custom;

import SwordofMagic11.Component.SomParticle;
import SwordofMagic11.Component.SomRay;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Component.SomTask;
import SwordofMagic11.Custom.CustomLocation;
import SwordofMagic11.Enemy.MobData;
import SwordofMagic11.Entity.Damage;
import SwordofMagic11.Entity.SearchEntity;
import SwordofMagic11.Entity.SomEntity;
import SwordofMagic11.Player.PlayerData;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;

import java.util.*;

import static SwordofMagic11.Component.Function.*;
import static SwordofMagic11.SomCore.Log;

public class Ledosia extends CustomData {

    public Ledosia(MobData mobData, int level, Location location) {
        super(mobData, level, location);
    }

    @Override
    public void tick() {
        shellingWait--;
        if (shellingWait <= 0) shelling();
        if (snipeDance && healthPercent() <= 0.8) SnipeDance();
        if (russianRoulette && healthPercent() <= 0.5) RussianRoulette();
        if (areaOfRisk && healthPercent() <= 0.2) AreaOfRisk();
    }

    private int shellingWait = 10;
    public void shelling() {
        shellingWait = 10;
        SomTask.async(() -> {
            if (hasTarget()) {
                SomParticle particle = new SomParticle(Color.RED, this);
                CustomLocation location = getTarget().getHipsLocation();
                particle.line(getEyeLocation(), location, 0.5);
                particle.circle(location, 3);
                for (SomEntity entity : SearchEntity.nearSomEntity(enemies(), location, 3)) {
                    Damage.makeDamage(this, entity, Damage.Type.Magic, 2);
                }
            }
        });
        SomTask.asyncCount(() -> AreaOfRiskTick(false), 3, 8, this);
    }

    private boolean snipeDance = true;
    public void SnipeDance() {
        snipeDance = false;
        sendMessage("§c「生きるために引き金を引く」", SomSound.BossWarn1);
        SomTask.asyncDelay(() -> SomTask.asyncCount(this::SnipeDanceTick, 1, 200, this), 60);

    }

    public void SnipeDanceTick() {
        for (SomEntity target : enemies()) {
            if (target.isDeath()) continue;
            SomTask.async(() -> {
                final SomParticle shotParticle = new SomParticle(Particle.FIREWORKS_SPARK, this);
                CustomLocation start = getEyeLocation().clone();
                start.look(target.getEyeLocation());
                SomTask.asyncDelay(() -> {
                    SomRay ray = SomRay.rayLocationEntity(start, 70, 1.0, enemies(), true);
                    shotParticle.line(start, 70, 1.0, 2);
                    for (SomEntity hitEntity : ray.getHitEntities()) {
                        Damage.makeDamage(this, hitEntity, Damage.Type.Physics, 0.2);
                        SomSound.Fire.play(hitEntity);
                    }
                }, 15);
            });
        }
    }

    private boolean areaOfRisk = true;
    public void AreaOfRisk() {
        areaOfRisk = false;
        sendMessage("§c「息を吸う暇もない」", SomSound.BossWarn1);
        SomTask.asyncDelay(() -> SomTask.asyncCount(() -> AreaOfRiskTick(true), 2, 300, this), 60);
    }

    public void AreaOfRiskTick(boolean outArea) {
        final SomParticle shotParticle = new SomParticle(Particle.FIREWORKS_SPARK, this);
        final SomParticle predictParticle = new SomParticle(Color.RED, this);
        final SomParticle circleParticle = new SomParticle(Particle.LAVA, this);
        circleParticle.setVectorUp();
        SomTask.async(() -> {
            Collection<SomEntity> enemies = enemies();
            circleParticle.circle(spawnLocation, 35, 16);
            SomSound.Tick.play(enemies);
            double random = randomDouble(0, Math.PI*2);
            CustomLocation start = spawnLocation.clone().addY(1.8);
            start.addXZ(Math.cos(random) * 35, Math.sin(random) * 35);
            if (enemies.isEmpty()) {
                start.look(spawnLocation);
            } else {
                SomEntity target = randomGet(enemies);
                start.look(target.getEyeLocation());
            }
            start.addYaw((float) randomDouble(-10, 10));
            start.setPitch(0);

            if (outArea) for (SomEntity entity : enemies) {
                if (entity.getLocation().distance(spawnLocation) > 35 || entity.getLocation().getY() > spawnLocation.getY() + 8) {
                    CustomLocation startClone = start.clone().look(entity.getHipsLocation());
                    Damage.makeDamage(this, entity, Damage.Type.Magic, 2, 0.5);
                    shotParticle.line(startClone, entity.getHipsLocation(), 0.35, 3);
                    SomSound.Fire.play(entity);
                }
            }

            SomTask.asyncCount(() -> predictParticle.line(start, 70, 0.35, 4), 5, 2, () -> {
                SomSound.Handgun.play(enemies());
                SomRay ray = SomRay.rayLocationEntity(start, 70, 0.35, enemies(), true);
                shotParticle.line(start, ray.getOriginPosition(), 0.35, 3);
                for (SomEntity hitEntity : ray.getHitEntities()) {
                    Damage.makeDamage(this, hitEntity, Damage.Type.Physics, 0.5);
                }
            }, this);
        });
    }

    private boolean russianRoulette = true;
    private final String[] russianRouletteColor = {"§e", "§6", "§c", "§d"};
    public void RussianRoulette() {
        russianRoulette = false;
        sendMessage("§c「危険には価値がある」", SomSound.BossWarn1);
        Location[] russianRouletteLocation = {
                new Location(getWorld(), 0.5, 32, 15.5),
                new Location(getWorld(), -12.5, 32, 18.5),
                new Location(getWorld(), -22.5, 32, 28.5),
                new Location(getWorld(), -25.5, 32, 41.5),
        };
        SomTask.asyncDelay(() -> {
            HashMap<PlayerData, Integer> index = new HashMap<>();
            SomTask.sync(() -> {
                for (PlayerData playerData : enemiesPlayer()) {
                    int i = randomInt(0, 3);
                    index.put(playerData, i);
                    playerData.sendBossBarMessage("§e指定§aの" + russianRouletteColor[i] + "魔法陣§aへ§c避難§aしてください", 120, true);
                }
                SomTask.asyncDelay(() -> {
                    if (isInvalid()) return;
                    for (PlayerData playerData : enemiesPlayer()) {
                        if (index.containsKey(playerData)) {
                            int i = index.get(playerData);
                            if (russianRouletteLocation[i].distance(playerData.getLocation()) > 5) {
                                sendMessage(playerData.getDisplayName() + "§aが§c§nロシアンルーレット§aにより§4死亡§aしました", SomSound.Tick);
                                playerData.death(this);
                            } else {
                                playerData.sendMessage(russianRouletteColor[i] + "魔法陣§aが§c§nロシアンルーレット§aによる§4§n死§aを防ぎました", SomSound.Tick);
                            }
                        }
                    }
                }, 120);
            });
        }, 60);
    }
}
