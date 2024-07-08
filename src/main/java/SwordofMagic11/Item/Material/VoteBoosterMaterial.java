package SwordofMagic11.Item.Material;

import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Player.PlayerBooster;
import SwordofMagic11.Player.PlayerData;
import org.bukkit.Material;

import static SwordofMagic11.Component.Function.loreText;

public class VoteBoosterMaterial extends UseAbleMaterial {

    public static String ID = "VoteBooster";

    public VoteBoosterMaterial() {
        setId(ID);
        setDisplay("投票ブースター");
        setIcon(Material.PAPER);
        setLore(loreText("§e10分間§cドロップ倍率§aが§e+25%§aされます"));
        setRare(true);
        setSell(100);
    }

    @Override
    protected synchronized void useMaterial(PlayerData playerData) {
        if (playerData.hasMaterial(this, 1)) {
            playerData.removeMaterial(this, 1);
            playerData.booster().add(PlayerBooster.Type.Vote, 600);
            playerData.sendMessage(getDisplay() + "§aを使用しました [" + playerData.booster().get(PlayerBooster.Type.Vote) + "秒]", SomSound.Tick);
        } else {
            playerData.sendMessage(getDisplay() + "§aを所持していません", SomSound.Nope);
        }
    }
}
