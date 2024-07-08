package SwordofMagic11;

import SwordofMagic11.Command.Developer.MobClear;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Component.SomTask;
import SwordofMagic11.Component.WorldManager;
import SwordofMagic11.Custom.UnsetLocation;
import SwordofMagic11.DataBase.MapDataLoader;
import SwordofMagic11.Enemy.EnemyData;
import SwordofMagic11.Entity.SomEntity;
import SwordofMagic11.Map.Gathering;
import SwordofMagic11.Map.MapData;
import SwordofMagic11.Player.Interact;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Map.PvPRaid;
import SwordofMagic11.Player.Setting.PlayerSetting;
import SwordofMagic11.Player.Statistics.Statistics;
import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

import java.util.HashSet;
import java.util.Set;

import static SwordofMagic11.SomCore.*;

public class Events implements Listener {

    @EventHandler
    public void onConnect(AsyncPlayerPreLoginEvent event) {
        if (SomCore.Restart) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, Component.text("§aサーバーは再起動中です"));
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        SomTask.syncDelay(() -> PlayerData.create(player).load(), 1);
        NameInvisible.addPlayer(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.get(player);
        PvPRaid.quit(playerData);
        if (playerData.hasPvPVictimTime()) {
            playerData.saveLastCity(true);
            playerData.deathInstantProcess(playerData.pvpAttacker());
        } else if (playerData.hasTimer("BossClear")) {
            playerData.deathInstantProcess(null);
        }
        SomTask.async(playerData::save);
        playerData.getNamePlate().remove();
        playerData.petMenu().getSummonList().forEach(pet -> {
            if (pet.isSummon()) {
                pet.getEntityIns().delete();
            }
        });
        if (playerData.hasParty()) {
            playerData.getParty().removeMember(playerData);
        }
        playerData.delete();
    }

    @EventHandler
    public void onJump(PlayerJumpEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.get(player);
        SomTask.async(() -> playerData.statistics().add(Statistics.IntEnum.JumpCount, 1));
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (SomEntity.is(entity)) {
            event.setCancelled(true);
            switch (event.getCause()) {
                case DROWNING -> {
                    if (entity instanceof Player player && player.isOnline()) {
                        PlayerData playerData = PlayerData.get(player);
                        playerData.removeHealth(playerData.getStatus(StatusType.MaxHealth)*0.025, SomCore.Cardinal);
                        playerData.hurt();
                    }
                }
                case CUSTOM -> event.setCancelled(true);
                case SUFFOCATION -> {
                    if (SomEntity.instance(entity) instanceof EnemyData enemyData) {
                        enemyData.delete();
                    }
                }
                case WORLD_BORDER -> {
                    if (entity instanceof Player player && player.isOnline()) {
                        PlayerData playerData = PlayerData.get(player);
                        playerData.death(SomCore.Cardinal);
                    } else {
                        entity.remove();
                    }
                }
            }
            if (event.getCause() == EntityDamageEvent.DamageCause.SUFFOCATION) {

            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            PlayerData playerData = PlayerData.get(player);
            if (playerData.isPlayMode()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPrePlayerAttack(PrePlayerAttackEntityEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.get(player);
        Entity entity = event.getAttacked();
        if (playerData.isPlayMode()) {
            event.setCancelled(true);
            if (SomEntity.is(entity)) {
                SomEntity victim = SomEntity.instance(entity);
                if (playerData.isEnemy(victim)) {
                    playerData.normalAttack(victim);
                }
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.get(player);
        Interact.interact(playerData, event);
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.get(player);
        Interact.interactEntity(playerData, event);
    }

    @EventHandler
    public void onOffhand(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.get(player);
        if (playerData.isPlayMode()) {
            event.setCancelled(true);
            playerData.strafe();
        }
    }

    @EventHandler
    public void onToolChange(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.get(player);
        if (playerData.isPlayMode()) Interact.toolChange(playerData, event);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.get(player);
        if (playerData.isPlayMode()) event.setCancelled(true);
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player) {
            PlayerData playerData = PlayerData.get(player);
            if (playerData.isPlayMode()) event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        PlayerData playerData = PlayerData.get(player);
        playerData.inventoryClick(event);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        PlayerData playerData = PlayerData.get(player);
        playerData.inventoryClose(event);
    }

    @EventHandler
    public void onSneakToggle(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.get(player);
        if (event.isSneaking()) {
            if (playerData.isPlayMode()) {
                if (playerData.getLocation().getPitch() <= -80) {
                    if (playerData.setting().is(PlayerSetting.BooleanEnum.SkyMenu)) {
                        playerData.userMenu().open();
                    }
                }
            }
        } else {
            if (!playerData.isDeath() && playerData.isActiveMap()) {
                if (!playerData.getMap().warpGateUse(playerData)) {
                    switch (player.getGameMode()) {
                        case SURVIVAL, ADVENTURE -> playerData.wallKick();
                    }
                }
            }
        }
    }

//    @EventHandler
//    public void onBlockPhysics(BlockPhysicsEvent event) {
//        event.setCancelled(true);
//    }

    Set<String> invalidCMD = new HashSet<>() {{
        add("kill @e");
        add("spawnpoint");
    }};

    @EventHandler
    public void onFlightToggle(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.get(player);
        if (playerData.isDeath()) {
            event.setCancelled(true);
            player.setFlying(true);
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        for (String cmd : invalidCMD) {
            if (event.getMessage().equalsIgnoreCase(cmd)) {
                event.setCancelled(true);
                Log("§c無効なコマンドです(" + event.getPlayer().getName() + ") -> " + event.getMessage());
            }
        }
    }

    @EventHandler
    public void onCommand(ServerCommandEvent event) {
        if (event.getSender() instanceof BlockCommandSender sender) {
            String id = WorldManager.ID(sender.getBlock().getWorld());
            if (MapDataLoader.getComplete().contains(id)) {
                MapData mapData = MapDataLoader.getMapData(id);
                if (mapData.isCommandBlock()) return;
            }
            event.setCancelled(true);
            UnsetLocation location = new UnsetLocation(sender.getBlock().getLocation());
            Log("§c無効なMapでCMDが実行されました -> " + id + "," + location);
        }
    }

    @EventHandler
    public void onFishing(PlayerFishEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.get(player);
        if (playerData.isPlayMode()) {
            playerData.fishing().trigger(event);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.get(player);
        if (playerData.isPlayMode()) {
            event.setCancelled(true);
        } else if (playerData.isInInstance()) {
            playerData.sendMessage("§cここはインスタンスです！建築しても反映されません！", SomSound.Nope);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.get(player);
        if (playerData.isPlayMode()) {
            event.setCancelled(true);
            SomTask.async(() -> Gathering.blockBreak(playerData, event.getBlock()));
        } else if (playerData.isInInstance()) {
            playerData.sendMessage("§cここはインスタンスです！建築しても反映されません！", SomSound.Nope);
        }
    }

    @EventHandler
    public void onBlockFadeEvent(BlockFadeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockFromTo(BlockFromToEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onTame(EntityTameEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        switch (event.getCause()) {
            case SPECTATE -> {
                event.setCancelled(true);
            }
            case COMMAND -> {
                for (Entity passenger : event.getPlayer().getPassengers()) {
                    event.getPlayer().removePassenger(passenger);
                }
            }
        }
    }

    @EventHandler
    public void onTarget(EntityTargetLivingEntityEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onLoadWorld(WorldLoadEvent event) {
        MobClear.clearForce(event.getWorld());
    }

    @EventHandler
    public void onUnloadWorld(WorldUnloadEvent event) {
        MobClear.clearForce(event.getWorld());
    }

    @EventHandler
    public void onUnloadChunk(ChunkUnloadEvent event) {
        MobClear.clear(event.getChunk());
    }

    @EventHandler
    public void onLoadChunk(ChunkLoadEvent event) {
        MobClear.clear(event.getChunk());
    }

    @EventHandler
    public void onEntityRemoveEvent(EntityRemoveFromWorldEvent event) {
        Entity entity = event.getEntity();
        if (SomEntity.is(entity)) {
            SomEntity somEntity = SomEntity.instance(entity);
            if (somEntity instanceof EnemyData enemyData) {
                enemyData.delete();
            }
        }
    }
}
