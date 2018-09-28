package alchemystar.freedom.engine.net.proto.util;

/**
 * ArrayUtil
 * @Author lizhuyang
 */
public class ArrayUtil {
    public static boolean equals(String str1, String str2) {
        if (str1 == null) {
            return str2 == null;
        }
        return str1.equals(str2);
    }

    public static boolean contains(String[] list, String str) {
        if (list == null)
            return false;
        for (String string : list) {
            if (equals(str, string)) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     *
     * @param obj
     * @param seperator
     * @return
     */
    public static String join(Object[] obj, String seperator) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0, len = obj.length; i < len; i++) {
            sb.append(obj[i] == null ? "" : obj[i].toString());
            if (i < obj.length - 1) {
                sb.append(seperator);
            }
        }
        return sb.toString();
    }
}
