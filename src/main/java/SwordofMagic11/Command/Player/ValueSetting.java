package SwordofMagic11.Command.Player;

import SwordofMagic11.Command.SomCommand;
import SwordofMagic11.Command.SomTabComplete;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Player.Setting.PlayerSetting;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

import static SwordofMagic11.Component.Function.MinMax;

public class ValueSetting implements SomCommand, SomTabComplete {
    @Override
    public boolean PlayerCommand(Player player, PlayerData playerData, String[] args) {
        if (args.length == 2) {
            PlayerSetting setting = playerData.setting();
            PlayerSetting.DoubleEnum doubleEnum = PlayerSetting.DoubleEnum.valueOf(args[0]);
            try {
                setting.setDouble(doubleEnum, MinMax(Double.parseDouble(args[1]), doubleEnum.min(), doubleEnum.max()));
                return true;
            } catch (NumberFormatException ignore) {
                playerData.sendMessage("§c無効な値です");
            }
        }
        player.sendMessage("§e/valueSetting <setting> <value>");
        return true;
    }

    @Override
    public boolean Command(CommandSender sender, String[] args) {
        return false;
    }

    @Override
    public List<String> PlayerTabComplete(Player player, PlayerData playerData, Command command, String[] args) {
        List<String> complete = new ArrayList<>();
        switch (args.length) {
            case 1 -> {
                for (PlayerSetting.DoubleEnum doubleEnum : PlayerSetting.DoubleEnum.values()) {
                    complete.add(doubleEnum.toString());
                }
            }
            case 2 -> {
                PlayerSetting.DoubleEnum doubleEnum = PlayerSetting.DoubleEnum.valueOf(args[0]);;
                complete.add(doubleEnum.min() + "~" + doubleEnum.max());
            }
        }
        return complete;
    }

    @Override
    public List<String> TabComplete(CommandSender sender, Command command, String[] args) {
        return null;
    }
}

