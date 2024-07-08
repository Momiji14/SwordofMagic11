package SwordofMagic11.Command.Developer;

import SwordofMagic11.Command.SomCommand;
import SwordofMagic11.Component.SomSQL;
import SwordofMagic11.Component.SomTask;
import SwordofMagic11.DataBase.DataBase;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.SomCore;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class Test implements SomCommand {
    @Override
    public boolean PlayerCommand(Player player, PlayerData playerData, String[] args) {
        SomTask.async(() -> {
            long start = System.currentTimeMillis();
            int loop = Integer.parseInt(args[0]);
            for (int i = 0; i < loop; i++) {
                SomSQL.getSqlList(DataBase.Table.PlayerData, "UUID", playerData.getUUID(), "*");
            }
            playerData.sendMessage("Time: " + (System.currentTimeMillis()-start) + "ms");
        });
        return true;
    }

    @Override
    public boolean Command(CommandSender sender, String[] args) {
        return false;
    }
}
