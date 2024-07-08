package SwordofMagic11.Player.Menu;

import SwordofMagic11.Map.PvPRaid;
import SwordofMagic11.Pet.PetMenu;
import SwordofMagic11.Player.Achievement.AchievementMenu;
import SwordofMagic11.Player.Gathering.CraftMenu;
import SwordofMagic11.Player.Gathering.GatheringMenu;
import SwordofMagic11.Player.Market.*;
import SwordofMagic11.Player.Memorial.MemorialMenu;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Player.Setting.SettingMenu;
import SwordofMagic11.Player.Shop.EldShop;
import SwordofMagic11.Player.Shop.SellMenu;
import SwordofMagic11.Player.Smith.SmithMenu;
import SwordofMagic11.Player.Statistics.StatisticsMenu;
import SwordofMagic11.Player.Storage.CapsuleMenu;
import SwordofMagic11.Player.Storage.MaterialMenu;
import SwordofMagic11.Player.UserMenu.*;

public class MenuHolder {

    private final PlayerData playerData;

    private final UserMenu userMenu;
    private final SkillMenu skillMenu;
    private final AttributeMenu attributeMenu;
    private final MaterialMenu materialMenu;
    private final CapsuleMenu capsuleMenu;
    private final PalletMenu palletMenu;
    private final MemorialMenu memorialMenu;
    private final TriggerMenu triggerMenu;
    private final GatheringMenu gatheringMenu;
    private final StatisticsMenu statisticsMenu;
    private final SettingMenu settingMenu;
    private final StatusMenu statusMenu;
    private final TeleportMenu teleportMenu;
    private final AchievementMenu achievementMenu;
    private final RaidMenu raidMenu;
    private final PetMenu petMenu;
    private final PartyBoard partyBoard;

    private final MarketPlayer marketPlayer;
    private final MarketCancel marketCancel;
    private final MarketProduct marketProduct;
    private final MarketSellMaterial marketSellMaterial;
    private final MarketSellCapsule marketSellCapsule;
    private final MarketSellItem marketSellItem;
    private final MarketOrderMaterial marketOrderMaterial;
    private final MarketOrderCapsule marketOrderCapsule;
    private final MarketSystemMaterial marketSystemMaterial;
    private final MarketSystemCapsule marketSystemCapsule;

    private final TrashMenu trashMenu;
    private final SellMenu sellMenu;
    private final SmithMenu smithMenu;
    private final ClassesMenu classesMenu;
    private final InstanceMenu instanceMenu;
    private final CraftMenu craftMenu;
    private final BossModeMenu bossModeMenu;
    private final BossTimeAttackMenu bossTimeAttackMenu;

    private final SendMel sendMel;
    private final SendItem sendItem;
    private final LockItem lockItem;
    private final ViewItem viewItem;
    private final DespairKeyEnter despairKeyEnter;
    private final PvPRaid.Kit.GUI pvpKit;
    private final PlayerSelect playerSelect;

    private final EldShop eldShop;

    public MenuHolder(PlayerData playerData) {
        this.playerData = playerData;

        userMenu = new UserMenu(playerData);
        skillMenu = new SkillMenu(playerData);
        attributeMenu = new AttributeMenu(playerData);
        materialMenu = new MaterialMenu(playerData);
        capsuleMenu = new CapsuleMenu(playerData);
        palletMenu = new PalletMenu(playerData);
        memorialMenu = new MemorialMenu(playerData);
        triggerMenu = new TriggerMenu(playerData);
        gatheringMenu = new GatheringMenu(playerData);
        statisticsMenu = new StatisticsMenu(playerData);
        settingMenu = new SettingMenu(playerData);
        statusMenu = new StatusMenu(playerData);
        teleportMenu = new TeleportMenu(playerData);
        achievementMenu = new AchievementMenu(playerData);
        raidMenu = new RaidMenu(playerData);
        petMenu = new PetMenu(playerData);
        partyBoard = new PartyBoard(playerData);

        marketPlayer = new MarketPlayer(playerData);
        marketCancel = new MarketCancel(playerData);
        marketProduct = new MarketProduct(playerData);
        marketSellMaterial = new MarketSellMaterial(playerData);
        marketSellCapsule = new MarketSellCapsule(playerData);
        marketSellItem = new MarketSellItem(playerData);
        marketOrderMaterial = new MarketOrderMaterial(playerData);
        marketOrderCapsule = new MarketOrderCapsule(playerData);
        marketSystemMaterial = new MarketSystemMaterial(playerData);
        marketSystemCapsule = new MarketSystemCapsule(playerData);

        trashMenu = new TrashMenu(playerData);
        sellMenu = new SellMenu(playerData);
        smithMenu = new SmithMenu(playerData);
        classesMenu = new ClassesMenu(playerData);
        instanceMenu = new InstanceMenu(playerData);
        craftMenu = new CraftMenu(playerData);
        bossModeMenu = new BossModeMenu(playerData);
        bossTimeAttackMenu = new BossTimeAttackMenu(playerData);

        sendMel = new SendMel(playerData);
        sendItem = new SendItem(playerData);
        lockItem = new LockItem(playerData);
        viewItem = new ViewItem(playerData);

        despairKeyEnter = new DespairKeyEnter(playerData);
        pvpKit = new PvPRaid.Kit.GUI(playerData);
        playerSelect = new PlayerSelect(playerData);

        eldShop = new EldShop(playerData);
    }

    public void menuClick(String menu) {
        switch (menu) {
            case "UserMenu" -> userMenu.open();
            case "SkillMenu" -> skillMenu.open();
            case "AttributeMenu" -> attributeMenu.open();
            case "MaterialMenu" -> materialMenu.open();
            case "CapsuleMenu" -> capsuleMenu.open();
            case "PalletMenu" -> palletMenu.open();
            case "MemorialMenu" -> memorialMenu.open();
            case "TriggerMenu" -> triggerMenu.open();
            case "StatisticsMenu" -> statisticsMenu.open();
            case "GatheringMenu" -> gatheringMenu.open();
            case "SettingMenu" -> settingMenu.open();
            case "StatusMenu" -> statusMenu.open(playerData);
            case "TeleportMenu" -> teleportMenu.open();
            case "AchievementMenu" -> achievementMenu.open();
            case "RaidMenu" -> raidMenu.open();
            case "PetMenu" -> petMenu.open();
            case "PartyBoard" -> partyBoard.open();
            case "LockItem" -> lockItem.open();
            case "ViewItem" -> viewItem.open();
            case "MarketPlayer" -> marketPlayer.open();

            case "EldShop" -> eldShop.open();
        }
    }

    public interface Interface {
        MenuHolder menuHolder();

        default UserMenu userMenu() {
            return menuHolder().userMenu;
        }

        default SkillMenu skillMenu() {
            return menuHolder().skillMenu;
        }

        default AttributeMenu attributeMenu() {
            return menuHolder().attributeMenu;
        }

        default MaterialMenu materialMenu() {
            return menuHolder().materialMenu;
        }

        default CapsuleMenu capsuleMenu() {
            return menuHolder().capsuleMenu;
        }

        default PalletMenu palletMenu() {
            return menuHolder().palletMenu;
        }

        default MemorialMenu memorialMenu() {
            return menuHolder().memorialMenu;
        }

        default TriggerMenu triggerMenu() {
            return menuHolder().triggerMenu;
        }

        default GatheringMenu gatheringMenu() {
            return menuHolder().gatheringMenu;
        }

        default StatisticsMenu statisticsMenu() {
            return menuHolder().statisticsMenu;
        }

        default SettingMenu settingMenu() {
            return menuHolder().settingMenu;
        }

        default StatusMenu statusMenu() {
            return menuHolder().statusMenu;
        }

        default TeleportMenu teleportMenu() {
            return menuHolder().teleportMenu;
        }

        default AchievementMenu achievementMenu() {
            return menuHolder().achievementMenu;
        }

        default RaidMenu raidMenu() {
            return menuHolder().raidMenu;
        }

        default PetMenu petMenu() {
            return menuHolder().petMenu;
        }

        default PartyBoard partyBoard() {
            return menuHolder().partyBoard;
        }

        default MarketPlayer marketPlayer() {
            return menuHolder().marketPlayer;
        }

        default MarketCancel marketCancel() {
            return menuHolder().marketCancel;
        }

        default MarketProduct marketSell() {
            return menuHolder().marketProduct;
        }

        default MarketSellMaterial marketSellMaterial() {
            return menuHolder().marketSellMaterial;
        }

        default MarketSellCapsule marketSellCapsule() {
            return menuHolder().marketSellCapsule;
        }

        default MarketSellItem marketSellItem() {
            return menuHolder().marketSellItem;
        }

        default MarketOrderMaterial marketOrderMaterial() {
            return menuHolder().marketOrderMaterial;
        }

        default MarketOrderCapsule marketOrderCapsule() {
            return menuHolder().marketOrderCapsule;
        }

        default MarketSystemMaterial marketSystemMaterial() {
            return menuHolder().marketSystemMaterial;
        }

        default MarketSystemCapsule marketSystemCapsule() {
            return menuHolder().marketSystemCapsule;
        }

        default TrashMenu trashMenu() {
            return menuHolder().trashMenu;
        }

        default SellMenu sellMenu() {
            return menuHolder().sellMenu;
        }

        default SmithMenu smithMenu() {
            return menuHolder().smithMenu;
        }

        default ClassesMenu classesMenu() {
            return menuHolder().classesMenu;
        }

        default InstanceMenu instanceMenu() {
            return menuHolder().instanceMenu;
        }

        default BossModeMenu bossModeMenu() {
            return menuHolder().bossModeMenu;
        }

        default BossTimeAttackMenu bossTimeAttackMenu() {
            return menuHolder().bossTimeAttackMenu;
        }

        default CraftMenu craftMenu() {
            return menuHolder().craftMenu;
        }

        default SendMel sendMel() {
            return menuHolder().sendMel;
        }

        default SendItem sendItem() {
            return menuHolder().sendItem;
        }

        default LockItem lockItem() {
            return menuHolder().lockItem;
        }

        default ViewItem viewItem() {
            return menuHolder().viewItem;
        }

        default DespairKeyEnter despairKeyEnter() {
            return menuHolder().despairKeyEnter;
        }

        default PvPRaid.Kit.GUI pvpKit() {
            return menuHolder().pvpKit;
        }

        default PlayerSelect playerSelect() {
            return menuHolder().playerSelect;
        }

        default EldShop eldShop() {
            return menuHolder().eldShop;
        }
    }
}
