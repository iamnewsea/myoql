package nbcp.myoql.db.mongo.base

import nbcp.base.comm.*;
import nbcp.base.db.*;
import nbcp.base.enums.*;
import nbcp.base.extend.*;
import nbcp.base.utils.*;
import nbcp.myoql.db.enums.*
import nbcp.myoql.db.*;
import org.springframework.core.convert.TypeDescriptor
import org.springframework.core.convert.converter.GenericConverter
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.*

/**
 * Created by udi on 17-5-22.
 */


class Date2LocalDateTimeConverter : GenericConverter {
    override fun getConvertibleTypes(): MutableSet<GenericConverter.ConvertiblePair> {
        var pairs = hashSetOf<GenericConverter.ConvertiblePair>();
        pairs.add(GenericConverter.ConvertiblePair(String::class.java, LocalDate::class.java));
        pairs.add(GenericConverter.ConvertiblePair(String::class.java, LocalTime::class.java));
        pairs.add(GenericConverter.ConvertiblePair(String::class.java, LocalDateTime::class.java));
        pairs.add(GenericConverter.ConvertiblePair(Date::class.java, LocalDate::class.java));
        pairs.add(GenericConverter.ConvertiblePair(Date::class.java, LocalTime::class.java));
        pairs.add(GenericConverter.ConvertiblePair(Date::class.java, LocalDateTime::class.java));
        pairs.add(GenericConverter.ConvertiblePair(LocalDate::class.java, Date::class.java));
        pairs.add(GenericConverter.ConvertiblePair(LocalTime::class.java, Date::class.java));
        pairs.add(GenericConverter.ConvertiblePair(LocalDateTime::class.java, Date::class.java));
        return pairs;
    }

    override fun convert(value: Any?, sourceType: TypeDescriptor, targetType: TypeDescriptor): Any? {
        if (value == null) return null

        val valueClass = sourceType.type;
        val targetClass = targetType.type;

        if (valueClass == String::class.java) {
            val strValue = value.AsString();
            if (strValue.isEmpty()) {
                return null;
            }

            if (targetClass == LocalDate::class.java) {
                return strValue.AsLocalDate();
            }
            if (targetClass == LocalTime::class.java) {
                return strValue.AsLocalTime()
            }
            if (targetClass == LocalDateTime::class.java) {
                return strValue.AsLocalDateTime();
            }
        }

        if (valueClass == Date::class.java) {
            if (targetClass == LocalDate::class.java) {
                return value.AsLocalDate();
            }
            if (targetClass == LocalTime::class.java) {
                return value.AsLocalTime();
            }
            if (targetClass == LocalDateTime::class.java) {
                return value.AsLocalDateTime()
            }
        }

        if (targetClass == Date::class.java) {
            if (valueClass == LocalDate::class.java) {
                return Date.from((value as LocalDate).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
            }
            if (valueClass == LocalTime::class.java) {
                return Date.from((value as LocalTime).atDate("1970-01-01".AsLocalDate()).atZone(ZoneId.systemDefault()).toInstant());
            }
            if (valueClass == LocalDateTime::class.java) {
                return Date.from((value as LocalDateTime).atZone(ZoneId.systemDefault()).toInstant());
            }
        }

        return null;
    }

}