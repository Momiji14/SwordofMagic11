package SwordofMagic11.Player.Menu;

import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.Player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.*;

import static SwordofMagic11.Component.Function.scrollDown;
import static SwordofMagic11.Component.Function.scrollUp;
import static SwordofMagic11.DataBase.DataBase.DownScroll;
import static SwordofMagic11.DataBase.DataBase.UpScroll;
import static SwordofMagic11.SomCore.Log;

public class PlayerSelect extends GUIManager {

    private int scroll = 0;
    private Action action;
    private List<PlayerData> playerList = new ArrayList<>();
    public PlayerSelect(PlayerData playerData) {
        super(playerData, "プレイヤー選択", 6);
    }

    public void open(Action action) {
        this.action = action;
        super.open();
    }

    @Override
    public void updateContainer() {
        playerList = new ArrayList<>(PlayerData.getPlayerList());
        playerList.remove(playerData);
        int slot = 0;
        for (int i = scroll * 8; i < playerList.size(); i++) {
            setItem(slot, viewItem(playerList.get(i)));
            slot++;
            if (isInvalidSlot(slot)) slot++;
            if (slot >= 53) break;
        }
        for (int i = 17; i < 45; i += 9) {
            setItem(i, ItemFlame);
        }
        setItem(8, UpScroll());
        setItem(53, DownScroll());
    }

    @Override
    public void topClick(int slot, ItemStack clickedItem, ClickType clickType) {
        if (CustomItemStack.hasCustomData(clickedItem, "UpScroll")) {
            scroll = scrollUp(scroll);
            update();
        } else if (CustomItemStack.hasCustomData(clickedItem, "DownScroll")) {
            scroll = scrollDown(playerList.size(), 6, scroll);
            update();
        } else if (CustomItemStack.hasCustomData(clickedItem, "Player")) {
            Player player = Bukkit.getPlayer(CustomItemStack.getCustomData(clickedItem, "Player"));
            if (player != null && PlayerData.isEntry(player)) {
                PlayerData targetData = PlayerData.get(player);
                switch (action) {
                    case SendMaterial -> playerData.materialMenu().trade(targetData);
                    case SendCapsule -> playerData.capsuleMenu().trade(targetData);
                    case SendItem -> playerData.sendItem().open(targetData);
                    case SendMel -> playerData.sendMel().open(targetData);
                    case OtherPlayerInfo -> playerData.statusMenu().open(targetData);
                    case PartyInvite -> playerData.getParty().invitePlayer(playerData, targetData);
                    default -> playerData.sendMessage("§cError Action", SomSound.Nope);
                }
            } else {
                playerData.sendMessage("§c無効なプレイヤーです", SomSound.Nope);
            }
        }
    }

    @Override
    public void bottomClick(int slot, ItemStack clickedItem, ClickType clickType) {

    }

    @Override
    public void close() {

    }

    public static CustomItemStack viewItem(PlayerData playerData) {
        CustomItemStack item = new CustomItemStack(Material.PLAYER_HEAD);
        item.setNonDecoDisplay(playerData.getName());
        item.setSkull(playerData);
        item.setCustomData("Player", playerData.getName());
        return item;
    }


    public enum Action {
        SendMaterial,
        SendCapsule,
        SendItem,
        SendMel,
        OtherPlayerInfo,
        PartyInvite,
    }
}
