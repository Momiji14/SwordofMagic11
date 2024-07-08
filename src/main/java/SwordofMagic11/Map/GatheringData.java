package SwordofMagic11.Map;

import SwordofMagic11.DataBase.CapsuleDataLoader;
import SwordofMagic11.DataBase.MaterialDataLoader;
import SwordofMagic11.Item.CapsuleData;
import SwordofMagic11.Item.Material.MaterialData;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class GatheringData {

    private double reqPower;
    private final Set<Material> block = new HashSet<>();
    private final HashMap<MaterialData, Double> drop = new HashMap<>();
    private final HashMap<CapsuleData, Double> capsule = new HashMap<>();

    public double getReqPower() {
        return reqPower;
    }

    public void setReqPower(double reqPower) {
        this.reqPower = reqPower;
    }

    public Set<Material> getBlock() {
        return block;
    }

    public void addBlock(Material material) {
        block.add(material);
    }

    public HashMap<MaterialData, Double> getDrop() {
        return drop;
    }

    public void setDrop(String materialId, double percent) {
        drop.put(MaterialDataLoader.getMaterialData(materialId), percent);
    }

    public HashMap<CapsuleData, Double> getCapsuleDrop() {
        return capsule;
    }

    public void setCapsuleDrop(String capsuleId, double percent) {
        capsule.put(CapsuleDataLoader.getCapsuleData(capsuleId), percent);
    }
}
