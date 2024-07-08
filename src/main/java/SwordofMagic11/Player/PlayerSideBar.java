package SwordofMagic11.Player;

import SwordofMagic11.Component.SomSQL;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Component.SomTask;
import SwordofMagic11.DataBase.CapsuleDataLoader;
import SwordofMagic11.DataBase.DataBase;
import SwordofMagic11.DataBase.MaterialDataLoader;
import SwordofMagic11.Entity.SomEffect;
import SwordofMagic11.Item.CapsuleData;
import SwordofMagic11.Item.Material.MaterialData;
import SwordofMagic11.Item.SomEquip;
import SwordofMagic11.Player.Gathering.GatheringMenu;
import SwordofMagic11.Player.Statistics.Statistics;
import com.github.jasync.sql.db.RowData;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.*;

import static SwordofMagic11.Component.Function.*;
import static SwordofMagic11.Player.Setting.PlayerSetting.BooleanEnum.SBTDTriggerSound;

public class PlayerSideBar {
    private static final String Prefix = "§6[SBTD]§r";
    private final PlayerData playerData;
    private final Player player;
    private Scoreboard scoreboard;
    private Objective objective;
    private boolean visible = false;
    private boolean totalMel = false;
    private final LinkedHashMap<MaterialData, Integer> amountMaterial = new LinkedHashMap<>();
    private final LinkedHashMap<CapsuleData, Integer> amountCapsule = new LinkedHashMap<>();
    private final Set<String> enemyKill = new HashSet<>();
    private final Set<Statistics.IntEnum> intEnum = new HashSet<>();
    private boolean partyInfo = false;
    private boolean buffInfo = false;
    private boolean gatheringInfo = false;
    private boolean equipExp = false;

    public PlayerSideBar(PlayerData playerData) {
        this.playerData = playerData;
        player = playerData.getPlayer();
        SomTask.sync(() -> {
            if (SomSQL.exists(DataBase.Table.SideBarToDo, "UUID", playerData.getUUID())) {
                for (RowData objects : SomSQL.getSqlList(DataBase.Table.SideBarToDo, "UUID", playerData.getUUID(), "*")) {
                    switch (objects.getString("SideBar")) {
                        case "TotalMel" -> totalMel = true;
                        case "AmountMaterial" -> {
                            for (String id : objects.getString("Value").split(",")) {
                                amountMaterial.put(MaterialDataLoader.getMaterialData(id), null);
                            }
                        }
                        case "AmountCapsule" -> {
                            for (String id : objects.getString("Value").split(",")) {
                                amountCapsule.put(CapsuleDataLoader.getCapsuleData(id), null);
                            }
                        }
                        case "EnemyKill" -> enemyKill.add(objects.getString("Value"));
                        case "IntEnum" -> intEnum.add(Statistics.IntEnum.valueOf(objects.getString("Value")));
                        case "PartyInfo" -> partyInfo = true;
                        case "BuffInfo" -> buffInfo = true;
                        case "GatheringInfo" -> gatheringInfo = true;
                        case "EquipExp" -> equipExp = true;
                    }
                }
            }

            scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
            player.setScoreboard(scoreboard);
            objective = scoreboard.registerNewObjective("SideBar", Criteria.DUMMY, Component.text(decoText("SideBar ToDo", 6)));

            List<String> sideBar = new ArrayList<>();
            List<String> lastLine = new ArrayList<>();
            SomTask.asyncTimer(() -> {
                if (visible) {
                    sideBar.clear();
                    if (totalMel) {
                        sideBar.add(decoLore("総資産") + playerData.userMenu().lastTotalMel());
                    }
                    if (!amountMaterial.isEmpty() || !amountCapsule.isEmpty()) {
                        if (!sideBar.isEmpty()) sideBar.add(decoText("Amount Info", 6));
                        amountMaterial.forEach((material, goal) -> sideBar.add("§7・§f" + material.getDisplay() + "x" + playerData.getMaterial(material) + (goal != null ? "/" + goal : "")));
                        amountCapsule.forEach((capsule, goal) -> sideBar.add("§7・§f" + capsule.getDisplay() + "x" + playerData.getCapsule(capsule) + (goal != null ? "/" + goal : "")));
                    }
                    if (isPartyInfo() && playerData.hasParty()) {
                        if (!sideBar.isEmpty()) sideBar.add(decoText("Party Info", 6));
                        for (PlayerData member : playerData.getParty().getMember()) {
                            sideBar.add("§7・§f" + member.getColorDisplayName() + " §c" + scale(member.healthPercent()*100) + "% §b" + scale(member.manaPercent()*100) + "%");
                        }
                    }
                    if (buffInfo) {
                        if (!sideBar.isEmpty()) sideBar.add(decoText("Buff Info", 6));
                        for (SomEffect effect : playerData.getEffects()) {
                            sideBar.add("§7・§f" + effect.getDisplay() + "§a" + effect.getTime()/20 + "秒");
                        }
                    }
                    if (gatheringInfo) {
                        if (!sideBar.isEmpty()) sideBar.add(decoText("Gathering Info", 6));
                        for (GatheringMenu.Type type : GatheringMenu.Type.values()) {
                            int level = playerData.gatheringMenu().getLevel(type);
                            double expPercent = playerData.gatheringMenu().getExpPercent(type);
                            sideBar.add("§7・§f" + type.getDisplay() + " §eLv" + level + " §a" + scale(expPercent * 100, 3) + "%");
                        }
                    }
                    if (equipExp) {
                        if (!sideBar.isEmpty()) sideBar.add(decoText("EquipExp Info", 6));
                        for (SomEquip equip : playerData.getEquipment().values()) {
                            sideBar.add("§7・" + equip.getColorDisplay() + " §a" + scale(equip.getExp() * 100, 2) + "%");
                        }
                    }
                    if (!enemyKill.isEmpty()) {
                        enemyKill.forEach(enemy -> sideBar.add(decoLore(enemy) + playerData.statistics().enemyKill(enemy)));
                    }
                    if (!intEnum.isEmpty()) {
                        intEnum.forEach(intEnum -> sideBar.add(decoLore(intEnum.getDisplay()) + playerData.statistics().get(intEnum)));
                    }
                    lastLine.removeAll(sideBar);
                    for (String line : lastLine) {
                        objective.getScore(line).resetScore();
                    }
                    int i = sideBar.size();
                    for (String line : sideBar) {
                        objective.getScore(line).setScore(i);
                        i--;
                    }
                    lastLine.addAll(sideBar);
                }
            }, 20, playerData);
        });
    }

    private static final String[] key = new String[]{"UUID", "SideBar"};

    public void show() {
        visible = true;
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        playerData.sendMessage(Prefix + "§eサイドバー§aを§b表示§aします");
    }

    public void hide() {
        visible = false;
        scoreboard.clearSlot(DisplaySlot.SIDEBAR);
        playerData.sendMessage(Prefix + "§eサイドバー§aを§c非表示§aにします");
    }

    public void toggleTotalMel() {
        totalMel = !totalMel;
        if (totalMel && !visible) show();
        playerData.sendMessage(Prefix + "§e総資産表示§aを" + boolText(totalMel) + "§aにしました", SomSound.Tick);
        SomTask.async(() -> {
            String[] value = new String[]{playerData.getUUID(), "TotalMel"};
            if (totalMel) {
                SomSQL.setSql(DataBase.Table.SideBarToDo, key, value, "Value", "true");
            } else {
                SomSQL.delete(DataBase.Table.SideBarToDo, key, value);
            }
        });
    }

    public void amountMaterial(String material, Integer goal) {
        amountMaterial(MaterialDataLoader.getMaterialData(material), goal);
    }

    public void amountMaterial(MaterialData material, Integer goal) {
        if (amountMaterial.containsKey(material)) {
            amountMaterial.remove(material);
        } else {
            amountMaterial.put(material, goal);
            if (!visible) show();
        }
        playerData.sendMessage(Prefix + "§f" + material.getDisplay() + "§aの§e個数表示§aを" + boolText(amountMaterial.containsKey(material)) + "§aにしました", SomSound.Tick);
        SomTask.async(() -> {
            String[] key = new String[]{"UUID", "SideBar"};
            String[] value = new String[]{playerData.getUUID(), "AmountMaterial"};
            if (amountMaterial.isEmpty()) {
                SomSQL.delete(DataBase.Table.SideBarToDo, key, value);
            } else {
                StringBuilder builder = new StringBuilder();
                for (MaterialData data : amountMaterial.keySet()) {
                    builder.append(data.getId()).append(",");
                }
                builder.deleteCharAt(builder.length());
                SomSQL.setSql(DataBase.Table.SideBarToDo, key, value, "Value", builder.toString());
            }
        });
    }

    public void amountCapsule(String capsule, Integer goal) {
        amountCapsule(CapsuleDataLoader.getCapsuleData(capsule), goal);
    }

    public void amountCapsule(CapsuleData capsule, Integer goal) {
        if (amountCapsule.containsKey(capsule)) {
            amountCapsule.remove(capsule);
        } else {
            amountCapsule.put(capsule, goal);
            if (!visible) show();
        }
        playerData.sendMessage(Prefix + "§f" + capsule.getDisplay() + "§aの§e個数表示§aを" + boolText(amountCapsule.containsKey(capsule)) + "§aにしました", SomSound.Tick);
        SomTask.async(() -> {
            String[] key = new String[]{"UUID", "SideBar"};
            String[] value = new String[]{playerData.getUUID(), "AmountCapsule"};
            if (amountCapsule.isEmpty()) {
                SomSQL.delete(DataBase.Table.SideBarToDo, key, value);
            } else {
                StringBuilder builder = new StringBuilder();
                for (CapsuleData data : amountCapsule.keySet()) {
                    builder.append(data.getId()).append(",");
                }
                builder.deleteCharAt(builder.length());
                SomSQL.setSql(DataBase.Table.SideBarToDo, key, value, "Value", builder.toString());
            }
        });
    }

    public void enemyKill(String enemy) {
        if (enemyKill.contains(enemy)) {
            enemyKill.remove(enemy);
            SomTask.async(() -> {
                String[] key = new String[]{"UUID", "SideBar", "Value"};
                String[] value = new String[]{playerData.getUUID(), "EnemyKill", enemy};
                SomSQL.delete(DataBase.Table.SideBarToDo, key, value);
            });
        } else {
            enemyKill.add(enemy);
            SomTask.async(() -> {
                String[] value = new String[]{playerData.getUUID(), "EnemyKill"};
                SomSQL.setSql(DataBase.Table.SideBarToDo, key, value, "Value", enemy);
            });
        }
    }

    public void intEnum(Statistics.IntEnum intEnum) {
        if (this.intEnum.contains(intEnum)) {
            this.intEnum.remove(intEnum);
            SomTask.async(() -> {
                String[] key = new String[]{"UUID", "SideBar", "Value"};
                String[] value = new String[]{playerData.getUUID(), "IntEnum", intEnum.toString()};
                SomSQL.delete(DataBase.Table.SideBarToDo, key, value);
            });
        } else {
            this.intEnum.add(intEnum);
            SomTask.async(() -> {
                String[] value = new String[]{playerData.getUUID(), "IntEnum"};
                SomSQL.setSql(DataBase.Table.SideBarToDo, key, value, "Value", intEnum.toString());
            });
        }
    }

    public void togglePartyInfo() {
        partyInfo = !partyInfo;
        if (partyInfo && !visible) show();
        playerData.sendMessage(Prefix + "§eパーティ情報§aを" + boolText(partyInfo) + "§aにしました", SomSound.Tick);
        SomTask.async(() -> {
            String[] value = new String[]{playerData.getUUID(), "PartyInfo"};
            if (partyInfo) {
                SomSQL.setSql(DataBase.Table.SideBarToDo, key, value, "Value", "true");
            } else {
                SomSQL.delete(DataBase.Table.SideBarToDo, key, value);
            }
        });
    }

    public void toggleBuffInfo() {
        buffInfo = !buffInfo;
        if (buffInfo && !visible) show();
        playerData.sendMessage(Prefix + "§eバフ情報§aを" + boolText(buffInfo) + "§aにしました", SomSound.Tick);
        SomTask.async(() -> {
            String[] value = new String[]{playerData.getUUID(), "BuffInfo"};
            if (buffInfo) {
                SomSQL.setSql(DataBase.Table.SideBarToDo, key, value, "Value", "true");
            } else {
                SomSQL.delete(DataBase.Table.SideBarToDo, key, value);
            }
        });
    }

    public void toggleGatheringInfo() {
        gatheringInfo = !gatheringInfo;
        if (gatheringInfo && !visible) show();
        playerData.sendMessage(Prefix + "§eギャザリング情報§aを" + boolText(gatheringInfo) + "§aにしました", SomSound.Tick);
        SomTask.async(() -> {
            String[] value = new String[]{playerData.getUUID(), "GatheringInfo"};
            if (gatheringInfo) {
                SomSQL.setSql(DataBase.Table.SideBarToDo, key, value, "Value", "true");
            } else {
                SomSQL.delete(DataBase.Table.SideBarToDo, key, value);
            }
        });
    }

    public void toggleEquipExp() {
        equipExp = !equipExp;
        if (equipExp && !visible) show();
        playerData.sendMessage(Prefix + "§e装備熟練度情報§aを" + boolText(equipExp) + "§aにしました", SomSound.Tick);
        SomTask.async(() -> {
            String[] value = new String[]{playerData.getUUID(), "EquipExp"};
            if (equipExp) {
                SomSQL.setSql(DataBase.Table.SideBarToDo, key, value, "Value", "true");
            } else {
                SomSQL.delete(DataBase.Table.SideBarToDo, key, value);
            }
        });
    }

    public boolean isPartyInfo() {
        return partyInfo;
    }

    public void triggerMaterial(MaterialData material) {
        if (playerData.setting().is(SBTDTriggerSound)) {
            if (amountMaterial.containsKey(material)) {
                Integer goal = amountMaterial.get(material);
                if (goal != null && playerData.getMaterial(material) >= goal) {
                    playerData.sendMessage(material.getDisplay() + "§aが§e"  + goal + "個§a集まりました", SomSound.Tick);
                }
            }
        }

    }

    public void triggerCapsule(CapsuleData capsule) {
        if (playerData.setting().is(SBTDTriggerSound)) {
            if (amountCapsule.containsKey(capsule)) {
                Integer goal = amountCapsule.get(capsule);
                if (goal != null && playerData.getCapsule(capsule) >= goal) {
                    playerData.sendMessage(capsule.getDisplay() + "§aが§e" + goal + "個§a集まりました", SomSound.Tick);
                }
            }
        }
    }
}
