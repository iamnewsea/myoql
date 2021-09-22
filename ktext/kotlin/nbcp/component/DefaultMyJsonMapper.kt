package nbcp.comm


import com.fasterxml.jackson.databind.*
import nbcp.utils.*
import java.util.*
import nbcp.scope.*


//private val sers: MutableList<SimpleModule> = mutableListOf()
//private val desers: MutableList<SimpleModule> = mutableListOf()

/**
 * 使用 字段值 方式序列化JSON，应用在数据库的场景中。
 */
//@Primary
//@Component
//open class DefaultMyJsonMapper : ObjectMapper(), InitializingBean {
//    override fun afterPropertiesSet() {
//        this.setStyle()
//    }
//
//    override fun writeValueAsString(value: Any?): String {
//        return super.writeValueAsString(value)
//    }
//
//    override fun <T : Any?> readValue(content: String?, valueType: Class<T>?): T {
//        return super.readValue(content, valueType)
//    }
//
//    companion object {
//        private val cacheManagers = StringKeyMap<ObjectMapper>();
//
//
//        @JvmStatic
//        fun get(vararg styles: JsonStyleEnumScope): ObjectMapper {
//            var scopeStyles = scopes.getScopeTypes<JsonStyleEnumScope>().plus(styles).toMutableSet()
//
//
//            //看互斥性
//            styles.forEach { style ->
//                scopeStyles.removeAll { it.mutexGroup == style.mutexGroup }
//                scopeStyles.add(style);
//            }
//
//            var key = scopeStyles.toSortedSet().joinToString(",")
//
//            var cacheManagerItem = cacheManagers.get(key);
//            if (cacheManagerItem != null) {
//                return cacheManagerItem
//            }
//
//            cacheManagerItem = DefaultMyJsonMapper().setStyle(*scopeStyles.toTypedArray())
//            cacheManagers.put(key, cacheManagerItem);
//            return cacheManagerItem;
//        }
//
//
//        @JvmStatic
//        fun <T> addSerializer(type: Class<T>, ser: JsonSerializer<T>, deser: JsonDeserializer<T>) {
//            cacheManagers.clear();
//
//            if (sers.any { it.moduleName == type.name } == false) {
//                var item = SimpleModule(type.name)
//                item.addSerializer(type, ser)
//                sers.add(item);
//            }
//            if (desers.any { it.moduleName == type.name } == false) {
//                var item = SimpleModule(type.name)
//                item.addDeserializer(type, deser)
//                desers.add(item)
//            }
//        }
//    }
//}

//fun ObjectMapper.setStyle(vararg styles: JsonStyleEnumScope): ObjectMapper {
//    // 设置输出时包含属性的风格
//    this.findAndRegisterModules();
//
//    //这句话会让 kotlin isXXX:Boolean 原样输出。
//    this.registerKotlinModule()
//
//    // 允许单引号、允许不带引号的字段名称
//    this.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
//    this.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
//    this.configure(MapperFeature.USE_STD_BEAN_NAMING, true)
//
//
//    // 设置输入时忽略在JSON字符串中存在但Java对象实际没有的属性
//    this.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
//
////    this.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
//
//    if (styles.contains(JsonStyleEnumScope.GetSetStyle)) {
//        this.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.DEFAULT);
//        this.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.DEFAULT);
//        this.setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.DEFAULT);
//        this.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.NONE);
//    } else {
//        this.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.NONE);
//        this.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
//        this.setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.NONE);
//        this.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
//    }
//
//    if (styles.contains(JsonStyleEnumScope.WithNull)) {
////        this.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT)
//    } else {
//        this.setSerializationInclusion(JsonInclude.Include.NON_NULL)
//    }
//
//    // 设置时区
//    this.setTimeZone(TimeZone.getTimeZone("GMT+:08:00"))
//
//    this.dateFormat = SimpleDateFormat(styles.getDateFormat());
//
//
//    //在某些时候，如 mongo.aggregate.group._id 时， null 。
//    //默认只序列化 not null 的。
//
//    sers.forEach {
//        this.registerModule(it);
//    }
//
//    desers.forEach {
//        this.registerModule(it);
//    }
//
////    if (styles.contains(JsonStyleEnumScope.Pretty)) {
////        this.setDefaultPrettyPrinter(this.serializationConfig.defaultPrettyPrinter)
////    }
//    return this;
//}

fun Array<out JsonStyleEnumScope>.getDateFormat(): String = this.toList().getDateFormat()
/**
 */
fun Collection<JsonStyleEnumScope>.getDateFormat(): String {
    if (this.contains(JsonStyleEnumScope.DateUtcStyle)) {
        return "yyyy-MM-dd'T'HH:mm:ss'Z'"
    } else if (this.contains(JsonStyleEnumScope.DateLocalStyle)) {
        return "yyyy/MM/dd HH:mm:ss"
    } else {
        return "yyyy-MM-dd HH:mm:ss";
    }
}


