package SwordofMagic11.Command.Player;

import SwordofMagic11.Command.SomCommand;
import SwordofMagic11.Command.SomTabComplete;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Component.SomTask;
import SwordofMagic11.DataBase.CapsuleDataLoader;
import SwordofMagic11.DataBase.MaterialDataLoader;
import SwordofMagic11.DataBase.MobDataLoader;
import SwordofMagic11.Item.CapsuleData;
import SwordofMagic11.Item.Material.MaterialData;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Player.PlayerSideBar;
import SwordofMagic11.Player.Statistics.Statistics;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

import static SwordofMagic11.Component.Function.decoText;

public class SideBarToDo implements SomCommand, SomTabComplete {

    private void sendHelp(PlayerData playerData) {
        List<String> message = new ArrayList<>() {{
            add(decoText("SideBarToDo Command"));
            add("§7・§e/sideBarToDo show");
            add("§7・§e/sideBarToDo hide");
            add("§7・§e/sideBarToDo totalMel");
            add("§7・§e/sideBarToDo amountMaterial <素材ID> [<目標個数>]");
            add("§7・§e/sideBarToDo amountCapsule <カプセルID> [<目標個数>]");
            add("§7・§e/sideBarToDo enemyKill <MobID>");
            add("§7・§e/sideBarToDo intEnum <intEnum>");
            add("§7・§e/sideBarToDo partyInfo");
            add("§7・§e/sideBarToDo buffInfo");
            add("§7・§e/sideBarToDo gatheringInfo");
            add("§7・§e/sideBarToDo equipExp");
        }};
        playerData.sendMessage(message, SomSound.Nope);
    }

    @Override
    public boolean PlayerCommand(Player player, PlayerData playerData, String[] args) {
        SomTask.async(() -> {
            PlayerSideBar sideBar = playerData.sideBar();
            if (args.length == 1) {
                boolean bool = true;
                switch (args[0].toLowerCase()) {
                    case "show" -> sideBar.show();
                    case "hide" -> sideBar.hide();
                    case "totalmel" -> sideBar.toggleTotalMel();
                    case "partyinfo" -> sideBar.togglePartyInfo();
                    case "buffinfo" -> sideBar.toggleBuffInfo();
                    case "gatheringinfo" -> sideBar.toggleGatheringInfo();
                    case "equipexp" -> sideBar.toggleEquipExp();
                    default -> bool = false;
                }
                if (bool) return;
            } else if (args.length >= 2) {
                String id = args[1];
                Integer goal = args.length >= 3 ? Integer.parseUnsignedInt(args[2]) : null;
                switch (args[0].toLowerCase()) {
                    case "amountmaterial" -> {
                        if (MaterialDataLoader.getComplete().contains(id)) {
                            MaterialData material = MaterialDataLoader.getMaterialData(id);
                            sideBar.amountMaterial(material, goal);
                            return;
                        }
                    }
                    case "amountcapsule" -> {
                        if (CapsuleDataLoader.getComplete().contains(id)) {
                            CapsuleData capsule = CapsuleDataLoader.getCapsuleData(id);
                            sideBar.amountCapsule(capsule, goal);
                            return;
                        }
                    }
                    case "enemykill" -> {
                        if (MobDataLoader.getComplete().contains(id)) {
                            sideBar.enemyKill(id);
                            return;
                        }
                    }
                    case "intenum" -> {
                        try {
                            sideBar.intEnum(Statistics.IntEnum.valueOf(id));
                            return;
                        } catch (Exception ignore) {}
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
                complete.add("show");
                complete.add("hide");
                complete.add("totalMel");
                complete.add("partyInfo");
                complete.add("buffInfo");
                complete.add("gatheringinfo");
                complete.add("equipexp");

                complete.add("amountMaterial");
                complete.add("amountCapsule");
                complete.add("enemyKill");
                complete.add("intEnum");
            }
            case 2 -> {
                switch (args[0].toLowerCase()) {
                    case "amountmaterial" -> complete.addAll(playerData.materialMenu().getStorage().keySet());
                    case "amountcapsule" -> complete.addAll(playerData.capsuleMenu().getStorage().keySet());
                    case "enemykill" -> complete.addAll(MobDataLoader.getComplete());
                    case "intenum" -> {
                        for (Statistics.IntEnum intEnum : Statistics.IntEnum.values()) {
                            complete.add(intEnum.toString());
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
