package SwordofMagic11.Player.Memorial;

import SwordofMagic11.Component.SomSQL;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.DataBase.DataBase;
import SwordofMagic11.DataBase.MemorialDataLoader;
import SwordofMagic11.Player.Menu.GUIManager;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Player.Setting.PlayerSetting;
import SwordofMagic11.StatusType;
import com.github.jasync.sql.db.RowData;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static SwordofMagic11.Component.Function.*;
import static SwordofMagic11.DataBase.DataBase.*;

public class MemorialMenu extends GUIManager {

    public CustomItemStack icon() {
        CustomItemStack item = new CustomItemStack(Material.REPEATING_COMMAND_BLOCK);
        int index = 0;
        HashMap<StatusType, Double> status = getMemorialStatus();
        item.setDisplay("メモリアル");
        item.addLore("§a所持§aしている§bメモリアル§aを確認できます");
        item.addLore("§eメモリアル数§aに応じて§eステータス§aの§a最大値§aが増加します");
        item.addLore("§aまた§eステータスポイント§aも§e2ポイント§a貰えます");
        item.addLore("§c※ステータスポイントが更新されるのは現在のクラスのみです");
        item.addLore("§c※他のクラスはステータスリセット時に更新されます");
        item.addLore(decoSeparator("メモリアルステータス"));
        for (StatusType statusType : StatusType.values()) {
            if (status.containsKey(statusType)) {
                item.addLore(decoLore(statusType.getDisplay()) + scale(status.get(statusType)*100, 3, true) + "%");
            }
        }
//        item.addSeparator("所持メモリアル一覧");
//        for (Map.Entry<String, Double> entry : memorial.entrySet()) {
//            MemorialData memorial = MemorialDataLoader.getMemorialData(entry.getKey());
//            item.addLore("§7・§f" + memorial.getDisplay() + " §a" + scale(entry.getValue() * 100, 2) + "%");
//            index++;
//            if (playerData.setting().is(PlayerSetting.BooleanEnum.StorageViewShort) && index > ContainerViewSize) {
//                item.addLore("§7その他..." + (getMemorial().size() - ContainerViewSize) + "種");
//                break;
//            }
//        }
        item.setCustomData("Menu", "MemorialMenu");
        return item;
    }

    private final HashMap<String, Double> memorial = new HashMap<>();
    private int scroll = 0;

    public MemorialMenu(PlayerData playerData) {
        super(playerData, "メモリアル", 6);
        for (RowData objects : SomSQL.getSqlList(DataBase.Table.PlayerMemorial, "UUID", playerData.getUUID(), "*")) {
            memorial.put(objects.getString("Memorial"), objects.getDouble("Value"));
        }
    }

    public int count() {
        return memorial.size();
    }

    private String[] key() {
        return new String[]{"UUID", "Memorial"};
    }

    private String[] value(String memorial) {
        return new String[]{playerData.getUUID(), memorial};
    }

    public double get(MemorialData memorial) {
        return get(memorial.getId());
    }

    public double get(String memorialId) {
        return memorial.getOrDefault(memorialId, 0.0);
    }

    public boolean has(MemorialData memorial) {
        return has(memorial.getId());
    }

    public boolean has(String memorialId) {
        return memorial.containsKey(memorialId);
    }

    public void set(MemorialData memorial, double value) {
        set(memorial.getId(), value);
    }

    public void set(String memorialId, double value) {
        if (!has(memorialId)) {
            playerData.classes().addAttributePoint(2);
        }
        memorial.put(memorialId, value);
        SomSQL.setSql(DataBase.Table.PlayerMemorial, key(), value(memorialId), "Value", value);
    }

    public HashMap<String, Double> getMemorial() {
        return memorial;
    }

    public HashMap<StatusType, Double> getMemorialStatus() {
        HashMap<StatusType, Double> status = new HashMap<>();
        for (Map.Entry<String, Double> entry : memorial.entrySet()) {
            MemorialData memorialData = MemorialDataLoader.getMemorialData(entry.getKey());
            memorialData.value(entry.getValue()).forEach((statusType, value) -> status.merge(statusType, value, Double::sum));
        }
        return status;
    }

    public void useMemorial(MemorialData memorial) {
        double current = get(memorial);
        double min = current + 0.005;
        double value = min < 1 ? randomDouble(min, Math.min(min + 0.35, 1)) : 1;
        if (value > current) {
            set(memorial, value);
            playerData.sendMessage("§a新たな§bメモリアル§aが§e登録§aされました §e[" + scale(value * 100, 3) + "%]", SomSound.Level);
            if (value == 1) {
                playerData.classes().addAttributePoint(1);
                playerData.sendMessage("§bメモリアル§aが§e100%§aになったため§eアトリビュートポイント§aが§e+1§aされました");
            }
        } else {
            playerData.sendMessage("§a現在の§bメモリアル§aの方が§e復元率§aが高いです §e[" + scale(current * 100, 3) + "%]", SomSound.Tick);
        }
    }

    @Override
    public void updateContainer() {
        int slot = 0;
        int index = 0;
        for (RowData objects : SomSQL.getSqlList(DataBase.Table.PlayerMemorial, "UUID", playerData.getUUID(), "*")) {
            if (index >= scroll * 8) {
                String memorialId = objects.getString("Memorial");
                if (MemorialDataLoader.getComplete().contains(memorialId)) {
                    MemorialData memorial = MemorialDataLoader.getMemorialData(memorialId);
                    CustomItemStack item = memorial.viewItem(objects.getDouble("Value"));
                    setItem(slot, item);
                    slot++;
                    if (isInvalidSlot(slot)) slot++;
                    if (slot >= 53) break;
                }
            }
            index++;
        }
        for (int i = 17; i < 45; i += 9) {
            setItem(i, ItemFlame);
        }
        setItem(8, UpScroll());
        setItem(53, DownScroll());
    }

    @Override
    public void topClick(int slot, ItemStack clickedItem, ClickType clickType) {
        if (CustomItemStack.hasCustomData(clickedItem, "UpScroll")) {
            scroll = scrollUp(scroll);
            update();
        } else if (CustomItemStack.hasCustomData(clickedItem, "DownScroll")) {
            scroll = scrollDown(memorial.size(), 6, scroll);
            update();
        }
    }

    @Override
    public void bottomClick(int slot, ItemStack clickedItem, ClickType clickType) {

    }

    @Override
    public void close() {

    }
}
