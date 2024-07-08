package SwordofMagic11.Player.Setting;

import SwordofMagic11.Component.SomSQL;
import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.DataBase.DataBase;
import SwordofMagic11.Player.HumanData;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.List;

import static SwordofMagic11.Component.Function.*;

public class PlayerSetting {

    private final String[] key = new String[]{"UUID", "Setting"};

    private String[] value(String setting) {
        return new String[]{humanData.getUUID(), setting};
    }

    private final HumanData humanData;
    private final HashMap<BooleanEnum, Boolean> booleanData = new HashMap<>();
    private final HashMap<DoubleEnum, Double> doubleData = new HashMap<>();

    public PlayerSetting(HumanData humanData) {
        this.humanData = humanData;
        for (BooleanEnum bool : BooleanEnum.values()) {
            if (SomSQL.exists(DataBase.Table.PlayerSetting, key, value(bool.toString()), "Value")) {
                boolean value = Boolean.parseBoolean(SomSQL.getString(DataBase.Table.PlayerSetting, key, value(bool.toString()), "Value"));
                booleanData.put(bool, value);
                if (bool.defaultBool == value) SomSQL.delete(DataBase.Table.PlayerSetting, key, value(bool.toString()));
            }
        }
        for (DoubleEnum doubleEnum : DoubleEnum.values()) {
            if (SomSQL.exists(DataBase.Table.PlayerSetting, key, value(doubleEnum.toString()), "Value")) {
                double value = Double.parseDouble(SomSQL.getString(DataBase.Table.PlayerSetting, key, value(doubleEnum.toString()), "Value"));
                doubleData.put(doubleEnum, value);
                if (defaultValue(doubleEnum) == value) SomSQL.delete(DataBase.Table.PlayerSetting, key, value(doubleEnum.toString()));
            }
        }
    }

    public void resetPvPMode() {
        if (is(BooleanEnum.PvPMode)) {
            setBool(BooleanEnum.PvPMode, false);
        }
    }

    public void setBool(BooleanEnum bool, boolean value) {
        booleanData.put(bool, value);
        humanData.sendMessage("§e" + bool.getDisplay() + "§aを" + boolText(value) + "§aにしました");
        if (bool.defaultBool == value) {
            SomSQL.delete(DataBase.Table.PlayerSetting, key, value(bool.toString()));
        } else {
            SomSQL.setSql(DataBase.Table.PlayerSetting, key, value(bool.toString()), "Value", String.valueOf(value));
        }
    }

    public void setDouble(DoubleEnum doubleEnum, double value) {
        doubleData.put(doubleEnum, value);
        humanData.sendMessage("§e" + doubleEnum.getDisplay() + "§aを§e" + text(doubleEnum) + "§aにしました");
        if (defaultValue(doubleEnum) == value) {
            SomSQL.delete(DataBase.Table.PlayerSetting, key, value(doubleEnum.toString()));
        } else {
            SomSQL.setSql(DataBase.Table.PlayerSetting, key, value(doubleEnum.toString()), "Value", String.valueOf(value));
        }
    }

    public void toggle(BooleanEnum bool) {
        setBool(bool, !is(bool));
        if (bool == BooleanEnum.NightVision) updateNightVision();
    }

    public void updateNightVision() {
        if (is(BooleanEnum.NightVision)) {
            humanData.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, PotionEffect.INFINITE_DURATION, 0, false, false));
        } else {
            humanData.removePotionEffect(PotionEffectType.NIGHT_VISION);
        }
    }

    public void valueNext(DoubleEnum doubleEnum) {
        if (get(doubleEnum) == doubleEnum.max) {
            setDouble(doubleEnum, doubleEnum.min);
        } else if (get(doubleEnum) + doubleEnum.next > doubleEnum.max) {
            setDouble(doubleEnum, doubleEnum.max);
        } else {
            setDouble(doubleEnum, scaleDouble(get(doubleEnum) + doubleEnum.next, 3));
        }
    }

    public boolean is(BooleanEnum bool) {
        return booleanData.getOrDefault(bool, bool.defaultBool);
    }

    public double get(DoubleEnum doubleEnum) {
        return doubleData.getOrDefault(doubleEnum, defaultValue(doubleEnum));
    }

    public String text(DoubleEnum doubleEnum) {
        double value = get(doubleEnum);
        return doubleEnum.isPercent ? scale(value * 100) + "%" : scale(value);
    }

    public double defaultValue(DoubleEnum doubleEnum) {
        return humanData.isJE() ? doubleEnum.defaultValueJE : doubleEnum.defaultValueBE;
    }

    public enum DoubleEnum {
        PlayerParticle("自分のパーティクル密度", "自身のパーティクル密度", Material.REDSTONE, 0, 1, 0.1, true, 0.5, 0.3),
        PlayerEnemyParticle("敵プレイヤーのパーティクル密度", "敵プレイヤーのパーティクル密度", Material.REDSTONE, 0, 1, 0.1, true, 0.3, 0.1),
        OtherParticle("他プレイヤーのパーティクル密度", "他プレイヤーのパーティクル密度", Material.REDSTONE, 0, 1, 0.1, true, 0.3, 0.1),
        EnemyParticle("エネミーのパーティクル密度", "エネミーのパーティクル密度", Material.REDSTONE, 0, 1, 0.1, true, 0.5, 0.3),
        ParticleViewDistance("パーティクル表示距離", "パーティクルの表示距離", Material.REDSTONE, 8, 128, 8, false, 48, 16),
        ;

        private final String display;
        private final Material icon;
        private final List<String> lore;
        private final double min;
        private final double max;
        private final double next;
        private final double defaultValueJE;
        private final double defaultValueBE;
        private final boolean isPercent;

        DoubleEnum(String display, String lore, Material icon, double min, double max, double next, boolean isPercent, double defaultValueJE, double defaultValueBE) {
            this.display = display;
            this.icon = icon;
            this.lore = loreText(lore);
            this.min = min;
            this.max = max;
            this.next = next;
            this.isPercent = isPercent;
            this.defaultValueJE = defaultValueJE;
            this.defaultValueBE = defaultValueBE;
        }

        public double min() {
            return min;
        }

        public double max() {
            return max;
        }

        public String getDisplay() {
            return display;
        }

        public List<String> getLore() {
            return lore;
        }

        public CustomItemStack viewItem() {
            CustomItemStack item = new CustomItemStack(icon);
            item.setDisplay("§e" + display);
            item.addLore(lore);
            item.setCustomData("ValueSetting", this.toString());
            return item;
        }
    }

    public enum BooleanEnum {
        MakeDamageLog("与ダメージログ", "与ダメージのログ", Material.RED_DYE, false),
        TakeDamageLog("被ダメージログ", "被ダメージのログ", Material.RED_DYE, false),
        HealLog("ヒールログ", "ヒールのログ", Material.APPLE, false),
        BuffLog("バフログ", "バフ効果の付与・削除のログ", Material.DRAGON_BREATH, true),
        ExpLog("経験値ログ", "経験値の獲得ログ", Material.EXPERIENCE_BOTTLE, false),
        GatheringLog("ギャザリングログ", "ギャザリングのログ", Material.IRON_PICKAXE, true),
        ItemLog("アイテムログ", "アイテムの獲得ログ", Material.CHEST_MINECART, true),
        CapsuleLog("カプセルログ", "カプセルの獲得ログ", Material.ENDER_CHEST, true),
        MaterialLog("素材ログ", "素材の獲得ログ", Material.CHEST, true),
        RareOnlyLog("レアドロップログのみ", "獲得系ログの表示をレアドロップのみにします\n§c※素材ログまたはカプセルログが必要です", Material.GOLD_INGOT, false),
        MemorialOnlyLog("メモリアルのみ", "素材ログの表示をメモリアルのみにします\n§c※レアドロップログのみが必要です", Material.WOLF_SPAWN_EGG, false),
        SoulCapsuleLog("ソールカプセルログ", "ソールカプセルを別途通知するか切り替えます", Material.HEART_OF_THE_SEA, false),
        MelLog("メルログ", "メルの獲得ログ", Material.GOLD_NUGGET, true),
        SkillMessage("スキルメッセージ", "スキルメッセージの表示を切り替えます", Material.END_CRYSTAL, true),
        QuickCast("クイックキャスト", "素早くキャストできる方式に切り替えます", Material.CLOCK, false),
        LevelLog("レベルアップログ", "他人のレベルアップ通知の表示を切り替えます", Material.LIME_DYE, false),
        TameLog("テイムログ", "他人のテイム通知の表示を切り替えます", Material.BEE_SPAWN_EGG, false),
        PvPMode("PvPモード", "プレイヤー同士でダメージが通るようになります", Material.IRON_SWORD, false),
        NightVision("暗視", "暗視を切り替えます", Material.FERMENTED_SPIDER_EYE, true),
        StorageViewShort("倉庫表示短縮", "倉庫の内容物の表示を短くします", Material.BIRCH_CHEST_BOAT, true),
        EquipView("装備外観表示", "装備の外観の表示を切り替えます\nPvPインスタンスでは§b強制有効§aです", Material.IRON_CHESTPLATE, true),
        WallKick("壁キック", "壁キックの有無を切り替えます", Material.LEATHER_BOOTS, true),
        RightStrafe("右クリストレイフ", "右クリックでストレイフするかを切り替えます", Material.FEATHER, true),
        BackStrafe("バックストレイフ", "スニークしながらストレイフすると\n後ろにストレフするかを切り替えます", Material.BLACK_DYE, true),
        SkyMenu("スカイメニュー", "上を向いてスニークでメニューを開きます", Material.BOOK, true),
        PartyRareDropSound("パーティーレアドロップ通知", "パーティーメンバーがレアドロップを獲得した際\n自身にも獲得通知を表示するかを切り替えます", Material.GOLDEN_HORSE_ARMOR, true),
        SBTDTriggerSound("SBTD通知", "SBTDの個数表示達成時の通知を切り替えます", Material.BELL, true),
        BossModeForceDisable("ボスモード強制無効", "ボスモードを強制的に一般にします", Material.WITHER_SKELETON_SKULL, false),
        PartyPublicNotice("パーティー募集通知", "パーティー募集通知を表示するかを切り替えます", Material.TOTEM_OF_UNDYING, true),
        ;

        private final String display;
        private final Material icon;
        private final List<String> lore;
        private final boolean defaultBool;

        BooleanEnum(String display, String lore, Material icon, boolean defaultBool) {
            this.display = display;
            this.icon = icon;
            this.lore = loreText(lore);
            this.defaultBool = defaultBool;
        }

        public String getDisplay() {
            return display;
        }

        public List<String> getLore() {
            return lore;
        }

        public CustomItemStack viewItem() {
            CustomItemStack item = new CustomItemStack(icon);
            item.setDisplay("§e" + display);
            item.addLore(lore);
            item.setCustomData("BooleanSetting", this.toString());
            return item;
        }
    }
}
