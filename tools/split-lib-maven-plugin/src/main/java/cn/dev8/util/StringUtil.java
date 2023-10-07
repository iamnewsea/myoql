package cn.dev8.util;

import kotlin.text.StringsKt;
import lombok.var;

import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Yuxh
 */
public class StringUtil {
    /**
     * 是否是空串
     *
     * @param value
     * @return
     */
    public static boolean isBlank(String value) {
        if (value == null) {
            return true;
        }
        int strLen = value.length();
        if (strLen == 0) {
            return true;
        } else {
            for (int i = 0; i < strLen; ++i) {
                if (!Character.isWhitespace(value.charAt(i))) {
                    return false;
                }
            }

            return true;
        }
    }


    /**
     * 是否无值
     *
     * @param value
     * @return
     */
    public static boolean isNullOrEmpty(String value) {
        if (value == null) return true;
        if (value.isEmpty()) return true;
        if (value.trim().isEmpty()) return true;
        return false;
    }


    /**
     * 是否有值
     *
     * @param value
     * @return
     */
    public static boolean hasValue(String value) {
        return !StringUtil.isNullOrEmpty(value);
    }


    /**
     * 不区分大小的判断是否包含
     *
     * @param source
     * @param find
     * @return
     */
    public static boolean containsIgnoreCase(String source, String find) {
        return StringsKt.contains(source, find, true);
    }

    /**
     * 获取第一个有效字符串
     *
     * @param value
     * @return
     */
    public static String asString(Object... value) {
        var ret = Arrays.asList(value)
                .stream()
                .filter(it -> it != null)
                .map(it -> getReadableString(it))
                .filter(it -> !StringUtil.isNullOrEmpty(it))
                .findFirst();

        if (ret.isPresent() == false) {
            return "";
        }


        return ret.orElse("");
    }


    /**
     * 获取可读字符串
     *
     * @param value
     * @return
     */
    public static String getReadableString(Object value) {
        if (value == null) {
            return "";
        }

        if (value instanceof LocalDateTime) {
            LocalDateTime dateTime = (LocalDateTime) value;
            if (dateTime.isEqual(LocalDateTime.MIN)) {
                return "";
            }

            return DateTimeUtil.asString(dateTime);
        }

        if (value instanceof LocalDate) {
            LocalDate dateTime = (LocalDate) value;
            if (dateTime.isEqual(LocalDate.MIN)) {
                return "";
            }
            return DateTimeUtil.asString(dateTime);
        }

        if (value instanceof LocalTime) {
            LocalTime dateTime = (LocalTime) value;
            return dateTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        }

        if (value instanceof Date) {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(value);
        }

        return value.toString();
    }

    public static String replaceAll(String value, String oldValue, String newValue) {
        return replaceAll(value, oldValue, newValue, false);
    }

    /**
     * 替换字符串,摘抄自 kotlin
     *
     * @param oldValue
     * @param newValue
     * @return
     */
    public static String replaceAll(String value, String oldValue, String newValue, boolean ignoreCase) {
        return StringsKt.replace(value, oldValue, newValue, ignoreCase);
    }

    /**
     * 判断所有字符是否是小写
     *
     * @param value
     * @return
     */
    public static boolean allCharIsLowerCase(String value) {
        for (var it : value.toCharArray()) {
            if (!Character.isLowerCase(it)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断所有字符是否是大写
     *
     * @param value
     * @return
     */
    public static boolean allCharIsUpperCase(String value) {
        for (var it : value.toCharArray()) {
            if (!Character.isUpperCase(it)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 是否全大小，或全小写。 有任意字符则返回false
     */
    public static boolean allCharIsSameCase(String value) {
        if (value.length() <= 1) {
            return true;
        }
        if (Character.isUpperCase(value.charAt(0))) {
            return allCharIsUpperCase(value);
        }
        if (Character.isLowerCase(value.charAt(0))) {
            return allCharIsLowerCase(value);
        }
        return false;
    }


    /**
     * 分隔为各个部分
     */
    public static List<String> splitWordParts(String value) {
        var ret = Arrays.stream(value.split("[\\W_]+"))
                .map(it -> {
                    if (allCharIsSameCase(it)) {
                        return new String[]{it};
                    } else {
                        //连续大写，按一部分处理
                        var list = Arrays.asList(it.split("(?=[A-Z])"));
                        if (list.size() == 0) {
                            return new String[0];
                        }
                        //合并连续大写
                        var prevItem = list.get(0);
                        for (var i = 1; i < list.size(); i++) {
                            var item = list.get(i);

                            if (item.length() > 0 && Character.isUpperCase(item.charAt(0)) && allCharIsUpperCase(prevItem)) {
                                prevItem = prevItem + item;
                                list.add(i, prevItem);
                                list.set(i - 1, "");
                            }
                        }

                        return list.toArray(new String[0]);
                    }
                })
                .collect(Collectors.toList());


        return ListUtil.unwindArray(ret)
                .stream()
                .filter(it2 -> !it2.isEmpty())
                .collect(Collectors.toList());

    }

    /**
     * 大驼峰 ,仅保留字母，数字
     */

    public static String getBigCamelCase(String value) {

        return String.join("", splitWordParts(value)
                .stream()
                .map(it -> {
                    return Character.toUpperCase(it.charAt(0)) + it.substring(1).toLowerCase();
                })
                .collect(Collectors.toList())
        );
    }

    /**
     * 小驼峰
     */

    public static String getSmallCamelCase(String value) {
        var ret = getBigCamelCase(value);
        if (ret.isEmpty()) {
            return "";
        }
        return Character.toLowerCase(ret.charAt(0)) + ret.substring(1);
    }


    /**
     * 短横线格式，全小写
     */

    public static String getKebabCase(String value) {
        return String.join("-", splitWordParts(value)
                .stream()
                .map(it -> {
                    return it.toLowerCase();
                })
                .collect(Collectors.toList())
        );
    }

    /**
     * 下划线格式，全小写
     */

    public static String getUnderlineCase(String value) {
        return String.join("_", splitWordParts(value)
                .stream()
                .map(it -> {
                    return it.toLowerCase();
                })
                .collect(Collectors.toList())
        );
    }

    /**
     * 截取
     *
     * @param value
     * @param startIndex
     * @return
     */
    public static String slice(String value, int startIndex) {
        return slice(value, startIndex, Integer.MAX_VALUE);
    }

    /**
     * 截取
     *
     * @param value
     * @param startIndex
     * @param endIndex
     * @return
     */
    public static String slice(String value, int startIndex, int endIndex) {
        var startIndexValue = startIndex;

        if (startIndexValue <= 0) {
            startIndexValue = value.length() + (startIndexValue % value.length());
        }

        var endIndexValue = endIndex;
        if (endIndexValue <= 0) {
            endIndexValue = value.length() + (endIndexValue % value.length());
        }

        if (endIndexValue > value.length()) {
            endIndexValue = value.length();
        }
        return value.substring(startIndexValue, endIndexValue);
    }


    /**
     * 第一个值的索引
     *
     * @param value
     * @param fun
     * @return
     */
    public static int indexOfFirst(String value, Function<Character, Boolean> fun) {
        for (var i = 0; i < value.length(); i++) {
            if (fun.apply(value.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 字符串转换为日期时间类型
     *
     * @param strValue
     * @return
     */
    public static LocalDateTime convertToLocalDateTime(String strValue) {
        //关于 160空格：https://blog.csdn.net/lewky_liu/article/details/79353151
        //本函数不做处理。

        //如果是全数字.
        if (strValue.chars().anyMatch(it -> Character.isDigit(it) == false) == false) {
            if (strValue.length() == 8) {
                return LocalDate.parse(strValue, DateTimeFormatter.ofPattern("yyyyMMdd")).atStartOfDay();
            } else if (strValue.length() == 14) {
                return LocalDateTime.parse(strValue, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            } else if (strValue.length() == 17) {
                return LocalDateTime.parse(
                        slice(strValue, 0, -3) + "." + slice(strValue, -3),
                        DateTimeFormatter.ofPattern("yyyyMMddHHmmss[.SSS]")
                );
            }
        }

        // Fri, 04 Dec 2020 03:11:42 GMT
        // DateTimeFormatter.RFC_1123_DATE_TIME
        if (strValue.endsWith("GMT") && strValue.contains(",")) {
            return LocalDateTime.parse(strValue, DateTimeFormatter.RFC_1123_DATE_TIME);
        }

        var withZ = strValue.endsWith("Z");

        if (withZ) {
            strValue = slice(strValue, 0, -1);
        }

        //分成两部分。 找冒号前面找字母或空格 ,T, 'T'
        var timeSignIndex = strValue.indexOf(':');
        if (timeSignIndex < 0) {
            return convertToLocalDate(strValue).atStartOfDay();
        }

        var fenIndex = indexOfFirst(strValue.substring(0, timeSignIndex), it -> it == ' ' || Character.isLetter(it));
        if (fenIndex < 0) {
            throw new RuntimeException("不正确的时间格式:" + strValue);
        }

        var wrappeT = false;
        if (fenIndex > 1 && fenIndex < strValue.length() - 1) {
            if (Character.isDigit(strValue.charAt(1)) == false && strValue.charAt(fenIndex - 1) == strValue.charAt(fenIndex + 1)) {
                wrappeT = true;
            }
        }

        var datePartString = "";

        if (wrappeT) {
            datePartString = strValue.substring(0, fenIndex - 1);
            strValue = strValue.substring(fenIndex + 2);
        } else {
            datePartString = strValue.substring(0, fenIndex);
            strValue = strValue.substring(fenIndex + 1);
        }

        var timePartString = "";
        if (withZ) {
            timePartString = slice(strValue, 0, -1);
        } else {
            timePartString = strValue;
        }

        var zoneSecond = 0;
        if (withZ) {
            zoneSecond = ZoneId.systemDefault().getRules().getOffset(Instant.EPOCH).getTotalSeconds();
        }


        var timezoneValueMatch = Pattern.compile(".*([+|-])(\\d+\\:\\d+)$").matcher(timePartString);
        if (timezoneValueMatch.matches()) {
            var timezoneOffset = DateTimeUtil.getTotalSeconds(convertToLocalTime(timezoneValueMatch.group(2) + ":00"));

            if (timezoneValueMatch.group(1).equals("-")) {
                timezoneOffset = -timezoneOffset;
            }

            timePartString = timePartString.substring(0, timezoneValueMatch.start(1));


            zoneSecond = ZoneId.systemDefault().getRules().getOffset(Instant.EPOCH).getTotalSeconds() - timezoneOffset;
        }

        return convertToLocalDate(datePartString).atTime(convertToLocalTime(timePartString)).plusSeconds(zoneSecond);
    }


    /**
     * 字符串转换为日期类型
     *
     * @param strValue
     * @return
     */
    public static LocalDate convertToLocalDate(String strValue) {
        if (strValue.length() == 8 && !strValue.chars().anyMatch(it -> Character.isDigit(it) == false)) {
            var ret = LocalDate.parse(strValue, DateTimeFormatter.ofPattern("yyyyMMdd"));
            return ret;
        }

        var fen = strValue.charAt(4);

        if (fen != '-' && fen != '/' && fen != '_' && fen != '.') {
            return null;
        }

        var sects = strValue.split(fen + "");
        if (sects.length != 3) {
            throw new RuntimeException("不识别的日期格式: " + strValue);
        }

        var year = Integer.parseInt(sects[0]);
        var month = Integer.parseInt(sects[1]);
        var day = Integer.parseInt(sects[2]);

        return LocalDate.of(year, month, day);
    }


    /**
     * 字符串转换为时间类型
     *
     * @param timeString
     * @return
     */
    public static LocalTime convertToLocalTime(String timeString) {
        timeString = ArrayUtil.getLast(timeString.split(" "));
        var nanos = 0L;
        var dotIndex = timeString.indexOf('.');
        if (dotIndex >= 0) {
            nanos = Long.parseLong(timeString.substring(dotIndex + 1)) * 1000000;
            timeString = timeString.substring(0, dotIndex);
        }

        var sects = timeString.split(":");
        var hour = Integer.parseInt(sects[0]);
        var minute = Integer.parseInt(sects[1]);
        var second = 0;
        if (sects.length > 2) {
            second = Integer.parseInt(sects[2]);
        }

        return LocalTime.of(hour, minute, second).plusNanos(nanos);
    }


    /**
     * 重复字符串
     *
     * @param item
     * @param count
     * @return
     */
    public static String repeat(String item, int count) {
        if (count <= 0) {
            return "";
        }
        var list = new ArrayList<String>();
        for (var i = 0; i < count; i++) {
            list.add(item);
        }
        return String.join("", list);
    }


    /**
     * 返回英文半角状态下的宽度值，算法：如果是如果字符编码值大于129，算2个。
     *
     * @param line
     * @return
     */
    private static int getEnViewWidthLine(String line) {
        int enWidth = 0;
        for (var i = 0; i < line.length(); i++) {
            enWidth += 1;
            if (Character.codePointAt(line, i) >= 128) {
                enWidth += 1;
            }
        }

        return enWidth;
    }

    public static int getEnViewWidth(String value) {
        var enWith = 0;
        for (var line : StringsKt.lines(value)) {
            enWith = Math.max(enWith, getEnViewWidthLine(line));
        }

        return enWith;
    }

    public static String removeStart(String value, char item) {
        if (value.length() == 0) {
            return value;
        }
        if (value.charAt(0) != item) {
            return value;
        }
        return value.substring(1);
    }


    public static String removeEnd(String value, char item) {
        var lastIndex = value.length() - 1;
        if (lastIndex < 0) {
            return value;
        }
        if (value.charAt(lastIndex) != item) {
            return value;
        }
        return value.substring(0, lastIndex);
    }

    public static String[] getQuoteValues(String value, String left) {
        return getQuoteValues(value, left, left);
    }


    /**
     * 获取被包裹的内容
     *
     * @param value
     * @param left
     * @param right
     * @return
     */
    public static String[] getQuoteValues(String value, String left, String right) {
        return getGroupsWithPattern(value, left + "(.*?)" + right);
    }

    public static String[] getGroupsWithPattern(String value, String pattern) {
        var p = Pattern.compile(pattern, Pattern.MULTILINE | Pattern.DOTALL)
                .matcher(value);

        var ret = new ArrayList<String>();
        while (p.find()) {
            ret.add(p.group(1));
        }
        return ret.toArray(new String[0]);
    }


    public static String replaceQuoteValue(String value, BiFunction<String, Integer, String> replace, String left) {
        return replaceQuoteValue(value, replace, left, left);
    }

    /**
     * 替换被包裹的内容
     *
     * @param value
     * @param replace
     * @param left
     * @param right
     * @return
     */
    public static String replaceQuoteValue(
            String value,
            BiFunction<String, Integer, String> replace,
            String left,
            String right) {
        return replaceWithPattern(value, left + "(.*?)" + right, replace);
    }


    public static String replaceWithPattern(
            String value,
            String pattern,
            BiFunction<String, Integer, String> replace) {

        StringBuffer result = new StringBuffer();
        var p = Pattern.compile(pattern, Pattern.MULTILINE | Pattern.DOTALL);
        var matcher = p.matcher(value);
        var index = -1;
        while (matcher.find()) {
            index++;

            String keyText = matcher.group(1);
            String v = replace.apply(keyText, index);
            matcher.appendReplacement(result, v);
        }

        matcher.appendTail(result);
        return result.toString();
    }



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

    public static String trimWith(String strValue, String trims) {
        if (isNullOrEmpty(strValue)) {
            return "";
        }


        var startIndex = Integer.MAX_VALUE;
        for (var i = 0; i < strValue.length(); i++) {
            var it = strValue.charAt(i) + "";
            if (!trims.contains(it)) {
                startIndex = i;
                break;
            }
        }

        if (startIndex >= strValue.length()) {
            return "";
        }

        var endIndex = -1;
        for (var i = strValue.length() - 1; i >= 0; i--) {
            var it = strValue.charAt(i) + "";
            if (!trims.contains(it)) {
                endIndex = i;
                break;
            }
        }

        if (endIndex < 0) {
            return "";
        }

        return strValue.substring(startIndex, endIndex + 1);
    }
}
