package SwordofMagic11.Item.Donation;

import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Player.Shop.EldShop;
import org.bukkit.Material;

public class CapsuleStorageSizeTicket extends SomDonationItem {

    private final int size;

    public CapsuleStorageSizeTicket(int size, int eld, int slot) {
        this.size = size;
        setId("CapsuleStorageSizeTicket" + size);
        setDisplay("カプセル倉庫拡張券[" + size + "]");
        setLore("使用するとカプセル倉庫の容量が§e" + size + "§a増えます");
        setIcon(Material.MAP);
        setCategory(Category.Donation);
        EldShop.addContainer(this, eld, slot);
    }

    @Override
    public void use(PlayerData playerData) {
        playerData.donation().addCapsuleStorageSize(size);
        playerData.sendMessage(getDisplay() + "§aを使用しました", SomSound.Tick);
        playerData.sendMessage("カプセル倉庫§aが§e" + playerData.capsuleStorageSize() + "枠§aになりました");
        playerData.itemInventory().delete(this);
    }
}
