package SwordofMagic11.Item.Donation;

import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Item.SomItem;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Player.Shop.EldShop;
import org.bukkit.Material;

public class PrivateInstanceTicket extends SomDonationItem {

    private final long day;

    public PrivateInstanceTicket(long day, int eld, int slot) {
        this.day = day;
        setId("PrivateInsTicketDay" + day);
        setDisplay("プライベートインスタンス利用券" + day + "日");
        setLore("使用するとプライベートインスタンスを§e" + day + "日間§a利用できます");
        setIcon(Material.MAP);
        setCategory(SomItem.Category.Donation);
        EldShop.addContainer(this, eld, slot);
    }

    @Override
    public void use(PlayerData playerData) {
        playerData.donation().addPrivateIns(day*24);
        playerData.sendMessage(getDisplay() + "§aを使用しました", SomSound.Tick);
        playerData.sendMessage(playerData.donation().getPrivateInsText() + "§aまで有効です");
        playerData.itemInventory().delete(this);
    }
}
