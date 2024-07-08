package SwordofMagic11.Entity;

import SwordofMagic11.Skill.SkillData;
import SwordofMagic11.Skill.SomSkill;
import SwordofMagic11.StatusType;

import java.util.HashMap;
import java.util.function.Consumer;

public class SomEffect implements Cloneable {

    public static SomEffect EnemyEffect(String id) {
        return new SomEffect(id, id, Integer.MAX_VALUE, null);
    }

    private final String id;
    private final String display;
    private final SomEntity owner;
    private double priority = 0;
    private int time;
    private boolean log = true;
    private final HashMap<String, Double> valueDouble = new HashMap<>();
    private final HashMap<String, Integer> valueInt = new HashMap<>();
    private final HashMap<String, String> valueText = new HashMap<>();
    private final HashMap<StatusType, Double> status = new HashMap<>();
    private final HashMap<StatusType, Double> statusMultiply = new HashMap<>();

    /**
     * バフのインスタンスを作成
     *
     * @param id      ID
     * @param display 表示名
     * @param time    効果時間 (tick)
     * @param owner   付与者
     */
    public SomEffect(String id, String display, int time, SomEntity owner) {
        this.id = id;
        this.display = display;
        this.time = time;
        this.owner = owner;
    }

    /**
     * バフのインスタンスを作成
     *
     * @param skillData SkillDataからIDと表示名を取得
     * @param time      効果時間 (tick)
     * @param owner     付与者
     */
    public SomEffect(SkillData skillData, int time, SomEntity owner) {
        id = skillData.getId();
        display = skillData.getDisplay();
        this.time = time;
        this.owner = owner;
    }

    /**
     * バフのインスタンスを作成
     * ※このバフが同プレイヤーに複数つく可能性がある場合 setPriority で優先度を設定する
     *
     * @param skill SomSkillからIDと表示名・付与者を取得
     * @param time  効果時間 (tick)
     */
    public SomEffect(SomSkill skill, int time) {
        id = skill.getSkillData().getId();
        display = skill.getSkillData().getDisplay();
        this.time = time;
        this.owner = skill.getOwner();
    }

    public String getId() {
        return id;
    }

    public String getDisplay() {
        return display;
    }

    public SomEntity getOwner() {
        return owner;
    }

    public double getPriority() {
        return priority;
    }


    /**
     * 同IDのバフ付与時の優先度 (数値が大きいほうが優先)
     *
     * @param priority 優先度
     */
    public SomEffect setPriority(double priority) {
        this.priority = priority;
        return this;
    }

    public int getTime() {
        return time;
    }

    /**
     * バフの効果時間を設定
     *
     * @param time 効果時間 (tick)
     */
    public void setTime(int time) {
        this.time = time;
    }

    /**
     * バフの効果時間を延長
     *
     * @param time 延長時間 (tick)
     */
    public void addTime(int time) {
        this.time -= time;
    }

    /**
     * バフの効果時間を減少
     *
     * @param time 減少時間 (tick)
     */
    public void removeTime(int time) {
        this.time -= time;
    }

    public HashMap<StatusType, Double> getStatus() {
        return status;
    }

    /**
     * バフのステータスを設定 (固定値)
     *
     * @param statusType ステータスのタイプ
     * @param value      効果量
     */
    public void setStatus(StatusType statusType, double value) {
        status.put(statusType, value);
    }

    /**
     * バフの攻撃系ステータスを設定 (固定値)
     * ※ATK,MAT,SAT
     *
     * @param value 効果量
     */
    public void setStatusAttack(double value) {
        status.put(StatusType.ATK, value);
        status.put(StatusType.MAT, value);
        status.put(StatusType.SAT, value);
    }

    /**
     * バフの攻撃系ステータスを設定 (固定値)
     * ※DEF,MDF,SDF
     *
     * @param value 効果量
     */
    public void setStatusDefense(double value) {
        status.put(StatusType.DEF, value);
        status.put(StatusType.MDF, value);
        status.put(StatusType.SDF, value);
    }

    public HashMap<StatusType, Double> getStatusMultiply() {
        return statusMultiply;
    }

    /**
     * バフのステータスを設定 (倍率)
     *
     * @param statusType ステータスのタイプ
     * @param value      効果倍率
     */
    public void setStatusMultiply(StatusType statusType, double value) {
        statusMultiply.put(statusType, value);
    }

    /**
     * バフの攻撃系ステータスを設定 (倍率)
     * ※ATK,MAT,SAT
     *
     * @param value 効果倍率
     */
    public void setStatusMultiplyAttack(double value) {
        statusMultiply.put(StatusType.ATK, value);
        statusMultiply.put(StatusType.MAT, value);
        statusMultiply.put(StatusType.SAT, value);
    }

    /**
     * バフの攻撃系ステータスを設定 (倍率)
     * ※DEF,MDF,SDF
     *
     * @param value 効果倍率
     */
    public void setStatusMultiplyDefense(double value) {
        statusMultiply.put(StatusType.DEF, value);
        statusMultiply.put(StatusType.MDF, value);
        statusMultiply.put(StatusType.SDF, value);
    }

    public double getDouble(String id) {
        return valueDouble.getOrDefault(id, 0.0);
    }

    /**
     * 汎用の数値を設定
     *
     * @param value 数値
     */
    public void setDouble(String id, double value) {
        this.valueDouble.put(id, value);
    }

    public void addDouble(String id, double value) {
        this.valueDouble.merge(id, value, Double::sum);
    }

    public void removeDouble(String id, double value) {
        this.valueDouble.merge(id, -value, Double::sum);
    }

    public int getInt(String id) {
        return valueInt.getOrDefault(id, 0);
    }

    /**
     * 汎用の数値を設定
     *
     * @param value 数値
     */
    public void setInt(String id, int value) {
        this.valueInt.put(id, value);
    }

    public void addInt(String id, int value) {
        this.valueInt.merge(id, value, Integer::sum);
    }

    public void removeInt(String id, int value) {
        this.valueInt.merge(id, -value, Integer::sum);
    }

    public String getString(String id) {
        return valueText.getOrDefault(id, "");
    }


    private Consumer<SomEntity> applyRunnable;
    private Consumer<SomEntity> removeRunnable;
    /**
     * 汎用の値を設定
     *
     * @param value 数値
     */
    public void setString(String id, String value) {
        this.valueText.put(id, value);
    }

    public Consumer<SomEntity> getApplyRunnable() {
        return applyRunnable;
    }

    public boolean hasApplyRunnable() {
        return applyRunnable != null;
    }

    /**
     * バフ付与時にRunnableを実行します
     *
     * @param applyRunnable 付与時のRunnable
     */
    public void setApplyRunnable(Consumer<SomEntity> applyRunnable) {
        this.applyRunnable = applyRunnable;
    }

    public Consumer<SomEntity> getRemoveRunnable() {
        return removeRunnable;
    }

    public boolean hasRemoveRunnable() {
        return removeRunnable != null;
    }

    /**
     * バフ削除にRunnableを実行します
     *
     * @param removeRunnable 削除時のRunnable
     */
    public void setRemoveRunnable(Consumer<SomEntity> removeRunnable) {
        this.removeRunnable = removeRunnable;
    }

    public boolean isLog() {
        return log;
    }

    public void setLog(boolean log) {
        this.log = log;
    }

    public boolean isUpdate() {
        return !status.isEmpty() || !statusMultiply.isEmpty();
    }

    @Override
    public SomEffect clone() {
        try {
            return (SomEffect) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
