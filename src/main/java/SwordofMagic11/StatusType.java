package SwordofMagic11;

public enum StatusType {
    MaxHealth("最大体力"),
    HealthRegen("体力回復"),
    MaxMana("最大マナ"),
    ManaRegen("マナ回復"),
    ATK("攻撃力"),
    MAT("魔法力"),
    SAT("射撃力"),
    DEF("防御力"),
    MDF("魔防力"),
    SDF("射防力"),
    SPT("支援力"),
    CriticalRate("クリティカル発生"),
    CriticalDamage("クリティカル威力"),
    CriticalResist("クリティカル耐性"),
    GatheringPower("ギャザリング力"),
    ManaCost("消費マナ"),
    Movement("移動速度"),
    Penetration("防御貫通率"),
    ;

    private final String display;

    StatusType(String display) {
        this.display = display;
    }

    public String getDisplay() {
        return display;
    }
}
