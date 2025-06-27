package SwordofMagic11.Enemy.Custom;

import SwordofMagic11.Component.*;
import SwordofMagic11.Custom.CustomLocation;
import SwordofMagic11.Enemy.MobData;
import SwordofMagic11.Entity.Damage;
import SwordofMagic11.Entity.SearchEntity;
import SwordofMagic11.Entity.SomEntity;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.StatusType;
import kotlin.Pair;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;

public class Bleass extends CustomData {
    public Bleass(MobData mobData, int level, Location location) {
        super(mobData, level, location);
        ignoreCrowdControl(true);
        for (Galaxy galaxy : Galaxies) {
            galaxy.Active(true);
        }
        timer("CosmicRay", 300);
        timer("MeteorShower", 300);
        timer("MinHPChecker", 20);
    }

    @Override
    public void tick() {
        if (!hasTimer("DeathChecker")) DeathChecker();
        if (!hasTimer("MinHPChecker")) MinHPChecker();
        if (!hasTimer("RideGalaxy")) RideGalaxy();

        if (!hasTimer("CosmicEffect")) CosmicEffect();

        if (!hasTimer("CosmicRay")) CosmicRay();
        if (!hasTimer("Inhale")) Inhale();
        if (!hasTimer("MeteorShower")) MeteorShower();

        if (!hasTimer("Debug")) ;
    }

    @Override
    public synchronized void death(SomEntity killer) {
        super.death(killer);
        ActiveGalaxies().forEach(galaxy -> galaxy.Active(false));
    }

    final int InhaleCT = 440;
    final int MeteorShowerCT = 390;
    final int CosmicRayCT = 580;
    int DeathCount = 0;
    Set<SomEntity> DeathCheckList = new HashSet<>();

    List<Pair<Double, String>> HealthPercentEventList = new ArrayList<>() {
        {
            add(new Pair<>(0.9, "weak"));
            add(new Pair<>(0.8, "weak"));
            add(new Pair<>(0.7, "weak"));
            add(new Pair<>(0.6, "weak"));
            add(new Pair<>(0.5, "hyperInhale"));
            add(new Pair<>(0.4, "strong"));
            add(new Pair<>(0.3, "strong"));
            add(new Pair<>(0.2, "strong"));
            add(new Pair<>(0.1, "strong"));
        }
    };

    void DeathChecker() {
        timer("DeathChecker", 5);

        DeathCheckList.addAll(enemies());
        DeathCheckList.removeIf((target) -> {
            if (target.getWorld() != getWorld()) {
                DeathCount++;
                PlayerDead();
                return true;
            }
            return false;
        });
    }

    void PlayerDead() {
        if (DeathCount >= 5) {
            Ender();
        }
    }

    void Ender() {
        playSound(SomSound.BossCharge1);

        SomParticle signParticle = new SomParticle(Particle.SOUL_FIRE_FLAME, this);
        signParticle.setShrink();
        signParticle.setSpeed(16);
        signParticle.sphere(getLocation(), 32);

        Collection<PlayerData> list = new ArrayList<>();
        for (SomEntity target : enemies()) {
            list.add((PlayerData) target);
        }
        SomTask.asyncDelay(() -> list.forEach((data) -> data.deathInstantProcess(this)), 40);
    }

    void MinHPChecker() {
        timer("MinHPChecker", 5);
        if (HealthPercentEventList.isEmpty()) return;

        if (HealthPercentEventList.get(0).component1() >= healthPercent()) {
            switch (HealthPercentEventList.get(0).component2()) {
                case "weak" -> WeakDestructiveWave();
                case "strong" -> StrongDestructiveWave();
                case "hyperInhale" -> HyperInhale();
            }
            HealthPercentEventList.remove(0);
        }
    }

    void RideGalaxy() {
        timer("RideGalaxy", 5);

        for (Galaxy galaxy : ActiveGalaxies()) {
            galaxy.RideClickTick();
        }
    }

    void CosmicEffect() {
        timer("CosmicEffect", 10);

        for (SomEntity enemy : enemies()) {
            enemy.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 20, 3));
            enemy.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 20, 1));
        }
    }

    void Inhale() {
        timer("Inhale", InhaleCT);

        double radius = 16;
        SomParticle signParticle = new SomParticle(Color.RED, this);
        SomParticle atkParticle = new SomParticle(Particle.SCRAPE, this);
        atkParticle.setShrink();
        atkParticle.setSpeed(2);

        signParticle.sphere(getLocation(), radius);
        setNoAI();

        SomTask.asyncCount(() -> {
            //loop
            atkParticle.sphere(getLocation(), radius);
            for (SomEntity target : SearchEntity.nearSomEntity(enemies(), getLocation(), radius)) {
                Vector vec = getLocation().toVector().subtract(target.getLocation().toVector()).normalize();
                target.setVelocity(vec);
                Damage.makeDamage(this, target, Damage.Type.Shoot, 0.5);
                addHealth(this.getStatus(StatusType.MaxHealth) * 0.0002);
            }
        }, 10, 14, 20, () -> {
            //end
            for (SomEntity target : SearchEntity.nearSomEntity(enemies(), getLocation(), radius)) {
                Vector vec = target.getLocation().toVector().subtract(getLocation().toVector()).normalize().multiply(2);
                target.setVelocity(vec);
                Damage.makeDamage(this, target, Damage.Type.Shoot, 6);
                addHealth(this.getStatus(StatusType.MaxHealth) * 0.001);
            }
            resetNoAI();
        }, this);
    }

    void HyperInhale() {
        timer("Inhale", InhaleCT);

        double radius = 64;
        double dRadius = 16;    //ダメージ範囲
        SomParticle signParticle = new SomParticle(Color.RED, this);
        SomParticle atkParticle = new SomParticle(Particle.SCRAPE, this);
        atkParticle.setShrink();
        atkParticle.setSpeed(2);

        signParticle.sphere(getLocation(), radius);
        setNoAI();

        final double[] pullPower = {0.1};

        SomTask.asyncCount(() -> {
            //loop
            atkParticle.sphere(getLocation(), radius);
            for (SomEntity target : SearchEntity.nearSomEntity(enemies(), getLocation(), radius)) {
                Vector vec = getLocation().toVector().subtract(target.getLocation().toVector()).normalize();
                vec = vec.multiply(pullPower[0]);
                target.setVelocity(vec);
                //Damage.makeDamage(this, target, Damage.Type.Shoot, 0.5);
                //addHealth(this.getStatus(StatusType.MaxHealth) * 0.0002);
            }
            pullPower[0] +=0.1;
        }, 10, 14, 20, () -> {
            //end
            for (SomEntity target : SearchEntity.nearSomEntity(enemies(), getLocation(), dRadius)) {
                Vector vec = target.getLocation().toVector().subtract(getLocation().toVector()).normalize().multiply(2);
                target.setVelocity(vec);
                Damage.makeDamage(this, target, Damage.Type.Shoot, 50);
                addHealth(this.getStatus(StatusType.MaxHealth) * 0.1);
            }
            resetNoAI();
        }, this);
    }

    void MeteorShower() {
        timer("MeteorShower", MeteorShowerCT);

        SomParticle meteorParticle1 = new SomParticle(Particle.FLAME, this);
        SomParticle meteorParticle2 = new SomParticle(Particle.GUST_EMITTER_LARGE, this);
        PlayerData target = (PlayerData) SearchEntity.farthestSomEntity(enemies(), getLocation());

        if (target != null) {
            SomSound.BleassAtk2.radius(target);

            SomTask.asyncCountIf(() -> {
                //loop
                CustomLocation loc = target.getLocation().addY(10);
                Vector vec = target.getVelocity();
                if (!vec.setY(0).isZero()) loc.add(vec.normalize().multiply(5));
                loc.setDirection(new Vector(0, -1, 0));
                SomTask.asyncCountIf(() -> {
                    //隕石処理のloop
                    meteorParticle1.sphere(loc, 1);
                    loc.addY(-1.5);
                    SomSound.Fire.radius(loc);
                }, 5, 30, 0, () -> {
                    //隕石処理のcondition
                    return (!SomRay.rayLocationBlock(loc, 0.25, true).isHitBlock()
                            && SearchEntity.nearSomEntity(enemies(), loc, 1).isEmpty());
                }, () -> {
                    //隕石処理のend
                    meteorParticle2.spawn(loc);
                    for (SomEntity entity : SearchEntity.nearSomEntity(enemies(), loc, 5)) {
                        SomSound.Explode.radius(loc);
                        entity.setVelocity(entity.getLocation().toVector().subtract(loc.toVector()).add(new Vector(0, 1, 0)).normalize().multiply(1.5));
                        Damage.makeDamage(this, entity, Damage.Type.Shoot, 4);
                    }
                }, this);
            }, 20, 5, 20, () -> {
                //condition
                return (target.getWorld() == getWorld());
            }, () -> {/*end*/}, this);
        }
    }

    void CosmicRay() {
        timer("CosmicRay", CosmicRayCT);

        playSound(SomSound.BossWarn1);
        SomTask.asyncDelay(() -> {
            int cnt = 0;
            for (Galaxy galaxy : ActiveGalaxies()) {
                SomTask.asyncDelay(galaxy::RayShoot, cnt);
                cnt += 2;
            }
        }, 20);

    }

    void WeakDestructiveWave() {
        playSound(SomSound.BleassAtk1);

        SomTask.asyncDelay(() -> {
            List<Galaxy> list = ActiveGalaxies();

            for (int i = 0; i < 5; i++) {
                if (!list.isEmpty()) {
                    Galaxy random = Function.randomGet(list);
                    random.Active(false);
                    list.remove(random);
                }
            }
            for (SomEntity target : enemies()) {
                Damage.makeDamage(this, target, Damage.Type.Physics, target.getStatus(StatusType.MaxHealth) / 2, 0, 0, 1, 1, 1);
            }
        }, 15);
    }

    void StrongDestructiveWave() {
        playSound(SomSound.BleassAtk1);

        SomTask.asyncDelay(() -> {
            List<Galaxy> list = ActiveGalaxies();
            Collection<PlayerData> targets = enemiesPlayer();

            for(Galaxy galaxy : list){
                if (galaxy.rider != null){
                    targets.remove(galaxy.rider);
                    galaxy.Active(false);
                }
            }
            for(PlayerData target: targets){
                target.deathInstantProcess(this);
            }
        }, 100);
    }

    final Galaxy[] Galaxies = {
            new Galaxy(new CustomLocation(getWorld(), 14.5, 100, -0.5), this),
            new Galaxy(new CustomLocation(getWorld(), 8.5, 97, 5.5), this),
            new Galaxy(new CustomLocation(getWorld(), 22.5, 103, 8.5), this),
            new Galaxy(new CustomLocation(getWorld(), 17.5, 96, 13.5), this),
            new Galaxy(new CustomLocation(getWorld(), 22.5, 101, 19.5), this),
            new Galaxy(new CustomLocation(getWorld(), 8.5, 113, 19.5), this),
            new Galaxy(new CustomLocation(getWorld(), 13.5, 89, 27.5), this),
            new Galaxy(new CustomLocation(getWorld(), 27.5, 106, 30.5), this),
            new Galaxy(new CustomLocation(getWorld(), 21.5, 99, 35.5), this),
            new Galaxy(new CustomLocation(getWorld(), 12.5, 117, 31.5), this),
            new Galaxy(new CustomLocation(getWorld(), 16.5, 106, 40.5), this),
            new Galaxy(new CustomLocation(getWorld(), 13.5, 90, 45.5), this),
            new Galaxy(new CustomLocation(getWorld(), 2.5, 122, 34.5), this),
            new Galaxy(new CustomLocation(getWorld(), 5.5, 113, 44.5), this),
            new Galaxy(new CustomLocation(getWorld(), 8.5, 100, 50.5), this),
            new Galaxy(new CustomLocation(getWorld(), 0.5, 97, 49.5), this),
            new Galaxy(new CustomLocation(getWorld(), -2.5, 108, 61.5), this),
            new Galaxy(new CustomLocation(getWorld(), -5.5, 119, 52.5), this),
            new Galaxy(new CustomLocation(getWorld(), -10.5, 101, 57.5), this),
            new Galaxy(new CustomLocation(getWorld(), -12.5, 111, 45.5), this),
            new Galaxy(new CustomLocation(getWorld(), -19.5, 101, 66.5), this),
            new Galaxy(new CustomLocation(getWorld(), -21.5, 93, 51.5), this),
            new Galaxy(new CustomLocation(getWorld(), -25.5, 117, 50.5), this),
            new Galaxy(new CustomLocation(getWorld(), -29.5, 107, 55.5), this),
            new Galaxy(new CustomLocation(getWorld(), -33.5, 100, 51.5), this),
            new Galaxy(new CustomLocation(getWorld(), -46.5, 107, 52.5), this),
            new Galaxy(new CustomLocation(getWorld(), -41.5, 98, 43.5), this),
            new Galaxy(new CustomLocation(getWorld(), -35.5, 118, 41.5), this),
            new Galaxy(new CustomLocation(getWorld(), -56.5, 102, 40.5), this),
            new Galaxy(new CustomLocation(getWorld(), -54.5, 108, 36.5), this),
            new Galaxy(new CustomLocation(getWorld(), -45.5, 105, 32.5), this),
            new Galaxy(new CustomLocation(getWorld(), -53.5, 92, 33.5), this),
            new Galaxy(new CustomLocation(getWorld(), -32.5, 115, 29.5), this),
            new Galaxy(new CustomLocation(getWorld(), -61.5, 98, 26.5), this),
            new Galaxy(new CustomLocation(getWorld(), -47.5, 99, 26.5), this),
            new Galaxy(new CustomLocation(getWorld(), -69.5, 105, 22.5), this),
            new Galaxy(new CustomLocation(getWorld(), -35.5, 120, 14.5), this),
            new Galaxy(new CustomLocation(getWorld(), -52.5, 113, 15.5), this),
            new Galaxy(new CustomLocation(getWorld(), -63.5, 100, 15.5), this),
            new Galaxy(new CustomLocation(getWorld(), -69.5, 95, 9.5), this),
            new Galaxy(new CustomLocation(getWorld(), -50.5, 103, 4.5), this),
            new Galaxy(new CustomLocation(getWorld(), -60.5, 104, 2.5), this),
            new Galaxy(new CustomLocation(getWorld(), -53.5, 90, 0.5), this),
            new Galaxy(new CustomLocation(getWorld(), -63.5, 95, -1.5), this),
            new Galaxy(new CustomLocation(getWorld(), -51.5, 99, -6.5), this),
            new Galaxy(new CustomLocation(getWorld(), -39.5, 87, -7.5), this),
            new Galaxy(new CustomLocation(getWorld(), -36.5, 102, -12.5), this),
            new Galaxy(new CustomLocation(getWorld(), -47.5, 94, -13.5), this),
            new Galaxy(new CustomLocation(getWorld(), -24.5, 99, -24.5), this),
            new Galaxy(new CustomLocation(getWorld(), -29.5, 93, -15.5), this),
    };

    class Galaxy {
        Bleass bleass;
        boolean active;  //銀河として使えるか
        PlayerData rider = null;
        final CustomLocation location;
        final List<BlockDisplay> displays = new ArrayList<>();

        public Galaxy(CustomLocation loc, Bleass bleass) {
            this.bleass = bleass;
            active = true;
            location = loc;
            for (Entity entity : location.getNearbyEntities(3, 3, 3)) {
                if (entity instanceof BlockDisplay display) {
                    displays.add(display);
                }
            }
            Active(true);
        }

        void Show(boolean bool) {
            if (bool) {
                displays.forEach(display -> {
                    display.setBlock(Material.SOUL_FIRE.createBlockData());
                    display.setViewRange(1.0f);
                });
            } else {
                displays.forEach(display -> {
                    display.setBlock(Material.FIRE.createBlockData());
                    SomTask.asyncDelay(() -> display.setViewRange(0.0f), 20);
                });
            }
        }

        void Active(boolean bool) {
            active = bool;
            Show(bool);
            if (!bool && rider != null) {
                rider.resetLeftClickOverride();
                SomTask.sync(() -> {
                    displays.get(0).removePassenger(rider.getEntity());
                    rider = null;
                });
            }
        }

        void RayShoot() {
            if (rider != null) return;

            CustomLocation loc = new CustomLocation(location);
            SomParticle atkParticle = new SomParticle(Particle.SOUL_FIRE_FLAME, bleass);
            double speed = 15.0;
            loc.setDirection(new Vector(Function.randomDouble(-1, 1), Function.randomDouble(-1, 1), Function.randomDouble(-1, 1)));

            SomTask.asyncCountIf(() -> {
                //loop
                atkParticle.line(loc, speed, 0, 4);
                SomRay.rayLocationBlock(loc, speed, true);
            }, 3, 70, 0, () -> {
                //condition
                SomRay ray = SomRay.rayLocationBlock(loc, speed, true);
                if (ray.isHitBlock()) {
                    Vector dir = loc.getDirection();
                    loc.setLocation(ray.getHitPosition());

                    Vector face = ray.getHitBlockFace().getDirection();
                    if (face.getX() != 0) dir.setX(dir.getX() * -1);
                    if (face.getY() != 0) dir.setY(dir.getY() * -1);
                    if (face.getZ() != 0) dir.setZ(dir.getZ() * -1);
                    dir = dir.normalize().add(bleass.getLocation().toVector().subtract(loc.toVector()).divide(new Vector(2, 2, 2)).normalize());
                    loc.setDirection(dir);
                    loc.add(loc.getDirection().normalize());
                } else {
                    loc.add(loc.getDirection().multiply(speed));
                }
                return SearchEntity.nearSomEntity(enemies(), loc, 3).isEmpty();
            }, () -> {
                //end
                new SomParticle(Particle.SONIC_BOOM, bleass).spawn(loc);
                for (SomEntity target : SearchEntity.nearSomEntity(enemies(), loc, 3)) {
                    Damage.makeDamage(bleass, target, Damage.Type.Shoot, 2.5);
                }
            }, bleass);
        }

        void RideClickTick() {
            if (rider != null) {
                if (!displays.get(0).getPassengers().contains(rider.getEntity())) {
                    rider.resetLeftClickOverride();
                    rider = null;
                    displays.forEach(display -> display.setBlock(Material.SOUL_FIRE.createBlockData()));
                } else {
                    //ここからleftClickOverride
                    rider.leftClickOverride(() -> {
                        if (rider.hasTimer("BleassGalaxyRay")) return;

                        rider.timer("BleassGalaxyRay", 30);
                        CustomLocation loc = new CustomLocation(rider.getEyeLocation());
                        SomParticle atkParticle = new SomParticle(Particle.END_ROD, bleass);
                        double speed = 10.0;

                        SomTask.asyncCountIf(() -> {
                            //loop
                            atkParticle.line(loc, speed, 0, 4);
                            SomRay.rayLocationBlock(loc, speed, true);
                        }, 3, 70, 0, () -> {
                            //condition
                            if (rider == null) return false;

                            SomRay ray = SomRay.rayLocationBlock(loc, speed, true);
                            if (ray.isHitBlock()) {
                                Vector dir = loc.getDirection();
                                loc.setLocation(ray.getHitPosition());

                                Vector face = ray.getHitBlockFace().getDirection();
                                if (face.getX() != 0) dir.setX(dir.getX() * -1);
                                if (face.getY() != 0) dir.setY(dir.getY() * -1);
                                if (face.getZ() != 0) dir.setZ(dir.getZ() * -1);
                                loc.setDirection(dir);
                                loc.add(loc.getDirection().normalize());
                            } else {
                                loc.add(loc.getDirection().multiply(speed));
                            }
                            return SearchEntity.nearSomEntity(rider.enemies(), loc, 3).isEmpty();
                        }, () -> {
                            //end
                            if (rider == null) return;

                            new SomParticle(Particle.FLASH, bleass).spawn(loc);
                            for (SomEntity target : SearchEntity.nearSomEntity(rider.enemies(), loc, 3)) {
                                Damage.Type type = Damage.Type.Physics;
                                switch (rider.getHighestAttackStatusType(rider.getFinalStatus())) {
                                    case ATK -> type = Damage.Type.Physics;
                                    case MAT -> type = Damage.Type.Magic;
                                    case SAT -> type = Damage.Type.Shoot;
                                    case SPT -> type = Damage.Type.Holy;
                                }
                                Damage.makeDamage(rider, target, type, 10);
                            }
                        }, bleass);
                    });
                    //ここまでleftClickOverride
                    displays.forEach(display -> display.setBlock(Material.GLASS.createBlockData()));
                }
            } else {
                double radius = 5;
                for (SomEntity entity : SearchEntity.nearSomEntity(bleass.enemies(), location, radius)) {
                    if (entity instanceof PlayerData target) {
                        if (!target.hasLeftClickOverride()) {
                            new SomParticle(Color.YELLOW, bleass).sphere(location, radius);
                            target.leftClickOverride(() -> {
                                if (rider == null) {
                                    displays.get(0).addPassenger(target.getEntity());
                                    rider = target;
                                    SomSound.Level.play(target);
                                } else {
                                    SomSound.Nope.play(target);
                                }
                            });
                            SomTask.asyncDelay(target::resetLeftClickOverride, 20);
                        }
                    }
                }
            }
        }
    }

    List<Galaxy> ActiveGalaxies() {
        List<Galaxy> list = new ArrayList<>();
        for (Galaxy galaxy : Galaxies) {
            if (galaxy.active) list.add(galaxy);
        }
        return list;
    }
}
