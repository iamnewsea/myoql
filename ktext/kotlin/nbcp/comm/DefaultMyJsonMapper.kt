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
        private val sers: MutableList<SimpleModule> = mutableListOf()
        private val desers: MutableList<SimpleModule> = mutableListOf()

        private val cacheManagers = StringKeyMap<ObjectMapper>();


        @JvmStatic
        fun get(vararg styles: JsonStyleEnumScope): ObjectMapper {
            var scopeStyles = scopes.getScopeTypes<JsonStyleEnumScope>();

            var scopeStyleList = scopeStyles.toMutableSet();

            //看互斥性
            styles.forEach { style ->
                scopeStyleList.removeAll { it.mutexGroup == style.mutexGroup }
                scopeStyleList.add(style);
            }

            var key = scopeStyleList.toSortedSet().joinToString(",")

            var cacheManagerItem = cacheManagers.get(key);
            if (cacheManagerItem != null) {
                return cacheManagerItem
            }

            cacheManagerItem = DefaultMyJsonMapper().setStyle(*scopeStyleList.toTypedArray())
            cacheManagers.put(key, cacheManagerItem);
            return cacheManagerItem;
        }


        @JvmStatic
        fun <T> addSerializer(type: Class<T>, ser: JsonSerializer<T>, deser: JsonDeserializer<T>) {
            cacheManagers.clear();

            if (this.sers.any { it.moduleName == type.name } == false) {
                var item = SimpleModule(type.name)
                item.addSerializer(type, ser)
                this.sers.add(item);
            }
            if (this.desers.any { it.moduleName == type.name } == false) {
                var item = SimpleModule(type.name)
                item.addDeserializer(type, deser)
                this.desers.add(item)
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

//    this.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)

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

            this.dateFormat = SimpleDateFormat(styles.toList().getDateFormat());


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

        /**
         * 这个方法不准确，应该按 scopes.getLatestScope(JsonStyleEnumScope.DateLocalStyle,JsonStyleEnumScope.DateUtcStyle, JsonStyleEnumScope.DateStandardStyle) ?: JsonStyleEnumScope.DateStandardStyle
         */
        private fun List<JsonStyleEnumScope>.getDateFormat(): String {
            if (this.contains(JsonStyleEnumScope.DateUtcStyle)) {
                return "yyyy-MM-dd'T'HH:mm:ss'Z'"
            } else if (this.contains(JsonStyleEnumScope.DateLocalStyle)) {
                return "yyyy/MM/dd HH:mm:ss"
            } else {
                return "yyyy-MM-dd HH:mm:ss";
            }
        }

    }

}


