package cn.dev8.util;

import lombok.var;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 日期时间工具类
 */
public class DateTimeUtil {

    /**
     * 是否有值
     *
     * @param value
     * @return
     */
    public static boolean hasValue(LocalDateTime value) {
        if (value == null) {
            return false;
        }
        return !value.isEqual(LocalDateTime.MIN);
    }


    /**
     * 是否有值
     *
     * @param value
     * @return
     */
    public static boolean hasValue(LocalDate value) {
        if (value == null) {
            return false;
        }
        return !value.isEqual(LocalDate.MIN);
    }


    /**
     * 是否无值
     *
     * @param value
     * @return
     */
    public static boolean isNullOrEmpty(LocalDateTime value) {
        return !hasValue(value);
    }

    /**
     * 是否无值
     *
     * @param value
     * @return
     */
    public static boolean isNullOrEmpty(LocalDate value) {
        return !hasValue(value);
    }


    /**
     * 获取总秒数
     *
     * @param time
     * @return
     */
    public static int getTotalSeconds(LocalTime time) {
        return time.getHour() * 3600 + time.getMinute() * 60 + time.getSecond();
    }

    /**
     * 转换为日期类型
     *
     * @param date
     * @return
     */
    public static LocalDateTime convertToLocalDateTime(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }


    public static LocalDateTime convertToLocalDateTime(String value) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(getDateFormat(value));

        //+00:00. 如果是 +其它时间,也按 +00:00 计算
        if (value.contains("+")) {
            formatter = DateTimeFormatter.ISO_ZONED_DATE_TIME;

            var ret = LocalDateTime.parse(value, formatter);
            ret = ret.plusHours(8);
            return ret;
        }
        return LocalDateTime.parse(value, formatter);
    }

    public static long getTotalSeconds(LocalDateTime localDateTime) {
        return localDateTime.toEpochSecond(ZoneId.systemDefault().getRules().getOffset(Instant.EPOCH));
    }

    public static LocalDateTime fromTotalSeconds(Long totalSeconds) {
        return LocalDateTime.ofEpochSecond(totalSeconds, 0, ZoneId.systemDefault().getRules().getOffset(Instant.EPOCH));
    }

    /**
     * 转为字符串格式， 如果时间部分是0，则转为日期字符串。
     *
     * @param dateTime
     * @return
     */
    public static String asString(LocalDateTime dateTime) {
        if (dateTime.toLocalTime().toSecondOfDay() == 0) {
            return asString(dateTime.toLocalDate());
        }
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * 转为字符串
     *
     * @param dateTime
     * @return
     */
    public static String asString(LocalDate dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    public static String asString(LocalTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    /**
     * 获取Format
     *
     * @param dateTime
     * @return
     */
    public static String getDateFormat(String dateTime) {
        //2023-04-22T04:08:39.000+00:00
        if (dateTime.contains("T")) {
            if (dateTime.contains(".")) {
                if (dateTime.contains("Z")) {
                    return "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
                }
                return "yyyy-MM-dd'T'HH:mm:ss.SSS";
            }

//            if (dateTime.contains("+")) {
//                return "yyyy-MM-dd'T'HH:mm:ss";
//            }
            return "yyyy-MM-dd'T'HH:mm:ss";
        }


        if (dateTime.contains(" ")) {
            if (dateTime.contains("/")) {
                return "yyyy/MM/dd HH:mm:ss";
            } else {
                return "yyyy-MM-dd HH:mm:ss";
            }
        }

        if (dateTime.contains("/")) {
            return "yyyy/MM/dd";
        } else {
            return "yyyy-MM-dd";
        }

    }

    /**
     * 类型转换
     *
     * @param localDateTime
     * @return
     */
    public static Date convertToDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}
