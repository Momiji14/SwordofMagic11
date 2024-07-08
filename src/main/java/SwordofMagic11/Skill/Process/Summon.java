package SwordofMagic11.Skill.Process;

import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.Pet.SomPet;
import SwordofMagic11.Pet.SyncPet;
import SwordofMagic11.Player.ClassType;
import SwordofMagic11.Player.Menu.GUIManager;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Skill.SkillData;
import SwordofMagic11.Skill.SomSkill;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Objects;

import static SwordofMagic11.Component.Function.scrollDown;
import static SwordofMagic11.Component.Function.scrollUp;

public class Summon extends SomSkill {

    private final SummonGUI summonGUI;
    public Summon(PlayerData owner, SkillData skillData) {
        super(owner, skillData);
        summonGUI = new SummonGUI(owner);
    }

    @Override
    public boolean active() {
        summonGUI.open();
        return true;
    }

    public static class SummonGUI extends GUIManager {
        private int scroll = 0;
        public SummonGUI(PlayerData playerData) {
            super(playerData, "ペット召喚", 6);
        }

        @Override
        public void updateContainer() {
            int slot = 0;
            List<SomPet> petList = playerData.petMenu().getCageList();
            if (!petList.isEmpty()) {
                for (int i = scroll * 8; i < petList.size(); i++) {
                    setItem(slot, petList.get(i).viewItem());
                    slot++;
                    if (isInvalidSlot(slot)) slot++;
                    if (slot >= 53) break;
                }
            } else {
                playerData.sendMessage("§b召喚可能§aな§eペット§aがいません", SomSound.Nope);
            }
        }

        @Override
        public void topClick(int slot, ItemStack clickedItem, ClickType clickType) {
            if (CustomItemStack.hasCustomData(clickedItem, "UpScroll")) {
                scroll = scrollUp(scroll);
                update();
            } else if (CustomItemStack.hasCustomData(clickedItem, "DownScroll")) {
                scroll = scrollDown(playerData.petCageSize(), 3, scroll);
                update();
            } else if (CustomItemStack.hasCustomData(clickedItem, "Pet")) {
                int limit;
                if (playerData.classes().getMainClass() == ClassType.DualStar) {
                    limit = 2;
                } else {
                    limit = 1;
                }
                if (playerData.petMenu().getSummonList().size() < limit) {
                    SomPet pet = SyncPet.getSomPet(CustomItemStack.getCustomData(clickedItem, "Pet"));
                    pet.summon();
                    update();
                } else {
                    playerData.sendMessage("§e召喚数上限§aです", SomSound.Nope);
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
}
