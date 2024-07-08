package SwordofMagic11.Player;

import SwordofMagic11.Component.*;
import SwordofMagic11.DataBase.DataBase;
import SwordofMagic11.DataBase.MapDataLoader;
import SwordofMagic11.Enemy.EnemyData;
import SwordofMagic11.Entity.SomEntity;
import SwordofMagic11.Item.CapsuleData;
import SwordofMagic11.Item.SomEquip;
import SwordofMagic11.Item.SyncItem;
import SwordofMagic11.Map.MapData;
import SwordofMagic11.Map.PvPRaid;
import SwordofMagic11.Pet.SomPet;
import SwordofMagic11.Player.Menu.GUIManager;
import SwordofMagic11.Player.Setting.PlayerSetting;
import SwordofMagic11.Player.Storage.CapsuleStorage;
import SwordofMagic11.Player.Storage.MaterialStorage;
import SwordofMagic11.Skill.SomSkill;
import SwordofMagic11.SomCore;
import SwordofMagic11.StatusType;
import SwordofMagic11.TaskOwner;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.util.*;

public abstract class HumanData extends SomEntity implements MaterialStorage, CapsuleStorage {

    protected Player player;
    protected String uuid;
    private boolean playMode = true;
    protected final PlayerSetting playerSetting;
    private KillParticle killParticle = KillParticle.Normal;
    private final Collection<GUIManager> guiManagers = new HashSet<>();
    protected final HashMap<SomEquip.Slot, SomEquip> equipment = new HashMap<>();
    private SomParty party;

    public HumanData(Player player) {
        super(player);
        this.player = player;
        uuid = player.getUniqueId().toString();
        playerSetting = new PlayerSetting(this);
        reset();

        SomTask.sync(() -> {
            for (PotionEffect potionEffect : player.getActivePotionEffects()) {
                player.removePotionEffect(potionEffect.getType());
            }
        });
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public String getUUID() {
        return uuid;
    }

    public boolean isPlayMode() {
        return playMode;
    }

    public void setPlayMode(boolean playMode) {
        this.playMode = playMode;
    }

    public PlayerSetting setting() {
        return playerSetting;
    }

    public KillParticle getKillParticle() {
        return killParticle;
    }

    public void setKillParticle(KillParticle killParticle) {
        this.killParticle = killParticle;
    }

    public void registerGUIManager(GUIManager guiManager) {
        guiManagers.add(guiManager);
    }

    public Collection<GUIManager> getGuiManagers() {
        return guiManagers;
    }

    public HashMap<SomEquip.Slot, SomEquip> getEquipment() {
        return equipment;
    }

    public HashMap<StatusType, Double> getTotalCapsuleStatus() {
        HashMap<StatusType, Double> status = new HashMap<>();
        for (SomEquip equip : equipment.values()) {
            for (CapsuleData capsule : equip.getCapsules()) {
                capsule.getStatus().forEach((statusType, value) -> status.merge(statusType, value, Double::sum));
            }
        }
        return status;
    }

    public boolean isJE() {
        return player.getName().charAt(0) != '.';
    }

    public boolean isBE() {
        return !isJE();
    }

    public MapData getLastCity() {
        return MapDataLoader.getMapData(SomSQL.getString(DataBase.Table.PlayerData, "UUID", uuid, "LastCity"));
    }

    public void setLastCity(MapData mapData) {
        SomTask.async(() -> SomSQL.setSql(DataBase.Table.PlayerData, "UUID", uuid, "LastCity", mapData.getId()));
    }

    public Location lastSpawnLocation() {
        return getLastCity().getGlobalInstance(false).getSpawnLocation();
    }

    public boolean isActiveMap() {
        return WorldManager.isActive(getWorld());
    }

    public boolean isInPrivateIns() {
        return WorldManager.isPrivate(getWorld());
    }

    public boolean hasParty() {
        return party != null;
    }

    public SomParty getParty() {
        return party;
    }

    public void setParty(SomParty party) {
        this.party = party;
    }

    public void sendMessage(SomText somText) {
        player.sendMessage(somText.toComponent());
    }

    public void sendMessage(SomText somText, SomSound sound) {
        player.sendMessage(somText.toComponent());
        sound.play(player, getLocation());
    }

    public void sendMessage(String message, SomSound sound) {
        sendMessage(message);
        sound.play(player, getLocation());
    }

    public void sendMessage(List<String> message, SomSound sound) {
        sendMessage(message);
        sound.play(player, getLocation());
    }

    public void sendMessage(String message) {
        player.sendMessage(message);
    }

    public void sendMessage(List<String> messages) {
        for (String message : messages) {
            player.sendMessage(message);
        }
    }

    public void sendNonGrinder() {
        sendMessage("§cグラインダーが足りません", SomSound.Nope);
    }

    public void sendNonMel() {
        sendMessage("§cメルが足りません", SomSound.Nope);
    }

    public void sendNonEld() {
        sendMessage("§cエルドが足りません", SomSound.Nope);
    }

    public void sendNonMana(SomSkill skill) {
        sendMessage("§cマナが足りません §e[" + skill.getSkillData().getDisplay() + "]", SomSound.Nope);
    }

    public void sendReqInCity() {
        sendMessage("§c街にいる必要があります", SomSound.Nope);
    }

    public void sendNonJoinParty() {
        sendMessage("§cパーティに参加していません", SomSound.Nope);
    }

    public void sendNoPartyLeader() {
        sendMessage("§eパーティリーダー§aではありません", SomSound.Nope);
    }

    public void sendNoTargetMe() {
        sendMessage("§c自身を対象にすることはできません", SomSound.Nope);
    }


    public BossBar sendBossBarMessage(String text, int delay, boolean chat) {
        return sendBossBarMessage(text, BarColor.WHITE, delay, chat);
    }

    public BossBar sendBossBarMessage(String text, BarColor color, int delay, boolean chat) {
        if (chat) sendMessage(text);
        BossBar bossBar = Bukkit.createBossBar(text, color, BarStyle.SOLID);
        bossBar.setVisible(true);
        bossBar.addPlayer(player);
        double value = 1.0/delay;
        SomTask.asyncCount(() -> bossBar.setProgress(Math.max(0, bossBar.getProgress() - value)), 1, delay, SomCore.TaskOwner);
        if (delay > 0) SomTask.syncDelay(bossBar::removeAll, delay);
        return bossBar;
    }

    public void sendTitle(String title, String subTitle, int in, int time, int out) {
        player.sendTitle(title, subTitle, in, time, out);
    }

    public void setEquip(SomEquip.Slot slot, SomEquip equip) {
        if (equip != null && equip.getReqLevel() > playerData().getLevel()) {
            sendMessage("§c装備条件§aを満たしていません", SomSound.Nope);
            return;
        }
        if (equipment.containsKey(slot)) {
            equipment.get(slot).setState(SyncItem.State.ItemInventory);
        }
        String[] key = new String[]{"UUID", "EquipSlot"};
        String[] value = new String[]{uuid, slot.toString()};
        if (equip != null) {
            equipment.put(slot, equip);
            equip.setState(SyncItem.State.Equipped);
            SomSQL.setSql(DataBase.Table.PlayerEquipment, key, value, "EquipUUID", equip.getUUID());
        } else {
            equipment.remove(slot);
            SomSQL.delete(DataBase.Table.PlayerEquipment, key, value);
        }
        if (this instanceof PlayerData playerData) {
            playerData.updateInventory();
        }
        statusUpdate();
        updateGameMode();
    }

    public SomEquip getEquip(SomEquip.Slot slot) {
        return equipment.get(slot);
    }

    public boolean hasEquip(SomEquip.Slot slot) {
        return equipment.containsKey(slot);
    }

    public SomEquip.Category getEquipType(SomEquip.Slot slot) {
        if (hasEquip(slot)) {
            return getEquip(slot).getEquipCategory();
        }
        return null;
    }

    public void updateGameMode() {
        if (isPlayMode()) {
            if (!isDeath()) {
                SomTask.sync(() -> {
                    if (hasEquip(SomEquip.Slot.MainHand)) {
                        if (getEquip(SomEquip.Slot.MainHand).isTool()) {
                            player.setGameMode(GameMode.SURVIVAL);
                            return;
                        }
                    }
                    player.setGameMode(GameMode.ADVENTURE);
                });
            }
        }
    }

    public void closeInventory() {
        SomTask.sync(() -> player.closeInventory());
    }

    public boolean isPvPMode() {
        return playerSetting.is(PlayerSetting.BooleanEnum.PvPMode) || isArsha();
    }

    @Override
    public boolean isValid() {
        return player.isOnline() && player.isValid() && PlayerData.isEntry(this);
    }

    @Override
    public void hurt() {
        player.sendHurtAnimation(0);
        SomSound.Hurt.radius(getLocation(), 16);
    }

    @Override
    public String getName() {
        return player.getName();
    }

    @Override
    public Collection<SomEntity> enemies() {
        Set<SomEntity> enemies = new HashSet<>(EnemyData.getList(getWorld()));
        for (PlayerData playerData : PlayerData.getPlayerList(getWorld())) {
            if (isEnemy(playerData) && playerData.isPlayMode()) {
                for (SomPet somPet : playerData.petMenu().getSummonList()) {
                    if (somPet.isSummon()) {
                        enemies.add(somPet.getEntityIns());
                    }
                }
                enemies.add(playerData);
            }
        }
        return enemies;
    }

    public Collection<SomEntity> playerEnemies() {
        Set<SomEntity> enemies = new HashSet<>();
        for (PlayerData playerData : PlayerData.getPlayerList(getWorld())) {
            if (isEnemy(playerData) && playerData.isPlayMode()) {
                enemies.add(playerData);
            }
        }
        return enemies;
    }

    @Override
    public boolean isEnemy(SomEntity entity) {
        if (this != entity) {
            if (entity.isDeath()) return false;
            if (entity instanceof EnemyData) {
                return true;
            } else if (entity instanceof PlayerData playerData) {
                if (!playerData.isPlayMode() || entity.isInvinciblePvP()) return false;
                if (PvPRaid.isEnemy(this, playerData)) {
                    return true;
                } else if (isPvPMode()) {
                    if (hasParty() && playerData.isPvPMode()) {
                        return getParty() != playerData.getParty();
                    }
                    return playerData.isPvPMode();
                }
            }
        }
        return false;
    }

    @Override
    public Collection<SomEntity> allies() {
        Set<SomEntity> allies = new HashSet<>();
        for (PlayerData playerData : PlayerData.getPlayerList(getWorld())) {
            if (isAlly(playerData)) {
                allies.add(playerData);
            }
        }
        return allies;
    }

    public Collection<PlayerData> alliesPlayerOnly() {
        Set<PlayerData> allies = new HashSet<>();
        for (PlayerData playerData : PlayerData.getPlayerList(getWorld())) {
            if (isAlly(playerData)) {
                allies.add(playerData);
            }
        }
        return allies;
    }

    public Collection<SomEntity> alliesWithMe() {
        Collection<SomEntity> allies = allies();
        allies.add(this);
        return allies;
    }

    private boolean isAllyProcess(SomEntity entity) {
        if (this != entity) {
            if (entity instanceof PlayerData playerData) {
                if (!playerData.isPlayMode()) return false;
                if (PvPRaid.isInPvPRaid(this) && PvPRaid.isInPvPRaid(playerData)) {
                    return PvPRaid.getTeam(this) == PvPRaid.getTeam(playerData);
                } else if (isPvPMode()) {
                    if (hasParty()) {
                        return getParty() == playerData.getParty();
                    }
                    return !playerData.isPvPMode();
                } else return true;
            }
        }
        return false;
    }

    @Override
    public boolean isAlly(SomEntity entity) {
        if (!entity.isDeath()) {
            return isAllyProcess(entity);
        }
        return false;
    }

    @Override
    public Collection<SomEntity> alliesIsDeath() {
        Set<SomEntity> allies = new HashSet<>();
        for (PlayerData playerData : PlayerData.getPlayerList(getWorld())) {
            if (isAllyIsDeath(playerData)) {
                allies.add(playerData);
            }
        }
        return allies;
    }

    @Override
    public boolean isAllyIsDeath(SomEntity entity) {
        if (entity.isDeath()) {
            return isAllyProcess(entity);
        }
        return false;
    }

    public void reset() {
        player.setInvisible(false);
        player.setVisualFire(false);
        player.setGravity(true);
    }
}
