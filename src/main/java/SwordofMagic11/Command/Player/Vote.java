package SwordofMagic11.Command.Player;

import SwordofMagic11.Command.SomCommand;
import SwordofMagic11.Command.SomTabComplete;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Component.SomText;
import SwordofMagic11.Item.SyncItem;
import SwordofMagic11.Item.VoteBox;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Player.PlayerVote;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

import static SwordofMagic11.Component.Function.decoText;

public class Vote implements SomCommand, SomTabComplete {
    @Override
    public boolean PlayerCommand(Player player, PlayerData playerData, String[] args) {
        if (args.length >= 1) {
            switch (args[0].toLowerCase()) {
                case "url" -> {
                    String url = "https://mineportal.jp/servers/clrq6cwlw00001142qy0nufm3?voteUser=" + player.getName();
                    playerData.sendMessage(SomText.create("§eURL§7: §a").addOpenURL(url, "クリックで開く", url));
                    return true;
                }
                case "reward" -> {
                    int vote = PlayerVote.get(playerData.getName());
                    if (vote > 0) {
                        for (int i = 0; i < vote; i++) {
                            SyncItem.register(VoteBox.ID, playerData, SyncItem.State.ItemInventory);
                        }
                        PlayerVote.set(playerData.getName(), 0);
                        playerData.updateInventory();
                        playerData.sendMessage("§d投票報酬BOX§aを§e" + vote + "個§a受け取りました", SomSound.Tick);
                    } else {
                        playerData.sendMessage("§a残り§d投票報酬BOX§aは§e0個§aです", SomSound.Nope);
                    }
                    //playerData.sendMessage("§a現在未実装です。実装された場合、過去の投票数分の報酬を受け取れます", SomSound.Tick);
                    return true;
                }
            }
        }
        List<String> helpMsg = new ArrayList<>() {{
            add(decoText("Vote Command"));
            add("§7・§e/vote url");
            add("§7・§e/vote reward");
        }};
        playerData.sendMessage(helpMsg, SomSound.Nope);
        return true;
    }

    @Override
    public boolean Command(CommandSender sender, String[] args) {
        return false;
    }

    @Override
    public List<String> PlayerTabComplete(Player player, PlayerData playerData, Command command, String[] args) {
        List<String> complete = new ArrayList<>();
        if (args.length == 1) {
            complete.add("url");
            complete.add("reward");
        }
        return complete;
    }

    @Override
    public List<String> TabComplete(CommandSender sender, Command command, String[] args) {
        return null;
    }
}

