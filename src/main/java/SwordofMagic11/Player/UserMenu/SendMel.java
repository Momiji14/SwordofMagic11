package SwordofMagic11.Player.UserMenu;

import SwordofMagic11.Command.Player.Trade;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.Player.Market.MarketPlayer;
import SwordofMagic11.Player.Menu.NumberInput;
import SwordofMagic11.Player.PlayerData;
import org.bukkit.Material;

import static SwordofMagic11.Component.Function.ceil;

public class SendMel extends NumberInput {

    public CustomItemStack icon() {
        CustomItemStack item = new CustomItemStack(Material.GOLD_INGOT);
        item.setDisplay("メル送金");
        item.addLore("§aメルを他のプレイヤーへ送ります");
        item.setCustomData("PlayerSelect", "SendMel");
        return item;
    }

    private PlayerData targetData;

    public SendMel(PlayerData playerData) {
        super(playerData, "送金額入力");
    }

    public void open(PlayerData targetData) {
        this.targetData = targetData;
        super.open();
    }

    @Override
    public void enter(int input) {
        Trade.tradeMel(playerData, targetData, input);
    }
}
