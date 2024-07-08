package SwordofMagic11.Pet;

import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.Item.Material.PetCookie;
import SwordofMagic11.Player.Menu.GUIManager;
import SwordofMagic11.Player.PlayerData;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import static SwordofMagic11.Component.Function.*;
import static SwordofMagic11.Player.Smith.EquipPlusMenu.MaxPlus;

public class PetPlus extends GUIManager {

    public CustomItemStack icon() {
        CustomItemStack item = new CustomItemStack(Material.ANVIL);
        item.setDisplay("ペット強化");
        item.addLore("§aここからペット強化にアクセスできます");
        item.setCustomData("PetEditor", "PetPlus");
        return item;
    }

    public CustomItemStack percentIcon() {
        CustomItemStack item = new CustomItemStack(Material.ENCHANTED_BOOK);
        item.setDisplay("確率強化");
        item.addLore("§a確率でペットを強化します");
        item.addSeparator("強化情報");
        item.addLore(decoLore("成功確率") + scale(percent() * 100, 2) + "%");
        item.addLore(decoLore("必要ローボ") + 1 + "個");
        item.setCustomData("PetPlus", "Percent");
        return item;
    }

    public CustomItemStack confirmIcon() {
        CustomItemStack item = new CustomItemStack(Material.ENCHANTED_BOOK);
        item.setDisplay("確定強化");
        item.addLore("§a確率でペットを強化します");
        item.addSeparator("強化情報");
        item.addLore(decoLore("成功確率") + scale(100, 2) + "%");
        item.addLore(decoLore("必要ローボ") + amount() + "個");
        item.setCustomData("PetPlus", "Confirm");
        return item;
    }

    private SomPet somPet;

    public PetPlus(PlayerData playerData) {
        super(playerData, "ペット強化", 1);
    }

    public double percent() {
        return 1.0 / (somPet.getPlus()+1);
    }

    public int amount() {
        return ceil(1.25 / percent());
    }

    public void open(SomPet somPet) {
        this.somPet = somPet;
        super.open();
    }

    @Override
    public void updateContainer() {

        for (int i = 0; i < 9; i++) {
            setItem(i, ItemFlame);
        }

        setItem(6, percentIcon());
        setItem(7, confirmIcon());

        if (somPet != null) {
            setItem(1, somPet.viewItem());
        }
    }

    @Override
    public void topClick(int slot, ItemStack clickedItem, ClickType clickType) {
        if (CustomItemStack.hasCustomData(clickedItem, "PetPlus")) {
            if (somPet != null) {
                switch (CustomItemStack.getCustomData(clickedItem, "PetPlus")) {
                    case "Percent" -> {
                        if (playerData.hasMaterial(PetCookie.PetCookie, 1)) {
                            playerData.removeMaterial(PetCookie.PetCookie, 1);
                            double percent = percent();
                            if (randomDouble() < percent) {
                                success(scale(percent, 2) + "%");
                                update();
                            } else {
                                playerData.sendMessage(somPet.getName() + "§aの§e強化§aに§c失敗§aしました §e[" + scale(percent, 2) + "%]", SomSound.Tick);
                            }
                        } else {
                            playerData.sendMessage(PetCookie.PetCookie.getDisplay() + "§aが必要です", SomSound.Nope);
                        }
                    }
                    case "Confirm" -> {
                        int amount = amount();
                        if (playerData.hasMaterial(PetCookie.PetCookie, amount)) {
                            playerData.removeMaterial(PetCookie.PetCookie, amount);
                            success("確定強化");
                            update();
                        } else {
                            playerData.sendMessage(PetCookie.PetCookie.getDisplay() + "§aが§e" + amount + "個§a必要です", SomSound.Nope);
                        }
                    }
                }
            }
        }
    }

    private void success(String suffix) {
        somPet.addPlus(1);
        if (somPet.getPlus() >= MaxPlus) {
            playerData.sendMessage(somPet.getName() + "§aの§e強化値§aが§c最大[+" + somPet.getPlus() + "]§aになりました §e[" + suffix + "]", SomSound.Level);
            playerData.closeInventory();
        } else {
            playerData.sendMessage(somPet.getName() + "§aの§e強化値§aが§e[+" + somPet.getPlus() + "]§aになりました §e[" + suffix + "]", SomSound.Level);
        }
    }

    @Override
    public void bottomClick(int slot, ItemStack clickedItem, ClickType clickType) {

    }

    @Override
    public void close() {
        somPet = null;
    }
}
