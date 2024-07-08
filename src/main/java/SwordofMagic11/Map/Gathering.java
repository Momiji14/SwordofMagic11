package SwordofMagic11.Map;

import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Component.SomTask;
import SwordofMagic11.Component.WorldManager;
import SwordofMagic11.DataBase.MaterialDataLoader;
import SwordofMagic11.Item.CapsuleData;
import SwordofMagic11.Item.Material.MaterialData;
import SwordofMagic11.Item.SomEquip;
import SwordofMagic11.Player.Gathering.GatheringMenu;
import SwordofMagic11.Player.Memorial.MemorialData;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Player.Setting.PlayerSetting;
import SwordofMagic11.Player.Statistics.Statistics;
import SwordofMagic11.StatusType;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static SwordofMagic11.Component.Function.*;

public interface Gathering {
    int FishingMultiply = 5;
    int BaseMax = 3;

    HashMap<Material, Material> MiningBlock = new HashMap<>() {{
        put(Material.COAL_ORE, Material.COBBLESTONE);
        put(Material.COPPER_ORE, Material.COBBLESTONE);
        put(Material.IRON_ORE, Material.COBBLESTONE);
        put(Material.GOLD_ORE, Material.COBBLESTONE);
        put(Material.LAPIS_ORE, Material.COBBLESTONE);
        put(Material.REDSTONE_ORE, Material.COBBLESTONE);
        put(Material.DIAMOND_ORE, Material.COBBLESTONE);
        put(Material.EMERALD_ORE, Material.COBBLESTONE);
        put(Material.DEEPSLATE_COAL_ORE, Material.COBBLED_DEEPSLATE);
        put(Material.DEEPSLATE_COPPER_ORE, Material.COBBLED_DEEPSLATE);
        put(Material.DEEPSLATE_IRON_ORE, Material.COBBLED_DEEPSLATE);
        put(Material.DEEPSLATE_GOLD_ORE, Material.COBBLED_DEEPSLATE);
        put(Material.DEEPSLATE_REDSTONE_ORE, Material.COBBLED_DEEPSLATE);
        put(Material.DEEPSLATE_LAPIS_ORE, Material.COBBLED_DEEPSLATE);
        put(Material.DEEPSLATE_DIAMOND_ORE, Material.COBBLED_DEEPSLATE);
        put(Material.DEEPSLATE_EMERALD_ORE, Material.COBBLED_DEEPSLATE);
        put(Material.AMETHYST_CLUSTER, Material.AIR);
        put(Material.LARGE_AMETHYST_BUD, Material.AIR);
        put(Material.MEDIUM_AMETHYST_BUD, Material.AIR);
    }};

    Set<Material> CollectBlock = new HashSet<>() {{
        add(Material.POPPY);
        add(Material.DANDELION);
        add(Material.OXEYE_DAISY);
        add(Material.AZURE_BLUET);
        add(Material.CORNFLOWER);
        add(Material.TUBE_CORAL);
        add(Material.BLUE_ORCHID);
        add(Material.ALLIUM);
        add(Material.TUBE_CORAL_FAN);
        add(Material.DEAD_HORN_CORAL);
        add(Material.DEAD_TUBE_CORAL);
        add(Material.BUBBLE_CORAL);
        add(Material.BUBBLE_CORAL_FAN);
    }};

    static void blockBreak(PlayerData playerData, Block block) {
        if (playerData.hasEquip(SomEquip.Slot.MainHand)) {
            Player player = playerData.getPlayer();
            GatheringData gatheringData;
            Set<Material> blocks;
            GatheringMenu.Type type;
            Material replace;
            Statistics.IntEnum intEnum;
            SomEquip tool = playerData.getEquip(SomEquip.Slot.MainHand);
            switch (tool.getEquipCategory()) {
                case MiningTool -> {
                    gatheringData = playerData.getMap().getMiningData();
                    blocks = MiningBlock.keySet();
                    type = GatheringMenu.Type.Mining;
                    replace = MiningBlock.getOrDefault(block.getType(), Material.COBBLESTONE);
                    intEnum = Statistics.IntEnum.GatheringMiningCount;
                }
                case CollectTool -> {
                    gatheringData = playerData.getMap().getCollectData();
                    blocks = CollectBlock;
                    type = GatheringMenu.Type.Collect;
                    replace = Material.AIR;
                    intEnum = Statistics.IntEnum.GatheringCollectCount;
                    if (CollectBlock.contains(block.getType())) {
                        SomTask.sync(() -> player.setGameMode(GameMode.ADVENTURE));
                        SomTask.syncDelay(() -> {
                            if (!playerData.isDeath()) {
                                player.setGameMode(GameMode.SURVIVAL);
                            }
                        }, 15 - playerData.gatheringMenu().getSkillValueInt(GatheringMenu.Type.Collect, GatheringMenu.Skill.Efficiency));
                    }
                }
                default -> {
                    gatheringData = null;
                    blocks = null;
                    type = null;
                    replace = null;
                    intEnum = null;
                }
            }
            if (gatheringData != null) {
                Material blockType = block.getType();
                if (blocks.contains(blockType)) {
                    drop(playerData, intEnum, gatheringData, type, tool, block.getWorld());
                    if (WorldManager.isInstance(block.getWorld())) {
                        int tick = ceil(20*60 * playerData.gatheringMenu().getSkillValue(type, GatheringMenu.Skill.TimeSave));
                        SomTask.sync(() -> block.setType(replace));
                        SomTask.syncDelay(() -> {
                            block.setType(blockType);
                            if (block.getBlockData() instanceof Waterlogged waterlogged) {
                                waterlogged.setWaterlogged(false);
                                block.setBlockData(waterlogged);
                            }
                        }, tick);
                    }
                }
            }
        }
    }

    static void drop(PlayerData playerData, Statistics.IntEnum intEnum, GatheringData gatheringData, GatheringMenu.Type type, SomEquip tool, World world) {
        playerData.statistics().add(intEnum, 1);
        double addition = playerData.gatheringMenu().getSkillValue(type, GatheringMenu.Skill.Percent);
        double globalMultiply = WorldManager.totalMultiply(world) * playerData.booster().multiply();
        double multiply = (playerData.getStatus(StatusType.GatheringPower) / gatheringData.getReqPower()) * addition * globalMultiply;
        int typeMultiply = type == GatheringMenu.Type.Fishing ? FishingMultiply : 1;
        boolean nonDrop = true;
        if (multiply > 0.1) {
            for (Map.Entry<MaterialData, Double> entry : gatheringData.getDrop().entrySet()) {
                double percent = entry.getValue() * multiply * typeMultiply;
                double basePercent = percent;
                MaterialData material = entry.getKey();
                int amount = 0;
                while (percent > 1) {
                    amount++;
                    percent--;
                }
                if (randomDouble() < percent) {
                    amount++;
                }
                if (amount > 0) {
                    int maxUp = (int) (playerData.gatheringMenu().getSkillValue(type, GatheringMenu.Skill.MaxUp));
                    amount = Math.min(amount, (int) ((BaseMax + maxUp) * globalMultiply * typeMultiply));
                    MaterialData.drop(playerData, material, amount, basePercent);
                    nonDrop = false;
                }
            }

            double exp = gatheringData.getReqPower() * globalMultiply * typeMultiply;
            playerData.gatheringMenu().addExp(type, exp);
            for (int i = 0; i < typeMultiply; i++) {
                tool.chanceExp();
            }

            double grinderPercent = Math.min(0.025, 0.001 * multiply) * typeMultiply;
            if (randomDouble() < grinderPercent) {
                MaterialData grinder = MaterialDataLoader.getMaterialData("グラインダー");
                int amount = randomInt(1, ceil(gatheringData.getReqPower()*0.01));
                MaterialData.drop(playerData, grinder, amount, grinderPercent);
                nonDrop = false;
            }
            MemorialData memorial = playerData.getMap().getMemorial();
            if (memorial != null) {
                double memorialPercent = Math.min(0.0025, 0.00025 * multiply) * typeMultiply;
                if (randomDouble() < memorialPercent) {
                    MaterialData memorialMaterial = MaterialDataLoader.getMaterialData(memorial.getId() + "Memorial");
                    MaterialData.drop(playerData, memorialMaterial, 1, memorialPercent);
                    nonDrop = false;
                }
            }
            for (Map.Entry<CapsuleData, Double> entry : gatheringData.getCapsuleDrop().entrySet()) {
                CapsuleData capsuleData = entry.getKey();
                double percent = Math.min(0.1, entry.getValue() * multiply) * typeMultiply;
                CapsuleData.drop(playerData, capsuleData, percent);
            }
            boolean boolLog = playerData.setting().is(PlayerSetting.BooleanEnum.MaterialLog) || playerData.setting().is(PlayerSetting.BooleanEnum.CapsuleLog);
            if (nonDrop && boolLog) playerData.sendMessage("§cなにも得られなかった...", SomSound.Nope);
        } else {
            playerData.sendMessage("§eギャザリング力§aが足りません", SomSound.Nope);
        }
    }
}
