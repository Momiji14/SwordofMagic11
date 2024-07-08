package SwordofMagic11.Player;

import SwordofMagic11.Component.SomSQL;
import SwordofMagic11.Component.SomTask;
import SwordofMagic11.DataBase.DataBase;
import SwordofMagic11.SomCore;
import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerVote implements Listener {

    @EventHandler
    public void onVotifierEvent(VotifierEvent event) {
        Vote vote = event.getVote();
        PlayerVote.trigger(vote);
    }

    public static void trigger(Vote vote) {
        SomTask.async(() -> {
            String username = vote.getUsername();
            if (SomSQL.exists(DataBase.Table.PlayerData, "Username", username)) {
                SomCore.sendGlobalMessageComponent(Component.text(vote.getUsername() + "§aが§d投票§aしました §e/vote"));
            } else {
                SomCore.sendGlobalMessageComponent(Component.text("§c※" + vote.getUsername() + "の投票がありましたがログイン履歴がありません。記入ミス等の場合、自己責任のためご理解ください"));
            }
            add(username, 1);
        });
    }

    public static void set(String username, int vote) {
        SomSQL.setSql(DataBase.Table.PlayerVote, "Username", username, "Vote", vote);
    }

    public static void add(String username, int vote) {
        set(username, get(username) + vote);
        SomSQL.setSql(DataBase.Table.PlayerVote, "Username", username, "Total", total(username) + vote);
    }

    public static int get(String username) {
        if (SomSQL.exists(DataBase.Table.PlayerVote, "Username", username)) {
            return SomSQL.getInt(DataBase.Table.PlayerVote, "Username", username, "Vote");
        } else return 0;
    }

    public static int total(String username) {
        if (SomSQL.exists(DataBase.Table.PlayerVote, "Username", username)) {
            return SomSQL.getInt(DataBase.Table.PlayerVote, "Username", username, "Total");
        } else return 0;
    }
}
