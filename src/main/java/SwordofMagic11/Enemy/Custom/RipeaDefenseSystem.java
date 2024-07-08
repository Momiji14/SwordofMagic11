package SwordofMagic11.Enemy.Custom;

import SwordofMagic11.Component.Function;
import SwordofMagic11.Component.SomParticle;
import SwordofMagic11.Component.SomRay;
import SwordofMagic11.Component.SomTask;
import SwordofMagic11.Custom.CustomLocation;
import SwordofMagic11.Enemy.EnemyData;
import SwordofMagic11.Enemy.MobData;
import SwordofMagic11.Entity.Damage;
import SwordofMagic11.Entity.SearchEntity;
import SwordofMagic11.Entity.SomEntity;
import SwordofMagic11.Player.PlayerData;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

public class RipeaDefenseSystem extends CustomData implements EnemyParent {
    public RipeaDefenseSystem(MobData mobData, int level, Location location) {
        super(mobData, level, location);
        timer("TrackingBeam", TrackingBeamCT);
        timer("ScanBeam", ScanBeamCT);
    }

    @Override
    public void tick() {
        if (!hasTimer("TrackingBeam")) TrackingBeam();
        if (!hasTimer("ScanBeam")) ScanBeam();
    }

    @Override
    public void onChildDeath(EnemyData enemyData, SomEntity killer) {

    }

    final CustomLocation RipeaCoreLocation = new CustomLocation(getWorld(), -9, 82, 5);   //コアのビーム発射位置的なやつ

    //スキャンロケーションはパーティクルにlower付くのでY座標気持ち高めに設定すること
    final CustomLocation RipeaScanStartLocation = new CustomLocation(getWorld(), -25, 82, -11);    //スキャンの開始地点
    final CustomLocation RipeaScanEndLocation = new CustomLocation(getWorld(), 7, 82, 21);   //スキャンの終了地点

    final String WeakpointEffectId = "RipeaWeakpoint";

    final int TrackingBeamCT = 600;
    final int ScanBeamCT = 300;

    public Set<EnemyData> barriers = new HashSet<>();

    public Set<SomEntity> barriersAsSomEntity() {
        return new HashSet<>(barriers);
    }

    void Damage(SomEntity victim, double multi) {
        if (victim.hasEffect(WeakpointEffectId)) {
            multi *= 1 + victim.getEffect(WeakpointEffectId).getInt("Level") * 0.1;
        }
        Damage.makeDamage(this, victim, Damage.Type.Shoot, multi);
    }

    void TrackingBeam() {
        timer("TrackingBeam", TrackingBeamCT);

        int time = 100;
        double speed = 0.5;
        double radius = 1;
        SomParticle signParticle = new SomParticle(Color.RED, this);
        CustomLocation loc = new CustomLocation(RipeaCoreLocation);
        if (!enemies().isEmpty()) {
            PlayerData target = (PlayerData) Function.randomGet(enemies());
            SomTask.asyncCountIf(() -> {
                //loop
                Vector vec = target.getHipsLocation().toVector().subtract(loc.toVector()).normalize().multiply(speed);
                loc.setDirection(vec);
                loc.add(vec);
                signParticle.line(loc, radius);
            }, 2, time / 5, 0, () -> {
                //condition
                if (SomRay.rayLocationBlock(loc, radius, true).getHitBlock() != null) {
                    return false;
                }
                SomEntity hit;
                hit = SomRay.rayLocationEntity(loc, radius, 0.1, barriersAsSomEntity(), true).getHitEntity();
                if (hit != null) {
                    //バリアにヒット時の処理
                    //全てのバリアにある程度のダメージ
                    return false;
                }
                hit = SomRay.rayLocationEntity(loc, radius, 0.1, enemies(), true).getHitEntity();
                if (hit != null) {
                    //プレイヤーにヒット時の処理
                    Damage(hit, 2);
                    return false;
                }
                return true;
            }, () -> {
                //end
            }, this);
        }
    }

    void ScanBeam() {
        timer("ScanBeam", ScanBeamCT);

        int time = 100 / 2;
        double range = RipeaScanEndLocation.getZ() - RipeaScanStartLocation.getZ() + 0.9;
        double radius = 1;
        //X座標方向にスキャンする
        SomParticle signParticle = new SomParticle(Color.RED, this);
        signParticle.setLower();

        CustomLocation loc = new CustomLocation(RipeaScanStartLocation);
        loc.setDirection(new Vector(0, 0, range));
        Vector vec = new Vector(RipeaScanEndLocation.getX() - RipeaScanStartLocation.getX() + 0.9, 0, 0).normalize().multiply(range / time);

        SomTask.asyncCount(() -> {
            //loop
            loc.add(vec);
            signParticle.line(loc, Math.abs(range));
            //引っかかったプレイヤーにダメージ
            for (SomEntity target : SearchEntity.squareXZSomEntity(enemies(), loc, new CustomLocation(loc).addXZ(loc.getDirection().getX(), loc.getDirection().getZ()))) {
                //座標に対して1秒後に検索かけて範囲内にいるプレイヤーにダメージ
                CustomLocation targetLoc = target.getLocation();
                SomTask.asyncDelay(() -> {
                    for (SomEntity victim : SearchEntity.nearXZSomEntity(enemies(), targetLoc, radius)) {
                        Damage(victim, 4);
                    }
                }, 20);
            }
        }, 2, time, this);
    }
}
