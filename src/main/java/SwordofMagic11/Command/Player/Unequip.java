package SwordofMagic11.Command.Player;

import SwordofMagic11.Command.SomCommand;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Component.SomTask;
import SwordofMagic11.Item.SomEquip;
import SwordofMagic11.Item.SomItem;
import SwordofMagic11.Item.SyncItem;
import SwordofMagic11.Pet.SomPet;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Player.Statistics.Statistics;
import SwordofMagic11.SomCore;
import SwordofMagic11.TaskOwner;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

public class Unequip implements SomCommand {


    @Override
    public boolean PlayerCommand(Player player, PlayerData playerData, String[] args) {
        for (SomItem item : SyncItem.getList(playerData)) {
            if (item.getState() == SyncItem.State.Equipped) {
                item.setState(SyncItem.State.ItemInventory);
            }
        }
        for (SomEquip.Slot slot : SomEquip.Slot.values()) {
            playerData.setEquip(slot, null);
            for (SomPet somPet : playerData.petMenu().getAllList()) {
                somPet.setEquip(slot, null);
            }
        }
        playerData.sendMessage("§e装備解除§aを行いました", SomSound.Tick);
        return true;
    }

    @Override
    public boolean Command(CommandSender sender, String[] args) {
        return false;
    }
}

