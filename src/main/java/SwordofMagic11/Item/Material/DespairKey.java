package SwordofMagic11.Item.Material;

import SwordofMagic11.Player.PlayerData;
import org.bukkit.Material;

import static SwordofMagic11.Component.Function.loreText;

public class DespairKey extends UseAbleMaterial {

    public static DespairKey DespairKey = new DespairKey();

    public DespairKey() {
        setId("DespairKey");
        setDisplay("黒㞢鍵");
        setIcon(Material.NETHERITE_INGOT);
        setLore(loreText("解は流木にあり"));
        setRare(true);
        setSell(100);
    }


    @Override
    protected void useMaterial(PlayerData playerData) {
        if (playerData.despairKeyEnter().isPassed()) {
            playerData.despairKeyEnter().success();
        } else {
            playerData.despairKeyEnter().open();
        }
    }
}
