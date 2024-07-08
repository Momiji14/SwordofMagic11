package SwordofMagic11.Player.Menu;

import SwordofMagic11.Component.SomSQL;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Component.SomTask;
import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.DataBase.DataBase;
import SwordofMagic11.Map.MapData;
import SwordofMagic11.Map.WarpGate;
import SwordofMagic11.Player.PlayerData;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import static SwordofMagic11.Component.Function.decoLore;
import static SwordofMagic11.Component.Function.scale;
import static SwordofMagic11.Map.MapData.*;

public class InstanceMenu extends GUIManager {

    private final CustomItemStack normal = new CustomItemStack(Material.GLASS)
            .setDisplay("一般インスタンス")
            .addLore("§a通常のインスタンスです")
            .setCustomData("InstanceMenu", "Normal");

    private CustomItemStack arsha() {
        return new CustomItemStack(Material.REDSTONE_BLOCK)
                .setDisplay("PvPインスタンス")
                .addLore("§a常時§cPvP§aが有効のインスタンスです")
                .addLore("§cドロップ倍率§aが§e+" + scale(ArshaMultiply * 100) + "%§aされます")
                .addLore("§aPKすると経験値とグラインダーを§e" + scale(ArshaRisk * 100) + "%§a奪えます")
                .addLore("§aPK以外の要因で死亡した場合は§e" + scale(ArshaEnemyRisk * 100) + "%§a失います")
                .addLore("§a入場時に§e5秒§aの§b無敵§aが付与されます")
                .addLore("§c※PK略奪のリソース量は加害者の所持量を超えません")
                .addSeparator("システムに吸収された統計")
                .addLore(decoLore("グラインダー") + SomSQL.getSql(DataBase.Table.GlobalPvPDrop, "Grinder").getInt("Grinder"))
                .addLore(decoLore("経験値") + scale(SomSQL.getSql(DataBase.Table.GlobalPvPDrop, "Exp").getDouble("Exp")))
                .setCustomData("InstanceMenu", "Arsha");
    }

    private final CustomItemStack privateIns = new CustomItemStack(Material.TOTEM_OF_UNDYING)
            .setDisplay("プライベートインスタンス")
            .addLore("§aパーティメンバーのみが参加できるインスタンスできる")
            .addLore("§aプライベートインスタンスは利用権利が必要です")
            .addLore("§aパーティ内に利用権利所持者が1人でもいると")
            .addLore("§e無料参加枠が3枠付与されます")
            .setCustomData("InstanceMenu", "Private");

    private MapData nextMap;
    private WarpGate warpGate;

    public InstanceMenu(PlayerData playerData) {
        super(playerData, "インスタンス選択", 1);
    }

    public void open(WarpGate warpGate) {
        this.warpGate = warpGate;
        nextMap = warpGate.getToMap();
        super.open();
    }

    @Override
    public void updateContainer() {
        setItem(1, normal);
        setItem(4, privateIns);
        setItem(7, arsha());
    }

    @Override
    public void topClick(int slot, ItemStack clickedItem, ClickType clickType) {
        if (warpGate != null) {
            if (CustomItemStack.hasCustomData(clickedItem, "InstanceMenu")) {
                playerData.setting().resetPvPMode();
                String data = CustomItemStack.getCustomData(clickedItem, "InstanceMenu");
                switch (data) {
                    case "Normal", "Arsha" -> {
                        boolean arsha = data.equals("Arsha");
                        if (arsha && playerData.getLevel() < 20) {
                            playerData.sendMessage("§cPvPインスタンス§aは§eLv20§aから§b利用§aできます", SomSound.Nope);
                            return;
                        }
                        SomTask.sync(() -> {
                            MapData nextMap = warpGate.getToMap();
                            SomSound.Teleport.radius(playerData);
                            playerData.teleport(warpGate.getToLocation().as(nextMap.getGlobalInstance(arsha)));
                            nextMap.enter(playerData, arsha);
                            if (arsha) playerData.invinciblePvP(100);
                        });
                    }
                    case "Private" -> {
                        if (!playerData.hasParty()) {
                            playerData.sendMessage("§cパーティに参加していません", SomSound.Nope);
                            return;
                        }
                        if (!playerData.donation().hasPrivateInsRight()) {
                            boolean hasPrivateInsRight = false;
                            int freeCount = 0;
                            for (PlayerData member : playerData.getParty().getMember()) {
                                if (member.donation().hasPrivateInsRight()) {
                                    hasPrivateInsRight = true;
                                } else if (member.isInPrivateIns()) {
                                    freeCount++;
                                }
                            }
                            if (!hasPrivateInsRight) {
                                playerData.sendMessage("§c利用権利所有者がいません", SomSound.Nope);
                                return;
                            }
                            if (freeCount >= 3) {
                                playerData.sendMessage("§cすでに§e無料枠§cがすべて使われています", SomSound.Nope);
                                return;
                            }
                        }

                        SomTask.sync(() -> MapData.mapEnter(playerData, nextMap.getPrivateInstance(playerData.getParty()), warpGate, false));
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
        warpGate = null;
    }
}
