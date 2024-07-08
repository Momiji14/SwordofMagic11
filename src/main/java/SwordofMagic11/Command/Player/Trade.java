package SwordofMagic11.Command.Player;

import SwordofMagic11.Command.SomCommand;
import SwordofMagic11.Command.SomTabComplete;
import SwordofMagic11.Component.DiscordWebhook;
import SwordofMagic11.Component.Function;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Component.SomTask;
import SwordofMagic11.DataBase.CapsuleDataLoader;
import SwordofMagic11.DataBase.MaterialDataLoader;
import SwordofMagic11.Item.CapsuleData;
import SwordofMagic11.Item.Material.MaterialData;
import SwordofMagic11.Item.SomEquip;
import SwordofMagic11.Item.SomItem;
import SwordofMagic11.Item.SyncItem;
import SwordofMagic11.Pet.SomPet;
import SwordofMagic11.Pet.SyncPet;
import SwordofMagic11.Player.Classes;
import SwordofMagic11.Player.Market.MarketPlayer;
import SwordofMagic11.Player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.WeakHashMap;

import static SwordofMagic11.Component.Function.*;

public class Trade implements SomCommand, SomTabComplete {

    public static void log(PlayerData sender, PlayerData receiver, String log) {
        SomTask.async(() -> {
            try {
                DiscordWebhook discordWebhook = new DiscordWebhook("https://discord.com/api/webhooks/1191775779781091339/iRp4ceC9rOmaKZkpXWJE42W3_WLe6BOeNdoOziDmnETyLELZ4RWu1eyslVqxNMsBs5v9");
                discordWebhook.setContent("```[" + sender.getName() + " -> " + receiver.getName() + "] " + log + "```");
                discordWebhook.execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private final List<String> helpMsg = new ArrayList<>() {{
        add(decoText("Trade Command"));
        add("§7・§e/trade <player> sendItem <itemUUID:itemID>");
        add("§7・§e/trade <player> sendItem gui");
        add("§7・§e/trade <player> sendPet <petUUID:petID>");
        add("§7・§e/trade <player> sendPet gui");
        add("§7・§e/trade <player> sendMaterial <素材ID> <個数>");
        add("§7・§e/trade <player> sendCapsule <カプセルID> <個数>");
        add("§7・§e/trade <player> sendMel <メル>");
        add("§7・§e/trade <player> sendExp <経験値>");
    }};

    @Override
    public boolean PlayerCommand(Player player, PlayerData playerData, String[] args) {
        SomTask.async(() -> {
            if (playerData.isDeath()) {
                playerData.sendMessage("§c死亡しています", SomSound.Nope);
                return;
            }
            if (playerData.getMap().isRoom()) {
                playerData.sendMessage("§cボス戦中です", SomSound.Nope);
                return;
            }
            if (args.length >= 3) {
                Player target = Bukkit.getPlayer(args[0]);
                if (target != null && target.isOnline()) {
                    PlayerData targetData = PlayerData.get(target);
                    if (targetData != playerData) {
                        switch (args[1].toLowerCase()) {
                            case "sendmaterial" -> {
                                String material = args[2];
                                if (Lock.check(playerData, material)) return;
                                if (!playerData.materialMenu().hasEmpty(material)) {
                                    playerData.materialMenu().sendNonHasEmpty();
                                    return;
                                }
                                int amount = args.length >= 4 ? Integer.parseUnsignedInt(args[3]) : 1;
                                if (amount < 1) amount = 1;
                                if (MaterialDataLoader.getComplete().contains(material)) {
                                    tradeMaterial(playerData, targetData, material, amount);
                                }
                            }
                            case "sendcapsule" -> {
                                String capsule = args[2];
                                if (Lock.check(playerData, capsule)) return;
                                if (!playerData.capsuleMenu().hasEmpty(capsule)) {
                                    playerData.capsuleMenu().sendNonHasEmpty();
                                    return;
                                }
                                int amount = args.length >= 4 ? Integer.parseUnsignedInt(args[3]) : 1;
                                if (amount < 1) amount = 1;
                                if (CapsuleDataLoader.getComplete().contains(capsule)) {
                                    tradeCapsule(playerData, targetData, capsule, amount);
                                }
                            }
                            case "senditem" -> {
                                if (args[2].equals("gui")) {
                                    playerData.sendItem().open(targetData);
                                    return;
                                }
                                String uuid = args[2].split(":")[0];
                                if (Lock.check(playerData, uuid)) return;
                                if (SyncItem.hasSomItem(uuid)) {
                                    SomItem item = SyncItem.getSomItem(uuid);
                                    if (item.getOwner() == playerData) {
                                        if (item.getState() == SyncItem.State.ItemInventory) {
                                            item.setOwner(targetData);
                                            Lock.delete(playerData, uuid);
                                            playerData.sendMessage(targetData.getName() + "§aに§f" + item.getDisplay() + "§aを送りました", SomSound.Tick);
                                            targetData.sendMessage(playerData.getName() + "§aから§f" + item.getDisplay() + "§aが送られてきました", SomSound.Tick);
                                            playerData.updateInventory();
                                            targetData.updateInventory();
                                            Trade.log(playerData, targetData, "Item:" + item.getId() + ":" + item.getUUID());
                                        } else {
                                            playerData.sendMessage(item.getDisplay() + "§aがインベントリにありません", SomSound.Nope);
                                        }
                                    } else {
                                        playerData.sendMessage("§cあなたのアイテムではありません", SomSound.Nope);
                                    }
                                } else {
                                    playerData.sendMessage("§c存在しないアイテムです", SomSound.Nope);
                                }
                            }
                            case "sendpet" -> {
                                if (args[2].equals("gui")) {
                                    playerData.sendItem().open(targetData);
                                    return;
                                }
                                String uuid = args[2].split(":")[0];
                                if (Lock.check(playerData, uuid)) return;
                                if (SyncPet.hasSomPet(uuid)) {
                                    SomPet pet = SyncPet.getSomPet(uuid);
                                    if (pet.getOwner() == playerData) {
                                        pet.setOwner(targetData);
                                        playerData.sendMessage(targetData.getName() + "§aに§f" + pet.colorName() + "§aを送りました", SomSound.Tick);
                                        targetData.sendMessage(playerData.getName() + "§aから§f" + pet.colorName() + "§aが送られてきました", SomSound.Tick);
                                        Trade.log(playerData, targetData, "Pet:" + pet.getMobData().getId() + ":" + pet.getUUID());
                                    } else {
                                        playerData.sendMessage("§cあなたのペットではありません", SomSound.Nope);
                                    }
                                } else {
                                    playerData.sendMessage("§c存在しないペットです", SomSound.Nope);
                                }
                            }
                            case "sendmel" -> {
                                int mel = Integer.parseUnsignedInt(args[2]);
                                tradeMel(playerData, targetData, mel);
                            }
                            case "sendexp" -> {
                                if (playerData.getLevel() < Classes.MaxLevel) {
                                    playerData.sendMessage("§eLv" + Classes.MaxLevel + "§aのみ利用出来ます", SomSound.Nope);
                                    return;
                                }
                                double exp = Double.parseDouble(args[2]);
                                if (exp < 1) exp = 1;
                                if (playerData.classes().getExp() >= exp) {
                                    playerData.classes().removeExp(exp);
                                    targetData.classes().addExp(exp);
                                    playerData.sendMessage(targetData.getName() + "§aに§e" + exp + "EXP§a送りました", SomSound.Tick);
                                    targetData.sendMessage(playerData.getName() + "§aから§e" + exp + "EXP§aが送られてきました", SomSound.Tick);
                                    log(playerData, targetData, "Exp:" + exp);
                                } else {
                                    playerData.sendMessage("§e" + exp + "EXP§cを所持していません", SomSound.Nope);
                                }
                            }
                        }
                    } else {
                        playerData.sendNoTargetMe();
                    }
                } else {
                    playerData.sendMessage("§cオフラインまたは存在しないプレイヤーです", SomSound.Nope);
                }
            } else {
                playerData.sendMessage(helpMsg, SomSound.Nope);
            }
        });
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
                return SomTabComplete.getPlayerList();
            }
            case 2 -> {
                complete.add("sendItem");
                complete.add("sendPet");
                complete.add("sendMaterial");
                complete.add("sendCapsule");
                complete.add("sendMel");
                complete.add("sendExp");
            }
            case 3 -> {
                switch (args[1].toLowerCase()) {
                    case "sendmaterial" -> {
                        return new ArrayList<>(playerData.materialMenu().getStorage().keySet());
                    }
                    case "sendcapsule" -> {
                        return new ArrayList<>(playerData.capsuleMenu().getStorage().keySet());
                    }
                    case "senditem" -> {
                        complete.add("gui");
                        for (SomItem somItem : playerData.itemInventory().getList()) {
                            complete.add(itemCompleteText(somItem));
                        }
                    }
                    case "sendpet" -> {
                        complete.add("gui");
                        for (SomPet somPet : playerData.petMenu().getCageList()) {
                            complete.add(somPet.getUUID() + ":" + somPet.getName());
                        }
                    }
                    case "sendmel" -> complete.add(String.valueOf(playerData.getMel()));
                    case "sendexp" -> complete.add(String.valueOf(playerData.classes().getExp()));
                }
            }
            case 4 -> {
                switch (args[1].toLowerCase()) {
                    case "sendmaterial" -> complete.add(String.valueOf(playerData.getMaterial(args[2])));
                    case "sendcapsule" -> complete.add(String.valueOf(playerData.capsuleMenu().get(args[2])));
                }
            }
        }
        return complete;
    }

    @Override
    public List<String> TabComplete(CommandSender sender, Command command, String[] args) {
        return null;
    }

    public static String itemCompleteText(SomItem somItem) {
        String prefix = "";
        String suffix = "";
        if (somItem instanceof SomEquip somEquip) {
            if (somEquip.getTrance() > 0) prefix = Function.numberRoma(somEquip.getTrance());
            if (somEquip.getPlus() > 0) suffix = scale(somEquip.getPlus(), true);
        }
        return  somItem.getUUID() + ":" + prefix + somItem.getDisplay() + suffix;
    }

    public static int tax(MaterialData materialData, int amount) {
        return ceil(0.01 * amount);
//        int cost;
//        if (MarketPlayer.hasAverage(materialData.getId())) {
//            cost = MarketPlayer.getAverageAmount(materialData.getId());
//        } else {
//            cost = materialData.getSell();
//        }
//        return ceil(cost * 0.01);
    }

    public static void tradeMaterial(PlayerData sender, PlayerData target, String material, int amount) {
        MaterialData materialData = MaterialDataLoader.getMaterialData(material);
        int tax = tax(materialData, amount);
        if (sender.hasMel(tax)) {
            if (sender.hasMaterial(material, amount)) {
                sender.removeTax(tax);
                sender.removeMaterial(material, amount);
                target.addMaterial(material, amount);
                sender.sendMessage(target.getName() + "§aに§f" + material + "x" + amount + "§aを送りました", SomSound.Tick);
                target.sendMessage(sender.getName() + "§aから§f" + material + "x" + amount + "§aが送られてきました", SomSound.Tick);
                sender.sendMessage("§e[取引手数料]§c-" + tax + "メル");
                log(sender, target, "Material:" + material + "x" + amount);
            } else {
                sender.sendMessage("§f" + materialData.getDisplay() + "x" + amount + "§cを所持していません", SomSound.Nope);
            }
        } else {
            sender.sendMessage("§e取引手数料が足りません [" + tax + "メル]", SomSound.Nope);
        }
    }

    public static int tax(CapsuleData capsuleData, int amount) {
        return ceil(0.01 * amount);
//        int cost;
//        if (MarketPlayer.hasAverage(capsuleData.getId())) {
//            cost = MarketPlayer.getAverageAmount(capsuleData.getId());
//        } else {
//            cost = CapsuleData.Sell;
//        }
//        return ceil(cost * 0.01);
    }

    public static void tradeCapsule(PlayerData sender, PlayerData target, String capsule, int amount) {
        CapsuleData capsuleData = CapsuleDataLoader.getCapsuleData(capsule);
        int tax = tax(capsuleData, amount);
        if (sender.hasMel(tax)) {
            if (sender.capsuleMenu().has(capsule, amount)) {
                sender.removeTax(tax);
                sender.capsuleMenu().remove(capsule, amount);
                target.capsuleMenu().add(capsule, amount);
                sender.sendMessage(target.getName() + "§aに§f" + capsule + "x" + amount + "§aを送りました", SomSound.Tick);
                target.sendMessage(sender.getName() + "§aから§f" + capsule + "x" + amount + "§aが送られてきました", SomSound.Tick);
                sender.sendMessage("§e[取引手数料]§c-" + tax + "メル");
                log(sender, target, "Capsule:" + capsule + "x" + amount);
            } else {
                sender.sendMessage("§f" + capsuleData.getDisplay() + "x" + amount + "§cを所持していません", SomSound.Nope);
            }
        } else {
            sender.sendMessage("§e取引手数料が足りません [" + tax + "メル]", SomSound.Nope);
        }
    }

    public static void tradeMel(PlayerData playerData, PlayerData targetData, int mel) {
        if (mel >= 100) {
            int tax = ceil(mel * MarketPlayer.Tax);
            int reqMel = mel + tax;
            if (playerData.hasMel(mel)) {
                if (playerData.hasMel(reqMel)) {
                    playerData.removeTax(mel, tax);
                    targetData.addMel(mel);
                    playerData.sendMessage(targetData.getName() + "§aに§e" + mel + "メル§a送りました", SomSound.Tick);
                    targetData.sendMessage(playerData.getName() + "§aから§e" + mel + "メル§aが送られてきました", SomSound.Tick);
                    playerData.sendMessage("§e[取引手数料]§c-" + tax + "メル");
                    log(playerData, targetData, "Mel:" + mel);
                } else {
                    playerData.sendMessage("§e取引手数料が足りません [" + tax + "メル]", SomSound.Nope);
                }
            } else {
                playerData.sendMessage("§e" + mel + "メル§cを所持していません", SomSound.Nope);
            }
        } else {
            playerData.sendMessage("§e送金§aは§e100メル以上§aから可能です", SomSound.Nope);
        }
    }
}
