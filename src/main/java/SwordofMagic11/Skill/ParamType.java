package SwordofMagic11.Skill;

public enum ParamType {
    Value("倍率", 0, false, "%", 100),
    Value2("倍率", 0, false, "%", 100),
    ValueInt("固定値", 0, false, "", 1),
    Damage("ダメージ", 0, false, "%", 100),
    Damage2("ダメージ", 0, false, "%", 100),
    Damage3("ダメージ", 0, false, "%", 100),
    Heal("ヒール", 0, false, "%", 100),
    Hate("ヘイト", 0, false, "%", 100),
    Count("回数", 0, false, "回"),
    Interval("間隔", -1, false, "秒", 0.05),
    Time("時間", -1, false, "秒", 0.05),
    Range("射程", 1, false, "m"),
    Range2("射程", 1, false, "m"),
    Radius("半径", 1, false, "m"),
    Radius2("半径", 1, false, "m"),
    Angle("角度", 0, false, "°"),
    Width("範幅", 1, false, "m"),
    Percent("確率", 0, false, "%", 100),
    MicroPercent("確率", 2, false, "%", 100),
    ;

    private final String display;
    private final int scale;
    private final boolean prefix;
    private final String suffix;
    private final double viewMultiply;

    ParamType(String display, int scale, boolean prefix, String suffix, double viewMultiply) {
        this.display = display;
        this.scale = scale;
        this.prefix = prefix;
        this.suffix = suffix;
        this.viewMultiply = viewMultiply;
    }

    ParamType(String display, int scale, boolean prefix, String suffix) {
        this.display = display;
        this.scale = scale;
        this.prefix = prefix;
        this.suffix = suffix;
        viewMultiply = 1;
    }

    public String getDisplay() {
        return display;
    }

    public int getScale() {
        return scale;
    }

    public boolean isPrefix() {
        return prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public double getViewMultiply() {
        return viewMultiply;
    }
}
