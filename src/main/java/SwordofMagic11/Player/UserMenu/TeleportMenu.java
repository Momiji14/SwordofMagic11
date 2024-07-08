package SwordofMagic11.Player.UserMenu;

import SwordofMagic11.Component.SomSQL;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Component.SomTask;
import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.Custom.CustomLocation;
import SwordofMagic11.DataBase.DataBase;
import SwordofMagic11.DataBase.MapDataLoader;
import SwordofMagic11.Map.MapData;
import SwordofMagic11.Player.Menu.GUIManager;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Map.PvPRaid;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

public class TeleportMenu extends GUIManager {

    public CustomItemStack icon() {
        CustomItemStack item = new CustomItemStack(Material.ENDER_PEARL);
        item.setDisplay("転移メニュー");
        item.addLore("§a各街に転移できます");
        item.setCustomData("Menu", "TeleportMenu");
        return item;
    }

    public TeleportMenu(PlayerData playerData) {
        super(playerData, "転移メニュー", 1);
    }

    public void active(MapData mapData) {
        SomTask.async(() -> {
            String[] key = new String[]{"UUID", "City"};
            String[] value = new String[]{playerData.getUUID(), mapData.getId()};
            if (!SomSQL.exists(DataBase.Table.PlayerCity, key, value)) {
                SomSQL.setSql(DataBase.Table.PlayerCity, key, value, "Value", "true");
                playerData.sendMessage("§e" + mapData.getDisplay() + "§aの§e転移門§aを§b解放§aしました", SomSound.Level);
            }
        });
    }

    @Override
    public void open() {
        if (PvPRaid.isInPvPRaid(playerData)) {
            playerData.sendMessage(PvPRaid.Display + "§aでは利用できません", SomSound.Nope);
            return;
        }
        super.open();
    }

    @Override
    public void updateContainer() {
        int slot = 0;
        String[] key = new String[]{"UUID", "City"};
        for (MapData mapData : MapDataLoader.getCities()) {
            String[] value = new String[]{playerData.getUUID(), mapData.getId()};
            if (SomSQL.exists(DataBase.Table.PlayerCity, key, value, "Value")) {
                setItem(slot, mapData.viewItem());
                slot++;
            }
        }
    }

    private BukkitTask task;

    @Override
    public void topClick(int slot, ItemStack clickedItem, ClickType clickType) {
        if (CustomItemStack.hasCustomData(clickedItem, "MapData")) {
            int mel = CustomItemStack.getCustomDataInt(clickedItem, "TeleportMel");
            if (playerData.hasMel(mel)) {
                MapData mapData = MapDataLoader.getMapData(CustomItemStack.getCustomData(clickedItem, "MapData"));
                if (mapData != playerData.getMap()) {
                    if (task == null) {
                        CustomLocation location = playerData.getLocation();
                        playerData.sendTitle("§e転移を開始します", "§cその場から動かないでください", 10, 100, 0);
                        playerData.sendMessage("§e転移を開始します\n§cその場から動かないでください", SomSound.Tick);
                        task = SomTask.asyncCount(() -> {
                            if (location.distance(playerData.getLocation()) > 1 || playerData.isSilence() || playerData.isDeath()) {
                                playerData.sendMessage("§c転移が無効化されました", SomSound.Nope);
                                task.cancel();
                                task = null;
                            }
                        }, 20, 5, () -> {
                            if (location.distance(playerData.getLocation()) < 1) {
                                playerData.teleport(mapData.getGlobalInstance(false).getSpawnLocation());
                                mapData.enter(playerData, false);
                                playerData.removeTax(mel);
                            } else {
                                playerData.sendMessage("§c移動したため転移が無効化されます", SomSound.Nope);
                            }
                            task = null;
                        }, playerData);
                    }
                } else {
                    playerData.teleport(playerData.getWorld().getSpawnLocation());
                }
            } else playerData.sendNonMel();
        }
    }

    @Override
    public void bottomClick(int slot, ItemStack clickedItem, ClickType clickType) {

    }

    @Override
    public void close() {

    }
}
