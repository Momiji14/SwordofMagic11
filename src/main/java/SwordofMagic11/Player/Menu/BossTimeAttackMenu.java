package SwordofMagic11.Player.Menu;

import SwordofMagic11.Component.SomSQL;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.DataBase.AchievementDataLoader;
import SwordofMagic11.DataBase.DataBase;
import SwordofMagic11.DataBase.MapDataLoader;
import SwordofMagic11.Map.MapData;
import SwordofMagic11.Map.RoomData;
import SwordofMagic11.Player.ClassType;
import SwordofMagic11.Player.PlayerData;
import com.github.jasync.sql.db.RowData;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static SwordofMagic11.Component.Function.decoLore;
import static SwordofMagic11.Component.Function.scale;

public class BossTimeAttackMenu extends GUIManager {

    private final HashMap<MapData, ClearRecord> clearRecord = new HashMap<>();

    public BossTimeAttackMenu(PlayerData playerData) {
        super(playerData, "タイムアタック", 3);

        for (RowData objects : SomSQL.getSqlList(DataBase.Table.BossTimeAttack, "UUID", playerData.getUUID(), "*")) {
            MapData mapData = MapDataLoader.getMapData(objects.getString("MapData"));
            double time = objects.getDouble("Time");
            int playerCount = objects.getInt("PlayerCount");
            ClassType classType = ClassType.valueOf(objects.getString("ClassType"));
            double makeDamage = objects.getDouble("MakeDamage");
            double takeDamage = objects.getDouble("TakeDamage");
            clearRecord.put(mapData, new ClearRecord(mapData, time, playerCount, classType, makeDamage, takeDamage));
        }
    }

    public List<String> getLine(MapData mapData) {
        ClearRecord clearRecord = this.clearRecord.get(mapData);
        List<String> line = new ArrayList<>();
        String time;
        String playerCount;
        String classType;
        String makeDamage;
        String takeDamage;
        if (clearRecord != null) {
            time = clearRecord.time + "秒";
            playerCount = clearRecord.playerCount + "人";
            classType = clearRecord.classType.getColorDisplay();
            makeDamage = scale(clearRecord.makeDamage);
            takeDamage = scale(clearRecord.takeDamage);
        } else {
            String missing = "§7記録なし";
            time = missing;
            playerCount = missing;
            classType = missing;
            makeDamage = missing;
            takeDamage = missing;
        }
        line.add(decoLore("クリアタイム") + time);
        line.add(decoLore("パーティ人数") + playerCount);
        line.add(decoLore("クリアクラス") + classType);
        line.add(decoLore("与ダメージ") + takeDamage);
        line.add(decoLore("被ダメージ") + makeDamage);
        return line;
    }

    @Override
    public void updateContainer() {
        int slot = 0;
        for (MapData mapData : MapDataLoader.getRooms()) {
            CustomItemStack item = new CustomItemStack(mapData.getIcon()).setDisplay(mapData.getDisplay());
            item.addLore(getLine(mapData));
            setItem(slot, item);
            slot++;
        }
    }

    @Override
    public void topClick(int slot, ItemStack clickedItem, ClickType clickType) {

    }

    @Override
    public void bottomClick(int slot, ItemStack clickedItem, ClickType clickType) {

    }

    @Override
    public void close() {

    }

    public boolean hasRecord(String mapId) {
        return hasRecord(MapDataLoader.getMapData(mapId));
    }

    public boolean hasRecord(MapData mapData) {
        return clearRecord.containsKey(mapData);
    }

    public ClearRecord getRecord(String mapId) {
        return getRecord(MapDataLoader.getMapData(mapId));
    }

    public ClearRecord getRecord(MapData mapData) {
        return clearRecord.get(mapData);
    }

    public void trigger(ClearRecord record) {
        if (!clearRecord.containsKey(record.mapData()) || clearRecord.get(record.mapData).time() > record.time()) {
            clearRecord.put(record.mapData(), record);
            String[] key = new String[]{"UUID", "MapData"};
            String[] value = new String[]{playerData.getUUID(), record.mapData().getId()};
            SomSQL.setSql(DataBase.Table.BossTimeAttack, key, value, "Time", record.time());
            SomSQL.setSql(DataBase.Table.BossTimeAttack, key, value, "PlayerCount", record.playerCount());
            SomSQL.setSql(DataBase.Table.BossTimeAttack, key, value, "ClassType", record.classType().toString());
            SomSQL.setSql(DataBase.Table.BossTimeAttack, key, value, "MakeDamage", record.makeDamage());
            SomSQL.setSql(DataBase.Table.BossTimeAttack, key, value, "TakeDamage", record.takeDamage());
            playerData.sendMessage("§c" + record.mapData.getDisplay() + "§aの§eクリアレコード§aを§b更新§aしました！", SomSound.Level);
        }
    }

    public record ClearRecord(MapData mapData, double time, int playerCount, ClassType classType, double makeDamage, double takeDamage) {}
}
