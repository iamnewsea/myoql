package nbcp.base.extend

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.core.util.VersionUtil
import com.fasterxml.jackson.databind.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.stereotype.Component
import nbcp.base.utils.SpringUtil
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.util.*
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.cfg.MapperConfig
import com.fasterxml.jackson.databind.introspect.AnnotatedClass
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.context.annotation.Primary

/**
 * 使用 字段值 方式序列化JSON，应用在数据库的场景中。
 */
@Primary
@Component
class FieldTypeJsonMapper : ObjectMapper(),InitializingBean {
    init {
        // 设置输出时包含属性的风格
        this.findAndRegisterModules();
        this.setSerializationInclusion(JsonInclude.Include.NON_NULL)

        // 允许单引号、允许不带引号的字段名称
        this.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
        this.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
        this.configure(MapperFeature.USE_STD_BEAN_NAMING, true)

        this.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.NONE);
        this.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
        this.setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.NONE);
        this.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        this.setDateFormat(SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

        // 设置输入时忽略在JSON字符串中存在但Java对象实际没有的属性
        this.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

        // 设置时区
        this.setTimeZone(TimeZone.getTimeZone("GMT+:08:00"))

        this.registerKotlinModule()
    }



    companion object {
        /**
         * 创建只输出非Null且非Empty(如List.isEmpty)的属性到Json字符串的Mapper,建议在外部接口中使用.
         */
        val instance: FieldTypeJsonMapper by lazy { SpringUtil.getBean<FieldTypeJsonMapper>() }

        internal val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }


    @Autowired
    lateinit var  dateModule: JavascriptDateModule;

    override fun afterPropertiesSet() {

        this.registerModule(dateModule);
    }

}