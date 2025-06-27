package SwordofMagic11.Command;

import SwordofMagic11.Command.Developer.*;
import SwordofMagic11.Command.Player.*;
import org.bukkit.Bukkit;

import static SwordofMagic11.SomCore.Log;

public class CommandRegister {

    public static void run() {
        register("Test", new Test());
        register("SomReload", new SomReload());
        register("TeleportPlayer", new TeleportPlayer());
        register("TeleportWorld", new TeleportWorld());
        register("CreateWorld", new CreateWorld());
        register("DeleteWorld", new DeleteWorld());
        register("LoadWorld", new LoadWorld());
        register("UnloadWorld", new UnloadWorld());
        register("CreateInstance", new CreateInstance());
        register("Cast", new Cast());
        register("MobClear", new MobClear());
        register("MobSpawn", new MobSpawn());
        register("CreateMaterial", new CreateMaterial());
        register("Load", new Load());
        register("Save", new Save());
        register("PlayMode", new PlayMode());
        register("GetItem", new GetItem());
        register("BuilderSet", new BuilderSet());
        register("BossBarMessage", new BossBarMessage());
        register("GetMaterial", new GetMaterial());
        register("GetCapsule", new GetCapsule());
        register("AddEld", new AddEld());
        register("GetMel", new GetMel());
        register("GetPet", new GetPet());
        register("ChangeClass", new ChangeClass());
        register("GetSkillPoint", new GetSkillPoint());
        register("DefenseBattleOpenToggle", new DefenseBattleOpenToggle());
        register("DefenseBattleStart", new DefenseBattleStart());
        register("PvPRaidOpenToggle", new PvPRaidOpenToggle());
        register("PvPRaidStartToggle", new PvPRaidStartToggle());
        register("ViewMaterialStorage", new ViewMaterialStorage());
        register("Som10Spawner", new Som10Spawner());
        register("Som10Mob", new Som10Mob());
        register("SetAchievement", new SetAchievement());
        register("Clean", new Clean());

        register("Menu", new Menu());
        register("Skill", new Skill());
        register("Attr", new Attr());
        register("Trash", new Trash());
        register("Camera", new Camera());
        register("BooleanSetting", new BooleanSetting());
        register("ValueSetting", new ValueSetting());
        register("Bind", new Bind());
        register("Sit", new Sit());
        register("PlayerInfo", new PlayerInfo());
        register("Party", new Party());
        register("Trade", new Trade());
        register("ReqExp", new ReqExp());
        register("Gathering", new Gathering());
        register("TriggerMenu", new TriggerMenu());
        register("EldShop", new EldShop());
        register("TotalMemorialStatus", new TotalMemorialStatus());
        register("ResetDPS", new ResetDPS());
        register("Mania", new Mania());
        register("SellMaterial", new SellMaterial());
        register("ViewItem", new ViewItem());
        register("Market", new Market());
        register("Lock", new Lock());
        register("SideBarToDo", new SideBarToDo());
        register("Vote", new Vote());
        register("Pet", new Pet());
        register("LightsOut", new LightsOut());
        register("PalletStorage", new PalletStorage());
        register("Unequip", new Unequip());
        register("BossTimeAttack", new BossTimeAttack());

        SomRestart.register();

        Log("§b[Som11]§aCommandRegister");
    }

    //コマンドの登録
    static void register(String command, SomCommand executor) {
        try {
            Bukkit.getPluginCommand(command).setExecutor(executor);
            if (executor instanceof SomTabComplete tabComplete) {
                Bukkit.getPluginCommand(command).setTabCompleter(tabComplete);
            } else {
                Bukkit.getPluginCommand(command).setTabCompleter(new DefaultComplete());
            }
        } catch (Exception e) {
            Log("§a[CommandManager]§cRegister Error -> " + command);
            throw new RuntimeException(e);
        }
    }
}
