package SwordofMagic11.Command.Player;

import SwordofMagic11.Command.SomCommand;
import SwordofMagic11.Command.SomTabComplete;
import SwordofMagic11.DataBase.SkillDataLoader;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Skill.SomSkill;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class Bind implements SomCommand, SomTabComplete {
    @Override
    public boolean PlayerCommand(Player player, PlayerData playerData, String[] args) {
        int slot = Integer.parseUnsignedInt(args[0]);
        SomSkill skill = playerData.skillManager().instance(args[1]);
        playerData.classes().setPallet(playerData.classes().getMainClass(), slot, skill);
        return true;
    }

    @Override
    public boolean Command(CommandSender sender, String[] args) {
        return false;
    }

    @Override
    public List<String> PlayerTabComplete(Player player, PlayerData playerData, Command command, String[] args) {
        switch (args.length) {
            case 1 -> {
                return Collections.singletonList("Slot");
            }
            case 2 -> {
                return SkillDataLoader.getComplete();
            }
        }
        return null;
    }

    @Override
    public List<String> TabComplete(CommandSender sender, Command command, String[] args) {
        return null;
    }
}
