package SwordofMagic11.Command.Player;

import SwordofMagic11.Command.SomCommand;
import SwordofMagic11.Command.SomTabComplete;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Component.SomTask;
import SwordofMagic11.DataBase.CapsuleDataLoader;
import SwordofMagic11.DataBase.MaterialDataLoader;
import SwordofMagic11.Item.CapsuleData;
import SwordofMagic11.Item.Material.MaterialData;
import SwordofMagic11.Item.SomItem;
import SwordofMagic11.Item.SyncItem;
import SwordofMagic11.Player.Market.MarketPlayer;
import SwordofMagic11.Player.Market.MarketSellItem;
import SwordofMagic11.Player.PlayerData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static SwordofMagic11.Component.Function.*;

public class Market implements SomCommand, SomTabComplete {

    public void sendHelp(PlayerData playerData) {
        List<String> message = new ArrayList<>();
        message.add(decoText("Market Command"));
        message.add("§e/market sellMaterial <素材ID> <個数> <単価>");
        message.add("§e/market orderMaterial <素材ID> <個数> <単価>");
        message.add("§e/market sellCapsule <カプセルID> <個数> <単価>");
        message.add("§e/market orderCapsule <カプセルID> <個数> <単価>");
        playerData.sendMessage(message, SomSound.Nope);
    }

    @Override
    public boolean PlayerCommand(Player player, PlayerData playerData, String[] args) {
        SomTask.async(() -> {
            if (!playerData.getMap().isCity()) {
                playerData.sendReqInCity();
                return;
            }
            if (args.length >= 3 && args[0].equalsIgnoreCase("sellItem")) {
                if (!playerData.marketPlayer().hasItemEmpty()) {
                    playerData.sendMessage("§c出品枠が足りません", SomSound.Nope);
                    return;
                }
                int mel = Integer.parseUnsignedInt(args[2]);
                if (mel < 1) mel = 1;
                String uuid = args[1].split(":")[0];
                SomItem somItem = SyncItem.getSomItem(uuid);
                if (playerData == somItem.getOwner()) {
                    if (Lock.check(playerData, uuid)) return;
                    if (somItem.getState() == SyncItem.State.ItemInventory) {
                        playerData.marketSell().productItem(somItem, mel);
                    } else {
                        playerData.sendMessage(somItem.getDisplay() + "§aがインベントリにありません", SomSound.Nope);
                    }
                }
                playerData.sendMessage(somItem.getDisplay() + "§aを所持していません", SomSound.Nope);
                return;
            } else if (args.length >= 4) {
                String id = args[1];
                if (Lock.check(playerData, id)) return;
                int amount;
                int mel = -1;
                try {
                    amount = Integer.parseUnsignedInt(args[2]);
                    if (args[3].equalsIgnoreCase("exists")) {
                        switch (args[0].toLowerCase()) {
                            case "sellmaterial" -> {
                                if (playerData.marketSellMaterial().has(id)) {
                                    mel = playerData.marketSellMaterial().getMel(id);
                                }
                            }
                            case "sellcapsule" -> {
                                if (playerData.marketSellCapsule().has(id)) {
                                    mel = playerData.marketSellCapsule().getMel(id);
                                }
                            }
                        }
                        if (mel == -1) {
                            playerData.sendMessage("すでに登録された§f" + id + "§aはありません", SomSound.Nope);
                            return;
                        }
                    } else {
                        mel = Integer.parseUnsignedInt(args[3]);
                    }
                } catch (Exception ignore) {
                    sendHelp(playerData);
                    return;
                }
                if (amount < 1) amount = 1;
                if (mel < 1) mel = 1;

                switch (args[0].toLowerCase()) {
                    case "sellmaterial" -> {
                        if (!playerData.marketPlayer().hasSellEmpty(id)) {
                            playerData.sendMessage("§c出品枠が足りません", SomSound.Nope);
                            return;
                        }
                        if (playerData.materialMenu().getStorage().containsKey(id)) {
                            if (playerData.marketSell().productMaterial(MaterialDataLoader.getMaterialData(id), mel, amount)) {
                                return;
                            }
                        }
                        playerData.sendMessage(id + "を所持していません", SomSound.Nope);
                        return;
                    }
                    case "sellcapsule" -> {
                        if (!playerData.marketPlayer().hasSellEmpty(id)) {
                            playerData.sendMessage("§c出品枠が足りません", SomSound.Nope);
                            return;
                        }
                        if (playerData.capsuleMenu().getStorage().containsKey(id)) {
                            if (playerData.marketSell().productCapsule(CapsuleDataLoader.getCapsuleData(id), mel, amount)) {
                                return;
                            }
                        }
                        playerData.sendMessage(id + "を所持していません", SomSound.Nope);
                        return;
                    }
                    case "ordermaterial" -> {
                        if (!playerData.marketPlayer().hasOrderEmpty(id)) {
                            playerData.sendMessage("§c注文枠が足りません", SomSound.Nope);
                            return;
                        }
                        if (checkAverage(id, mel, playerData)) return;
                        if (MaterialDataLoader.getComplete().contains(id)) {
                            int rawMel = ceil(safeMultiply(mel, amount));
                            int tax = ceil(rawMel * MarketPlayer.Tax);
                            int reqMel = rawMel + tax;
                            if (playerData.hasMel(reqMel)) {
                                MaterialData material = MaterialDataLoader.getMaterialData(id);
                                MarketPlayer.Pack pack;
                                if (playerData.marketOrderMaterial().has(id)) {
                                    pack = playerData.marketOrderMaterial().get(id);
                                    if (pack.mel() != mel) {
                                        playerData.sendMessage("§aすでに§e" + material.getDisplay() + "§aは§e単価" + mel + "§b注文§aされています\n§e単価§aを§e変更§aする際は§c過去§aの§b注文§aを取り下げてください", SomSound.Nope);
                                        return;
                                    } else {
                                        pack.addAmount(amount);
                                        playerData.sendMessage(material.getDisplay() + "x" + amount + "§aに§b更新§aしました", SomSound.Tick);
                                    }
                                } else {
                                    pack = new MarketPlayer.Pack(id, amount, mel);
                                    playerData.sendMessage(material.getDisplay() + "x" + amount + "§aを§e単価" + mel + "メル§aで§b注文§aしました", SomSound.Tick);
                                }
                                playerData.removeTax(rawMel, tax);
                                playerData.marketOrderMaterial().set(pack);
                                playerData.sendMessage("§e[買付金・注文手数料]§c-" + reqMel + "メル");
                            } else {
                                playerData.sendMessage("§e買付金・注文手数料§aが足りません §e[" + reqMel + "メル]", SomSound.Nope);
                            }
                            return;
                        }
                        playerData.sendMessage(id + "は存在していません", SomSound.Nope);
                        return;
                    }
                    case "ordercapsule" -> {
                        if (!playerData.marketPlayer().hasOrderEmpty(id)) {
                            playerData.sendMessage("§c注文枠が足りません", SomSound.Nope);
                            return;
                        }
                        if (checkAverage(id, mel, playerData)) return;
                        if (CapsuleDataLoader.getComplete().contains(id)) {
                            int rawMel = ceil(safeMultiply(mel, amount));
                            int tax = ceil(rawMel * MarketPlayer.Tax);
                            int reqMel = rawMel + tax;
                            if (playerData.hasMel(reqMel)) {
                                CapsuleData capsule = CapsuleDataLoader.getCapsuleData(id);
                                MarketPlayer.Pack pack;
                                if (playerData.marketOrderCapsule().has(id)) {
                                    pack = playerData.marketOrderCapsule().get(id);
                                    if (pack.mel() != mel) {
                                        playerData.sendMessage("§aすでに§e" + capsule.getDisplay() + "§aは§e単価" + mel + "§b注文§aされています\n§e単価§aを§e変更§aする際は§c過去§aの§b注文§aを取り下げてください", SomSound.Nope);
                                        return;
                                    } else {
                                        pack.addAmount(amount);
                                        playerData.sendMessage(capsule.getDisplay() + "x" + amount + "§aに§b更新§aしました", SomSound.Tick);
                                    }
                                } else {
                                    pack = new MarketPlayer.Pack(id, amount, mel);
                                    playerData.sendMessage(capsule.getDisplay() + "x" + amount + "§aを§e単価" + mel + "メル§aで§b注文§aしました", SomSound.Tick);
                                }
                                playerData.removeTax(rawMel, tax);
                                playerData.marketOrderCapsule().set(pack);
                                playerData.sendMessage("§e[買付金・注文手数料]§c-" + reqMel + "メル");
                            } else {
                                playerData.sendMessage("§e買付金・注文手数料§aが足りません §e[" + reqMel + "メル]", SomSound.Nope);
                            }
                            return;
                        }
                        playerData.sendMessage(id + "は存在していません", SomSound.Nope);
                        return;
                    }
                }
            }
            sendHelp(playerData);
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
                complete.add("sellMaterial");
                complete.add("sellCapsule");
                complete.add("sellItem");
                complete.add("orderMaterial");
                complete.add("orderCapsule");
            }
            case 2 -> {
                switch (args[0].toLowerCase()) {
                    case "sellmaterial", "ordermaterial" -> {
                        return new ArrayList<>(playerData.materialMenu().getStorage().keySet());
                    }
                    case "sellcapsule", "ordercapsule" -> {
                        return new ArrayList<>(playerData.capsuleMenu().getStorage().keySet());
                    }
                    case "sellitem" -> {
                        for (SomItem somItem : playerData.itemInventory().getList()) {
                            complete.add(Trade.itemCompleteText(somItem));
                        }
                    }
                }
            }
            case 3 -> {
                switch (args[0].toLowerCase()) {
                    case "sellmaterial" -> {
                        if (playerData.materialMenu().getStorage().containsKey(args[1])) {
                            return Collections.singletonList(String.valueOf(playerData.getMaterial(args[1])));
                        } else {
                            return Collections.singletonList("このアイテムを所持していません");
                        }
                    }
                    case "sellcapsule" -> {
                        if (playerData.capsuleMenu().getStorage().containsKey(args[1])) {
                            return Collections.singletonList(String.valueOf(playerData.getCapsule(args[1])));
                        } else {
                            return Collections.singletonList("このカプセルを所持していません");
                        }
                    }
                    case "sellitem" -> {
                        return Collections.singletonList("販売価格");
                    }
                    case "ordermaterial", "ordercapsule" -> {
                        return Collections.singletonList("個数");
                    }
                }
            }
            case 4 -> {
                switch (args[0].toLowerCase()) {
                    case "sellmaterial", "ordermaterial", "sellcapsule", "ordercapsule" -> complete.add("単価 (" + minAverage(args[1]) + "~" + maxAverage(args[1]));
                }
            }
        }
        return complete;
    }

    @Override
    public List<String> TabComplete(CommandSender sender, Command command, String[] args) {
        return null;
    }

    public static int minAverage(String id) {
        return ceil(MarketPlayer.getAverageMel(id) * 0.7);
    }

    public static int maxAverage(String id) {
        return ceil(MarketPlayer.getAverageMel(id) * 1.3);
    }

    public static boolean checkAverage(String id, int mel, PlayerData playerData) {
        if (MarketPlayer.hasAverage(id)) {
            int min = minAverage(id);
            int max = maxAverage(id);
            if (min > mel || mel > max) {
                playerData.sendMessage("§e平均取引価格§aの§e±30%§a内でしか取引できません §e(" + min + "~" + max + ")", SomSound.Nope);
                return true;
            }
        }
        return false;
    }

    public static boolean illegalCheck(PlayerData playerData, int reqMel) {
        if (reqMel <= 0) {
            playerData.sendMessage("§c不正な数値です", SomSound.Nope);
            return true;
        } else return false;
    }
}
