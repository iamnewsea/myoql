package cn.dev8.util;

import lombok.var;

public class StringUtil {
    public static String fillWithPad(String text, int length, char pad) {
        if (text.length() >= length) return text;
        return text + getPadWithLength(length - text.length(), pad);
    }

    private static String getPadWithLength(int length, char pad) {
        var ret = new StringBuilder();
        for (var i = 0; i < length; i++) {
            ret.append(pad);
        }
        return ret.toString();
    }
}
