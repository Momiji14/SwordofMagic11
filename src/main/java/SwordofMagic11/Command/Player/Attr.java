package SwordofMagic11.Command.Player;

import SwordofMagic11.AttributeType;
import SwordofMagic11.Command.SomCommand;
import SwordofMagic11.Command.SomTabComplete;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Player.PlayerData;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

import static SwordofMagic11.Component.Function.decoText;

public class Attr implements SomCommand, SomTabComplete {

    public void sendHelp(PlayerData playerData) {
        List<String> message = new ArrayList<>();
        message.add(decoText("Attribute Command"));
        message.add("§e/attr <タイプ> <ポイント>");
        StringBuilder builder = new StringBuilder();
        for (AttributeType type : AttributeType.values()) {
            builder.append(" <").append(type.getDisplay()).append(">");
        }
        message.add("§e/attr " + builder);
        playerData.sendMessage(message, SomSound.Nope);
    }

    @Override
    public boolean PlayerCommand(Player player, PlayerData playerData, String[] args) {
        try {
            switch (args.length) {
                case 0 -> playerData.attributeMenu().open();
                case 2 -> {
                    AttributeType type = AttributeType.from(args[0]);
                    int usePoint = Integer.parseUnsignedInt(args[1]);
                    playerData.attributeMenu().apply(type, usePoint);
                    playerData.statusUpdate();
                    playerData.sendMessage("§e" + type.getDisplay() + "§aが§e" + playerData.getBaseAttribute(type) + "§aになりました");
                }
                case 6 -> {
                    int i = 0;
                    for (String text : args) {
                        int point = Integer.parseUnsignedInt(text);
                        if (point > 0) {
                            AttributeType type = AttributeType.values()[i];
                            playerData.attributeMenu().apply(type, point);
                            playerData.sendMessage("§e" + type.getDisplay() + "§aが§e" + playerData.getBaseAttribute(type) + "§aになりました");
                        }
                        i++;
                    }
                    playerData.statusUpdate();
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
                    return AttributeType.complete();
                }
                case 2 -> {
                    if (!StringUtils.isNumeric(args[0])) {
                        AttributeType attr = AttributeType.from(args[0]);
                        int current = playerData.getBaseAttribute(attr);
                        int point = Math.min(Math.max(playerData.getLevel()*2 - current, 50 - current), playerData.classes().getAttributePoint());
                        complete.add(String.valueOf(point));
                    }
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

