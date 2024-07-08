package SwordofMagic11.Item.Material;

import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Player.PlayerData;
import org.bukkit.Material;

import static SwordofMagic11.Component.Function.loreText;

public class ClassExpTicketMaterial extends UseAbleMaterial {

    public static ClassExpTicketMaterial Ticket_10000 = new ClassExpTicketMaterial(10000);

    private final int exp;

    public ClassExpTicketMaterial(int exp) {
        this.exp = exp;
        setId("ExpTicket_" + exp);
        setDisplay("クラス経験値チケット [" + (exp/1000) + "k]");
        setIcon(Material.EXPERIENCE_BOTTLE);
        setLore(loreText("クラス経験値を§e" + (exp/1000) + "k§a取得できます"));
        setRare(true);
        setSell(100);
    }

    @Override
    protected synchronized void useMaterial(PlayerData playerData) {
        if (playerData.hasMaterial(this, 1)) {
            playerData.removeMaterial(this, 1);
            playerData.classes().addExp(exp);
        } else {
            playerData.sendMessage(getDisplay() + "§aを所持していません", SomSound.Nope);
        }
    }
}
