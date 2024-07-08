package SwordofMagic11.Player.Statistics;

import SwordofMagic11.Component.SomSQL;
import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.DataBase.DataBase;
import SwordofMagic11.Enemy.EnemyData;
import SwordofMagic11.Player.PlayerData;
import com.github.jasync.sql.db.RowData;
import org.bukkit.Material;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static SwordofMagic11.Component.Function.decoLore;
import static SwordofMagic11.Component.Function.scale;

public class Statistics {

    private final PlayerData playerData;

    public Statistics(PlayerData playerData) {
        this.playerData = playerData;

        if (SomSQL.exists(DataBase.Table.StatisticsInt, "UUID", playerData.getUUID())) {
            for (RowData objects : SomSQL.getSqlList(DataBase.Table.StatisticsInt, "UUID", playerData.getUUID(), "*")) {
                IntEnum intEnum = IntEnum.valueOf(objects.getString("Statistics"));
                valueInt.put(intEnum, objects.getInt("Value"));
            }
        }
        if (SomSQL.exists(DataBase.Table.StatisticsDouble, "UUID", playerData.getUUID())) {
            for (RowData objects : SomSQL.getSqlList(DataBase.Table.StatisticsDouble, "UUID", playerData.getUUID(), "*")) {
                DoubleEnum doubleEnum = DoubleEnum.valueOf(objects.getString("Statistics"));
                valueDouble.put(doubleEnum, objects.getDouble("Value"));
            }
        }
        if (SomSQL.exists(DataBase.Table.StatisticsEnemyKill, "UUID", playerData.getUUID())) {
            for (RowData objects : SomSQL.getSqlList(DataBase.Table.StatisticsEnemyKill, "UUID", playerData.getUUID(), "*")) {
                enemyKill.put(objects.getString("Statistics"), objects.getInt("Value"));
            }
        }
    }

    private final ConcurrentHashMap<DoubleEnum, Double> valueDouble = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<IntEnum, Integer> valueInt = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Integer> enemyKill = new ConcurrentHashMap<>();

    private final String[] key = new String[]{"UUID", "Statistics"};

    private String[] value(IntEnum intEnum) {
        return new String[]{playerData.getUUID(), intEnum.toString()};
    }

    private String[] value(DoubleEnum doubleEnum) {
        return new String[]{playerData.getUUID(), doubleEnum.toString()};
    }

    private String[] value(String id) {
        return new String[]{playerData.getUUID(), id};
    }

    public void set(IntEnum intEnum, int value) {
        valueInt.put(intEnum, value);
        SomSQL.setSql(DataBase.Table.StatisticsInt, key, value(intEnum), "Value", value);
    }

    public void set(DoubleEnum doubleEnum, double value) {
        valueDouble.put(doubleEnum, value);
        SomSQL.setSql(DataBase.Table.StatisticsDouble, key, value(doubleEnum), "Value", value);
    }

    public void add(IntEnum intEnum, int value) {
        set(intEnum, get(intEnum) + value);
    }

    public void add(DoubleEnum doubleEnum, double value) {
        set(doubleEnum, get(doubleEnum) + value);
    }

    public void add(EnemyData enemyData) {
        String id = enemyData.getMobData().getId();
        enemyKill.merge(id, 1, Integer::sum);
        SomSQL.setSql(DataBase.Table.StatisticsEnemyKill, key, value(id), "Value", enemyKill.get(id));
    }

    public int get(IntEnum intEnum) {
        return valueInt.getOrDefault(intEnum, 0);
    }

    public double get(DoubleEnum doubleEnum) {
        return valueDouble.getOrDefault(doubleEnum, 0.0);
    }

    public int get(EnemyData enemyData) {
        return enemyKill.getOrDefault(enemyData.getName(), 0);
    }

    public Set<Map.Entry<String, Integer>> enemyKill() {
        return this.enemyKill.entrySet();
    }

    public int enemyKill(String id) {
        return enemyKill.getOrDefault(id, 0);
    }

    public int totalEnemyKill() {
        int kill = 0;
        for (Integer i : enemyKill.values()) {
            kill += i;
        }
        return kill;
    }

    public CustomItemStack viewItemInt() {
        CustomItemStack intItem = new CustomItemStack(Material.PAPER).setDisplay("整数系統計");
        for (Statistics.IntEnum intEnum : Statistics.IntEnum.values()) {
            intItem.addLore(decoLore(intEnum.getDisplay()) + playerData.statistics().get(intEnum));
        }
        return intItem;
    }

    public CustomItemStack viewItemDouble() {
        CustomItemStack doubleItem = new CustomItemStack(Material.PAPER).setDisplay("小数系統計");
        for (Statistics.DoubleEnum doubleEnum : Statistics.DoubleEnum.values()) {
            doubleItem.addLore(decoLore(doubleEnum.getDisplay()) + scale(playerData.statistics().get(doubleEnum), 3));
        }
        return doubleItem;
    }

    public CustomItemStack viewItemEnemyKill() {
        CustomItemStack enemyKill = new CustomItemStack(Material.PAPER).setDisplay("エネミー討伐");
        for (Map.Entry<String, Integer> entry : playerData.statistics().enemyKill()) {
            enemyKill.addLore(decoLore(entry.getKey()) + entry.getValue());
        }
        return enemyKill;
    }

    public enum IntEnum {
        PlayTime("プレイ時間"),
        PvPTime("PvP滞在時間"),
        AFKTime("AFK時間"),
        SitTime("座った時間"),
        JumpCount("ジャンプ数"),
        StrafeCount("ストレイフ数"),
        WallKickCount("壁キック数"),
        ManiaTapCount("Mania打数"),
        ManiaMaxCombo("Mania最大コンボ"),
        LightsOutClearCount("LOクリア回数"),
        PlayerKillAttackerCount("プレイヤーをキルした数"),
        PlayerKillVictimCount("プレイヤーにキルされた数"),
        GivenPvP_Grinder("PKで奪ったグラインダー"),
        LostPvP_Grinder("PKで奪われたグラインダー"),
        LostPvE_Grinder("システムに奪われたグラインダー"),
        GatheringMiningCount("採掘数"),
        GatheringCollectCount("採取数"),
        GatheringFishingCount("漁獲数"),
        GatheringCraftCount("制作数"),
        MarketSellMel("マーケット売上"),
        TranceChallenge("超越挑戦数"),
        TranceSuccess("超越成功数"),
        TranceFailed("超越失敗数"),
        TranceDown("超越下落数"),
        TranceMel("超越に使用したメル"),
        TranceGrinder("超越に使用したグラインダー"),
        ;

        private final String display;

        IntEnum(String display) {
            this.display = display;
        }

        public String getDisplay() {
            return display;
        }
    }

    public enum DoubleEnum {
        GivenPvP_Exp("PKで奪った経験値"),
        LostPvP_Exp("PKで奪われた経験値"),
        LostPvE_Exp("システムに奪われた経験値"),
        ManiaKPS("ManiaKPS"),
        LightsOutClearTime("LOクリアタイム"),
        FishingCPS("漁獲CPS"),
        ;

        private final String display;

        DoubleEnum(String display) {
            this.display = display;
        }

        public String getDisplay() {
            return display;
        }
    }
}
