package SwordofMagic11.Pet;

import SwordofMagic11.Command.Player.Lock;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.Item.Donation.SomDonationItem;
import SwordofMagic11.Item.SomEquip;
import SwordofMagic11.Item.SomItem;
import SwordofMagic11.Item.SyncItem;
import SwordofMagic11.Player.Menu.GUIManager;
import SwordofMagic11.Player.PlayerData;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import static SwordofMagic11.Pet.PetMenu.TaskSlot;

public class PetEditor extends GUIManager {

    private final PetPlus petPlus;
    private final PetMixer petMixer;

    public SomPet somPet;

    public PetEditor(PlayerData playerData) {
        super(playerData, "ペット編集", 3);
        petPlus = new PetPlus(playerData);
        petMixer = new PetMixer(playerData);
    }

    public void open(SomPet somPet) {
        this.somPet = somPet;
        super.open();
    }

    public PetPlus petPlus() {
        return petPlus;
    }

    public PetMixer petMixer() {
        return petMixer;
    }

    @Override
    public void updateContainer() {
        setItem(1, ItemFlame);
        setItem(5, ItemFlame);
        setItem(23, ItemFlame);
        for (int i = 9; i < 15; i++) {
            setItem(i, ItemFlame);
        }
        setItem(0, somPet.viewItem());
        setItem(6, petPlus.icon());
        setItem(7, petMixer.icon());
        setItem(26, playerData.petMenu().icon());

        int slot = 2;
        for (PetTaskType type : PetTaskType.values()) {
            setItem(slot, type.viewItem(somPet));
            slot++;
        }

        slot = 18;
        for (SomEquip.Slot equipSlot : SomEquip.Slot.values()) {
            if (somPet.hasEquip(equipSlot)) {
                setItem(slot, somPet.getEquip(equipSlot).viewItem());
            } else {
                setItem(slot, new CustomItemStack(Material.BARRIER).setNonDecoDisplay("§c" + equipSlot.getDisplay()));
            }
            slot++;
        }
    }

    @Override
    public void topClick(int slot, ItemStack clickedItem, ClickType clickType) {
        if (CustomItemStack.hasCustomData(clickedItem, "Task")) {
            if (playerData.petMenu().getTaskList().size() < TaskSlot) {
                PetTaskType taskType = PetTaskType.valueOf(CustomItemStack.getCustomData(clickedItem, "Task"));
                somPet.updateState(PetState.Task);
                somPet.updateTask(taskType);
                playerData.sendMessage(somPet.colorName() + "§aを§e" + taskType.getDisplay() + "§aに向かわせました", SomSound.Tick);
                update();
            } else {
                playerData.sendMessage("§eタスク枠§aが§c満員§aです", SomSound.Nope);
            }
        } else if (CustomItemStack.hasCustomData(clickedItem, "UUID")) {
            String uuid = CustomItemStack.getCustomData(clickedItem, "UUID");
            if (SyncItem.hasSomItem(uuid)) {
                SomItem somItem = SyncItem.getSomItem(uuid);
                if (somItem instanceof SomEquip equip) {
                    somPet.setEquip(equip.getSlot(), null);
                }
                update();
            }
        } else if (CustomItemStack.hasCustomData(clickedItem, "PetEditor")) {
            switch (CustomItemStack.getCustomData(clickedItem, "PetEditor")) {
                case "PetPlus" -> petPlus.open(somPet);
                case "PetMixer" -> petMixer.open(somPet);
            }
        }
    }

    @Override
    public void bottomClick(int slot, ItemStack clickedItem, ClickType clickType) {
        if (CustomItemStack.hasCustomData(clickedItem, "UUID")) {
            String uuid = CustomItemStack.getCustomData(clickedItem, "UUID");
            if (SyncItem.hasSomItem(uuid)) {
                SomItem item = SyncItem.getSomItem(uuid);
                if (item instanceof SomEquip equip) {
                    somPet.setEquip(equip.getSlot(), equip);
                }
                update();
            }
        }
    }

    @Override
    public void close() {

    }
}
