package red.vuis.vbm.enigma;

public final class BooleanUtils {
    private BooleanUtils() {}

    public static boolean all(boolean... values) {
        for (boolean value : values) {
            if (!value) {
                return false;
            }
        }
        return true;
    }
    
    public static boolean any(boolean... values) {
        for (boolean value : values) {
            if (value) {
                return true;
            }
        }
        return false;
    }
}
