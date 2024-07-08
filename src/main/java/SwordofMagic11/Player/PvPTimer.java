package SwordofMagic11.Player;

public interface PvPTimer {
    PlayerData playerData();

    default int pvpAttackTime() {
        return playerData().timer("PvPAttackTime");
    }

    default void pvpAttackTime(int time) {
        playerData().timer("PvPAttackTime", time);
    }

    default int pvpVictimTime() {
        return playerData().timer("PvPVictimTime");
    }

    default void pvpVictimTime(int time) {
        playerData().timer("PvPVictimTime", time);
    }

    default boolean hasPvPAttackTime() {
        return playerData().hasTimer("PvPAttackTime");
    }

    default boolean hasPvPVictimTime() {
        return playerData().hasTimer("PvPVictimTime");
    }
}
