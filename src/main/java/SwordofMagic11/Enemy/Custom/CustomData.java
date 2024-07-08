package SwordofMagic11.Enemy.Custom;

import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Enemy.EnemyData;
import SwordofMagic11.Enemy.MobData;
import SwordofMagic11.Player.PlayerData;
import org.bukkit.Location;

public abstract class CustomData extends EnemyData {
    public CustomData(MobData mobData, int level, Location location) {
        super(mobData, level, location, null, Rank.Normal);
    }

    public abstract void tick();

    public void sendMessage(String message) {
        for (PlayerData playerData : PlayerData.getPlayerList(getWorld())) {
            playerData.sendMessage(message);
        }
    }

    public void sendMessage(String message, SomSound sound) {
        for (PlayerData playerData : PlayerData.getPlayerList(getWorld())) {
            playerData.sendMessage(message, sound);
        }
    }

    public void playSound(SomSound sound) {
        for (PlayerData playerData : PlayerData.getPlayerList(getWorld())) {
            sound.play(playerData);
        }
    }
}
