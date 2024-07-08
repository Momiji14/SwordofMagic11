package SwordofMagic11.Player.Menu;

import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Component.SomTask;
import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.Custom.CustomLocation;
import SwordofMagic11.DataBase.MapDataLoader;
import SwordofMagic11.Map.*;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Player.SomParty;
import SwordofMagic11.StatusType;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

import static SwordofMagic11.Component.Function.decoLore;
import static SwordofMagic11.Component.Function.scale;

public class BossModeMenu extends GUIManager {

    private final int MemberSize = 4;
    private MapData nextMap;
    private WarpGate warpGate;
    public BossModeMenu(PlayerData playerData) {
        super(playerData, "ボスモード", 1);
    }

    public boolean isInBossTimeAttackMode() {
        return playerData.getWorld().getName().contains("BossTimeAttack_");
    }

    public void open(WarpGate warpGate) {
        this.warpGate = warpGate;
        nextMap = warpGate.getToMap();
        super.open();
    }

    private CustomItemStack timeAttack() {
        RoomData roomData = nextMap.getRoomData();
        CustomItemStack item = new CustomItemStack(Material.CLOCK);
        item.setDisplay("タイムアタック");
        item.addLore("§eクリアタイム§aを追及するモードです");
        item.addLore("§eステータス§aに§c上限§aがあります");
        item.addLore("§e回復倍率§aは§cPvP時§aと同じになります");
        item.addLore("§eパーティーインスタンス§aが生成されます");
        item.addLore("§c最大" + MemberSize + "人§aまで§b入場§a出来ます");
        item.addLore("§c※未実装");
        item.addSeparator("ステータス制限");
        for (StatusType statusType : StatusType.values()) {
            if (roomData.getLimitStatus().containsKey(statusType)) {
                item.addLore(decoLore(statusType.getDisplay()) + scale(roomData.getLimitStatus().get(statusType)));
            }
        }
        item.setCustomData("BossMode", "TimeAttack");
        return item;
    }

    public CustomItemStack normal() {
        CustomItemStack item = new CustomItemStack(Material.GRASS_BLOCK);
        item.setDisplay("ノーマルモード");
        item.addLore("§a普通のモードです");
        item.setCustomData("BossMode", "Normal");
        return item;
    }

    public CustomItemStack skip() {
        CustomItemStack item = new CustomItemStack(Material.GLASS);
        item.setDisplay("スキップ");
        item.addLore("§aクリアレコードを持っている場合");
        item.addLore("§aボスをスキップ出来ます");
        item.addSeparator("クリアレコード");
        item.addLore(playerData.bossTimeAttackMenu().getLine(nextMap));
        item.setCustomData("BossMode", "Skip");
        return item;
    }

    @Override
    public void updateContainer() {
        setItem(1, normal());
        setItem(4, timeAttack());
        setItem(7, skip());
    }

    @Override
    public void topClick(int slot, ItemStack clickedItem, ClickType clickType) {
        if (CustomItemStack.hasCustomData(clickedItem, "BossMode")) {
            switch (CustomItemStack.getCustomData(clickedItem, "BossMode")) {
                case "Normal" -> SomTask.sync(() -> {
                    World instance = MapData.getInstanceAtPlayer(playerData, nextMap);
                    MapData.mapEnter(playerData, instance, warpGate, false);
                });
                case "Skip" -> {
                    if (playerData.bossTimeAttackMenu().hasRecord(nextMap)) {
                        SomTask.sync(() -> {
                            RoomData roomData = nextMap.getRoomData();
                            World world = MapData.getInstanceAtPlayer(playerData, roomData.getExitMap());
                            CustomLocation location = roomData.getExit().as(world);
                            playerData.teleport(location);
                        });
                    } else {
                        playerData.sendMessage("§eクリアレコード§aがありません", SomSound.Nope);
                    }
                }
                case "TimeAttack" -> {
                    RoomData roomData = nextMap.getRoomData();
                    if (!roomData.getLimitStatus().isEmpty()) {
                        if (playerData.hasParty()) {
                            SomParty party = playerData.getParty();
                            if (party.getMember().size() <= MemberSize) {
                                SomTask.sync(() -> {
                                    World instance = nextMap.getInstance(null, "BossTimeAttack_" + UUID.randomUUID());
                                    roomData.registerTimeAttack(instance);
                                    SomTask.async(() -> {
                                        for (PlayerData member : party.getMember()) {
                                            member.removeEffectAll();
                                            member.setLimitStatus(roomData.getLimitStatus());
                                            MapData.mapEnter(member, instance, warpGate, false);
                                        }
                                    });
                                });
                            } else {
                                playerData.sendMessage("§eパーティーメンバー§aが§c人数制限§aを§c超過§aしています", SomSound.Nope);
                            }
                        } else {
                            playerData.sendNonJoinParty();
                        }
                    } else {
                        playerData.sendMessage("§aこの§cボス§aは§eタイムアタック§aの対象ではありません");
                    }
                }
            }
        }
    }

    @Override
    public void bottomClick(int slot, ItemStack clickedItem, ClickType clickType) {

    }

    @Override
    public void close() {

    }
}
