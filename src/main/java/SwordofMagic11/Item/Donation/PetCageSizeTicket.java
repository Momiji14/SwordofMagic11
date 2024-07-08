package SwordofMagic11.Item.Donation;

import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Player.Shop.EldShop;
import org.bukkit.Material;

public class PetCageSizeTicket extends SomDonationItem {

    private final int size;

    public PetCageSizeTicket(int size, int eld, int slot) {
        this.size = size;
        setId("PetCageSizeTicket" + size);
        setDisplay("ペットケージ拡張券[" + size + "]");
        setLore("使用するとペットケージの容量が§e" + size + "§a増えます");
        setIcon(Material.MAP);
        setCategory(Category.Donation);
        EldShop.addContainer(this, eld, slot);
    }

    @Override
    public void use(PlayerData playerData) {
        playerData.donation().addPetCageSize(size);
        playerData.sendMessage(getDisplay() + "§aを使用しました", SomSound.Tick);
        playerData.sendMessage("ペットケージ§aが§e" + playerData.petCageSize() + "枠§aになりました");
        playerData.itemInventory().delete(this);
    }
}
