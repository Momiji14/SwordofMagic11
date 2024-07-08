package SwordofMagic11.Player.Menu;

import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.Player.PlayerData;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public abstract class PasswordGUI extends GUIManager {
    private static final String[] keys = new String[]{"1","2","3","4","5","6","7","8","9","0","A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};
    protected String rawPass;
    protected String inputPass = "";

    public PasswordGUI(PlayerData playerData, String display) {
        super(playerData, display, 6);
    }

    @Override
    public void open() {
        inputPass = "";
        super.open();
    }

    @Override
    public void updateContainer() {
        String[] input = inputPass.split("");
        for (int i = 0; i < inputPass.length(); i++) {
            setItem(i, new CustomItemStack(Material.EMERALD_BLOCK).setNonDecoDisplay("§f" + input[0]));
        }
        for (int i = 0; i < rawPass.length()-inputPass.length(); i++) {
            setItem(inputPass.length() + i, new CustomItemStack(Material.REDSTONE_BLOCK).setNonDecoDisplay(" "));
        }
        for (int i = 9; i < 18; i++) {
            setItem(i, ItemFlame);
        }
        for (int i = 0; i < keys.length; i++) {
            setItem(i + 18, new CustomItemStack(Material.STONE_BUTTON).setNonDecoDisplay("§f" + keys[i]).setCustomData("Key", keys[i]));
        }
    }

    @Override
    public void topClick(int slot, ItemStack clickedItem, ClickType clickType) {
        if (CustomItemStack.hasCustomData(clickedItem, "Key")) {
            String key = CustomItemStack.getCustomData(clickedItem, "Key");
            inputPass += key;
            if (rawPass.length() <= inputPass.length()) {
                if (rawPass.equals(inputPass)) {
                    success();
                    playerData.sendMessage("§e認証§aしました", SomSound.Level);
                } else {
                    playerData.sendMessage("§eパスワード§aが違います", SomSound.Nope);
                }
                playerData.closeInventory();
            } else {
                SomSound.Tick.play(playerData);
                update();
            }
        }
    }

    @Override
    public void bottomClick(int slot, ItemStack clickedItem, ClickType clickType) {

    }

    public abstract void success();

    @Override
    public void close() {
        inputPass = "";
    }
}
