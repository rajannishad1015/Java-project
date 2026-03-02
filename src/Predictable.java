public interface Predictable {
    void predict();
    default boolean isValidNumber(String s) {
        try {
            return Double.parseDouble(s) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
