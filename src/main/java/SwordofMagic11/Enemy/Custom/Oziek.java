package SwordofMagic11.Enemy.Custom;

import SwordofMagic11.Component.SomParticle;
import SwordofMagic11.Component.SomTask;
import SwordofMagic11.Custom.CustomLocation;
import SwordofMagic11.Enemy.MobData;
import SwordofMagic11.Entity.Damage;
import SwordofMagic11.Entity.SearchEntity;
import SwordofMagic11.Entity.SomEntity;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;

import java.util.Collections;

import static SwordofMagic11.Component.Function.randomInt;

public class Oziek extends CustomData {

    public Oziek(MobData mobData, int level, Location location) {
        super(mobData, level, location);
        timer("GoldenRing", GoldenRingCT);
        timer("LightOfGlory", LightOfGloryCT);
        timer("Suppression", SuppressionCT);
    }

    CustomLocation[] posSLeft = {new CustomLocation(getWorld(), -14, 65, 63), new CustomLocation(getWorld(), -4, 65, -3)};
    CustomLocation[] posSCenter = {new CustomLocation(getWorld(), -4, 65, 63), new CustomLocation(getWorld(), 5, 65, -3)};
    CustomLocation[] posSRight = {new CustomLocation(getWorld(), 5, 65, 63), new CustomLocation(getWorld(), 15, 65, -3)};


    CustomLocation[] posRLeft = {new CustomLocation(getWorld(), -8.5, 65, 12), new CustomLocation(getWorld(), -8.5, 65, 62)};
    CustomLocation[] posRCenter = {new CustomLocation(getWorld(), 0.5, 65, 12), new CustomLocation(getWorld(), 0.5, 65, 62)};
    CustomLocation[] posRRight = {new CustomLocation(getWorld(), 9.5, 65, 12), new CustomLocation(getWorld(), 9.5, 65, 62)};

    CustomLocation[][] lanesS = {posSLeft, posSCenter, posSRight};
    CustomLocation[] signs = {new CustomLocation(getWorld(), -8.5, 74, 0.5), new CustomLocation(getWorld(), 0.5, 74, 0.5), new CustomLocation(getWorld(), 9.5, 74, 0.5)};

    @Override
    public void tick() {
        if (!hasTimer("GoldenRing")) GoldenRing();
        if (!hasTimer("LightOfGlory") && healthPercent() < 0.85) LightOfGlory();
        if (!hasTimer("Suppression") && healthPercent() < 0.7) Suppression();
        if (!FerociousFlg && healthPercent() <= 0.45) Ferocious();
    }

    private int GoldenRingCT = 8;
    private int LightOfGloryCT = 10;
    private int LightOfGloryFactor = 1;
    private int SuppressionCT = 18;
    private int SuppressionFactor = 1;
    private boolean FerociousFlg = false;

    public void GoldenRing() {
        if (FerociousFlg) {
            GoldenRing(posRLeft[0].randomSquareLocation(posRLeft[1]));
            GoldenRing(posRRight[0].randomSquareLocation(posRRight[1]));
        } else GoldenRing(posRCenter[0].randomSquareLocation(posRCenter[1]));

        timer("GoldenRing", GoldenRingCT * 20);
    }

    private void GoldenRing(CustomLocation targetLocation) {
        SomParticle atkParticle = new SomParticle(Color.YELLOW, this);

        final int[] i = {0};
        int n = 3;
        SomTask.asyncCount(() -> {
            if (i[0] != 0) atkParticle.circle(targetLocation, i[0]);
            atkParticle.circle(targetLocation, i[0] + n);
            for (SomEntity target : SearchEntity.nearSomEntity(enemies(), targetLocation, i[0], i[0] + n)) {
                if (target.getEntity().isOnGround()) {
                    Damage.makeDamage(this, target, Damage.Type.Magic, 2);
                }
            }
            i[0] += n;
        }, 8, 15, this);
    }

    int LightOfGloryDelay = 20;

    public void LightOfGlory() {
        SomParticle preFlashParticle = new SomParticle(Particle.ELECTRIC_SPARK, this);
        SomParticle flashParticle = new SomParticle(Particle.FLASH, this);

        SomTask.asyncCount(() -> preFlashParticle.circle(getLocation().addY(4), 0.1), 2, LightOfGloryDelay, () -> {
            flashParticle.circle(getLocation().addY(4), 0.2);
            for (SomEntity target : enemies()) {
                if (75 <= target.getLocation().getPitch()) continue;
                if (!SearchEntity.fanShapedSomEntity(Collections.singleton(this), target.getLocation(), 72, 90).isEmpty()) {
                    target.SLOWNESS(4, LightOfGloryCT / 2 * 20 * LightOfGloryFactor);
                    target.silence(1, this);
                }
            }
        }, this);

        timer("LightOfGlory", LightOfGloryCT * 20);
    }

    public void Suppression() {
        int randomIndex = randomInt(0, 2);

        if (FerociousFlg) {
            Suppression(randomIndex);
            Suppression((randomIndex + 1) % 3);
        } else Suppression(randomIndex);

        timer("Suppression", SuppressionCT * 20);
    }

    private void Suppression(int index) {
        if (2 < index) return;

        SomParticle preParticle = new SomParticle(Color.RED, this);
        SomParticle signParticle = new SomParticle(Particle.CAMPFIRE_SIGNAL_SMOKE, this);
        SomParticle atkParticle = new SomParticle(Particle.SMOKE, this);

        CustomLocation location1 = lanesS[index][0];
        CustomLocation location2 = lanesS[index][1];

        preParticle.square(location1, location2, 0.5);
        signParticle.sphere(signs[index], 2, 48);
        SomTask.asyncDelay(() -> {
            atkParticle.square(location1, location2, 0.5);
            for (SomEntity target : SearchEntity.squareXZSomEntity(enemies(), location1, location2)) {
                Damage.makeDamage(this, target, Damage.Type.Physics, 4);
                target.silence(2 * 20 * SuppressionFactor, this);
            }
        }, 40);
    }

    public void Ferocious() {
        GoldenRingCT = 6;

        LightOfGloryDelay = 10;
        LightOfGloryFactor = 2;

        SuppressionFactor = 2;

        FerociousFlg = true;
    }

}
