package SwordofMagic11.Player;

import SwordofMagic11.Component.SomTask;
import SwordofMagic11.Map.PvPRaid;
import SwordofMagic11.SomCore;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.persistence.PersistentDataType;

public class NamePlate {

    private final Player player;
    private final PlayerData playerData;
    private TextDisplay namePlate;
    private String nameText;
    private String belowText;

    public NamePlate(PlayerData playerData) {
        this.playerData = playerData;
        player = playerData.getPlayer();
    }

    private static final int size = 35;

    public void initNamePlate() {
        SomTask.sync(() -> {
            namePlate = (TextDisplay) playerData.getWorld().spawnEntity(player.getWorld().getSpawnLocation(), EntityType.TEXT_DISPLAY);
            namePlate.getPersistentDataContainer().set(SomCore.SomParticle, PersistentDataType.BOOLEAN, true);
            namePlate.setBillboard(Display.Billboard.VERTICAL);
            namePlate.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
            namePlate.setDisplayHeight(1.5f);
        });
    }

    public void updateTabList() {
        ClassType mainClass = playerData.classes().getMainClass();
        player.playerListName(Component.text(mainClass.getDecoNick() + " " + (playerData.isAFK() ? "§8" : "§f") + playerData.getName() + " §eLv" + playerData.getLevel()));
    }

    public void updateView() {
        if (namePlate != null && namePlate.isValid()) {
            if (PvPRaid.isInPvPRaid(playerData)) {
                PvPRaid.Team team = PvPRaid.getTeam(playerData);
                namePlate.text(Component.text(team.colorDisplay() + "\n" + playerData.classes().getMainClass().getColorDisplay() + "\n" + belowText));
                return;
            }
            if (!player.isSneaking()) {
                if (playerData.achievementMenu().hasSelectAchievement()) {
                    String text = playerData.achievementMenu().getSelectAchievementText();
                    namePlate.text(Component.text(text + "\n" + nameText + "\n" + belowText));
                } else {
                    namePlate.text(Component.text(nameText + "\n" + belowText));
                }
            } else {
                namePlate.text(Component.text(nameText));
            }
        }
    }

    public void update() {
        SomTask.async(() -> {
            if (playerData.isInvalid()) {
                if (namePlate != null) SomTask.sync(() -> namePlate.remove());
                return;
            }
            if (namePlate == null || !namePlate.isValid()) {
                initNamePlate();
            } else {
                if (namePlate.getVehicle() != player) {
                    SomTask.sync(() -> player.addPassenger(namePlate));
                }
                int hp = Math.max(0, (int) (playerData.healthPercent() * size));
                int hp2 = Math.max(0, (int) ((1 - playerData.healthPercent()) * size));
                int mp = Math.max(0, (int) (playerData.manaPercent() * size));
                int mp2 = Math.max(0, (int) ((1 - playerData.manaPercent()) * size));
                ClassType mainClass = playerData.classes().getMainClass();
                nameText = mainClass.getDecoNick() + " " + playerData.getColorDisplayName() + " §eLv" + playerData.getLevel();
                belowText = "§a" + "|".repeat(hp) + "§7" + "|".repeat(hp2) + "\n" +
                        (playerData.skillManager().isCasting() ? "§e" : "§b") + "|".repeat(mp) + "§7" + "|".repeat(mp2);
                updateView();
            }
        });
    }

    public void remove() {
        if (namePlate != null) {
            SomTask.sync(() -> namePlate.remove());
        }
    }
}
