package SwordofMagic11.Player.Smith;

import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.DataBase.CapsuleDataLoader;
import SwordofMagic11.Item.CapsuleData;
import SwordofMagic11.Player.Menu.GUIManager;
import SwordofMagic11.Player.PlayerData;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static SwordofMagic11.Component.Function.*;
import static SwordofMagic11.DataBase.DataBase.DownScroll;
import static SwordofMagic11.DataBase.DataBase.UpScroll;

public class CapsuleShopMenu extends GUIManager {

    public static CustomItemStack Icon = icon();

    private static CustomItemStack icon() {
        CustomItemStack item = new CustomItemStack(Material.SEA_LANTERN);
        item.setDisplay("カプセル合成");
        item.addLore("§aカプセルを合成してより強力にできます");
        item.setCustomData("SmithMenu", "CapsuleShopMenu");
        return item;
    }

    private int scroll = 0;
    private int amount = 0;

    private int amount() {
        return (int) Math.pow(10, amount);
    }

    private final List<CapsuleData> capsuleDataList;

    public CapsuleShopMenu(PlayerData playerData) {
        super(playerData, "カプセル合成", 6);
        capsuleDataList = new ArrayList<>(CapsuleDataLoader.getCapsuleDataList());
        capsuleDataList.removeIf(capsule -> capsule.getRecipes().isEmpty());
    }

    private CustomItemStack amountIcon() {
        CustomItemStack item = new CustomItemStack(Material.GOLD_NUGGET);
        item.setNonDecoDisplay(decoLore("合成個数") + amount());
        item.setAmountReturn(amount + 1);
        item.setCustomData("AmountIcon", true);
        return item;
    }

    @Override
    public void updateContainer() {
        int slot = 0;
        int index = 0;
        End:
        for (CapsuleData capsule : capsuleDataList) {
            int recipeIndex = 0;
            for (HashMap<String, Integer> recipe : capsule.getRecipes()) {
                if (index >= scroll * 8) {
                    CustomItemStack item = capsule.viewItem();
                    item.setCustomData("CapsuleShop", capsule.getId());
                    item.setCustomData("CapsuleShopIndex", recipeIndex);
                    item.addSeparator("必要アイテム");
                    recipe.forEach((id, amount) -> item.addLore(decoLore(CapsuleDataLoader.getCapsuleData(id).getDisplay()) + amount + "個 §7(" + playerData.capsuleMenu().get(id) + ")"));
                    setItem(slot, item);
                    recipeIndex++;
                    slot++;
                    if (isInvalidSlot(slot)) slot++;
                    if (slot >= 53) break End;
                }
                index++;
            }
        }
        for (int i = 17; i < 45; i += 9) {
            setItem(i, ItemFlame);
        }
        setItem(8, UpScroll());
        setItem(17, amountIcon());
        setItem(53, DownScroll());
    }

    @Override
    public void topClick(int slot, ItemStack clickedItem, ClickType clickType) {
        if (CustomItemStack.hasCustomData(clickedItem, "AmountIcon")) {
            amount++;
            if (amount >= 4) amount = 0;
            SomSound.Tick.play(playerData);
            update();
        } else if (CustomItemStack.hasCustomData(clickedItem, "UpScroll")) {
            scroll = scrollUp(scroll);
            update();
        } else if (CustomItemStack.hasCustomData(clickedItem, "DownScroll")) {
            scroll = scrollDown(capsuleDataList.size(), 6, scroll);
            update();
        } else if (CustomItemStack.hasCustomData(clickedItem, "CapsuleShop")) {
            CapsuleData capsule = CapsuleDataLoader.getCapsuleData(CustomItemStack.getCustomData(clickedItem, "CapsuleShop"));
            if (!playerData.capsuleMenu().hasEmpty(capsule)) {
                playerData.capsuleMenu().sendNonHasEmpty();
                return;
            }
            int recipeIndex = CustomItemStack.getCustomDataInt(clickedItem, "CapsuleShopIndex");
            HashMap<String, Integer> recipe = capsule.getRecipes().get(recipeIndex);
            List<String> message = new ArrayList<>();
            message.add(decoText(capsule.getDisplay()));
            boolean check = true;
            int x = amount();
            for (Map.Entry<String, Integer> entry : recipe.entrySet()) {
                String capsuleId = entry.getKey();
                int amount = entry.getValue() * x;
                CapsuleData capsuleData = CapsuleDataLoader.getCapsuleData(capsuleId);
                String hasAmount = " §7(" + playerData.getCapsule(capsuleData) + ")";
                if (playerData.capsuleMenu().has(capsuleId, amount)) {
                    message.add("§b✓ §f" + capsuleData.getDisplay() + "§ax" + amount + hasAmount);
                } else {
                    message.add("§c× §f" + capsuleData.getDisplay() + "§ax" + amount + hasAmount);
                    check = false;
                }
            }
            if (check) {
                for (Map.Entry<String, Integer> entry : recipe.entrySet()) {
                    String capsuleId = entry.getKey();
                    int amount = entry.getValue() * x;
                    playerData.capsuleMenu().remove(capsuleId, amount);
                }
                playerData.capsuleMenu().add(capsule.getId(), x);
                playerData.sendMessage(capsule.getDisplay() + (x > 1 ? "x" + x : "") + "を§b合成§aしました", SomSound.Tick);
                update();
            } else {
                playerData.sendMessage(message);
            }
        }
    }

    @Override
    public void bottomClick(int slot, ItemStack clickedItem, ClickType clickType) {

    }

    @Override
    public void close() {
        scroll = 0;
        amount = 0;
    }
}
