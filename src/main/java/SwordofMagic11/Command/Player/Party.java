package SwordofMagic11.Command.Player;

import SwordofMagic11.Command.SomCommand;
import SwordofMagic11.Command.SomTabComplete;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Component.SomText;
import SwordofMagic11.Player.ClassType;
import SwordofMagic11.Player.Menu.PlayerSelect;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Player.Setting.PlayerSetting;
import SwordofMagic11.Player.SomParty;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

import static SwordofMagic11.Component.Function.*;

public class Party implements SomCommand, SomTabComplete {

    private final  List<String> helpMsg = new ArrayList<>() {{
        add(decoText("Party Command"));
        add("§7・§e/party create <name>");
        add("§7・§e/party invite <player>");
        add("§7・§e/party accept");
        add("§7・§e/party leave");
        add("§7・§e/party kick <player>");
        add("§7・§e/party status");
        add("§7・§e/party list");
        add("§7・§e/party entryBoard [<text>]");
        add("§7・§e/party icon <id>");
        add("§7・§e/party lore <text>");
        add("§7・§e/party limit <number>");
        add("§7・§e/party bossRepeat");
    }};

    @Override
    public boolean PlayerCommand(Player player, PlayerData playerData, String[] args) {
        if (args.length >= 1) {
            switch (args[0].toLowerCase()) {
                case "create", "c" -> {
                    if (playerData.hasParty()) {
                        playerData.sendMessage("§cすでにパーティに参加しています", SomSound.Nope);
                        return true;
                    }
                    String name = args.length >= 2 ? args[1] : playerData.getName() + "のパーティ";
                    if (SomParty.partyList.containsKey(name)) {
                        playerData.sendMessage("§cすでに§e" + name + "§cは存在しています", SomSound.Nope);
                        return true;
                    }
                    new SomParty(name, playerData);
                }
                case "invite", "i" -> {
                    if (args.length >= 2) {
                        if (playerData.hasParty()) {
                            Player target = Bukkit.getPlayer(args[1]);
                            if (target != null && target.isOnline()) {
                                PlayerData targetData = PlayerData.get(target);
                                playerData.getParty().invitePlayer(playerData, targetData);
                            } else {
                                playerData.sendMessage("§cオフラインまたは存在しないプレイヤーです", SomSound.Nope);
                            }
                        } else {
                            playerData.sendNonJoinParty();
                        }
                    } else {
                        playerData.playerSelect().open(PlayerSelect.Action.PartyInvite);
                    }
                }
                case "accept", "a" -> SomParty.accept(playerData);
                case "leave", "l" -> {
                    if (playerData.hasParty()) {
                        SomParty party = playerData.getParty();
                        party.removeMember(playerData);
                    } else {
                        playerData.sendNonJoinParty();
                    }
                }
                case "kick", "k" -> {
                    if (args.length >= 2) {
                        if (playerData.hasParty()) {
                            SomParty party = playerData.getParty();
                            if (party.getLeader() == playerData) {
                                Player target = Bukkit.getPlayer(args[1]);
                                if (target != null && target.isOnline()) {
                                    PlayerData targetData = PlayerData.get(target);
                                    if (playerData.hasParty() && targetData.getParty() == party) {
                                        party.removeMember(targetData);
                                        party.sendMessage(playerData.getDisplayName() + "§aが§r" + targetData.getDisplayName() + "§aを§c脱退§aさせました", SomSound.Tick);
                                    }
                                } else {
                                    playerData.sendMessage("§cオフラインまたは存在しないプレイヤーです", SomSound.Nope);
                                }
                            } else {
                                playerData.sendNoPartyLeader();
                            }
                        } else {
                            playerData.sendNonJoinParty();
                        }
                    }
                }
                case "status" -> {
                    if (playerData.hasParty()) {
                        List<String> message = new ArrayList<>();
                        message.add(decoText("パーティーメンバー"));
                        SomParty party = playerData.getParty();
                        for (PlayerData memberData : party.getMember()) {
                            ClassType mainClass = memberData.classes().getMainClass();
                            message.add("§7・§r" + mainClass.getDecoNick() + "§r" + memberData.getName() + " §eLv" + memberData.getLevel());
                        }
                        playerData.sendMessage(message, SomSound.Tick);
                    } else {
                        playerData.sendNonJoinParty();
                    }
                }
                case "list" -> {
                    List<String> message = new ArrayList<>();
                    message.add(decoText("パーティーリスト"));
                    for (SomParty party : SomParty.partyList.values()) {
                        message.add("§7・§e" + party.getId() + " §a" + party.getMember().size() + "人");
                    }
                    playerData.sendMessage(message, SomSound.Tick);
                }
                case "entryboard", "eb" -> {
                    if (playerData.hasParty()) {
                        SomParty party = playerData.getParty();
                        if (party.getLeader() == playerData) {
                            if (args.length >= 2) party.setLore(args[1]);
                            if (party.hasLore()) {
                                party.setEntryBoard(!party.isEntryBoard());
                                party.sendMessage("§eパーティ§aが" + (party.isEntryBoard() ? "§b公開状態" : "§c非公開状態") + "§aなりました", SomSound.Tick);

                                if (party.isEntryBoard()) {
                                    for (PlayerData onlinePlayer : PlayerData.getPlayerList()) {
                                        if (onlinePlayer.setting().is(PlayerSetting.BooleanEnum.PartyPublicNotice)) {
                                            String text = "§e" + party.getId() + "§aが§bメンバー募集§aしています";
                                            SomText somText = SomText.create().addHover(text, party.viewText());
                                            onlinePlayer.sendMessage(somText);
                                        }
                                    }
                                }
                            } else {
                                playerData.sendMessage("§eパーティー説明文§aが設定されていません");
                            }
                        } else {
                            playerData.sendNoPartyLeader();
                        }
                    } else {
                        playerData.sendNonJoinParty();
                    }
                }
                case "icon" -> {
                    if (args.length >= 2) {
                        if (playerData.hasParty()) {
                            SomParty party = playerData.getParty();
                            if (party.getLeader() == playerData) {
                                try {
                                    Material icon = Material.valueOf(args[1].toUpperCase());
                                    party.setIcon(icon);
                                    party.sendMessage(playerData.getDisplayName() + "§aが§eパーティアイコン§aを§e" +  icon.name() + "§aにしました", SomSound.Tick);
                                } catch (Exception ignore) {}
                            } else {
                                playerData.sendNoPartyLeader();
                            }
                        } else {
                            playerData.sendNonJoinParty();
                        }
                    }
                }
                case "lore" -> {
                    if (args.length >= 2) {
                        if (playerData.hasParty()) {
                            SomParty party = playerData.getParty();
                            if (party.getLeader() == playerData) {
                                party.setLore(args[1]);
                                party.sendMessage(playerData.getDisplayName() + "§aが§eパーティ説明文§aを§b更新§aしました", SomSound.Tick);
                            } else {
                                playerData.sendNoPartyLeader();
                            }
                        } else {
                            playerData.sendNonJoinParty();
                        }
                    }
                }
                case "limit" -> {
                    if (args.length >= 2) {
                        if (playerData.hasParty()) {
                            SomParty party = playerData.getParty();
                            if (party.getLeader() == playerData) {
                                party.setLimit(Integer.parseInt(args[1]));
                                playerData.sendMessage("§eパーティ人数上限§aを§e" + party.getLimit() + "人§aしました", SomSound.Tick);
                            } else {
                                playerData.sendNoPartyLeader();
                            }
                        } else {
                            playerData.sendNonJoinParty();
                        }
                    }
                }
                case "bossrepeat", "br" -> {
                    if (playerData.hasParty()) {
                        SomParty party = playerData.getParty();
                        if (party.getLeader() == playerData) {
                            party.setBossRepeat(!party.isBossRepeat());
                            party.sendMessage("§eボス周回モード§aが§e" + boolText(party.isBossRepeat()) + "§aになりました", SomSound.Tick);
                        } else {
                            playerData.sendNoPartyLeader();
                        }
                    } else {
                        playerData.sendNonJoinParty();
                    }
                }
                default -> playerData.sendMessage(helpMsg, SomSound.Nope);
            }
        } else {
            playerData.sendMessage(helpMsg, SomSound.Nope);
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
        if (args.length >= 1) {
            if (!playerData.hasParty()) {
                complete.add("create");
            } else if (SomParty.hasInvite(playerData)) {
                complete.add("accept");
            } else {
                complete.add("invite");
                complete.add("leave");
            }
            if (args.length >= 2) {
                switch (args[0]) {
                    case "invite", "i" -> {
                        return SomTabComplete.getPlayerList();
                    }
                    case "icon" -> {
                        for (Material material : Material.values()) {
                            if (material.isItem()) {
                                complete.add(material.toString());
                            }
                        }
                    }
                }
            }
        }
        return complete;
    }

    @Override
    public List<String> TabComplete(CommandSender sender, Command command, String[] args) {
        return null;
    }
}
