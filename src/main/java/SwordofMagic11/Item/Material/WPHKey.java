package SwordofMagic11.Item.Material;

import SwordofMagic11.Component.SomTask;
import SwordofMagic11.Custom.CustomLocation;
import SwordofMagic11.DataBase.MapDataLoader;
import SwordofMagic11.Map.MapData;
import SwordofMagic11.Player.PlayerData;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

public class WPHKey extends UseAbleMaterial {

    public static WPHKey WPHKey = new WPHKey();

    public WPHKey() {
        setId("WPHKey");
        setDisplay("次元の鍵");
        setIcon(Material.TRIPWIRE_HOOK);
        List<String> lore = new ArrayList<>();
        lore.add("§a使用することで次元の門へ移動できる");
        setLore(lore);
        setRare(true);
        setSell(10000);
    }

    @Override
    protected void useMaterial(PlayerData playerData) {
        SomTask.sync(() -> {
            World world = MapData.getInstanceAtPlayer(playerData, MapDataLoader.getMapData("WPH_Home"));
            CustomLocation location = new CustomLocation(world, 0.5, 68, 0.5);
            playerData.teleport(location);
        });
    }
}
