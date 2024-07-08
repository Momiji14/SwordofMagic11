package SwordofMagic11.Pet;

import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.Player.Menu.GUIManager;
import SwordofMagic11.Player.PlayerData;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.List;

import static SwordofMagic11.Component.Function.scrollDown;
import static SwordofMagic11.Component.Function.scrollUp;
import static SwordofMagic11.DataBase.DataBase.DownScroll;
import static SwordofMagic11.DataBase.DataBase.UpScroll;

public abstract class PetSelect extends GUIManager {

    private final int petRow;
    private int scroll = 0;

    public PetSelect(PlayerData playerData, String display, int petRow) {
        super(playerData, display, 6);
        this.petRow = petRow;
    }

    public abstract void updateUpperContainer();

    @Override
    public void updateContainer() {
        updateContainer(playerData.petMenu().getCageList());
    }

    public void updateContainer(List<SomPet> petList) {
        updateUpperContainer();

        int startRow = 6 - petRow;
        int slot = startRow * 9;
        for (int i = scroll * 8; i < petList.size(); i++) {
            setItem(slot, petList.get(i).viewItem());
            slot++;
            if (isInvalidSlot(slot)) slot++;
            if (slot >= 53) break;
        }

        setItem(startRow * 9 - 1, UpScroll());
        for (int i = startRow; i < getRow(); i++) {
            setItem(startRow * 9 - 1, ItemFlame);
        }
        setItem(getRow() * 9 - 1, DownScroll());
    }

    @Override
    public void topClick(int slot, ItemStack clickedItem, ClickType clickType) {
        if (CustomItemStack.hasCustomData(clickedItem, "UpScroll")) {
            scroll = scrollUp(scroll);
            update();
        } else if (CustomItemStack.hasCustomData(clickedItem, "DownScroll")) {
            scroll = scrollDown(playerData.petMenu().getCageList().size(), petRow, scroll);
            update();
        }
    }

    @Override
    public void close() {
        scroll = 0;
    }
}
