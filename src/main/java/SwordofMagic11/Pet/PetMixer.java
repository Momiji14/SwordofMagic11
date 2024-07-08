package SwordofMagic11.Pet;

import SwordofMagic11.AttributeType;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.Enemy.Custom.CustomData;
import SwordofMagic11.Player.Menu.GUIManager;
import SwordofMagic11.Player.PlayerData;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.units.qual.C;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import static SwordofMagic11.Component.Function.*;
import static SwordofMagic11.DataBase.DataBase.DownScroll;
import static SwordofMagic11.DataBase.DataBase.UpScroll;

public class PetMixer extends PetSelect {

    public CustomItemStack icon() {
        CustomItemStack item = new CustomItemStack(Material.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE);
        item.setDisplay("ペット合成");
        item.addLore("§aここからペット合成にアクセスできます");
        item.setCustomData("PetEditor", "PetMixer");
        return item;
    }

    private SomPet somPet;
    private List<SomPet> materialPet = new ArrayList<>();

    public PetMixer(PlayerData playerData) {
        super(playerData, "ペット合成", 4);
    }

    public void open(SomPet somPet) {
        this.somPet = somPet;
        super.open();
    }

    public CustomItemStack enterIcon() {
        boolean check = (somPet != null && !materialPet.isEmpty());
        CustomItemStack icon;
        if (check) {
            icon = new CustomItemStack(Material.EMERALD).setNonDecoDisplay("§aペットを合成する");
        } else {
            icon = new CustomItemStack(Material.REDSTONE);
            if (somPet != null) {
                icon.setNonDecoDisplay("§c素材ペットを選択してください");
            } else {
                icon.setNonDecoDisplay("§c本体ペットを選択してください");
            }
        }
        icon.setCustomData("Enter", check);
        return icon;
    }

    @Override
    public void updateContainer() {
        List<SomPet> petList = playerData.petMenu().getCageList();
        petList.removeAll(materialPet);
        petList.remove(somPet);
        updateContainer(petList);
    }

    @Override
    public void updateUpperContainer() {
        for (int i = 9; i < 18; i++) {
            setItem(i, ItemFlame);
        }
        setItem(1, ItemFlame);
        setItem(8, enterIcon());

        if (somPet != null) {
            setItem(0, somPet.viewItem());
        }

        int slot = 2;
        for (SomPet pet : materialPet) {
            setItem(slot, pet.viewItem());
            slot++;
        }
    }

    @Override
    public void topClick(int slot, ItemStack clickedItem, ClickType clickType) {
        if (CustomItemStack.hasCustomData(clickedItem, "Pet")) {
            String uuid = CustomItemStack.getCustomData(clickedItem, "Pet");
            SomPet pet = SyncPet.getSomPet(uuid);
            if (pet == somPet) {
                somPet = null;
                materialPet.clear();
            } else {
                materialPet.remove(pet);
            }
        } else if (CustomItemStack.hasCustomData(clickedItem, "Enter")) {
            if (CustomItemStack.getCustomDataBoolean(clickedItem, "Enter")) {
                int success = 0;
                int failed = 0;
                HashMap<AttributeType, Double> addition = new HashMap<>();
                double percent = materialPet.size() * 0.1;
                for (SomPet pet : materialPet) {
                    if (randomDouble() < percent) {
                        addition.merge(pet.additionAttributeType(), randomDouble(0.5, 1.5), Double::sum);
                        success++;
                    } else failed++;
                    SyncPet.delete(pet.getUUID());
                }
                List<String> message = new ArrayList<>();
                message.add(decoText("合成結果"));
                message.add(decoLore("§b成功") + success + "回");
                message.add(decoLore("§c失敗") + failed + "回");
                message.add(decoSeparator("上昇ステータス"));
                addition.forEach((attr, value) -> message.add(decoLore(attr.getDisplay()) + scale(value, 2, true)));
                playerData.sendMessage(message, success > 0 ? SomSound.Level : SomSound.Tick);
                update();
            } else {
                playerData.sendMessage("§e本体ペット§aと§e素材ペット§aを§c最低一体§aは§bセット§aしてください", SomSound.Nope);
            }
        }
    }

    @Override
    public void bottomClick(int slot, ItemStack clickedItem, ClickType clickType) {

    }

    @Override
    public void close() {
        super.close();
        somPet = null;
        materialPet.clear();
    }
}
