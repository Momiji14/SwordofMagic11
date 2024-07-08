package SwordofMagic11.Player.Smith;

import SwordofMagic11.Command.Player.Lock;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.Item.SomEquip;
import SwordofMagic11.Item.SyncItem;
import SwordofMagic11.Player.Market.MarketPlayer;
import SwordofMagic11.Player.Menu.GUIManager;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Player.Statistics.Statistics;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import static SwordofMagic11.Component.Function.*;

public class EquipTranceMenu extends GUIManager {

    public static CustomItemStack Icon = icon();

    private static CustomItemStack icon() {
        CustomItemStack item = new CustomItemStack(Material.BEACON);
        item.setDisplay("装備超越");
        item.addLore("§a超越値を上げることができます");
        item.addLore("§a同名の武器を生贄にします");
        item.addLore("§a成功率は§e本体強化値*2%§aです");
        item.addLore("§aさらに§e素材強化値§aで成功率が上昇します");
        item.addLore("§c失敗§aに§c10%§aの確率で§4下落§aします");
        item.setCustomData("SmithMenu", "EquipTranceMenu");
        return item;
    }

    private SomEquip equipment;
    private SomEquip materialEquipment;
    private boolean useMel = true;

    public EquipTranceMenu(PlayerData playerData) {
        super(playerData, "装備超越", 1);
    }

    public CustomItemStack currency() {
        CustomItemStack item;
        if (useMel) {
            item = new CustomItemStack(Material.GOLD_NUGGET);
            item.setDisplay("使用通貨");
            item.addLore(decoLore("所持メル") + playerData.getMel());
            item.addLore(decoLore("消費メル") + mel());
            item.setCustomData("Currency", "Mel");
        } else {
            item = new CustomItemStack(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE);
            item.setDisplay("使用通貨");
            item.addLore(decoLore("所持グラインダー") + playerData.getMaterial("グラインダー"));
            item.addLore(decoLore("消費グラインダー") + grinder());
            item.setCustomData("Currency", "Grinder");
        }
        return item;
    }


    public double percent() {
        return (equipment.getPlus() * 0.02) + (Math.pow(1.001, Math.pow(materialEquipment.getPlus(), 1.3))-1);
    }

    public int mel() {
        if (equipment == null) return 0;
        return ceil(equipment.getPower() * (equipment.getTrance()+1) * 2500);
    }

    public int grinder() {
        return ceil((mel() * 1.25) / Math.min(100, MarketPlayer.getAverageMel("グラインダー")));
    }

    public double reqExp() {
        return 0.3 + equipment.getTrance() * 0.05;
    }

    @Override
    public void updateContainer() {
        for (int i = 0; i < 9; i++) {
            setItem(i, ItemFlame);
        }
        if (equipment != null) {
            setItem(1, equipment.viewItem());
            if (materialEquipment != null) {
                setItem(3, materialEquipment.viewItem());
                SomEquip result = equipment.clone();
                result.addTrance(1);
                CustomItemStack resultItem = result.viewItem().setCustomData("Result", true);
                resultItem.addSeparator("超越情報");
                if (useMel) {
                    resultItem.addLore(decoLore("必要メル") + mel());
                } else {
                    resultItem.addLore(decoLore("必要グラインダー") + grinder());
                }
                resultItem.addLore(decoLore("成功確率") + scale(percent()*100, 3) + "%");
                setItem(5, resultItem);
            } else {
                setItem(3, null);
                setItem(5, null);
            }
        } else {
            setItem(1, null);
            setItem(3, null);
            setItem(5, null);
        }
        setItem(7, currency());
    }

    @Override
    public void topClick(int slot, ItemStack clickedItem, ClickType clickType) {
        if (CustomItemStack.hasCustomData(clickedItem, "Currency")) {
            switch (CustomItemStack.getCustomData(clickedItem, "Currency")) {
                case "Mel" -> useMel = false;
                case "Grinder" -> useMel = true;
            }
            update();
        } else if (CustomItemStack.hasCustomData(clickedItem, "Result")) {
            if (equipment.getExp() < reqExp()) {
                playerData.sendMessage("§e超越[" + numberRoma(equipment.getTrance()+1) + "]§aに挑戦するには§e熟練度§aが§e" + scale(reqExp()*100) + "%§a以上必要です", SomSound.Nope);
                return;
            }
            if (useMel) {
                int mel = mel();
                if (playerData.getMel() < mel) {
                    playerData.sendNonMel();
                    return;
                }
                playerData.removeMel(mel);
                playerData.statistics().add(Statistics.IntEnum.TranceMel, mel);
            } else {
                int grinder = grinder();
                if (!playerData.hasMaterial("グラインダー", grinder)) {
                    playerData.sendNonGrinder();
                    return;
                }
                playerData.removeMaterial("グラインダー", grinder);
                playerData.statistics().add(Statistics.IntEnum.TranceGrinder, grinder);
            }
            playerData.itemInventory().delete(materialEquipment);
            playerData.statistics().add(Statistics.IntEnum.TranceChallenge, 1);
            double percent = percent();
            if (randomDouble() < percent) {
                equipment.addTrance(1);
                playerData.statistics().add(Statistics.IntEnum.TranceSuccess, 1);
                playerData.sendMessage(equipment.getDisplay() + "§aの§e超越§aに§b成功§aしました", SomSound.Level);
            } else if (randomDouble() > 0.1 || equipment.getTrance() == 0) {
                playerData.statistics().add(Statistics.IntEnum.TranceFailed, 1);
                playerData.sendMessage(equipment.getDisplay() + "§aの§e超越§aに§c失敗§aしました", SomSound.Tick);
            } else {
                equipment.addTrance(-1);
                playerData.statistics().add(Statistics.IntEnum.TranceDown, 1);
                playerData.sendMessage(equipment.getDisplay() + "§aの§e超越§aが§4下落§aしました", SomSound.Tick);
            }
        }
        if (CustomItemStack.hasCustomData(clickedItem, "Equip.Category")) {
            String uuid = CustomItemStack.getCustomData(clickedItem, "UUID");
            if (equipment != null && equipment.getUUID().equals(uuid)) {
                equipment = null;
                materialEquipment = null;
                update();
            }
            if (materialEquipment != null && materialEquipment.getUUID().equals(uuid)) {
                materialEquipment = null;
                update();
            }
        }
    }

    @Override
    public void bottomClick(int slot, ItemStack clickedItem, ClickType clickType) {
        if (CustomItemStack.hasCustomData(clickedItem, "Equip.Category")) {
            SomEquip equip = (SomEquip) SyncItem.getSomItem(CustomItemStack.getCustomData(clickedItem, "UUID"));
            if (equipment == null) {
                if (equip.getPlus() < 25) {
                    playerData.sendMessage("§e超越§aは§e+25§aから行えます", SomSound.Nope);
                    return;
                }
                equipment = equip;
                update();
            } else if (equipment.getId().equals(equip.getId())) {
                if (Lock.check(playerData, equip.getUUID())) return;
                if (!equipment.getUUID().equals(equip.getUUID())) {
                    materialEquipment = equip;
                    update();
                } else {
                    playerData.sendMessage("§aこれは§e素体装備§aです", SomSound.Nope);
                }
            } else {
                playerData.sendMessage("§e同名装備§aが§c必要§aです", SomSound.Nope);
            }
        }
    }

    @Override
    public void close() {
        equipment = null;
        materialEquipment = null;
    }
}
