package SwordofMagic11.Map;

import SwordofMagic11.Component.Function;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Component.SomTask;
import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.Custom.CustomLocation;
import SwordofMagic11.Custom.UnsetLocation;
import SwordofMagic11.DataBase.MapDataLoader;
import SwordofMagic11.DataBase.MaterialDataLoader;
import SwordofMagic11.Entity.SearchEntity;
import SwordofMagic11.Entity.SomEffect;
import SwordofMagic11.Entity.SomEntity;
import SwordofMagic11.Item.Material.MaterialData;
import SwordofMagic11.Player.ClassType;
import SwordofMagic11.Player.HumanData;
import SwordofMagic11.Player.Menu.GUIManager;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.SomCore;
import SwordofMagic11.StatusType;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

import static SwordofMagic11.Component.Function.*;
import static SwordofMagic11.SomCore.Log;

public class PvPRaid {

    public static final String Display = "§4䨩理聖戦";
    public static final int PointPerMakeDamage = 2500;
    public static final int PointPerTakeDamage = 2500;
    public static final int PointPerHeal = 2500;
    public static final double BuffMaxValue = 35;

    private static int EndPoint = 10000;
    private static MapData mapData;
    private static int playerCountDiff = 0;

    private static double totalPoint = 0;

    public static void load() {
        mapData = MapDataLoader.getMapData("PvPRaid");
        SomTask.asyncTimer(() -> {
            if (isStart) {
                EndPoint = getInPvPRaidPlayers().size() * 1000;
                totalPoint = totalPoint();
                Team.Red.point = teamPoint(Team.Red);
                Team.Blue.point  = teamPoint(Team.Blue);
                if (totalPoint > 0) {
                    Team.Red.bossBar.setProgress(MinMax(Team.Red.point / totalPoint, 0, 1));
                    Team.Blue.bossBar.setProgress(MinMax(Team.Blue.point / totalPoint, 0, 1));
                }

                for (Beacon beacon : Beacon.values()) {
                    beacon.updatePlayers();
                    beacon.triggerPoint();
                }

                Team.Red.bossBar.setTitle(Team.Red.color + "自チーム: " + scale(Team.Red.point) + "/" + EndPoint + " " + Team.Blue.color + " 敵チーム: " + scale(Team.Blue.point) + "/" + EndPoint);
                Team.Blue.bossBar.setTitle(Team.Blue.color + "自チーム: " + scale(Team.Blue.point) + "/" + EndPoint + " " + Team.Red.color + " 敵チーム: " + scale(Team.Red.point) + "/" + EndPoint);

                for (Team team : Team.values()) {
                    for (PlayerData playerData : team.players()) {
                        if (playerData.getLocation().getY() > 129.9) {
                            Team killerTeam = team == Team.Red ? Team.Blue : Team.Red;
                            PlayerData killer = Function.randomGet(killerTeam.players());
                            kill(killer, playerData);
                            playerData.teleport(getTeam(playerData).getSpawnLocation());
                            sendMessage("§4場外違反§aにより" + playerData.getDisplayName() + "§aが" + killer.getDisplayName() + "§aに§4キル§aされた事になります", SomSound.Tick);
                        }
                        if (playerData.getLocation().distance(team.getSpawnLocation()) < 15) {
                            playerData.addHealth(playerData.getStatus(StatusType.MaxHealth) * 0.05);
                            playerData.addMana(playerData.getStatus(StatusType.MaxMana) * 0.05);
                        }
                        if (playerData.getAFKTime() > 60) {
                            resetPlayer(playerData);
                            sendMessage("§4AFK違反§aにより" + playerData.getDisplayName() + "§aが§4強制退場§aさせられました", SomSound.Tick);
                        }
                    }
                    if (teamPoint(team) >= EndPoint) {
                        sendMessage(team.colorDisplay() + "§aが§b勝利§aしました", SomSound.Winner);
                        isStart = false;
                        isOpen = false;
                        killRanking();
                        pointRanking();
                        for (PlayerData playerData : getInPvPRaidPlayers()) {
                            List<String> message = new ArrayList<>();
                            int kill = kills.getOrDefault(playerData, 0);
                            double point = getPoint(playerData);
                            int mel = (int) (point * 10 * (1 + kill * 0.01));
                            double exp = (point * 1000) * (1 + kill * 0.01);
                            int grinder = (int) ((point/3) * (1 + kill * 0.01));
                            message.add(decoText(Display));
                            message.add(decoLore("キル数") + kill);
                            message.add(decoLore("ポイント") + scale(point));
                            message.add(decoSeparator("Reward"));
                            message.add("§7・§a" + mel + "メル");
                            message.add("§7・§a" + scale(exp) + "Exp");
                            message.add("§7・§rグラインダーx" + grinder);
                            playerData.addMel(mel);
                            playerData.classes().addExp(exp);
                            playerData.addMaterial("グラインダー", grinder);
                            double percent = 0.00001 * point;
                            if (randomDouble() < percent) {
                                MaterialData material = MaterialDataLoader.getMaterialData("䨩理聖戦Memorial");
                                message.add("§7・§b" + material.getDisplay() + " §7(" + scale(percent*100, 3) + ")");
                                playerData.addMaterial(material, 1);
                                broadcast(playerData.getDisplayName() + "§aさんが§b" + material.getDisplay() + "§aを§e獲得§aしました");
                            }
                            playerData.sendMessage(message);
                        }
                        SomTask.asyncDelay(PvPRaid::reset, 100);
                        break;
                    }
                }
            }
        }, 20, SomCore.TaskOwner);

        SomTask.asyncTimer(() -> {
            if (isStart) {
                Team winTeam = Team.Red.point > Team.Blue.point ? Team.Red : Team.Blue;
                Team loseTeam = Team.Red.point > Team.Blue.point ? Team.Blue : Team.Red;
                double diff = loseTeam.point / winTeam.point;
                if (diff < 0.7) {
                    SomEffect effect = new SomEffect("PvPRaid", "背水之陣", 20*60, SomCore.Cardinal);
                    double multiply = BuffMaxValue - (diff * BuffMaxValue);
                    effect.setStatus(StatusType.Penetration, multiply);
                    for (PlayerData playerData : loseTeam.players()) {
                        playerData.addEffect(effect);
                    }
                    sendMessage(loseTeam.colorDisplay() + "§aに§d§n" + effect.getDisplay() + "§aが付与されました！ §e[" + StatusType.Penetration.getDisplay() + "+" + scale(multiply, 1) + "]", SomSound.BossSpawn);
                }
            }
        }, 20*60, SomCore.TaskOwner);

        SomTask.asyncTimer(() -> {
            if (isStart) {
                Team moreTeam = Team.Red.size > Team.Blue.size ? Team.Red : Team.Blue;
                Team lessTeam = Team.Red.size > Team.Blue.size ? Team.Blue : Team.Red;
                int diff = moreTeam.size - lessTeam.size;
                if (playerCountDiff != diff && diff > 1) {
                    playerCountDiff = diff;
                    SomEffect effect = new SomEffect("PvPRaidBalance", "兵数均衡", 20*10, SomCore.Cardinal);
                    double multiply = (double) moreTeam.size / lessTeam.size;
                    effect.setStatusMultiplyAttack(multiply);
                    effect.setStatusMultiplyDefense(multiply);
                    effect.setStatus(StatusType.SPT, multiply);
                    for (PlayerData playerData : lessTeam.players()) {
                        playerData.addEffect(effect);
                    }
                    sendMessage(lessTeam.colorDisplay() + "§aに§d§n" + effect.getDisplay() + "§aが付与されました！ §e[攻守ステータス+" + scale(multiply, 1) + "]", SomSound.Tick);
                }
            }
        }, 20*10, SomCore.TaskOwner);
    }

    public static void killRanking() {
        List<Map.Entry<HumanData, Integer>> ranking = new ArrayList<>(kills.entrySet());
        ranking.sort(Map.Entry.comparingByValue());
        Collections.reverse(ranking);
        List<String> rankingMessage = new ArrayList<>();
        rankingMessage.add(decoText("キルランキング"));
        int index = 1;
        for (Map.Entry<HumanData, Integer> entry : ranking) {
            HumanData humanData = entry.getKey();
            rankingMessage.add("§e[" + index + "位]§r" + getTeam(humanData).color + humanData.getName() + "§7: §a" + scale(entry.getValue()));
            index++;
            if (index > 5) break;
        }
        sendMessage(rankingMessage, SomSound.Tick);
    }

    public static void pointRanking() {
        List<Map.Entry<HumanData, Double>> ranking = new ArrayList<>(points.entrySet());
        ranking.sort(Map.Entry.comparingByValue());
        Collections.reverse(ranking);
        List<String> rankingMessage = new ArrayList<>();
        rankingMessage.add(decoText("ポイントランキング"));
        int index = 1;
        for (Map.Entry<HumanData, Double> entry : ranking) {
            HumanData humanData = entry.getKey();
            rankingMessage.add("§e[" + index + "位]§r" + getTeam(humanData).color + humanData.getName() + "§7: §a" + scale(entry.getValue()));
            index++;
            if (index > 5) break;
        }
        sendMessage(rankingMessage, SomSound.Tick);
    }

    public enum Beacon {
        Red(new UnsetLocation(41.5, 78, 83.5), 15, 1, 5),
        Blue(new UnsetLocation(-40.5, 78, -82.5), 15, 1, 5),
        RedSide(new UnsetLocation(-51.5, 84, 0.5), 7.5, 2, 3),
        BlueSide(new UnsetLocation(51.5, 84, 0.5), 7.5, 2, 3),
        Global(new UnsetLocation(0.5, 72, 0.5), 15, 3, 9),
        ;

        private final UnsetLocation location;
        private final int point;
        private final int limit;
        private final double radius;
        private List<PlayerData> players;

        Beacon(UnsetLocation location, double radius, int point, int limit) {
            this.location = location;
            this.radius = radius;
            this.point = point;
            this.limit = limit;
        }

        public int point() {
            if (players.size() <= limit) {
                return point;
            } else {
                return point * limit / players.size();
            }
        }

        public void triggerPoint() {
            HashMap<Team, Double> addition = new HashMap<>();
            switch (this) {
                case RedSide -> {
                    if (!players.isEmpty()) {
                        Team.Red.sideArea = true;
                        for (PlayerData playerData : players) {
                            if (getTeam(playerData) == Team.Blue) {
                                Team.Red.sideArea = false;
                                break;
                            }
                        }
                    } else Team.Red.sideArea = false;
                }
                case BlueSide -> {
                    if (!players.isEmpty()) {
                        Team.Blue.sideArea = true;
                        for (PlayerData playerData : players) {
                            if (getTeam(playerData) == Team.Red) {
                                Team.Blue.sideArea = false;
                                break;
                            }
                        }
                    } else Team.Blue.sideArea = false;

                }
                case Global -> {
                    if (Team.Red.sideArea) {
                        addition.merge(Team.Red, 1.0, Double::sum);
                        addition.merge(Team.Blue, -1.0, Double::sum);
                    }
                    if (Team.Blue.sideArea) {
                        addition.merge(Team.Blue, 1.0, Double::sum);
                        addition.merge(Team.Red, -1.0, Double::sum);
                    }
                }
            }
            int point = point();
            for (PlayerData playerData : players) {
                addPoint(playerData, point + addition.getOrDefault(getTeam(playerData), 0.0));
            }
        }

        public void updatePlayers() {
            players = SearchEntity.nearPlayer(getInPvPRaidPlayers(), location.as(mapData.getGlobalInstance(false)), radius);
            players.removeIf(SomEntity::isDeath);
        }
    }

    public enum Team {
        Red("クラフタ", "§c", Color.RED, "PvPRaid", new UnsetLocation(-103.5, 70, 101.5, -135, 0), Bukkit.createBossBar(Display, BarColor.RED, BarStyle.SOLID)),
        Blue("ミリア", "§9", Color.BLUE, "PvPRaid", new UnsetLocation(104.5, 70, -100.5, 45, 0),  Bukkit.createBossBar(Display, BarColor.BLUE, BarStyle.SOLID)),
        ;

        private final String display;
        private final String color;
        private final MapData mapData;
        private final UnsetLocation location;
        private final BossBar bossBar;
        private int size;
        private double point;
        private boolean sideArea = false;

        public final CustomItemStack Chest;
        public final CustomItemStack Legs;
        public final CustomItemStack Foot;

        Team(String display, String colorText, Color color, String mapId, UnsetLocation location, BossBar bossBar) {
            this.display = colorText + display;
            this.color = colorText;
            mapData = MapDataLoader.getMapData(mapId);
            this.location = location;
            this.bossBar = bossBar;


            Chest = new CustomItemStack(Material.LEATHER_CHESTPLATE).setNonDecoDisplay(" ");
            Legs = new CustomItemStack(Material.LEATHER_LEGGINGS).setNonDecoDisplay(" ");
            Foot = new CustomItemStack(Material.LEATHER_BOOTS).setNonDecoDisplay(" ");
            CustomItemStack.setupColor(Chest, color);
            CustomItemStack.setupColor(Legs, color);
            CustomItemStack.setupColor(Foot, color);
        }

        public String colorDisplay() {
            return color + display;
        }

        public MapData getMapData() {
            return mapData;
        }

        public UnsetLocation getLocation() {
            return location;
        }

        public CustomLocation getSpawnLocation() {
            return location.as(mapData.getGlobalInstance(false));
        }

        public BossBar getBossBar() {
            return bossBar;
        }

        public void addSize(int size) {
            this.size += size;
        }

        public void removeSize(int size) {
            this.size -= size;
        }

        public Collection<PlayerData> players() {
            Collection<PlayerData> list = getInPvPRaidPlayers();
            list.removeIf(playerData -> getTeam(playerData) != this);
            return list;
        }
    }

    public enum Kit {
        Balance("バランス", Color.fromRGB(128, 128, 128)),
        Warrior("ウォーリア", Color.fromRGB(255, 0, 0)),
        Guardian("ガーディアン", Color.fromRGB(128, 0, 0)),
        DarkMage("ダークメイジ", Color.fromRGB(0, 0, 0)),
        WhiteMage("ホワイトメイジ", Color.fromRGB(255, 255, 255)),
        Archer("アーチャー", Color.fromRGB(0, 0, 255)),
        Sniper("スナイパー", Color.fromRGB(0, 0, 128)),
        Healer("ヒーラー", Color.fromRGB(255, 255, 0)),
        Templar("テンプラー", Color.fromRGB(128, 255, 128)),
        ;

        private final String display;
        private final Color color;
        private HashMap<StatusType, Double> overrideStatus;

        public final CustomItemStack Head;

        Kit(String display, Color color) {
            this.display = display;
            this.color = color;

            Head = new CustomItemStack(Material.LEATHER_HELMET).setNonDecoDisplay(" ");
            CustomItemStack.setupColor(Head, color);
        }

        public HashMap<StatusType, Double> overrideStatus() {
            if (overrideStatus == null) {
                overrideStatus = new HashMap<>();
                overrideStatus.put(StatusType.MaxHealth, 30000.0);
                overrideStatus.put(StatusType.HealthRegen, 400.0);
                overrideStatus.put(StatusType.MaxMana, 5000.0);
                overrideStatus.put(StatusType.ManaRegen, 100.0);
                overrideStatus.put(StatusType.ATK, 0.0);
                overrideStatus.put(StatusType.MAT, 0.0);
                overrideStatus.put(StatusType.SAT, 0.0);
                overrideStatus.put(StatusType.SPT, 0.0);
                overrideStatus.put(StatusType.CriticalRate, 500.0);
                overrideStatus.put(StatusType.CriticalDamage, 500.0);
                overrideStatus.put(StatusType.CriticalResist, 1000.0);
                overrideStatus.put(StatusType.Penetration, 5.0);
                switch (this) {
                    case Balance -> {
                        overrideStatus.put(StatusType.ATK, 5000.0);
                        overrideStatus.put(StatusType.MAT, 5000.0);
                        overrideStatus.put(StatusType.SAT, 5000.0);
                        overrideStatus.put(StatusType.SPT, 5000.0);
                        overrideStatus.put(StatusType.DEF, 4000.0);
                        overrideStatus.put(StatusType.MDF, 4000.0);
                        overrideStatus.put(StatusType.SDF, 4000.0);
                    }
                    case Warrior -> {
                        overrideStatus.put(StatusType.ATK, 7000.0);
                        overrideStatus.put(StatusType.DEF, 3000.0);
                        overrideStatus.put(StatusType.MDF, 3000.0);
                        overrideStatus.put(StatusType.SDF, 3000.0);
                    }
                    case Guardian -> {
                        overrideStatus.put(StatusType.MaxHealth, 40000.0);
                        overrideStatus.put(StatusType.HealthRegen, 600.0);
                        overrideStatus.put(StatusType.ATK, 5000.0);
                        overrideStatus.put(StatusType.DEF, 5000.0);
                        overrideStatus.put(StatusType.MDF, 5000.0);
                        overrideStatus.put(StatusType.SDF, 5000.0);
                    }
                    case DarkMage -> {
                        overrideStatus.put(StatusType.MaxHealth, 25000.0);
                        overrideStatus.put(StatusType.HealthRegen, 300.0);
                        overrideStatus.put(StatusType.MAT, 7000.0);
                        overrideStatus.put(StatusType.DEF, 2500.0);
                        overrideStatus.put(StatusType.MDF, 2500.0);
                        overrideStatus.put(StatusType.SDF, 2500.0);
                    }
                    case WhiteMage -> {
                        overrideStatus.put(StatusType.MaxHealth, 35000.0);
                        overrideStatus.put(StatusType.HealthRegen, 500.0);
                        overrideStatus.put(StatusType.MaxMana, 10000.0);
                        overrideStatus.put(StatusType.ManaRegen, 200.0);
                        overrideStatus.put(StatusType.MAT, 5000.0);
                        overrideStatus.put(StatusType.DEF, 4000.0);
                        overrideStatus.put(StatusType.MDF, 4000.0);
                        overrideStatus.put(StatusType.SDF, 4000.0);
                    }
                    case Archer -> {
                        overrideStatus.put(StatusType.SAT, 7000.0);
                        overrideStatus.put(StatusType.DEF, 3000.0);
                        overrideStatus.put(StatusType.MDF, 3000.0);
                        overrideStatus.put(StatusType.SDF, 3000.0);
                    }
                    case Sniper -> {
                        overrideStatus.put(StatusType.SAT, 6000.0);
                        overrideStatus.put(StatusType.DEF, 3000.0);
                        overrideStatus.put(StatusType.MDF, 3000.0);
                        overrideStatus.put(StatusType.SDF, 3000.0);
                        overrideStatus.put(StatusType.CriticalRate, 1500.0);
                        overrideStatus.put(StatusType.CriticalDamage, 1500.0);
                    }
                    case Healer -> {
                        overrideStatus.put(StatusType.MaxMana, 10000.0);
                        overrideStatus.put(StatusType.ManaRegen, 200.0);
                        overrideStatus.put(StatusType.MAT, 6000.0);
                        overrideStatus.put(StatusType.SPT, 6000.0);
                        overrideStatus.put(StatusType.DEF, 2500.0);
                        overrideStatus.put(StatusType.MDF, 2500.0);
                        overrideStatus.put(StatusType.SDF, 2500.0);
                    }
                    case Templar -> {
                        overrideStatus.put(StatusType.SPT, 4000.0);
                        overrideStatus.put(StatusType.DEF, 5000.0);
                        overrideStatus.put(StatusType.MDF, 5000.0);
                        overrideStatus.put(StatusType.SDF, 5000.0);
                    }
                }
            }
            return overrideStatus;
        }

        public static class GUI extends GUIManager {
            public GUI(PlayerData playerData) {
                super(playerData, "キット選択", 1);
            }

            @Override
            public void open() {
                if (isInPvPRaid(playerData)) {
                    super.open();
                }
            }

            @Override
            public void updateContainer() {
                int slot = 0;
                for (Kit kit : Kit.values()) {
                    CustomItemStack icon = new CustomItemStack(Material.LEATHER_CHESTPLATE);
                    icon.setDisplay(kit.display);
                    icon.setLeatherArmorColor(kit.color);
                    for (StatusType statusType : StatusType.values()) {
                        double value = kit.overrideStatus().getOrDefault(statusType, 0.0);
                        if (value != 0) icon.addLore(decoLore(statusType.getDisplay()) + scale(kit.overrideStatus().get(statusType)));
                    }
                    icon.setCustomData("Kit", kit.toString());
                    setItem(slot, icon);
                    slot++;
                }
            }

            @Override
            public void topClick(int slot, ItemStack clickedItem, ClickType clickType) {
                if (CustomItemStack.hasCustomData(clickedItem, "Kit")) {
                    Kit kit = Kit.valueOf(CustomItemStack.getCustomData(clickedItem, "Kit"));
                    kits.put(playerData, kit);
                    playerData.setOverrideStatus(kit.overrideStatus());
                    playerData.updateEquipView();
                    playerData.sendMessage("§e" + kit.display + "§aを選択しました", SomSound.Tick);
                    playerData.statusUpdate();
                }
            }

            @Override
            public void bottomClick(int slot, ItemStack clickedItem, ClickType clickType) {

            }

            @Override
            public void close() {

            }
        }
    }

    private static boolean isOpen = false;
    private static boolean isStart = false;
    private static final HashMap<HumanData, Team> teamMap = new HashMap<>();
    private static final HashMap<HumanData, Double> points = new HashMap<>();
    private static final HashMap<HumanData, Integer> kills = new HashMap<>();
    private static final HashMap<HumanData, Kit> kits = new HashMap<>();

    public static double totalPoint() {
        double point = 0;
        for (double value : points.values()) {
            point += value;
        }
        return point;
    }

    public static double teamPoint(Team team) {
        double point = 0;
        for (Map.Entry<HumanData, Team> entry : teamMap.entrySet()) {
            if (entry.getValue() == team) {
                point += points.get(entry.getKey());
            }
        }
        return point;
    }

    public static CustomItemStack viewItem(PlayerData playerData) {
        CustomItemStack item = new CustomItemStack(Material.STRUCTURE_BLOCK);
        item.setDisplay(Display);
        item.addLore("§e2チーム§aに分かれて戦う§cPvPレイド§aです");
        item.addLore("§aステータスは§e全員均衡§aになります");
        item.addLore("§eクラス・スキル・PS§aで差が生まれます");
        if (isInPvPRaid(playerData)) {
            item.addLore("§c※ここから退場できます");
        }
        item.addSeparator("開催情報");
        if (isOpen) {
            item.addLore(decoLore("開催状況") + (isStart ? "§b戦闘中" : "§e準備中"));
            item.addLore(decoLore("参加人数") + teamMap.size() + "人");
            for (Team team : Team.values()) {
                item.addLore(decoLore(team.display) + team.size + "人");
            }
        } else {
            item.addLore(decoLore("開催状況") + "§c未開催");
        }
        item.setCustomData("PvPRaid", true);
        return item;
    }

    public static void open() {
        isOpen = true;
        broadcast(Display + "§aが§b開催§aしました", SomSound.BossSpawn);
    }

    public static void openToggle() {
        isOpen = !isOpen;
        if (isOpen) {
            broadcast(Display + "§aが§b開催§aしました");
        } else {
            reset();
            broadcast(Display + "§aが§c終了§aされました");
        }
    }

    public static void startToggle() {
        if (!isStart) {
            start();
            broadcast(Display + "§aが§b開戦§aしました");
        } else {
            end();
            broadcast(Display + "§aが§c終戦§aされました");
        }
    }

    public static void start() {
        isStart = true;
        for (PlayerData playerData : getInPvPRaidPlayers()) {
            playerData.teleport(getTeam(playerData).getSpawnLocation());
        }
        sendMessage(Display + "§aが§c開戦§aしました", SomSound.BossSpawn);
    }

    public static void end() {
        isStart = false;
        sendMessage(Display + "§aが§c終戦§aしました", SomSound.Winner);
    }

    public static boolean isStart() {
        return isStart;
    }

    public static void clickItem(PlayerData playerData) {
        if (PvPRaid.disableClass(playerData, playerData.classes().getMainClass())) return;
        if (isOpen) {
            if (isInPvPRaid(playerData)) {
                Team team = getTeam(playerData);
                if (team.getSpawnLocation().distance(playerData.getLocation()) < 32) {
                    quit(playerData);
                } else {
                    playerData.sendMessage("§eスポーン地点付近§aにいる必要があります", SomSound.Nope);
                }
            } else if (playerData.getMap().isCity()) {
                join(playerData);
            } else {
                playerData.sendReqInCity();
            }
        } else if (isInPvPRaid(playerData)) {
            quit(playerData);
        } else {
            playerData.sendMessage("§c現在は開催されていません", SomSound.Nope);
        }
    }

    public static boolean isInPvPRaid(HumanData humanData) {
        return teamMap.containsKey(humanData);
    }

    public static boolean isEnemy(HumanData attacker, HumanData victim) {
        return isInPvPRaid(attacker) && isInPvPRaid(victim) && PvPRaid.isStart() && PvPRaid.getTeam(attacker) != PvPRaid.getTeam(victim) && PvPRaid.getTeam(victim).getSpawnLocation().distance(victim.getLocation()) > 15;
    }

    public static Team getTeam(HumanData humanData) {
        return teamMap.get(humanData);
    }

    public static Kit getKit(HumanData humanData) {
        return kits.get(humanData);
    }

    public static void join(PlayerData playerData) {
        if (!teamMap.containsKey(playerData)) {
            Team team = Team.Red.size < Team.Blue.size ? Team.Red : Team.Blue;
            teamMap.put(playerData, team);
            points.put(playerData, 0.0);
            team.bossBar.addPlayer(playerData.getPlayer());
            team.addSize(1);
            kits.put(playerData, Kit.Balance);
            playerData.setOverrideStatus(getKit(playerData).overrideStatus());
            playerData.heal();
            SomTask.sync(() -> playerData.teleport(team.getSpawnLocation()));
            team.mapData.enter(playerData, false);
            playerData.updateEquipView();
            sendMessage(playerData.getName() + "§aが" + team.display + "§aに§b参加§aしました", SomSound.Tick);
            SomTask.asyncDelay(() -> playerData.pvpKit().open(), 10);

        }
        if (!isStart && teamMap.size() >= 8) {
            start();
        }

        SomTask.sync(() -> {
            World instance = mapData.getGlobalInstance(false);
            if (mapData.isInvalidNPC(instance)) mapData.intiInstance(instance);
        });
    }

    public static void quit(PlayerData playerData) {
        if (teamMap.containsKey(playerData)) {
            resetPlayer(playerData);
            Team team = teamMap.get(playerData);
            team.removeSize(1);
            teamMap.remove(playerData);
            points.remove(playerData);
            kills.remove(playerData);
            kits.remove(playerData);
            playerData.sendMessage(Display + "§aから§c退場§aしました", SomSound.Tick);
            sendMessage(playerData.getName() + "§aが" + team.display + "§aから§c脱退§aしました", SomSound.Tick);
        }
    }

    public static void resetPlayer(PlayerData playerData) {
        SomTask.sync(() -> playerData.teleport(playerData.lastSpawnLocation()));
        playerData.saveLastCityTime(20);
        Team team = getTeam(playerData);
        team.bossBar.removePlayer(playerData.getPlayer());
        playerData.resetOverrideStatus();
        SomTask.asyncDelay(playerData::updateEquipView, 5);
    }

    public static void kill(PlayerData attacker, PlayerData victim) {
        double average = totalPoint / teamMap.size();
        double percent = Math.min(0.25, (points.get(victim) / average) * 0.25);
        double removePoint = points.get(victim) * percent;
        double getPoint = removePoint + 100;
        kills.merge(attacker, 1, Integer::sum);
        addPoint(attacker, getPoint);
        removePoint(victim, removePoint);
        attacker.sendMessage("§e" + scale(getPoint, 1) + "ポイント§aを§b獲得§aしました §e[" + scale(percent*100, 1) + "%]", SomSound.Tick);
        victim.sendMessage("§e" + scale(removePoint, 1) + "ポイント§aを§c奪取§aされました", SomSound.Tick);
    }

    public static double getPoint(PlayerData playerData) {
        return points.getOrDefault(playerData, 0.0);
    }

    private static final HashMap<PlayerData, BukkitTask> pointView = new HashMap<>();
    public static void addPoint(PlayerData playerData, double point) {
        if (!pointView.containsKey(playerData)) {
            double below = getPoint(playerData);
            pointView.put(playerData, SomTask.asyncDelay(() -> {
                double now = getPoint(playerData);
                if (now != below) {
                    double difference = now - below;
                    String color = difference > 0 ? "§e" : "§c";
                    playerData.sendTitle(" ", "            " + color + scale(difference, 1, true) + " §7(" + scale(now) + ")", 1, 10, 1);
                }
                pointView.remove(playerData);
            }, 10));
        }
        playerData.resetAFK();
        points.merge(playerData, point, Double::sum);
    }

    public static void removePoint(PlayerData playerData, double point) {
        points.merge(playerData, -point, Double::sum);
    }

    public static void reset() {
        end();
        for (PlayerData playerData : getInPvPRaidPlayers()) {
            resetPlayer(playerData);
        }
        for (Team team : Team.values()) {
            team.size = 0;
        }
        kills.clear();
        teamMap.clear();
        points.clear();
    }

    public static Collection<PlayerData> getInPvPRaidPlayers() {
        Set<PlayerData> list = new HashSet<>();
        for (HumanData humanData : teamMap.keySet()) {
            list.add((PlayerData) humanData);
        }
        return list;
    }

    public static void sendMessage(String message, SomSound sound) {
        for (HumanData humanData : teamMap.keySet()) {
            humanData.sendMessage(message, sound);
        }
    }

    public static void sendMessage(List<String> message, SomSound sound) {
        for (HumanData humanData : teamMap.keySet()) {
            humanData.sendMessage(message, sound);
        }
    }

    public static boolean disableClass(PlayerData playerData, ClassType classType) {
        switch (classType) {
            case Adventurer, Tamer, DualStar -> {
                playerData.sendMessage(PvPRaid.Display + "§aでは" + classType.getColorDisplay() + "§aは使用できません", SomSound.Nope);
                return true;
            }
        }
        return false;
    }
}
