package SwordofMagic11.Component;

import SwordofMagic11.AttributeType;
import SwordofMagic11.Entity.Damage;
import SwordofMagic11.Player.PlayerData;
import SwordofMagic11.Skill.ParamType;
import SwordofMagic11.Skill.Parameter;
import SwordofMagic11.Skill.SkillData;
import SwordofMagic11.StatusType;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.util.Vector;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

public class Function {

    private static final Random random = new Random();
    private static final SecureRandom secureRandom = new SecureRandom();

    public static boolean randomBool() {
        return random.nextBoolean();
    }

    /**
     * ランダムな整数を取得
     * ※最低値以上、最大値'以下'
     *
     * @param min 最低値
     * @param max 最大値
     */
    public static int randomInt(int min, int max) {
        return random.nextInt(min, max + 1);
    }

    public static double randomDouble() {
        return random.nextDouble();
    }

    public static double randomDouble(double min, double max) {
        return random.nextDouble(min, max);
    }

    public static float randomFloat(float min, float max) {
        return random.nextFloat(min, max);
    }

    /**
     * 符号もランダムな倍精度浮動小数点数を取得
     *
     * @param min 最低値
     * @param max 最大値
     */
    public static double randomDoubleSign(double min, double max) {
        return (random.nextBoolean() ? -1 : 1) * random.nextDouble(min, max);
    }

    /**
     * 符号もランダムな倍精度浮動小数点数を取得
     * ※SecureRandomを使用
     *
     * @param min 最低値
     * @param max 最大値
     */
    public static double secureRandomDouble(double min, double max) {
        return secureRandom.nextDouble(min, max);
    }

    /**
     * Collectionのからランダムに取得
     *
     * @param collection 対象のCollection
     */
    public static <T> T randomGet(Collection<T> collection) {
        List<T> list = new ArrayList<>(collection);
        return list.get(random.nextInt(collection.size()));
    }

    public static double safeAdd(double x, double y) throws ArithmeticException {
        if (y > 0 ? x > Integer.MAX_VALUE - y : x < Integer.MIN_VALUE - y) {
            throw new ArithmeticException("Integer overflow");
        }
        return x + y;
    }

    public static double safeSubtract(double x, double y) throws ArithmeticException {
        if (y > 0 ? x < Integer.MIN_VALUE + y : x > Integer.MAX_VALUE + y) {
            throw new ArithmeticException("Integer overflow");
        }
        return x - y;
    }

    public static double safeMultiply(double x, double y) throws ArithmeticException {
        if (y > 0 ? x > Integer.MAX_VALUE/y || x < Integer.MIN_VALUE/y :
                (y < -1 ? x > Integer.MIN_VALUE/y || x < Integer.MAX_VALUE/y :
                        y == -1 && x == Integer.MIN_VALUE) ) {
            throw new ArithmeticException("Integer overflow");
        }
        return x * y;
    }

    public static double MinMax(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static int MinMax(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static int scrollUp(int scroll) {
        return Math.max(0, scroll - 1);
    }

    public static int scrollDown(int size, int row, int scroll) {
        return Math.max(0, Math.min(ceil(((double) size) / 8) - (row-1), scroll + 1));
    }

    public static int scrollDown(Inventory inventory, int scroll) {
        return inventory.getItem(inventory.getSize() - 2) != null ? scroll + 1 : scroll;
    }

    /**
     * アイテム名などに使うテキストデコレーション
     */
    public static String decoText(String str) {
        return decoText(str, 8);
    }

    /**
     * アイテム名などに使うテキストデコレーション
     *
     * @param flames フレームの数
     */
    public static String decoText(String str, int flames) {
        if (str == null) return "null";
        int flame = flames - Math.round(uncolored(str).length() / 1.5f);
        StringBuilder deco = new StringBuilder("===");
        deco.append("=".repeat(Math.max(0, flame)));
        return "§6" + deco + "§r " + colored(str, "§e") + "§r §6" + deco;
    }

    /**
     * アイテムのLoreなどに使うテキストデコレーション
     */
    public static String decoLore(String str) {
        return "§7・" + colored(str, "§e") + "§7: §a";
    }

    /**
     * アイテムのLoreの分割部などに使うテキストデコレーション
     */
    public static String decoSeparator(String str) {
        return decoText("§3" + str);
    }

    /**
     * アイテムのLoreの分割部などに使うテキストデコレーション
     *
     * @param flames フレームの数
     */
    public static String decoSeparator(String str, int flames) {
        return decoText("§3" + str, flames);
    }


    /**
     * テキストデコレーションの削除
     */
    public static String unDecoText(String str) {
        return str
                .replace("§6=", "")
                .replace("=", "")
                .replace("§r ", "");
    }

    public static String colored(String str, String def) {
        if (str.contains("&")) {
            return str.replace("&", "§");
        } else {
            return def.replace("&", "§") + str;
        }
    }

    /**
     * カラーコードを削除
     */
    public static String uncolored(String string) {
        return string
                .replace("§0", "")
                .replace("§1", "")
                .replace("§2", "")
                .replace("§3", "")
                .replace("§4", "")
                .replace("§5", "")
                .replace("§6", "")
                .replace("§7", "")
                .replace("§8", "")
                .replace("§9", "")
                .replace("§a", "")
                .replace("§b", "")
                .replace("§c", "")
                .replace("§d", "")
                .replace("§e", "")
                .replace("§f", "")
                .replace("§l", "")
                .replace("§m", "")
                .replace("§n", "")
                .replace("§k", "")
                .replace("§r", "")
                ;
    }

    /**
     * 単行分を改行し、先頭に`§a`をつける
     */
    public static List<String> loreText(String text) {
        return loreText(List.of(text.split("\n")));
    }

    /**
     * 格行の先頭に`§a`をつける
     * %ATK%などのステータス表示を置き換える
     */
    public static List<String> loreText(List<String> list) {
        List<String> lore = new ArrayList<>();
        for (String str : list) {
            for (StatusType value : StatusType.values()) {
                str = str.replace("%" + value.toString() + "%", "§e" + value.getDisplay() + "§a");
            }
            lore.add("§a" + str);
        }
        return lore;
    }

    /**
     * 格行の先頭に`§a`をつける
     * %Param%などのパラメーター表示を置き換える
     *
     * @param param 置き換え対象のParameter
     */
    public static List<String> loreText(List<String> list, Parameter param) {
        List<String> lore = new ArrayList<>();
        for (String str : list) {
            for (ParamType paramType : param.getParam().keySet()) {
                str = str.replace("%" + paramType + "%", "§e" + SkillData.paramText(param, paramType) + "§a");
            }
            for (AttributeType attr : AttributeType.values()) {
                str = str.replace("%" + attr.getNick() + "%", "§e" + attr.getDisplay() + "§a");
            }
            for (Damage.Type type : Damage.Type.values()) {
                str = str.replace("%" + type + "%", type.getDisplay() + "§a");
            }
            lore.add(str);
        }
        return loreText(lore);
    }

    public static int ceil(double value) {
        return (int) Math.ceil(value);
    }

    /**
     * 少数桁数を指定して数字を表示 (-1で有効桁数まで)
     *
     * @param index 指定桁
     */
    public static String scale(double value, int index) {
        return scale(value, index, false);
    }

    /**
     * Prefixを加えて数字を表示 (少数桁数は0)
     */
    public static String scale(double value, boolean prefix) {
        return scale(value, 0, prefix);
    }

    /**
     * 数字を表示 (少数桁数は0)
     */
    public static String scale(double value) {
        return scale(value, 0);
    }

    /**
     * Prefixを加え、少数桁数を指定して数字を表示 (-1で有効桁数まで)
     *
     * @param index 指定桁
     */
    public static String scale(double value, int index, boolean prefix) {
        String prefixText = "";
        String valueText = index > -1 ? String.format("%." + index + "f", value) : String.valueOf(scaleDouble(value));
        if (prefix) {
            if (value == 0) prefixText = "±";
            else if (value > 0) prefixText = "+";
        }
        return prefixText + valueText;
    }

    public static double scaleDouble(double value) {
        return scaleDouble(value, 2);
    }

    public static double scaleDouble(double value, int index) {
        return Double.parseDouble(scale(value, index));
    }

    /**
     * Vectorを角度に変換 (1Dof)
     */
    public static int angle(Vector vector) {
        return angle(new Vector(), vector);
    }

    /**
     * Vectorを角度に変換 (1Dof)
     */
    public static int angle(Vector vector, Vector vector2) {
        double angle = Math.atan2(vector.getZ() - vector2.getZ(), vector.getX() - vector2.getX());
        if (angle < 0) {
            angle += 2 * Math.PI;
        }
        return (int) Math.floor(angle * 360 / (2 * Math.PI));
    }

    /**
     * 2DofをVectorに変換
     *
     * @param yawData   yaw
     * @param pitchData pitch
     */
    public static Vector VectorFromYawPitch(double yawData, double pitchData) {
        double pitch = (pitchData + 90) * Math.PI / 180;
        double yaw = (yawData + 90) * Math.PI / 180;
        double x = Math.sin(pitch) * Math.cos(yaw);
        double y = Math.cos(pitch);
        double z = Math.sin(pitch) * Math.sin(yaw);
        return new Vector(x, y, z);
    }

    /**
     * 1DofをVectorに変換
     *
     * @param yawData yaw
     */
    public static Vector VectorFromYaw(double yawData) {
        double yaw = (yawData + 90) * Math.PI / 180;
        double x = Math.cos(yaw);
        double z = Math.sin(yaw);
        return new Vector(x, 0, z);
    }

    public static String boolText(boolean bool) {
        return bool ? "§b有効" : "§c無効";
    }

    public static void broadcast(String message) {
        for (PlayerData playerData : PlayerData.getPlayerList()) {
            playerData.sendMessage(message);
        }
    }

    public static void broadcast(String message, SomSound somSound) {
        for (PlayerData playerData : PlayerData.getPlayerList()) {
            playerData.sendMessage(message, somSound);
        }
    }

    public static String numberColor(int i) {
        if (i < 5) {
            return "§f";
        }
        if (i < 10) {
            return "§b";
        }
        if (i < 15) {
            return "§4";
        }
        if (i < 20) {
            return "§5";
        }
        return "§0";
    }

    public static String numberRoma(int i) {
        String text;
        switch (i) {
            case 0 -> text = "N";
            case 1 -> text = "I";
            case 2 -> text = "II";
            case 3 -> text = "III";
            case 4 -> text = "IV";
            case 5 -> text = "V";
            case 6 -> text = "VI";
            case 7 -> text = "VII";
            case 8 -> text = "VIII";
            case 9 -> text = "IX";
            case 10 -> text = "X";
            case 11 -> text = "XI";
            case 12 -> text = "XII";
            case 13 -> text = "XIII";
            case 14 -> text = "XIV";
            case 15 -> text = "XV";
            case 16 -> text = "XVI";
            case 17 -> text = "XVII";
            case 18 -> text = "XVIII";
            case 19 -> text = "XIX";
            case 20 -> text = "XX";
            default -> text = String.valueOf(i);
        }
        return text;
    }

    public static int clickMultiply(ClickType clickType) {
        int i;
        switch (clickType) {
            case RIGHT, DROP -> i = 10;
            case SHIFT_LEFT -> i = 100;
            case SHIFT_RIGHT -> i = 1000;
            default -> i = 1;
        }
        return i;
    }
}
