package SwordofMagic11.Entity;

import java.util.Collection;
import java.util.HashSet;

public class  Cardinal extends SomEntity {

    public Cardinal() {
        super(null);
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public int getLevel() {
        return 999;
    }

    @Override
    public void hurt() {

    }

    @Override
    public String getName() {
        return "カーディナル";
    }

    @Override
    public Collection<SomEntity> enemies() {
        return new HashSet<>();
    }

    @Override
    public boolean isEnemy(SomEntity entity) {
        return false;
    }

    @Override
    public Collection<SomEntity> allies() {
        return new HashSet<>();
    }

    @Override
    public boolean isAlly(SomEntity entity) {
        return false;
    }

    @Override
    public Collection<SomEntity> alliesIsDeath() {
        return new HashSet<>();
    }

    @Override
    public boolean isAllyIsDeath(SomEntity entity) {
        return false;
    }

    @Override
    protected void deathProcess(SomEntity killer) {

    }

    @Override
    public void onMakeDamage(SomEntity victim, Damage.Type type, double multiply, boolean isCritical) {

    }

    @Override
    public void onTakeDamage(SomEntity attacker, Damage.Type type, double multiply, boolean isCritical) {

    }
}
