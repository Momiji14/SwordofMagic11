package SwordofMagic11;

public interface TaskOwner {

    boolean isValid();

    default boolean isInvalid() {
        return !isValid();
    }

    class Instance implements TaskOwner {

        boolean valid = true;

        @Override
        public boolean isValid() {
            return valid;
        }

        public void valid() {
            this.valid = true;
        }

        public void invalid() {
            this.valid = false;
        }
    }
}
