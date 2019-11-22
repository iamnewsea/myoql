package nbcp.web


import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.core.convert.support.GenericConversionService
import org.springframework.http.HttpInputMessage
import org.springframework.http.HttpOutputMessage
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.web.bind.support.ConfigurableWebBindingInitializer
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter
import nbcp.base.extend.AsLocalDateTime
import nbcp.base.extend.AsString
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*
import javax.annotation.PostConstruct

/**
 * Created by udi on 17-5-22.
 */

class StringToDateConverter : Converter<String, Date> {
    /**
     * @see org.springframework.core.convert.converter.Converter.convert
     */
    override fun convert(source: String): Date? {
        var source = source
        if (source.isNullOrEmpty()) {
            return null
        }
        source = source.trim { it <= ' ' }
        try {
            if (source.contains("-")) {
                val formatter: SimpleDateFormat
                if (source.contains(":")) {
                    formatter = SimpleDateFormat(dateFormat)
                } else {
                    formatter = SimpleDateFormat(shortDateFormat)
                }
                val dtDate = formatter.parse(source)
                return dtDate
            } else if (source.matches("^\\d+$".toRegex())) {
                val lDate = source as Long
                return Date(lDate)
            }
        } catch (e: Exception) {
            throw RuntimeException(String.format("parser %s to Date fail", source))
        }

        throw RuntimeException(String.format("parser %s to Date fail", source))
    }

    companion object {
        private val dateFormat = "yyyy-MM-dd HH:mm:ss"
        private val shortDateFormat = "yyyy-MM-dd"
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

@Configuration
open class WebConfigBeans {
    @Autowired
    private val handlerAdapter: RequestMappingHandlerAdapter? = null


    /**
     * 增加字符串转日期的功能
     */
    @PostConstruct
    fun initEditableValidation() {

        val initializer = handlerAdapter!!.webBindingInitializer as ConfigurableWebBindingInitializer
        if (initializer.conversionService != null) {
            val genericConversionService = initializer.conversionService as GenericConversionService
            genericConversionService.addConverter(StringToDateConverter())
            genericConversionService.addConverter(StringToLocalDateConverter())
            genericConversionService.addConverter(StringToLocalTimeConverter())
            genericConversionService.addConverter(StringToLocalDateTimeConverter())
        }

    }
}