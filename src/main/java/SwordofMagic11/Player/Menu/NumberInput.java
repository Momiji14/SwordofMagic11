package SwordofMagic11.Player.Menu;

import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.Player.PlayerData;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public abstract class NumberInput extends GUIManager {

    private CustomItemStack viewItem;
    private final List<Integer> input = new ArrayList<>();

    public int input() {
        int index = 0;
        int value = 0;
        for (Integer i : input) {
            if (i > 0) {
                value += (int) (i * Math.pow(10, index));
            }
            index++;
        }
        return value;
    }

    public NumberInput(PlayerData playerData, String display) {
        super(playerData, display, 6);
    }

    public boolean isEntered() {
        return !input.isEmpty();
    }

    public void setViewItem(CustomItemStack viewItem) {
        this.viewItem = viewItem;
    }

    @Override
    public void updateContainer() {
        int slot = 8;
        for (Integer i : input) {
            setItem(slot, CustomItemStack.Light(i));
            slot--;
            if (slot < 0) break;
        }

        for (int i = 9; i < 54; i++) {
            setItem(i, ItemFlame);
        }

        setItem(21, CustomItemStack.Light(1));
        setItem(22, CustomItemStack.Light(2));
        setItem(23, CustomItemStack.Light(3));
        setItem(30, CustomItemStack.Light(4));
        setItem(31, CustomItemStack.Light(5));
        setItem(32, CustomItemStack.Light(6));
        setItem(39, CustomItemStack.Light(7));
        setItem(40, CustomItemStack.Light(8));
        setItem(41, CustomItemStack.Light(9));
        setItem(48, new CustomItemStack(Material.REDSTONE_BLOCK).setNonDecoDisplay("§fBackSpace").setCustomData("Key", "BackSpace"));
        setItem(49, CustomItemStack.Light(0));
        setItem(50, new CustomItemStack(Material.EMERALD_BLOCK).setNonDecoDisplay("§fEnter").setCustomData("Key", "Enter"));

        if (viewItem != null) {
            setItem(28, viewItem);
            setItem(34, viewItem);
        }
    }

    @Override
    public void topClick(int slot, ItemStack clickedItem, ClickType clickType) {
        if (CustomItemStack.hasCustomData(clickedItem, "Number")) {
            int i = CustomItemStack.getCustomDataInt(clickedItem, "Number");
            if (input.size() < 9) {
                if (i > 0 || !input.isEmpty()) {
                    input.add(0, i);
                    update();
                }
            }
        }
        if (CustomItemStack.hasCustomData(clickedItem, "Key")) {
            switch (CustomItemStack.getCustomData(clickedItem, "Key")) {
                case "BackSpace" -> {
                    if (!input.isEmpty()) {
                        input.removeFirst();
                        update();
                    }
                }
                case "Enter" -> enter(input());
            }
        }
    }

    public abstract void enter(int input);

    @Override
    public void bottomClick(int slot, ItemStack clickedItem, ClickType clickType) {

    }

    @Override
    public void close() {
        viewItem = null;
        input.clear();
    }
}
