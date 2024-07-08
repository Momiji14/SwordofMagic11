package SwordofMagic11.Player.UserMenu;

import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.Player.Menu.GUIManager;
import SwordofMagic11.Player.Menu.PlayerSelect;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Map.PvPRaid;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class UserMenu extends GUIManager {

    private int lastTotalMel = 0;
    private final CustomItemStack OtherPlayerInfo;

    public CustomItemStack icon() {
        CustomItemStack item = new CustomItemStack(Material.BOOK);
        item.setDisplay("ユーザーメニュー");
        item.addLore("§aここから各種メニューにアクセスできます");
        item.addLore("§c※BE版は/menuまたは真上を向いてスニーク");
        item.setCustomData("Menu", "UserMenu");
        return item;
    }

    public UserMenu(PlayerData playerData) {
        super(playerData, "ユーザーメニュー", 3);

        OtherPlayerInfo = new CustomItemStack(Material.PAINTING).setDisplay("他プレイヤー情報");
        OtherPlayerInfo.addLore("§a他プレイヤーの情報を表示ます");
        OtherPlayerInfo.setCustomData("PlayerSelect", "OtherPlayerInfo");
    }

    @Override
    public void updateContainer() {
        setItem(0, playerData.materialMenu().icon());
        setItem(1, playerData.capsuleMenu().icon());
        setItem(2, playerData.skillMenu().icon());
        setItem(3, playerData.attributeMenu().icon());
        setItem(4, playerData.palletMenu().icon());
        setItem(5, playerData.triggerMenu().icon());

        setItem(9, playerData.gatheringMenu().icon());
        setItem(10, playerData.memorialMenu().icon());
        setItem(11, playerData.petMenu().icon());
        setItem(12, playerData.teleportMenu().icon());
        setItem(13, playerData.achievementMenu().icon());
        setItem(14, playerData.settingMenu().icon());

        setItem(18, playerData.sendMel().icon());
        setItem(19, playerData.materialMenu().iconTrade());
        setItem(20, playerData.capsuleMenu().iconTrade());
        setItem(21, playerData.sendItem().icon());
        setItem(22, playerData.lockItem().icon());
        setItem(23, playerData.viewItem().icon());

        setItem(7, playerData.raidMenu().icon());
        setItem(8, StatusMenu.statusIcon(playerData).setCustomData("Menu", "StatusMenu"));
        setItem(16, playerData.partyBoard().icon());
        setItem(17, OtherPlayerInfo);
        setItem(25, playerData.marketPlayer().icon());
        setItem(26, playerData.eldShop().icon());

        for (int i = 0; i < 3; i++) {
            setItem(6 + (i * 9), ItemFlame);
        }

        lastTotalMel = playerData.getMel() + playerData.materialMenu().lastTotalMel() + playerData.capsuleMenu().lastTotalMel();
    }

    @Override
    public void topClick(int slot, ItemStack clickedItem, ClickType clickType) {
        if (CustomItemStack.hasCustomData(clickedItem, "PlayerSelect")) {
            playerData.playerSelect().open(PlayerSelect.Action.valueOf(CustomItemStack.getCustomData(clickedItem, "PlayerSelect")));
        }
    }

    @Override
    public void bottomClick(int slot, ItemStack clickedItem, ClickType clickType) {

    }

    @Override
    public void close() {

    }

    public int lastTotalMel() {
        return lastTotalMel;
    }
}
