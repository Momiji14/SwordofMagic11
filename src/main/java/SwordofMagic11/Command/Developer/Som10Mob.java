package SwordofMagic11.Command.Developer;

import SwordofMagic11.Command.SomCommand;
import SwordofMagic11.Command.SomTabComplete;
import SwordofMagic11.DataBase.DataBase;
import SwordofMagic11.DataBase.SkillDataLoader;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.StatusType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static SwordofMagic11.Component.Function.randomGet;

public class Som10Mob implements SomCommand, SomTabComplete {
    @Override
    public boolean PlayerCommand(Player player, PlayerData playerData, String[] args) {
        List<File> files = DataBase.dumpFile(new File(DataBase.Path, "MobData\\時歪"));
        for (File file : files) {
            FileConfiguration data = YamlConfiguration.loadConfiguration(file);
            List<String> materialDrop = new ArrayList<>();
            for (String drop : data.getStringList("Drop")) {
                String[] split = drop.split(",");
                materialDrop.add(split[0] + " " + split[2].split(":")[1] + " " + split[1].split(":")[1]);
            }
            data.set("MaterialDrop", materialDrop);
            data.set("Drop", null);
            if (!data.isSet("Memorial.Icon")) data.set("Memorial.Icon", "BARRIER");
            List<String> list = new ArrayList<>();
            List<StatusType> types = new ArrayList<>(List.of(StatusType.values()));
            types.remove(StatusType.ManaCost);
            types.remove(StatusType.Movement);
            for (int i = 0; i < 3; i++) {
                StatusType type = randomGet(types);
                list.add(type.toString());
                types.remove(type);
            }
            data.set("Memorial.List", list);
            try {
                data.save(file);
                playerData.sendMessage("Convert " + file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return true;
    }

    @Override
    public boolean Command(CommandSender sender, String[] args) {
        return false;
    }

    @Override
    public List<String> PlayerTabComplete(Player player, PlayerData playerData, Command command, String[] args) {
        return SkillDataLoader.getComplete();
    }

    @Override
    public List<String> TabComplete(CommandSender sender, Command command, String[] args) {
        return null;
    }
}
