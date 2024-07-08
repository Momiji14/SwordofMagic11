package SwordofMagic11.Item.Material;

import SwordofMagic11.Component.SomRay;
import SwordofMagic11.Component.SomSound;
import SwordofMagic11.DataBase.MobDataLoader;
import SwordofMagic11.Enemy.EnemyData;
import SwordofMagic11.Pet.SyncPet;
import SwordofMagic11.Player.Memorial.MemorialData;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Player.Setting.PlayerSetting;
import SwordofMagic11.Skill.Process.Tame;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

import static SwordofMagic11.Component.Function.randomDouble;

public class PetCookie extends UseAbleMaterial {

    public static PetCookie PetCookie = new PetCookie();

    public PetCookie() {
        setId("ペットボーロ");
        setDisplay("ペットボーロ");
        setIcon(Material.HONEYCOMB);
        List<String> lore = new ArrayList<>();
        lore.add("§aペットに与えるとスタミナを回復します");
        lore.add("§aペットの育成する際にも使用します");
        lore.add("§a体力が10%未満のエネミーの与えると");
        lore.add("§a20%の確率でテイムすることが出来ます");
        setLore(lore);
        setRare(true);
        setSell(200);
    }

    @Override
    protected synchronized void useMaterial(PlayerData playerData) {
        if (playerData.hasMaterial(this, 1)) {
            Tame instance = (Tame) playerData.skillManager().instance("Tame");
            if (instance.tame(0.2)) {
                playerData.removeMaterial(this, 1);
            } else {
                playerData.sendMessage("テイム§aが実行されなかったため§r" + getDisplay() + "§aは消費されませんでした", SomSound.Tick);
            }
        } else {
            playerData.sendMessage(getDisplay() + "§aを所持していません", SomSound.Nope);
        }
    }
}
