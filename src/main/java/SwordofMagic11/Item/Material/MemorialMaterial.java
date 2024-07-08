package SwordofMagic11.Item.Material;

import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Player.Memorial.MemorialData;
import SwordofMagic11.Player.PlayerData;

public class MemorialMaterial extends UseAbleMaterial {

    private final MemorialData memorial;

    public MemorialMaterial(MemorialData memorial) {
        this.memorial = memorial;
    }

    @Override
    protected synchronized void useMaterial(PlayerData playerData) {
        if (playerData.hasMaterial(this, 1)) {
            if (playerData.memorialMenu().get(memorial) < 1) {
                playerData.removeMaterial(this, 1);
                playerData.memorialMenu().useMemorial(memorial);
            } else {
                playerData.sendMessage("§aすでに§e復元率§aが§c最大§aです", SomSound.Nope);
            }
        } else {
            playerData.sendMessage(getDisplay() + "§aを所持していません", SomSound.Nope);
        }
    }

    public MemorialData getMemorial() {
        return memorial;
    }
}
