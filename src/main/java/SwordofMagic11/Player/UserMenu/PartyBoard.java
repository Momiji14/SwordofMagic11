package SwordofMagic11.Player.UserMenu;

import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.Player.Menu.GUIManager;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Player.SomParty;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import static SwordofMagic11.Component.Function.decoLore;

public class PartyBoard extends GUIManager {

    public PartyBoard(PlayerData playerData) {
        super(playerData, "パーティ掲示板", 6);
    }

    public CustomItemStack icon() {
        CustomItemStack item = new CustomItemStack(Material.TOTEM_OF_UNDYING);
        item.setDisplay("パーティ掲示板");
        item.addLore("§a公開パーティの一覧を表示ます");
        item.addSeparator("パーティの公開方法");
        item.addLore(decoLore("パーティを公開") + "/party entryBoard");
        item.addLore(decoLore("アイコンを設定") + "/party icon <id>");
        item.addLore(decoLore("説明文を設定") + "/party lore <text>");
        item.addSeparator("チャットルームの使い方");
        item.addLore(decoLore("チャットルーム") + "/chatroom <部屋名>");
        item.setCustomData("Menu", "PartyBoard");
        return item;
    }

    @Override
    public void updateContainer() {
        int slot = 0;
        for (SomParty party : SomParty.partyList.values()) {
            if (party.isEntryBoard()) {
                setItem(slot, party.viewItem());
                slot++;
            }
        }
    }

    @Override
    public void topClick(int slot, ItemStack clickedItem, ClickType clickType) {
        if (CustomItemStack.hasCustomData(clickedItem, "Party")) {
            if (!playerData.hasParty()) {
                SomParty party = SomParty.partyList.get(CustomItemStack.getCustomData(clickedItem, "Party"));
                if (party.getLimit() > party.getMember().size()) {
                    party.addMember(playerData);
                } else {
                    playerData.sendMessage("§e人数上限§aです", SomSound.Nope);
                }
            } else {
                playerData.sendMessage("§aすでに§e" + playerData.getParty().getId() + "§aに§b参加§aしています", SomSound.Nope);
            }
        }
    }

    @Override
    public void bottomClick(int slot, ItemStack clickedItem, ClickType clickType) {

    }

    @Override
    public void close() {

    }
}
