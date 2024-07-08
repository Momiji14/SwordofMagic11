package SwordofMagic11.Command.Player;

import SwordofMagic11.Command.SomCommand;
import SwordofMagic11.Command.SomTabComplete;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Player.Gathering.GatheringMenu;
import SwordofMagic11.Player.PlayerData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

import static SwordofMagic11.Component.Function.decoText;

public class Gathering implements SomCommand, SomTabComplete {

    public void sendHelp(PlayerData playerData) {
        List<String> message = new ArrayList<>();
        message.add(decoText("Gathering Command"));
        message.add("§e/gathering <タイプ> <スキル> <ポイント>");
        playerData.sendMessage(message, SomSound.Nope);
    }

    @Override
    public boolean PlayerCommand(Player player, PlayerData playerData, String[] args) {
        try {
            switch (args.length) {
                case 0 -> playerData.gatheringMenu().open();
                case 3 -> {
                    GatheringMenu.Type type = GatheringMenu.Type.from(args[0]);
                    GatheringMenu.Skill skill = GatheringMenu.Skill.from(args[1]);
                    int point = Integer.parseUnsignedInt(args[2]);
                    playerData.gatheringMenu().apply(type, skill, point);
                    playerData.updateMainHand();
                    playerData.sendMessage("§e" + type.getDisplay() + "§aが§e" + playerData.gatheringMenu().getSkillLevel(type, skill) + "§aになりました");
                }
                default -> sendHelp(playerData);
            }
        } catch (Exception e) {
            sendHelp(playerData);
        }


        return true;
    }

    @Override
    public boolean Command(CommandSender sender, String[] args) {
        return false;
    }

    @Override
    public List<String> PlayerTabComplete(Player player, PlayerData playerData, Command command, String[] args) {
        List<String> complete = new ArrayList<>();
        try {
            switch (args.length) {
                case 1 -> {
                    return GatheringMenu.Type.complete();
                }
                case 2 -> {
                    GatheringMenu.Type type = GatheringMenu.Type.from(args[0]);
                    for (GatheringMenu.Skill skill : type.getSkill()) {
                        complete.add(skill.getDisplay());
                    }
                }
                case 3 -> {
                    GatheringMenu.Type type = GatheringMenu.Type.from(args[0]);
                    GatheringMenu.Skill skill = GatheringMenu.Skill.from(args[1]);
                    complete.add(String.valueOf(playerData.gatheringMenu().getSkillPoint(type) / skill.getReqPoint()));
                }
            }
        } catch (Exception e) {
            sendHelp(playerData);
        }
        return complete;
    }

    @Override
    public List<String> TabComplete(CommandSender sender, Command command, String[] args) {
        return null;
    }
}

