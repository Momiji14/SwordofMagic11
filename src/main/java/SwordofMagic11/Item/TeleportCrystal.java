package SwordofMagic11.Item;

import SwordofMagic11.Map.MapData;
import SwordofMagic11.Player.PlayerData;

public class TeleportCrystal extends SomUseItem {

    private MapData city;

    public void setCity(MapData city) {
        this.city = city;
    }

    @Override
    public void use(PlayerData playerData) {
        playerData.teleport(city.getGlobalInstance(false).getSpawnLocation());
        city.enter(playerData, false);
        playerData.itemInventory().delete(this);
    }
}
