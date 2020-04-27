package nbcp.comm

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Component
import nbcp.utils.*
import org.springframework.context.annotation.DependsOn
import org.springframework.context.annotation.Primary
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.LinkedHashMap

/**
 * 使用 字段值 方式序列化JSON，应用在数据库的场景中。
 */
@Primary
@Component
@DependsOn(value = arrayOf("springUtil"))
open class DefaultMyJsonMapper : ObjectMapper(), InitializingBean {
    override fun afterPropertiesSet() {
        this.setStyle()
    }

    companion object {
        val sers: MutableList<SimpleModule> = mutableListOf()
        val desers: MutableList<SimpleModule> = mutableListOf()


        fun get(): ObjectMapper {
            var styles = scopes.getScopeTypes<JsonStyleEnumScope>()
            return get(*styles.toTypedArray());
        }

        fun get(vararg styles: JsonStyleEnumScope): ObjectMapper {
            if (styles.isEmpty()) return SpringUtil.getBean<DefaultMyJsonMapper>()
            return ObjectMapper().setStyle(*styles)
        }

        fun <T> addSerializer(type: Class<out T>, ser: JsonSerializer<T>) {
            if (this.sers.any { it.moduleName == type.name }) return;
            var item = SimpleModule(type.name)
            item.addSerializer(type, ser)
            this.sers.add(item);
            SpringUtil.getBean<DefaultMyJsonMapper>().registerModule(item)
        }

        fun <T> addDeserializer(type: Class<T>, deser: JsonDeserializer<out T>) {
            if (this.desers.any { it.moduleName == type.name }) return;

            var item = SimpleModule(type.name)
            item.addDeserializer(type, deser)
            this.desers.add(item)
            SpringUtil.getBean<DefaultMyJsonMapper>().registerModule(item)
        }
    }
}


fun ObjectMapper.setStyle(vararg styles: JsonStyleEnumScope): ObjectMapper {
    // 设置输出时包含属性的风格
    this.findAndRegisterModules();
    this.registerKotlinModule()
    // 允许单引号、允许不带引号的字段名称
    this.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
    this.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
    this.configure(MapperFeature.USE_STD_BEAN_NAMING, true)


    // 设置输入时忽略在JSON字符串中存在但Java对象实际没有的属性
    this.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)


    if (styles.contains(JsonStyleEnumScope.GetSetStyle)) {
        this.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.DEFAULT);
        this.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.DEFAULT);
        this.setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.DEFAULT);
        this.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.NONE);
    } else {
        this.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.NONE);
        this.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
        this.setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.NONE);
        this.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    }

    if (styles.contains(JsonStyleEnumScope.WithNull)) {
//        this.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT)
    } else {
        this.setSerializationInclusion(JsonInclude.Include.NON_NULL)
    }

    // 设置时区
    this.setTimeZone(TimeZone.getTimeZone("GMT+:08:00"))
    if (styles.contains(JsonStyleEnumScope.DateUtcStyle)) {
        this.dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH)
    } else if (styles.contains(JsonStyleEnumScope.DateLocalStyle)) {
        this.dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
    } else {
        this.dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    }

    //在某些时候，如 mongo.aggregate.group._id 时， null 。
    //默认只序列化 not null 的。

    DefaultMyJsonMapper.sers.forEach {
        this.registerModule(it);
    }

    DefaultMyJsonMapper.desers.forEach {
        this.registerModule(it);
    }

//    if (styles.contains(JsonStyleEnumScope.Pretty)) {
//        this.setDefaultPrettyPrinter(this.serializationConfig.defaultPrettyPrinter)
//    }
    return this;
}
