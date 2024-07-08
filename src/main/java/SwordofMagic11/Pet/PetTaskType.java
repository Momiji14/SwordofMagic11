package SwordofMagic11.Pet;

import SwordofMagic11.Custom.CustomItemStack;
import org.bukkit.Material;

import static SwordofMagic11.Component.Function.decoLore;

public enum PetTaskType {
    Mining("採掘", Material.IRON_PICKAXE),
    Collect("採集", Material.SHEARS),
    Fishing("漁獲", Material.FISHING_ROD),
            ;

    private final String display;
    private final Material icon;

    PetTaskType(String display, Material icon) {
        this.display = display;
        this.icon = icon;
    }

    public String getDisplay() {
        return display;
    }

    public Material getIcon() {
        return icon;
    }

    public PetTaskType next() {
        int ordinal = this.ordinal() + 1;
        if (ordinal >= PetTaskType.values().length) ordinal = 0;
        return PetTaskType.values()[ordinal];
    }

    public CustomItemStack viewItem(SomPet somPet) {
        CustomItemStack icon = new CustomItemStack(getIcon());
        icon.setDisplay(getDisplay());
        icon.addLore(decoLore("レベル") + somPet.getLevel(PetLevelType.valueOf(toString())));
        icon.setCustomData("Task", toString());
        icon.setCustomData("TaskOwner", somPet.getUUID());
        return icon;
    }
}
