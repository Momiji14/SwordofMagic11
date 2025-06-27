package SwordofMagic11.DataBase;

import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.Player.ClassType;
import SwordofMagic11.Player.Shop.EldShop;
import org.bukkit.Material;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static SwordofMagic11.SomCore.Log;

public interface DataBase {
    double ResetCost = 0.001;
    int ContainerViewSize = 20;
    double PercentValue = 0.6321;
    File Path = new File("../DataBase");
    DateTimeFormatter DateFormat = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    static CustomItemStack UpScroll() {
        return new CustomItemStack(Material.ITEM_FRAME).setNonDecoDisplay("§e上にスクロール").setCustomData("UpScroll", true);
    }

    static CustomItemStack DownScroll() {
        return new CustomItemStack(Material.ITEM_FRAME).setNonDecoDisplay("§e下にスクロール").setCustomData("DownScroll", true);
    }

    static String fileId(File file) {
        return file.getName().replace(".yml", "");
    }

    static void error(File file, Exception e) {
        Log("§e" + file.getName() + "§cのロード中にエラーが発生しました");
        e.printStackTrace();
    }

    static List<File> dumpFile(File file) {
        List<File> list = new ArrayList<>();
        File[] files = file.listFiles();
        for (File tmpFile : files) {
            if (!tmpFile.getName().equals(".sync")) {
                if (tmpFile.isDirectory()) {
                    list.addAll(dumpFile(tmpFile));
                } else if (tmpFile.getName().contains(".yml")) {
                    list.add(tmpFile);
                }
            }
        }
        return list;
    }

    static void load() {
        MemorialDataLoader.load();
        MaterialDataLoader.load();
        CapsuleDataLoader.load();
        ItemDataLoader.load();
        SkillDataLoader.load();
        ShopDataLoader.load();
        ClassType.load();
        AchievementDataLoader.load();
        MobDataLoader.load();
        MapDataLoader.load();
        CraftDataLoader.load();
        //SyncItem.load();
        EldShop.donationItemRegister();
    }

    enum Table {
        PlayerData,
        Classes,
        ItemStorage,
        ItemMetaData,
        MaterialStorage,
        CapsuleStorage,
        PetCage,
        PetLevel,
        PetAttribute,
        PetAttributeMultiply,
        PetEquipment,
        PetMenuMaterialStorage,
        PetMenuCapsuleStorage,
        PlayerSetting,
        Pallet,
        PalletStorage,
        PlayerAchievement,
        PlayerEquipment,
        PlayerGathering,
        PlayerGatheringSkill,
        PlayerSkill,
        PlayerCity,
        PlayerCraft,
        PlayerMemorial,
        PlayerDonation,
        PlayerItemLock,
        PlayerVote,
        PlayerBooster,
        SideBarToDo,
        EquipmentPlus,
        EquipmentExp,
        EquipmentTrance,
        EquipmentLevelDown,
        EquipmentCapsuleSlot,
        EquipmentCapsule,
        StatisticsInt,
        StatisticsDouble,
        StatisticsEnemyKill,
        MarketAverage,
        MarketPlayer,
        MarketPlayerMaterial,
        MarketPlayerCapsule,
        MarketSellItem,
        MarketSellMaterial,
        MarketOrderMaterial,
        MarketSellCapsule,
        MarketOrderCapsule,
        MarketSystemMaterial,
        MarketSystemCapsule,
        BossTimeAttack,

        GlobalPvPDrop,
        SystemBank,
    }
}
