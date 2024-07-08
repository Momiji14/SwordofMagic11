package SwordofMagic11.Player.Achievement.Custom;

import SwordofMagic11.Player.PlayerData;

import java.util.function.Predicate;

public class Sample implements Predicate<PlayerData> {

    @Override
    public boolean test(PlayerData playerData) {
        return playerData.getName().equals("MomiNeko");
    }
}
