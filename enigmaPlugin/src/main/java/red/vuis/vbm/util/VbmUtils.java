package red.vuis.vbm.util;

public final class VbmUtils {
    private VbmUtils() {}

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

    public static String javaName(String rawName) {
        StringBuilder result = new StringBuilder(rawName.length());
        for (int i = 0; i < rawName.length(); i++) {
            char c = rawName.charAt(i);
            boolean alpha = Character.isAlphabetic(c);
            boolean digit = Character.isDigit(c);
            if (i == 0 && digit) {
                result.append('_');
            }
            if (alpha || digit) {
                result.append(Character.toUpperCase(c));
            } else {
                result.append('_');
            }
        }
        return result.toString();
    }
}
