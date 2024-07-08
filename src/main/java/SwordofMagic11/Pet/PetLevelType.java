package SwordofMagic11.Pet;

public enum PetLevelType {
    Combat("戦闘"),
    Mining("採掘"),
    Collect("採集"),
    Fishing("漁獲"),
    ;

    private final String display;

    PetLevelType(String display) {
        this.display = display;
    }

    public String getDisplay() {
        return display;
    }
}
