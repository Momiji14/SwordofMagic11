package SwordofMagic11.Component;

import SwordofMagic11.Entity.SearchEntity;
import SwordofMagic11.Entity.SomEntity;
import SwordofMagic11.Player.PlayerData;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;

public enum SomSound {
    Hurt(Sound.ENTITY_PLAYER_HURT, 1f, 1f),
    Tick(Sound.BLOCK_LEVER_CLICK, 1f, 1f),
    Nope(Sound.BLOCK_NOTE_BLOCK_HARP, 1f, 1f),
    Level(Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f),
    Equip(Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f),
    Teleport(Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f),
    Heal(Sound.ENTITY_ENDER_EYE_DEATH, 1f, 1f),
    LowHeal(Sound.ENTITY_ENDER_EYE_DEATH, 1f, 0.5f),
    Hit(Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 1f),
    Glass(Sound.BLOCK_GLASS_BREAK, 1f, 1f),
    Lever(Sound.BLOCK_LEVER_CLICK, 1f, 1f),
    Strong(Sound.ENTITY_PLAYER_ATTACK_STRONG, 1f, 1f),

    Dagger(Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 1.5f),
    Sword(Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 1f),
    Rod(Sound.ENTITY_SNOW_GOLEM_SHOOT, 1f, 1f),
    Bow(Sound.ENTITY_ARROW_SHOOT, 1f, 1f),
    Mace(Sound.BLOCK_AMETHYST_BLOCK_HIT, 1f, 1f),
    Handgun(Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1f, 1f),

    Fire(Sound.ENTITY_BLAZE_SHOOT, 1f, 1f),
    Lava(Sound.ITEM_BUCKET_FILL_LAVA, 1f, 1f),
    Ice(Sound.BLOCK_SNOW_STEP, 1f, 1f),
    Drown(Sound.ENTITY_PLAYER_HURT_DROWN, 1f, 1f),
    Blade(Sound.BLOCK_ANVIL_PLACE, 1f, 2f),
    Horn(Sound.EVENT_RAID_HORN, 100f, 1f),
    Bell(Sound.BLOCK_BELL_USE, 1f, 1f),
    Push(Sound.ENTITY_WITHER_SHOOT, 1f, 1f),
    Explode(Sound.ENTITY_GENERIC_EXPLODE, 1f, 1f),
    Light(Sound.BLOCK_AMETHYST_BLOCK_BREAK, 1f, 1f),
    BossSpawn(Sound.ENTITY_WITHER_SPAWN, 1f, 1.0f),
    Winner(Sound.ITEM_GOAT_HORN_SOUND_1, 1f, 1.5f),
    ErrorOver(Sound.ITEM_GOAT_HORN_SOUND_7, 1f, 1.4f),

    BossWarn1(Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 1f),
    BossCharge1(Sound.ENTITY_WARDEN_SONIC_CHARGE, 1f, 1f),
    FrostiaAtk1(Sound.BLOCK_ANVIL_PLACE, 1f, 2f),
    FrostiaAtk2(Sound.ENTITY_GHAST_SCREAM, 1f, 0.6f),
    FrostiaRemoveDebuff(Sound.BLOCK_BREWING_STAND_BREW, 1f, 1.5f),
    BleassAtk1(Sound.ENTITY_WARDEN_EMERGE, 1f, 1.4f),
    BleassAtk2(Sound.ENTITY_WARDEN_DEATH, 1f, 0.5f);

    private final Sound sound;
    private final float volume;
    private final float pitch;

    SomSound(Sound sound, float volume, float pitch) {
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
    }

    public void play(SomEntity entity) {
        play(entity, entity.getLocation());
    }

    public void play(SomEntity entity, Location location) {
        if (entity instanceof PlayerData playerData) {
            play(playerData, location);
        }
    }

    public void play(PlayerData playerData) {
        play(playerData, playerData.getLocation());
    }

    public void play(PlayerData playerData, Location location) {
        play(playerData.getPlayer(), location);
    }

    public void play(Player player, Location location) {
        player.playSound(location, sound, SoundCategory.PLAYERS, volume, pitch);
    }

    public void play(Collection<SomEntity> players) {
        players.forEach(player -> play(player, player.getLocation()));
        ;
    }

    /**
     * 周囲にサウンドを再生します
     * ※範囲の初期値は16m
     *
     * @param entity 再生座標(SomEntity)
     */
    public void radius(SomEntity entity) {
        radius(entity.getLocation(), 16);
    }

    /**
     * 周囲にサウンドを再生します
     * ※範囲の初期値は16m
     *
     * @param location 再生座標
     */
    public void radius(Location location) {
        radius(location, 16);
    }

    /**
     * 周囲にサウンドを再生します
     *
     * @param location 再生座標
     * @param radius   再生範囲
     */
    public void radius(Location location, double radius) {
        for (PlayerData playerData : SearchEntity.nearPlayerNoAFK(location, radius)) {
            play(playerData, location);
        }
    }

    /**
     * 周囲にサウンドを再生します
     *
     * @param entity 再生座標(SomEntity)
     * @param radius 再生範囲
     */
    public void radius(SomEntity entity, double radius) {
        for (PlayerData playerData : SearchEntity.nearPlayerNoAFK(entity.getLocation(), radius)) {
            play(playerData, entity.getLocation());
        }
    }
}
