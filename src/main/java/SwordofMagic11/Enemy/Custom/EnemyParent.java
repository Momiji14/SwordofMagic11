package SwordofMagic11.Enemy.Custom;

import SwordofMagic11.Enemy.EnemyData;
import SwordofMagic11.Entity.SomEntity;

public interface EnemyParent {

    void onChildDeath(EnemyData enemyData, SomEntity killer);

}
