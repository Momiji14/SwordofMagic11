package SwordofMagic11.Item.Material;

import SwordofMagic11.Component.SomParticle;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.Entity.SomEffect;
import SwordofMagic11.Player.PlayerData;
import org.bukkit.Color;
import org.bukkit.Particle;

public class PotionMaterial extends UseAbleMaterial {

    public static int MaxPotionLevel = 10;
    public static int[] HealValue = new int[]{150, 250, 400, 650, 1000, 1400, 1900, 2600, 3700, 4800};
    public static int[] BuffValue = new int[]{50, 100, 150, 210, 270, 330, 400, 470, 540, 620};
    public static int BuffTime = 300*20;
    public static String[] Suffix = new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "I", "J"};
    public static int PotionIndex(String index) {
        int i = 0;
        switch (index) {
            case "A" -> i = 1;
            case "B" -> i = 2;
            case "C" -> i = 3;
            case "D" -> i = 4;
            case "E" -> i = 5;
            case "F" -> i = 6;
            case "G" -> i = 7;
            case "H" -> i = 8;
            case "I" -> i = 9;
            case "J" -> i = 10;
        }
        return i;
    };

    private int level;


    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    @Override
    protected synchronized void useMaterial(PlayerData playerData) {
        if (playerData.hasMaterial(this, 1)) {
            if (!playerData.hasTimer("Potion")) {
                String[] split = getId().split("ポーション");
                int level = PotionIndex(split[1]) - 1;
                SomParticle particle = null;
                if (split[0].contains("回復")) {
                    playerData.addHealth(HealValue[level]);
                    particle = new SomParticle(Particle.VILLAGER_HAPPY, playerData);
                } else if (split[0].contains("マナ")) {
                    playerData.addMana(HealValue[level]);
                    particle = new SomParticle(Color.AQUA, playerData);
                } else if (split[0].contains("アターク")) {
                    SomEffect effect = new SomEffect("アタークポーション", getDisplay(), BuffTime, playerData);
                    effect.setStatusAttack(BuffValue[level]);
                    playerData.addEffect(effect);
                    particle = new SomParticle(Color.RED, playerData);
                } else if (split[0].contains("マモール")) {
                    SomEffect effect = new SomEffect("マモールポーション", getDisplay(), BuffTime, playerData);
                    effect.setStatusDefense(BuffValue[level]);
                    playerData.addEffect(effect);
                    particle = new SomParticle(Color.RED, playerData);
                }
                if (particle != null) {
                    particle.random(playerData.getHipsLocation());
                    playerData.removeMaterial(this, 1);
                    SomSound.Heal.play(playerData);
                    playerData.timer("Potion", 100);
                }
            } else {
                playerData.sendMessage("§cクールタイム中です", SomSound.Nope);
            }
        } else {
            playerData.sendMessage(getDisplay() + "§aを所持していません", SomSound.Nope);
        }
    }

}
