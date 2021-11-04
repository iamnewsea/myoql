package nbcp.base.mvc


import nbcp.comm.AsDate
import org.springframework.core.convert.converter.Converter
import nbcp.comm.AsLocalDateTime
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

/**
 * Created by udi on 17-5-22.
 */

class StringToDateConverter : Converter<String, Date> {
    /**
     * @see org.springframework.core.convert.converter.Converter.convert
     */
    override fun convert(source: String): Date? {
        return source.AsDate()
    }
}

class StringToLocalDateConverter : Converter<String, LocalDate> {
    /**
     * @see org.springframework.core.convert.converter.Converter.convert
     */
    override fun convert(source: String): LocalDate? {
        var v = source.AsLocalDateTime()
        if (v == null) return null;
        return v.toLocalDate()
    }
}

class StringToLocalTimeConverter : Converter<String, LocalTime> {
    /**
     * @see org.springframework.core.convert.converter.Converter.convert
     */
    override fun convert(source: String): LocalTime? {
        var v = source.AsLocalDateTime()
        if (v == null) return null;
        return v.toLocalTime()
    }
}

class StringToLocalDateTimeConverter : Converter<String, LocalDateTime> {
    /**
     * @see org.springframework.core.convert.converter.Converter.convert
     */
    override fun convert(source: String): LocalDateTime? {
        var v = source.AsLocalDateTime()
        if (v == null) return null;
        return v
    }
}