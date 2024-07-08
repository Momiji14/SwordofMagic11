package SwordofMagic11.Item.Donation;

import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Player.Shop.EldShop;
import org.bukkit.Material;

public class PalletStorageSizeTicket extends SomDonationItem {


    public PalletStorageSizeTicket(int eld, int slot) {
        setId("PalletStorageSizeTicket");
        setDisplay("パレットストレージ拡張券");
        setLore("使用するとパレットストレージが§e1枠§a増えます");
        setIcon(Material.MAP);
        setCategory(Category.Donation);
        EldShop.addContainer(this, eld, slot);
    }

    @Override
    public void use(PlayerData playerData) {
        playerData.donation().addPalletStorageSize(1);
        playerData.sendMessage(getDisplay() + "§aを使用しました", SomSound.Tick);
        playerData.sendMessage("パレットストレージ§aが§e" + playerData.palletStorageSize() + "枠§aになりました");
        playerData.itemInventory().delete(this);
    }
}
