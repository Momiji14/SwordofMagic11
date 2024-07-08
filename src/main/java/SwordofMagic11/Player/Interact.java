package SwordofMagic11.Player;

import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Component.SomTask;
import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.DataBase.ShopDataLoader;
import SwordofMagic11.Item.SomEquip;
import SwordofMagic11.Player.Gathering.Fishing;
import SwordofMagic11.Player.Setting.PlayerSetting;
import SwordofMagic11.Player.Shop.ShopData;
import SwordofMagic11.SomCore;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

import static SwordofMagic11.Component.Function.decoText;
import static SwordofMagic11.Component.Function.loreText;

public interface Interact {

    static void interact(PlayerData playerData, PlayerInteractEvent event) {
        if (playerData.isPlayMode()) {
            Action action = event.getAction();
            ItemStack clickedItem = event.getItem();
            Block block = event.getClickedBlock();
            if (playerData.fishing().isHoldFishingRod()) {
                if (playerData.fishing().isFishing()) {
                    if (action.isLeftClick()) {
                        playerData.fishing().click(Fishing.Combo.Centre);
                        event.setCancelled(true);
                    } else if (event.hasBlock()) {
                        event.setCancelled(true);
                    }
                }
                return;
            }
            switch (action) {
                case LEFT_CLICK_AIR, LEFT_CLICK_BLOCK -> {
                    if (playerData.hasLeftClickOverride()) {
                        playerData.leftClickOverride().run();
                    } else {
                        playerData.normalAttack();
                    }
                    if (!event.hasBlock()) event.setCancelled(true);
                }
                case RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK -> {
                    event.setCancelled(true);
                    if (clickedItem != null) {
                        SomTask.async(() -> {
                            int slot = event.getPlayer().getInventory().getHeldItemSlot();
                            if (slot < 8) {
                                Pallet pallet = playerData.classes().getPallet(slot);
                                if (pallet != null) pallet.use(playerData);
                            } else if (slot == 8) {
                                if (playerData.setting().is(PlayerSetting.BooleanEnum.RightStrafe)) {
                                    if (playerData.hasEquip(SomEquip.Slot.MainHand)) {
                                        SomEquip mainHand = playerData.getEquip(SomEquip.Slot.MainHand);
                                        if (mainHand.isWeapon() || mainHand.getEquipCategory() == SomEquip.Category.MiningTool || mainHand.getEquipCategory() == SomEquip.Category.CollectTool) {
                                            playerData.strafe();
                                        }
                                    }
                                }
                            }
                            if (CustomItemStack.hasCustomData(clickedItem, "Equip.Category")) {
                                if (CustomItemStack.getCustomData(clickedItem, "Equip.Category").equals("CraftTool")) {
                                    playerData.craftMenu().open();
                                }
                            }
                        });
                    }
                    if (block != null) {
                        switch (block.getType()) {
                            case LECTERN, STONE_BUTTON -> event.setCancelled(false);
                        }
                    }
                }
            }
        }
    }

    static void interactEntity(PlayerData playerData, PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        if (playerData.isPlayMode()) {
            event.setCancelled(true);
            PersistentDataContainer container = entity.getPersistentDataContainer();
            if (container.has(SomCore.Key("Shop"), PersistentDataType.STRING)) {
                ShopData shopData = ShopDataLoader.getShopData(container.get(SomCore.Key("Shop"), PersistentDataType.STRING));
                playerData.getShopManager().open(shopData);
            }
            if (container.has(SomCore.Key("Message"), PersistentDataType.STRING)) {
                List<String> message = new ArrayList<>();
                message.add(decoText(entity.getName()));
                message.addAll(loreText(container.get(SomCore.Key("Message"), PersistentDataType.STRING)));
                playerData.sendMessage(message, SomSound.Tick);
            }
            if (container.has(SomCore.Key("Classes"), PersistentDataType.STRING)) {
                playerData.classesMenu().open();
            }
            if (container.has(SomCore.Key("Buyer"), PersistentDataType.STRING)) {
                playerData.sellMenu().open();
            }
            if (container.has(SomCore.Key("Smith"), PersistentDataType.STRING)) {
                playerData.smithMenu().open();
            }
            if (container.has(SomCore.Key("PvPRaid"), PersistentDataType.STRING)) {
                if ("Kit".equals(container.get(SomCore.Key("PvPRaid"), PersistentDataType.STRING))) {
                    playerData.pvpKit().open();
                }
            }
        }
    }

    static void toolChange(PlayerData playerData, PlayerItemHeldEvent event) {
        if (playerData.mania().isStart()) {
            SomTask.async(() -> {
                switch (event.getNewSlot()) {
                    case 0 -> playerData.mania().tap(0);
                    case 1 -> playerData.mania().tap(1);
                    case 2, 7 -> playerData.mania().tap(2);
                    case 3, 8 -> playerData.mania().tap(3);
                }
            });
            return;
        } else if (playerData.fishing().isFishing()) {
            SomTask.async(() -> {
                switch (event.getNewSlot()) {
                    case 0,7 -> playerData.fishing().click(Fishing.Combo.Rim);
                    case 1,6 -> playerData.fishing().click(Fishing.Combo.Centre);
                }
            });
            event.setCancelled(true);
            return;
        } else if (playerData.fishing().isHoldFishingRod()) {
            playerData.getPlayer().resetTitle();
        }
        if (playerData.setting().is(PlayerSetting.BooleanEnum.QuickCast)) {
            int newSlot = event.getNewSlot();
            if (newSlot < 8) {
                Pallet pallet = playerData.classes().getPallet(newSlot);
                if (pallet != null) pallet.use(playerData);
                playerData.getPlayer().getInventory().setHeldItemSlot(8);
            }
        }
    }
}
