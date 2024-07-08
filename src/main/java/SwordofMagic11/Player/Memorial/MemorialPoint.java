package SwordofMagic11.Player.Memorial;

import SwordofMagic11.Component.SomSQL;
import SwordofMagic11.DataBase.DataBase;
import SwordofMagic11.Player.PlayerData;

public interface MemorialPoint {

    PlayerData playerData();

    default void setMemorialPoint(int point) {
        SomSQL.setSql(DataBase.Table.PlayerData, "UUID", playerData().getUUID(), "MemorialPoint", point);
    }

    default int getMemorialPoint() {
        return SomSQL.getInt(DataBase.Table.PlayerData, "UUID", playerData().getUUID(), "MemorialPoint");
    }

    default void addMemorialPoint(int point) {
        setMemorialPoint(getMemorialPoint() + point);
    }

    default void removeMemorialPoint(int point) {
        setMemorialPoint(getMemorialPoint() - point);
    }

    default boolean hasMemorialPoint(int point) {
        return getMemorialPoint() >= point;
    }
    
}
