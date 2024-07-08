package SwordofMagic11.Player;

import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Component.SomTask;
import SwordofMagic11.Custom.CustomInventory;
import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.Item.Material.PotionMaterial;
import SwordofMagic11.Item.SomEquip;
import SwordofMagic11.Item.SomItem;
import SwordofMagic11.Item.SomUseItem;
import SwordofMagic11.Item.SyncItem;
import SwordofMagic11.Map.PvPRaid;
import SwordofMagic11.Player.Gathering.GatheringMenu;
import SwordofMagic11.Player.Menu.GUIManager;
import SwordofMagic11.Player.Setting.PlayerSetting;
import SwordofMagic11.Player.Smith.MaterializeMenu;
import SwordofMagic11.Skill.SomSkill;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;

import static SwordofMagic11.Component.Function.*;
import static SwordofMagic11.DataBase.DataBase.DownScroll;
import static SwordofMagic11.DataBase.DataBase.UpScroll;
import static SwordofMagic11.Player.Menu.GUIManager.isInvalidSlot;
import static SwordofMagic11.Player.Pallet.nonPallet;

public class PlayerInventory {

    private final PlayerData playerData;
    private final CustomInventory itemInventory;
    private int scroll = 0;
    private boolean clickAble = true;

    public PlayerInventory(PlayerData playerData) {
        this.playerData = playerData;
        itemInventory = new CustomInventory(playerData);
    }

    public interface Interface {
        PlayerInventory inventory();

        default CustomInventory itemInventory() {
            return inventory().itemInventory;
        }

        default CustomItemStack viewEquip(SomEquip.Slot slot, boolean view) {
            PlayerData playerData = inventory().playerData;
            SomEquip equip = playerData.equipment.get(slot);
            if (equip != null) {
                CustomItemStack item = equip.viewItem().setCustomData("Equipped", true);
                if (!view) item.setType(Material.STONE_BUTTON);
                switch (equip.getEquipCategory()) {
                    case MiningTool -> {
                        int level = playerData.gatheringMenu().getSkillValueInt(GatheringMenu.Type.Mining, GatheringMenu.Skill.Efficiency);
                        item.addUnsafeEnchantment(Enchantment.DIG_SPEED, level);
                    }
                    case CraftTool -> playerData.craftMenu().setCraftTool(equip);
                }
                return item;
            } else return null;
        }

        default void updatePallet() {
            Inventory inventory = inventory().playerData.getPlayer().getInventory();
            PlayerData playerData = inventory().playerData;
            Classes classes = playerData.classes();

            Pallet[] pallet = classes.getPallet(classes.getMainClass());
            for (int i = 0; i < pallet.length; i++) {
                CustomItemStack palletItem;
                if (pallet[i] != null) {
                    CustomItemStack viewItem = pallet[i].viewItem();
                    if (pallet[i] instanceof SomSkill skill) {
                        if (skill.hasStack()) {
                            viewItem.setAmount(skill.getStack());
                        } else {
                            viewItem.setAmount(ceil(skill.getCurrentCoolTime() / 20.0));
                            viewItem.setIcon(Material.NETHER_STAR);
                        }
                    } else if (pallet[i] instanceof PotionMaterial) {
                        if (playerData.hasTimer("Potion")) {
                            viewItem.setAmount(ceil(playerData.timer("Potion") / 20.0));
                            viewItem.setIcon(Material.NETHER_STAR);
                        }
                    }
                    viewItem.setCustomData("Pallet", i);
                    palletItem = viewItem;
                } else {
                    palletItem = nonPallet(i);
                }
                int select = playerData.palletMenu().getSelect();
                if (select != -1 && palletItem != null) {
                    palletItem.setGlowing();
                }
                inventory.setItem(i, palletItem);
            }
        }

        default void updateMenu() {
            PlayerData playerData = inventory().playerData;
            Inventory inventory = playerData.getPlayer().getInventory();
            inventory.setItem(17, UpScroll());
            inventory.setItem(26, playerData.getShopManager().isOpen() ? MaterializeMenu.Icon : playerData.userMenu().icon());
            inventory.setItem(35, DownScroll());
        }

        default void updateMainHand() {
            PlayerData playerData = inventory().playerData;
            Inventory inventory = playerData.getPlayer().getInventory();
            inventory.setItem(8, viewEquip(SomEquip.Slot.MainHand, true));
        }

        default void updateInventory() {
            SomTask.async(() -> {
                PlayerData playerData = inventory().playerData;
                Inventory inventory = playerData.getPlayer().getInventory();
                int slot = 9;
                List<SomItem> itemList = itemInventory().getList();
                Collections.sort(itemList); //雑ソート実装 1/21 14:00 krkntn SomItemにComparebleを実装
                for (int i = inventory().scroll * 8; i < itemList.size(); i++) {
                    inventory.setItem(slot, itemList.get(i).viewItem());
                    slot++;
                    if (isInvalidSlot(slot)) slot++;
                    if (slot >= 35) break;
                }
                while (slot < 35) {
                    inventory.setItem(slot, null);
                    slot++;
                    if (isInvalidSlot(slot)) slot++;
                }

                //inventory.setItem(40, viewEquip(SomEquip.Slot.OffHand, true));

                updateEquipView();
                updateMainHand();
            });
        }

        default void updateEquipView() {
            updateEquipView(inventory().playerData.isArsha());
        }

        default void updateEquipView(boolean isArsha) {
            PlayerData playerData = inventory().playerData;
            Inventory inventory = playerData.getPlayer().getInventory();
            if (PvPRaid.isInPvPRaid(playerData)) {
                PvPRaid.Team team = PvPRaid.getTeam(playerData);
                PvPRaid.Kit kit = PvPRaid.getKit(playerData);
                inventory.setItem(39, kit.Head);
                inventory.setItem(38, team.Chest);
                inventory.setItem(37, team.Legs);
                inventory.setItem(36, team.Foot);
            } else {
                boolean view = isArsha || playerData.setting().is(PlayerSetting.BooleanEnum.EquipView);
                inventory.setItem(39, viewEquip(SomEquip.Slot.Head, view));
                inventory.setItem(38, viewEquip(SomEquip.Slot.Chest, view));
                inventory.setItem(37, viewEquip(SomEquip.Slot.Legs, view));
                inventory.setItem(36, viewEquip(SomEquip.Slot.Foot, view));
            }
        }

        default boolean isClickAble() {
            return inventory().clickAble;
        }

        default void clickAble(boolean bool) {
            inventory().clickAble = bool;
        }

        default void inventoryClick(InventoryClickEvent event) {
            PlayerData playerData = inventory().playerData;
            if (playerData.isPlayMode()) {
                event.setCancelled(true);
                if (playerData.isDeath() || !isClickAble()) return;
                clickAble(false);
                SomTask.async(() -> {
                    InventoryView view = event.getView();
                    Inventory topInv = view.getTopInventory();
                    Inventory bottomInv = view.getBottomInventory();
                    Inventory clickInv = event.getClickedInventory();
                    ClickType clickType = event.getClick();
                    int slot = event.getSlot();
                    ItemStack clickedItem = event.getCurrentItem();
                    if (clickedItem != null) {
                        if (bottomInv.equals(clickInv)) {
                            if (CustomItemStack.hasCustomData(clickedItem, "UpScroll")) {
                                inventory().scroll = scrollUp(inventory().scroll);
                                updateInventory();
                            } else if (CustomItemStack.hasCustomData(clickedItem, "DownScroll")) {
                                inventory().scroll = scrollDown(itemInventory().getList().size(), 3, inventory().scroll);
                                updateInventory();
                            }
                        }
                        if (CustomItemStack.hasCustomData(clickedItem, "Menu")) {
                            playerData.menuHolder().menuClick(CustomItemStack.getCustomData(clickedItem, "Menu"));
                            SomSound.Tick.play(playerData);
                            return;
                        }
                        if (CustomItemStack.hasCustomData(clickedItem, "Pallet")) {
                            int pallet = CustomItemStack.getCustomDataInt(clickedItem, "Pallet");
                            playerData.classes().setPallet(pallet, null);
                        }
                        if (!CustomItemStack.hasCustomData(clickedItem, "Equipped")) {
                            for (GUIManager manager : playerData.getGuiManagers()) {
                                if (manager.getInventory().equals(event.getInventory())) {
                                    if (topInv.equals(clickInv)) {
                                        manager.topClick(slot, clickedItem, clickType);
                                    } else if (bottomInv.equals(clickInv)) {
                                        manager.bottomClick(slot, clickedItem, clickType);
                                    }
                                    return;
                                }
                            }
                        }
                        if (bottomInv.equals(clickInv)) {
                            if (CustomItemStack.hasCustomData(clickedItem, "NonPallet")) {
                                playerData.palletMenu().setSelect(CustomItemStack.getCustomDataInt(clickedItem, "NonPallet"));
                                playerData.palletMenu().open();
                            } else if (CustomItemStack.hasCustomData(clickedItem, "Equip.Category")) {
                                SomEquip somEquip = (SomEquip) SyncItem.getSomItem(CustomItemStack.getCustomData(clickedItem, "UUID"));
                                SomEquip.Category category = SomEquip.Category.valueOf(CustomItemStack.getCustomData(clickedItem, "Equip.Category"));
                                if (CustomItemStack.hasCustomData(clickedItem, "Equipped")) {
                                    playerData.setEquip(category.getSlot(), null);
                                } else {
                                    playerData.setEquip(category.getSlot(), somEquip);
                                }
                                SomSound.Equip.play(playerData);
                            } else if (CustomItemStack.hasCustomData(clickedItem, "SomUseItem")) {
                                if (PvPRaid.isInPvPRaid(playerData)) return;
                                SomUseItem item = (SomUseItem) SyncItem.getSomItem(CustomItemStack.getCustomData(clickedItem, "UUID"));
                                if (item.getOwner() == playerData) {
                                    item.use(playerData);
                                } else {
                                    playerData.sendMessage("この§eアイテム§aの§e所有権§aがありません", SomSound.Nope);
                                }
                            }
                        }
                    }
                }, () -> clickAble(true));
            }
        }

        default void inventoryClose(InventoryCloseEvent event) {
            SomTask.async(() -> {
                PlayerData playerData = inventory().playerData;
                if (playerData.isPlayMode()) {
                    InventoryView view = event.getView();
                    for (GUIManager manager : playerData.getGuiManagers()) {
                        if (view.getTitle().equals(manager.getDisplay())) {
                            manager.close();
                        }
                    }
                }
            });
        }
    }
}
