package SwordofMagic11.Command.Player;

import SwordofMagic11.Command.SomCommand;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.DataBase.MemorialDataLoader;
import SwordofMagic11.Player.Memorial.MemorialData;
import SwordofMagic11.Player.Memorial.MemorialMenu;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.StatusType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static SwordofMagic11.Component.Function.*;

public class TotalMemorialStatus implements SomCommand {
    @Override
    public boolean PlayerCommand(Player player, PlayerData playerData, String[] args) {
        MemorialMenu memorialMenu = playerData.memorialMenu();
        if (args.length == 0) {
            HashMap<StatusType, Double> status = memorialMenu.getMemorialStatus();
            List<String> message = new ArrayList<>();
            message.add(decoText("メモリアルステータス"));
            for (StatusType statusType : StatusType.values()) {
                if (status.containsKey(statusType)) {
                    message.add(decoLore(statusType.getDisplay()) + scale(status.get(statusType)*100, 3, true) + "%");
                }
            }
            playerData.sendMessage(message, SomSound.Tick);
        } else if (args[0].equalsIgnoreCase("all")) {
            HashMap<StatusType, Double> status = new HashMap<>();
            for (MemorialData memorialData : MemorialDataLoader.getMemorialDataList()) {
                memorialData.value(1).forEach((statusType, value) -> status.merge(statusType, value, Double::sum));
            }
            List<String> message = new ArrayList<>();
            message.add(decoText("メモリアルステータス"));
            for (StatusType statusType : StatusType.values()) {
                if (status.containsKey(statusType)) {
                    message.add(decoLore(statusType.getDisplay()) + scale(status.get(statusType)*100, 3, true) + "%");
                }
            }
            playerData.sendMessage(message, SomSound.Tick);
        }

        return true;
    }

    @Override
    public boolean Command(CommandSender sender, String[] args) {
        return false;
    }
}

