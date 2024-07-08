package SwordofMagic11.Map;

import SwordofMagic11.Component.SomTask;
import SwordofMagic11.Custom.UnsetLocation;
import SwordofMagic11.SomCore;
import net.kyori.adventure.text.Component;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class NpcData {
    private String id;
    private String display;
    private UnsetLocation location;
    private Villager.Type type;
    private Villager.Profession profession;
    private List<String> param = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public UnsetLocation getLocation() {
        return location;
    }

    public void setLocation(UnsetLocation location) {
        this.location = location;
    }

    public Villager.Type getType() {
        return type;
    }

    public void setType(Villager.Type type) {
        this.type = type;
    }

    public Villager.Profession getProfession() {
        return profession;
    }

    public void setProfession(Villager.Profession profession) {
        this.profession = profession;
    }

    public List<String> getParam() {
        return param;
    }

    public void setParam(List<String> param) {
        this.param = param;
    }

    public void spawn(World world) {
        SomTask.syncDelay(() -> {
            Villager villager = (Villager) world.spawnEntity(location.as(world), EntityType.VILLAGER);
            villager.setProfession(profession);
            villager.setVillagerType(type);
            villager.setAI(false);
            villager.setInvulnerable(true);
            villager.customName(Component.text("§e" + display));
            villager.setCustomNameVisible(true);
            villager.setRemoveWhenFarAway(false);
            villager.setPersistent(false);
            villager.addScoreboardTag("NPC");
            PersistentDataContainer container = villager.getPersistentDataContainer();
            container.set(SomCore.SomNPCKey, PersistentDataType.BOOLEAN, true);
            for (String str : param) {
                String[] split = str.split(":");
                container.set(SomCore.Key(split[0]), PersistentDataType.STRING, split[1]);
            }
        }, 10);
    }
}
