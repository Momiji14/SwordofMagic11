package SwordofMagic11.Item.Material;

import SwordofMagic11.Player.Pallet;
import SwordofMagic11.Player.PlayerData;

public abstract class UseAbleMaterial extends MaterialData implements Pallet {

    public UseAbleMaterial() {
        setUseAble(true);
    }

    @Override
    public void use(PlayerData playerData) {
        if (!playerData.isSilence()) {
            useMaterial(playerData);
        }
    }

    protected abstract void useMaterial(PlayerData playerData);
}
