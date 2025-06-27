package SwordofMagic11.Map;

import SwordofMagic11.Component.SomParticle;
import SwordofMagic11.Component.WorldManager;
import SwordofMagic11.Custom.UnsetLocation;
import SwordofMagic11.DataBase.MaterialDataLoader;
import SwordofMagic11.Item.Material.MaterialData;
import SwordofMagic11.SomCore;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class WarpGate {

    private String id;
    private UnsetLocation fromLocation;
    private UnsetLocation toLocation;
    private MapData toMap;
    private double radius;
    private final Set<World> worlds = new HashSet<>();
    private final Set<MaterialData> reqMaterial = new HashSet<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public UnsetLocation getFromLocation() {
        return fromLocation;
    }

    public void setFromLocation(UnsetLocation fromLocation) {
        this.fromLocation = fromLocation;
    }

    public UnsetLocation getToLocation() {
        return toLocation;
    }

    public void setToLocation(UnsetLocation toLocation) {
        this.toLocation = toLocation;
    }

    public MapData getToMap() {
        return toMap;
    }

    public void setToMap(MapData toMap) {
        this.toMap = toMap;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public void addWorld(World world) {
        worlds.add(world);
    }

    public void setReqMaterial(Collection<String> reqMaterial) {
        for (String material : reqMaterial) {
            this.reqMaterial.add(MaterialDataLoader.getMaterialData(material));
        }
    }

    public Set<MaterialData> getReqMaterial() {
        return reqMaterial;
    }

    public boolean hasReqMaterial() {
        return !reqMaterial.isEmpty();
    }

    public void start() {
        new BukkitRunnable() {
            double i = 0;
            final SomParticle particle = new SomParticle(getReqMaterial().isEmpty() ? Particle.WITCH : Particle.FIREWORK, null).setVectorUp().setSpeed(0.1f);

            @Override
            public void run() {
                worlds.removeIf(WorldManager::isUnload);
                Collection<Vector> locations = new HashSet<>();
                for (double j = 0; j < 2; j += 1 / radius) {
                    double x = Math.cos(i) * radius;
                    double z = Math.sin(i) * radius;
                    locations.add(new Vector(x, 0, z));
                    locations.add(new Vector(-x, 0, -z));
                    i += 0.1 / radius;
                }
                for (World world : worlds) {
                    if (world.getPlayerCount() > 0) {
                        particle.spawn(locations, fromLocation.as(world));
                    }
                }
            }
        }.runTaskTimerAsynchronously(SomCore.plugin(), 1, 1);
    }
}
