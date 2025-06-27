package SwordofMagic11.Enemy.Custom;

import SwordofMagic11.Component.Function;
import SwordofMagic11.Component.SomParticle;
import SwordofMagic11.Component.SomTask;
import SwordofMagic11.Custom.CustomLocation;
import SwordofMagic11.DataBase.MobDataLoader;
import SwordofMagic11.Enemy.EnemyData;
import SwordofMagic11.Enemy.MobData;
import SwordofMagic11.Entity.Damage;
import SwordofMagic11.Entity.SearchEntity;
import SwordofMagic11.Entity.SomEffect;
import SwordofMagic11.Entity.SomEntity;
import SwordofMagic11.StatusType;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class Zordy extends CustomData {
    public Zordy(MobData mobData, int level, Location location) {
        super(mobData, level, location);
        timer("CurseAttack", CurseAttackCT);
        timer("CurseBeam", CurseBeamCT);
        timer("SummonTotem", SummonTotemCT);
        timer("AroundCurseSwitch", AroundCurseSwitch);
        this.ignoreCrowdControl(true);
    }

    public Set<EnemyData> child = new CopyOnWriteArraySet<>();

    @Override
    public void tick() {
        if (!hasTimer("CurseAttack")) CurseAttack();
        if (!hasTimer("CurseBeam")) CurseBeam();
        if (!hasTimer("SummonTotem")) SummonTotem();
        if (!hasTimer("AroundCurse")) AroundCurse();
        if (!hasTimer("SurroundCurse")) SurroundCurse();

        if (!hasTimer("AroundCurseSwitch")) {
            AroundCurseActive = !AroundCurseActive;
            timer("AroundCurseSwitch", AroundCurseSwitch);
        }
        if (!hasTimer("TotemLine")) TotemLine();    //5tickごとに実行する用

        TotemScore = 0;
        child.forEach((totem) -> {
            if (!totem.isDeath()) TotemScore++;
        });
    }

    //CT系の設定(tick)
    private final int CurseAttackCT = 60;
    private final int CurseBeamCT = 280;
    private final int AroundCurseCT = 10;
    private final int AroundCurseSwitch = 580;
    private final int SurroundCurseCT = 10;
    private final int SummonTotemCT = 1300;

    boolean AroundCurseActive = false;
    CustomLocation SurroundCurseCenter = new CustomLocation(getWorld(), -8, 65, -2);
    private int TotemScore = 0;
    final SomParticle totemLineParticle = new SomParticle(Color.BLUE, this);

    public void summon(CustomLocation loc) {
        child.add(EnemyData.spawn(MobDataLoader.getMobData("ゾーディートーテム"), getLevel(), loc));
    }

    @Override
    public synchronized void death(SomEntity killer) {
        super.death(killer);
        child.forEach(EnemyData::delete);
    }

    void Curse(SomEntity target) {
        int dur = 100;  //デバフの効果時間（tick）
        SomEffect curseEffect = new SomEffect("ZordyCurse", "ゾーディーの呪い", dur, this);
        curseEffect.setStatusMultiplyAttack(-0.03 * TotemScore);
        curseEffect.setStatusMultiplyDefense(0.03 * TotemScore);
        SomEffect weakEffect = new SomEffect("ZordyWeakCurse", "ボス用", 20, this);
        weakEffect.setStatusMultiply(StatusType.CriticalRate, -0.03 * TotemScore);

        target.SLOWNESS(1, dur);
        target.blind(1, dur);
        if (!target.hasEffect(curseEffect.getId())) target.addEffect(curseEffect);
        else target.getEffect(curseEffect.getId()).setTime(dur);
        if (!this.hasEffect(weakEffect.getId())) this.addEffect(weakEffect);
        else this.getEffect(weakEffect.getId()).setTime(dur);
        Damage.makeDamage(this, target, Damage.Type.Magic, 0.4 + TotemScore * 0.1);
    }

    void CurseAttack() {
        timer("CurseAttack", CurseAttackCT);    //CTの設定

        int range = 12;
        int angle = 50;
        int signWait = 20;  //攻撃前の予兆の時間(tick)
        SomParticle shapeParticle = new SomParticle(Color.RED, this);

        setNoAI();
        SomTask.asyncCount(() -> shapeParticle.fanShaped(getLocation(), range, angle), 5, signWait / 5, () -> {
            SearchEntity.fanShapedSomEntity(enemies(), getLocation(), range, angle).forEach((target) -> {
                Damage.makeDamage(this, target, Damage.Type.Magic, 1);
            });
            resetNoAI();
        }, this);
    }

    void CurseBeam() {
        timer("CurseBeam", CurseBeamCT);

        int signWait = 20;
        double radius = 2 + 0.5 * TotemScore;
        int dur = 200;  //フィールドの持続時間

        Collection<SomEntity> targets = new ArrayList<>();
        Collection<CustomLocation> loc = new HashSet<>();
        SomParticle atkParticle = new SomParticle(Color.RED, this);
        SomParticle shapeParticle = new SomParticle(Color.NAVY, this);

        //targetsにランダムなターゲット最大2人をセット
        Collection<SomEntity> list = new HashSet<>(enemies());
        for (int i = 0; i < 2; i++) {
            if (!list.isEmpty()) {
                SomEntity target = Function.randomGet(list);
                targets.add(target);
                list.remove(target);
            }
        }

        //ターゲティングから発射まで
        SomTask.asyncCount(() -> {
            targets.forEach((target) -> atkParticle.line(getHandLocation(), target.getLocation()));
        }, 5, signWait / 5, () -> {
            targets.forEach((target) -> loc.add(target.getLocation().lower()));
        }, this);

        //フィールド残す
        SomTask.asyncCount(() -> loc.forEach((point) -> {
            shapeParticle.circle(point, radius, 12 + radius * 3);
            SearchEntity.nearSomEntity(enemies(), point, radius).forEach(this::Curse);
        }), 10, dur / 10, 10, this);
    }

    void SummonTotem() {
        timer("SummonTotem", SummonTotemCT);

        int childLimit = 10;
        int signWait = 40;
        SomParticle signParticle = new SomParticle(Color.NAVY, this);
        Collection<CustomLocation> loc = new HashSet<>();

        for (int i = 1; i <= 3; i++) {
            if (child.size() + i < childLimit)
                loc.add(SurroundCurseCenter.randomRadiusLocation(20).lower());
        }
        SomTask.asyncCount(() -> loc.forEach(signParticle::random), 5, signWait / 5, () ->
                SomTask.sync(() -> {
                    loc.forEach(this::summon);
                    child.forEach(EnemyData::setNoAI);
                }), this);
    }

    void AroundCurse() {
        timer("AroundCurse", AroundCurseCT);

        int radius = 5 + TotemScore;
        SomParticle shapeParticle = new SomParticle(Color.NAVY, this);

        if (AroundCurseActive) {
            shapeParticle.circle(getHipsLocation(), radius, 12 + radius * 3);
            SearchEntity.nearSomEntity(enemies(), getLocation(), radius).forEach(this::Curse);
        } else {
            child.forEach((totem) -> {
                if (!totem.isDeath()) {
                    shapeParticle.circle(totem.getHipsLocation(), radius / 2, 12 + radius * 3);
                    SearchEntity.nearXZSomEntity(enemies(), totem.getLocation(), radius / 2).forEach(this::Curse);
                }
            });
        }
    }

    void SurroundCurse() {
        timer("SurroundCurse", SurroundCurseCT);

        final double maxSafeRange = 30;
        final double minSafeRange = 15;
        //体力割合に応じて安全地帯の範囲を縮める（残り20%で一番小さい）
        double safeRange = minSafeRange + (Math.max(healthPercent() - 0.2, 0) * (maxSafeRange - minSafeRange) / 0.8);
        SomParticle shapeParticle = new SomParticle(Particle.DRIPPING_OBSIDIAN_TEAR, this);

        SomTask.asyncCount(() -> shapeParticle.circle(SurroundCurseCenter, safeRange, 12 + safeRange * 3), 10, SurroundCurseCT / 10,
                () -> enemies().forEach((target) -> {
                    if (target.getLocation().distanceXZ(SurroundCurseCenter) > safeRange) {
                        Damage.makeDamage(this, target, Damage.Type.Magic, 1.5);
                    }
                }), this);
    }

    void TotemLine() {
        timer("TotemLine", 5);
        child.forEach((totem) -> {
            if (!totem.isDeath()) totemLineParticle.line(getHandLocation(), totem.getHipsLocation());
        });
    }
}
