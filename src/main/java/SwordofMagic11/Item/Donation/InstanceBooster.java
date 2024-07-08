package SwordofMagic11.Item.Donation;

import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Component.SomTask;
import SwordofMagic11.Player.Menu.GUIManager;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Player.Shop.EldShop;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static SwordofMagic11.Component.Function.scale;
import static SwordofMagic11.Player.Donation.InstanceBoosterMultiply;

public class InstanceBooster extends SomDonationItem {

    private static final int InstanceBoosterTime = 3600;
    private static final Set<PlayerData> confirm = new HashSet<>();

    public InstanceBooster(int eld, int slot) {
        setId("InstanceBooster");
        setDisplay("インスタンスブースター");
        setLore("使用すると§e" + InstanceBoosterTime + "秒間§aあなたがいる\n" +
                "インスタンスは§cドロップ倍率§aが§e+" + scale(InstanceBoosterMultiply*100) + "%§aされます");
        setIcon(Material.BELL);
        setCategory(Category.Donation);
        EldShop.addContainer(this, eld, slot);
    }

    @Override
    public void use(PlayerData playerData) {
        if (confirm.contains(playerData)) {
            playerData.donation().addInsBoost(InstanceBoosterTime);
            playerData.sendMessage(getDisplay() + "§aを使用しました", SomSound.Tick);
            SomSound.Bell.radius(playerData);
            playerData.itemInventory().delete(this);
            confirm.remove(playerData);
        } else {
            confirm.add(playerData);
            playerData.sendMessage("§a本当に§r" + getDisplay() + "§aを使用しますか？\n§a使用する場合は再度クリックしてください", SomSound.Tick);
            SomTask.asyncDelay(() -> confirm.remove(playerData), 200);
        }

    }
}
