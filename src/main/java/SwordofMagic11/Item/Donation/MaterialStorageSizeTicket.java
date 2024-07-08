package SwordofMagic11.Item.Donation;

import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Player.Shop.EldShop;
import org.bukkit.Material;

public class MaterialStorageSizeTicket extends SomDonationItem {

    private final int size;

    public MaterialStorageSizeTicket(int size, int eld, int slot) {
        this.size = size;
        setId("MaterialStorageSizeTicket" + size);
        setDisplay("素材倉庫拡張券[" + size + "]");
        setLore("使用すると素材倉庫の容量が§e" + size + "§a増えます");
        setIcon(Material.MAP);
        setCategory(Category.Donation);
        EldShop.addContainer(this, eld, slot);
    }

    @Override
    public void use(PlayerData playerData) {
        playerData.donation().addMaterialStorageSize(size);
        playerData.sendMessage(getDisplay() + "§aを使用しました", SomSound.Tick);
        playerData.sendMessage("素材倉庫§aが§e" + playerData.materialStorageSize() + "枠§aになりました");
        playerData.itemInventory().delete(this);
    }
}
