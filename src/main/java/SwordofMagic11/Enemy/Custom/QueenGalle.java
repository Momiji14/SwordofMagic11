package SwordofMagic11.Enemy.Custom;

import SwordofMagic11.Component.Function;
import SwordofMagic11.Component.SomParticle;
import SwordofMagic11.Component.SomTask;
import SwordofMagic11.Custom.CustomLocation;
import SwordofMagic11.Enemy.MobData;
import SwordofMagic11.Entity.Damage;
import SwordofMagic11.Entity.SearchEntity;
import SwordofMagic11.Entity.SomEffect;
import SwordofMagic11.Entity.SomEntity;
import org.bukkit.Color;
import org.bukkit.Location;

import java.util.Collection;

public class QueenGalle extends CustomData {

    public QueenGalle(MobData mobData, int level, Location location) {
        super(mobData, level, location);
    }

    CustomLocation Pivot = new CustomLocation(getWorld(), 0, 9, 0);
    int Radius = 15;

    @Override
    public void tick() {
        if (!hasTimer("Spitting")) Spitting();

        if (!hasTimer("ThrowWeb") && healthPercent() <= 0.75) ThrowWeb();
        if (!hasTimer("RandomWeb") && healthPercent() <= 0.5) RandomWeb();
        if (!PrisonWebFlg && healthPercent() <= 0.33) PrisonWeb();
    }


    int SpittingCT = 3;//秒
    int ThrowWebCT = 5;//秒
    int RandomWebCT = 10;//秒
    boolean PrisonWebFlg = false;

    public void Spitting() {
        //sendMessage("Spitting");

        SomParticle atkParticle = new SomParticle(Color.PURPLE, this);

        atkParticle.fanShaped(getLocation(), 5, 90);
        for (SomEntity target : SearchEntity.fanShapedSomEntity(enemies(), getLocation(), 5, 90)) {
            Damage.makeDamage(this, target, Damage.Type.Shoot, 3);
        }

        timer("Spitting", SpittingCT * 20);
    }

    int ThrowWebRadius = 3;

    public void ThrowWeb() {
        //sendMessage("ThrowWeb");

        SomParticle preParticle = new SomParticle(Color.RED, this);
        SomParticle atkParticle = new SomParticle(Color.WHITE, this);

        Collection<SomEntity> enemies = enemies();
        if (!enemies.isEmpty()) {
            CustomLocation targetLocation = Function.randomGet(enemies()).getLocation();

            preParticle.circle(targetLocation, ThrowWebRadius);

            SomTask.asyncDelay(() -> {
                atkParticle.circle(targetLocation, ThrowWebRadius);
                atkParticle.circle(targetLocation, ThrowWebRadius * (2 / 3.0));
                atkParticle.circle(targetLocation, ThrowWebRadius * (1 / 3.0));
                for (SomEntity target : SearchEntity.nearSomEntity(enemies(), targetLocation, ThrowWebRadius)) {
                    target.SLOWNESS(6, ThrowWebCT * 20);
                }
            }, 20);

            timer("ThrowWeb", ThrowWebCT * 20);
        }
    }

    public void RandomWeb() {
        //sendMessage("RandomWeb");

        SomParticle atkParticle = new SomParticle(Color.WHITE, this);
        int webRadius = 5;

        for (int i = 0; i < 6; i++) {
            CustomLocation location = Pivot.randomRadiusLocation(Radius);
            SomTask.asyncCount(() -> {
                atkParticle.circle(location, webRadius);
                atkParticle.circle(location, webRadius * (2 / 3.0));
                atkParticle.circle(location, webRadius * (1 / 3.0));
                for (SomEntity target : SearchEntity.nearSomEntity(enemies(), location, webRadius)) {
                    SomEffect effect = new SomEffect("RandomWeb", "束縛", 2 * 20, this);
                    effect.setStatusMultiplyAttack(-0.99);
                    target.SLOWNESS(10, 2 * 20);
                }
            }, 10, RandomWebCT * 2, this);
        }

        timer("RandomWeb", RandomWebCT * 20);
    }

    public void PrisonWeb() {
        //sendMessage("PrisonWeb");

        SomParticle atkParticle = new SomParticle(Color.WHITE, this);

        SomTask.asyncTimer(() -> {
            atkParticle.circle(Pivot, Radius);
            atkParticle.circle(Pivot.clone().addY(1), Radius);
            atkParticle.circle(Pivot.clone().addY(2), Radius);
            for (SomEntity target : enemies()) {
                if (Radius <= Pivot.distanceXZ(target.getLocation())) {
                    target.silence(2 * 20, this);
                    target.SLOWNESS(5, 2 * 20);
                }
            }
        }, 20, this);

        PrisonWebFlg = true;
    }
}
