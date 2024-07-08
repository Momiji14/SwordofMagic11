package SwordofMagic11.Item.Material;

import SwordofMagic11.Component.Function;
import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Player.Setting.PlayerSetting.BooleanEnum;
import org.bukkit.Color;
import org.bukkit.Material;

import java.util.List;

import static SwordofMagic11.Component.Function.decoLore;

public class MaterialData {
    private String id;
    private String display;
    private Material icon;
    private Color color;
    private List<String> lore;
    private int sell = -1;
    private boolean isRare = false;
    private boolean useAble = false;
    private boolean isNonUseSlot = false;

    public MaterialData() {
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplay() {
        return this.display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public Material getIcon() {
        return this.icon;
    }

    public void setIcon(Material icon) {
        this.icon = icon;
    }

    public Color getColor() {
        return this.color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setLore(List<String> lore) {
        this.lore = lore;
    }

    public List<String> getLore() {
        return this.lore;
    }

    public boolean hasLore() {
        return this.lore != null;
    }

    public int getSell() {
        return sell == -1 ? 1 : sell;
    }

    public int getRawSell() {
        return sell;
    }

    public void setSell(int sell) {
        this.sell = sell;
    }

    public boolean isRare() {
        return this.isRare;
    }

    public void setRare(boolean rare) {
        this.isRare = rare;
    }

    public boolean isUseAble() {
        return this.useAble;
    }

    public void setUseAble(boolean useAble) {
        this.useAble = useAble;
    }

    public boolean isNonUseSlot() {
        return isNonUseSlot;
    }

    public void setNonUseSlot(boolean nonUseSlot) {
        isNonUseSlot = nonUseSlot;
    }

    public CustomItemStack viewItem() {
        CustomItemStack item = new CustomItemStack(this.icon);
        CustomItemStack.setupColor(item, this.color);
        item.setDisplay(this.display);
        if (this.hasLore()) {
            item.addLore(this.lore);
        } else {
            item.addLore("§a" + this.display + "です");
        }

        item.addSeparator("アイテム情報");
        item.addLore(decoLore("ID") + id);
        item.addLore(decoLore("カテゴリ") + "素材");
        item.addLore(decoLore("売値") + getSell());

        if (this instanceof MemorialMaterial memorialMaterial) {
            item.addSeparator("メモリアル");
            item.addLore(decoLore("ポイント") + memorialMaterial.getMemorial().getPoint() + "P");
        }

        item.setCustomData("Material", this.id);
        return item;
    }

    public static void drop(PlayerData playerData, MaterialData material, int amount, double percent) {
        boolean log = playerData.setting().is(BooleanEnum.MaterialLog);
        if (playerData.materialMenu().hasEmpty(material)) {
            playerData.addMaterial(material, amount);
            if (log) {
                String percentText = " §7(" + Function.scale(percent * 100.0, 2) + "%)";
                if (playerData.hasParty() && material.isRare()) {
                    for (PlayerData member : playerData.getParty().getMember()) {
                        if (member.setting().is(BooleanEnum.PartyRareDropSound)) {
                            member.sendMessage(playerData.getDisplayName() + "§aが§f" + material.getDisplay() + "x" + amount + "§aを§e獲得§aしました" + percentText);
                        }
                    }
                }
                if (playerData.setting().is(BooleanEnum.RareOnlyLog)) {
                    if (material.isRare()) {
                        if (playerData.setting().is(BooleanEnum.MemorialOnlyLog)) {
                            if (!material.id.contains("Memorial")) return;
                        }
                        String color = material instanceof MemorialMaterial ? "§b" : "§e";
                        playerData.sendMessage("§b[+]" + color + material.getDisplay() + " §a+" + amount + percentText);
                    }
                } else {
                    playerData.sendMessage("§b[+]§f" + material.getDisplay() + " §a+" + amount + percentText);
                }
            }
        } else if (log) {
            playerData.materialMenu().sendNonHasEmpty();
        }

    }
}
