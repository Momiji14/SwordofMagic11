package SwordofMagic11.Player;

import SwordofMagic11.Component.SomSQL;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Component.SomTask;
import SwordofMagic11.DataBase.DataBase;
import com.github.jasync.sql.db.RowData;

import java.util.HashMap;

public class PlayerBooster {

    public enum Type {
        Vote("投票ブースター"),
        ;

        private final String display;

        Type(String display) {
            this.display = display;
        }

        public String getDisplay() {
            return display;
        }
    }

    private final PlayerData playerData;
    private final HashMap<Type, Integer> timer = new HashMap<>();

    public PlayerBooster(PlayerData playerData) {
        this.playerData = playerData;
        if (SomSQL.exists(DataBase.Table.PlayerBooster, "UUID", playerData.getUUID())) {
            for (RowData objects : SomSQL.getSqlList(DataBase.Table.PlayerBooster, "UUID", playerData.getUUID(), "*")) {
                Type type = Type.valueOf(objects.getString("Type"));
                int time = objects.getInt("Time");
                timer.put(type, time);
            }
        }

        SomTask.asyncTimer(() -> timer.keySet().removeIf(type -> {
            int time = timer.get(type) - 1;
            if (time <= 0) {
                playerData.sendMessage("§e" + type.getDisplay() + "§aの効果が切れました", SomSound.Tick);
                SomSQL.delete(DataBase.Table.PlayerBooster, key, value(type));
                return true;
            } else {
                switch (time) {
                    case 180 -> playerData.sendMessage("§e" + type.getDisplay() + "§aは残り§e3分§aです", SomSound.Tick);
                    case 60 -> playerData.sendMessage("§e" + type.getDisplay() + "§aは残り§e1分§aです", SomSound.Tick);
                }
                set(type, time);
                return false;
            }
        }), 20, playerData);
    }

    private static final String[] key = new String[]{"UUID", "Type"};
    private String[] value(Type type) {
        return new String[]{playerData.getUUID(), type.toString()};
    }

    public void set(Type type, int time) {
        timer.put(type, time);
        SomSQL.setSql(DataBase.Table.PlayerBooster, key, value(type), "Time", time);
    }

    public void add(Type type, int time) {
        set(type, get(type) + time);
    }

    public int get(Type type) {
        return timer.getOrDefault(type, 0);
    }

    public double multiply() {
        double multiply = 1;
        if (timer.containsKey(Type.Vote)) {
            multiply += 0.25;
        }
        return multiply;
    }
}
