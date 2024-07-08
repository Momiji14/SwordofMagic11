package SwordofMagic11.Custom;

import SwordofMagic11.Item.SomItem;
import SwordofMagic11.Item.SyncItem;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Player.Setting.PlayerSetting;

import java.util.List;

public class CustomInventory {

    private final PlayerData playerData;
    private int scroll = 0;

    public List<SomItem> getList() {
        return SyncItem.getList(playerData, SyncItem.State.ItemInventory);
    }

    public CustomInventory(PlayerData playerData) {
        this.playerData = playerData;
    }

    public void give(SomItem item) {
        item.setOwner(playerData);
        item.setState(SyncItem.State.ItemInventory);
        item.setSync(true);
        if (playerData.setting().is(PlayerSetting.BooleanEnum.ItemLog)) {
            playerData.sendMessage("§b[+]§f" + item.getDisplay());
        }
        playerData.updateInventory();
    }

    public void send(SomItem item, PlayerData playerData) {
        playerData.itemInventory().give(item);
        if (playerData.setting().is(PlayerSetting.BooleanEnum.ItemLog)) {
            playerData.sendMessage("§c[-]§f" + item.getDisplay());
        }
        playerData.updateInventory();
    }

    public void delete(SomItem item) {
        SyncItem.delete(item.getUUID());
        if (playerData.setting().is(PlayerSetting.BooleanEnum.ItemLog)) {
            playerData.sendMessage("§c[-]§f" + item.getDisplay());
        }
        playerData.updateInventory();
    }

    public void setScroll(int scroll) {
        this.scroll = scroll;
        playerData.updateInventory();
    }

    public int getScroll() {
        return scroll;
    }

    public void clearCache() {
        for (SomItem somItem : SyncItem.getList(playerData)) {
            SyncItem.clearCache(somItem.getUUID());
        }
    }
}
