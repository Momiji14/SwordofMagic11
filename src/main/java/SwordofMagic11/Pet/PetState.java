package SwordofMagic11.Pet;

public enum PetState {
    Cage("ケージ内"),
    Task("タスク中"),
    Summon("召喚中"),
    ;

    private final String display;

    PetState(String display) {
        this.display = display;
    }

    public String getDisplay() {
        return display;
    }
}
