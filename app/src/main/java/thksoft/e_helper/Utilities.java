/**
 * Created by TrNguyen on 29/01/2018.
 */
package thksoft.e_helper;

import android.util.Log;

public class Utilities {
    static String ExtractContent(String str) {
        try {
            if (str.contains("(") && str.contains(")")) {
                while (str.contains("(")) {
                    int idx = str.indexOf("(");
                    String tmp = str.substring(idx, str.indexOf(")", idx) + 1);
                    str = str.replace(tmp, "");
                }
            }
            if (Character.isDigit(str.charAt(0))) {
                str = str.substring(str.indexOf(" "));
            }
        } catch (Exception ex) {
            Log.d("EX", ex.toString());
        }
        return str.trim();
    }

    static boolean isNotNullNotEmpty(final String string) {
        return string != null && !string.isEmpty() && !string.trim().isEmpty();
    }
}
