package SwordofMagic11.DataBase;

import SwordofMagic11.Item.Material.*;
import SwordofMagic11.Item.SomEquip;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;
import java.util.*;

import static SwordofMagic11.Component.Function.loreText;
import static SwordofMagic11.Item.Material.PotionMaterial.*;
import static SwordofMagic11.SomCore.Log;

public class MaterialDataLoader {
    private static final HashMap<String, MaterialData> materialDataList = new HashMap<>();
    private static final Set<String> nonUseSlotList = new HashSet<>();

    @NonNull
    public static MaterialData getMaterialData(String id) {
        if (!materialDataList.containsKey(id)) {
            Log("§c存在しないMaterialDataが参照されました -> " + id);
            throw new RuntimeException("§c存在しないMaterialDataが参照されました -> " + id);
        }
        return materialDataList.get(id);
    }

    public static Collection<MaterialData> getMaterialDataList() {
        return materialDataList.values();
    }

    public static List<String> getComplete() {
        List<String> complete = new ArrayList<>();
        for (MaterialData materialData : getMaterialDataList()) {
            complete.add(materialData.getId());
        }
        return complete;
    }

    public static Set<String> getNonUseSlotList() {
        return nonUseSlotList;
    }

    public static void load() {
        for (File file : DataBase.dumpFile(new File(DataBase.Path, "MaterialData"))) {
            try {
                FileConfiguration data = YamlConfiguration.loadConfiguration(file);
                String id = DataBase.fileId(file);
                MaterialData materialData = new MaterialData();
                materialData.setId(id);
                materialData.setDisplay(data.getString("Display"));
                materialData.setIcon(Material.valueOf(data.getString("Icon")));
                if (data.isSet("Lore")) materialData.setLore(loreText(data.getStringList("Lore")));
                if (data.isSet("Sell")) materialData.setSell(data.getInt("Sell"));
                materialData.setRare(data.getBoolean("Rare"));
                if (data.isSet("Color")) {
                    materialData.setColor(Color.fromRGB(data.getInt("Color.r"), data.getInt("Color.g"), data.getInt("Color.b")));
                }
                register(materialData);
            } catch (Exception e) {
                DataBase.error(file, e);
            }
        }

        for (int i = 1; i <= MaxPotionLevel; i++) {
            for (Flower flower : Flower.values()) {
                MaterialData materialData = new MaterialData();
                materialData.setId(flower.id + i);
                materialData.setIcon(flower.icon);
                materialData.setDisplay(flower.id + i);
                materialData.setSell(1);
                register(materialData);
            }

            for (Fish fish : Fish.values()) {
                MaterialData materialData = new MaterialData();
                materialData.setId(fish.id + Suffix[i-1]);
                materialData.setIcon(fish.icon);
                materialData.setDisplay(fish.id + Suffix[i-1]);
                materialData.setSell(1);
                register(materialData);
            }

            for (Potion potion : Potion.values()) {
                PotionMaterial potionData = new PotionMaterial();
                potionData.setId(potion.id + Suffix[i-1]);
                potionData.setIcon(Material.POTION);
                potionData.setColor(potion.color);
                String lore = null;
                switch (potion) {
                    case Heal, Mana -> lore = potion.lore.replace("%Value%", String.valueOf(HealValue[i-1]));
                    case Attack, Defence -> lore = potion.lore.replace("%Value%", String.valueOf(BuffValue[i-1])).replace("%Time%", String.valueOf(BuffTime/20));
                }
                potionData.setLore(loreText(lore));
                potionData.setDisplay(potion.id + Suffix[i-1]);
                potionData.setSell(10*i);
                potionData.setLevel(i);
                register(potionData);
            }
        }

        register(ClassExpTicketMaterial.Ticket_10000);
        register(new VoteBoosterMaterial());
        register(WPHKey.WPHKey);
        register(DespairKey.DespairKey);
        register(PetCookie.PetCookie);

        Log("§a[MaterialDataLoader]§b" + materialDataList.size() + "個をロードしました");
    }

    public static void registerMaterialize(Type type, String name) {
        MaterialData materialData = new MaterialData();
        materialData.setId("素材化" + type.display + name);
        materialData.setDisplay("素材化" + type.display + "[" + name + "]");
        materialData.setIcon(Material.JUKEBOX);
        materialData.setSell(100);
        register(materialData);
    }

    public static void register(MaterialData materialData) {
        materialDataList.put(materialData.getId(), materialData);
    }

    public static void registerNonUseSlot(List<String> ids) {
        nonUseSlotList.addAll(ids);
    }

    public static void registerNonUseSlot(String id) {
        nonUseSlotList.add(id);
    }

    public enum Type {
        Weapon("武器"),
        Armor("防具"),

        ;
        private final String display;

        Type(String display) {
            this.display = display;
        }

        public String getDisplay() {
            return display;
        }

        public static Type getType(SomEquip equip) {
            if (equip.isWeapon()) {
                return Weapon;
            } else if (equip.isArmor()) {
                return Armor;
            }
            throw new RuntimeException();
        }
    }

    public enum Flower {
        Heal("薬草Lv", Material.POPPY),
        Mana("魔草Lv", Material.CORNFLOWER),
        ;

        private final String id;
        private final Material icon;

        Flower(String id, Material icon) {
            this.id = id;
            this.icon = icon;
        }

        public String id() {
            return id;
        }

        public Material icon() {
            return icon;
        }
    }

    public enum Fish {
        Attack("アタークフィッシュ", Material.SALMON),
        Defence("マモールフィッシュ", Material.COD),
        ;

        private final String id;
        private final Material icon;

        Fish(String id, Material icon) {
            this.id = id;
            this.icon = icon;
        }

        public String id() {
            return id;
        }

        public Material icon() {
            return icon;
        }
    }

    public enum Potion {
        Heal("回復ポーション", Color.fromRGB(255, 128, 128), "体力を§e%Value%回復§aします", "薬草Lv"),
        Mana("マナポーション", Color.AQUA, "マナを§e%Value%回復§aします", "魔草Lv"),
        Attack("アタークポーション", Color.fromRGB(255, 0, 0), "§e攻撃系ステータス§aが§e+%Value%§a上昇します\nこの効果は§e%Time%秒間§a持続します", "アタークフィッシュ"),
        Defence("マモールポーション", Color.BLUE, "§e防御系ステータス§aが§e+%Value%§a上昇します\nこの効果は§e%Time%秒間§a持続します", "マモールフィッシュ"),
        ;

        private final String id;
        private final Color color;
        private final String lore;
        private final String craft;

        Potion(String id, Color color, String lore, String craft) {
            this.id = id;
            this.color = color;
            this.lore = lore;
            this.craft = craft;
        }

        public String id() {
            return id;
        }

        public Color color() {
            return color;
        }

        public String lore() {
            return lore;
        }

        public String craft() {
            return craft;
        }
    }
}
