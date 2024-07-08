package SwordofMagic11.Player.Menu;

import SwordofMagic11.Component.SomTask;
import SwordofMagic11.Custom.CustomLocation;
import SwordofMagic11.DataBase.MapDataLoader;
import SwordofMagic11.Map.MapData;
import SwordofMagic11.Player.PlayerData;
import org.bukkit.World;

public class DespairKeyEnter extends PasswordGUI {

    private boolean passed = false;

    public DespairKeyEnter(PlayerData playerData) {
        super(playerData, "黒㞢鍵");
        rawPass = "39736";
    }

    @Override
    public void open() {
        super.open();
    }

    public boolean isPassed() {
        return passed;
    }

    @Override
    public void success() {
        SomTask.sync(() -> {
            World world = MapData.getInstanceAtPlayer(playerData, MapDataLoader.getMapData("WPH_A_DespairHeart"));
            CustomLocation location = new CustomLocation(world, 0, 65, -69);
            playerData.teleport(location);
            passed = true;
        });
    }
}
