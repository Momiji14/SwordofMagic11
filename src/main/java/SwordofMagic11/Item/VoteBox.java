package SwordofMagic11.Item;

import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Item.Material.PetCookie;
import SwordofMagic11.Item.Material.VoteBoosterMaterial;
import SwordofMagic11.Player.PlayerData;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

import static SwordofMagic11.Component.Function.*;
import static SwordofMagic11.Item.Material.ClassExpTicketMaterial.Ticket_10000;

public class VoteBox extends SomUseItem {

    public static final String ID = "VoteBox";
    private static final int MinMel = 10000;
    private static final int MaxMel = 50000;

    private static final int MinClassExpTicket = 8;
    private static final int MaxClassExpTicket = 25;

    private static final int MinBooster = 3;
    private static final int MaxBooster = 6;

    private static final int MinCookie = 10;
    private static final int MaxCookie = 20;

    public VoteBox() {
        setId(ID);
        setDisplay("投票報酬BOX");
        List<String> lore = new ArrayList<>();
        lore.add("§a使用すると下記のアイテムを受け取れます");
        lore.add(decoLore("メル") + MinMel + "~" + MaxMel);
        lore.add(decoLore("クラス経験値チケット") + MinClassExpTicket + "~" + MaxClassExpTicket);
        lore.add(decoLore("投票ブースター") + MinBooster + "~" + MaxBooster);
        lore.add(decoLore("ペットボーロ") + MinCookie + "~" + MaxCookie);
        lore.add(decoLore("エクスキューブ") + 0.1 + "%");
        setLore(lore);
        setIcon(Material.ENDER_CHEST);
        setCategory(Category.Vote);
    }

    @Override
    public void use(PlayerData playerData) {
        int mel = randomInt(MinMel, MaxMel);
        int ticket = randomInt(MinClassExpTicket, MaxClassExpTicket);
        int booster = randomInt(MinBooster, MaxBooster);
        int cookie = randomInt(MinCookie, MaxCookie);
        playerData.addMel(mel);
        playerData.addMaterial(Ticket_10000, ticket);
        playerData.addMaterial(VoteBoosterMaterial.ID, booster);
        playerData.addMaterial(PetCookie.PetCookie, cookie);
        List<String> message = new ArrayList<>();
        message.add(decoText("投票報酬BOX"));
        message.add("§7・§e" + mel + "メル");
        message.add("§7・§rクラス経験値チケットx" + ticket);
        message.add("§7・§r投票ブースターx" + booster);
        message.add("§7・§rペットボーロx" + cookie);
        if (randomDouble() < 0.001) {
            playerData.addMaterial("エクスキューブ", 1);
            message.add("§7・§dエクスキューブx" + 1);
        }
        playerData.sendMessage(message, SomSound.Tick);
        playerData.itemInventory().delete(this);
    }
}
