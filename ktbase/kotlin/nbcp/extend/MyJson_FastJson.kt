//package nbcp.comm
//
//import com.alibaba.fastjson.JSON
//import com.alibaba.fastjson.parser.DefaultJSONParser
//import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer
//import com.alibaba.fastjson.serializer.ValueFilter
//import org.bson.types.ObjectId
//import java.lang.reflect.Type
//
///**
// * Created by udi on 17-5-23.
// */
//
//
//class ObjectIdDeserializer : ObjectDeserializer {
//
//    override fun <T> deserialze(parser: DefaultJSONParser, type: Type,
//                                fieldName: Any): T {
//        val lexer = parser.getLexer()
//        val value = lexer.stringVal()
//        return ObjectId(value) as T;
//    }
//
//    override fun getFastMatchToken(): Int {
//        // TODO Auto-generated method stub
//        return 0
//    }
//}
//
//fun <T> T.ToJson(): String {
//    if (this is String) return this;
//
//    val filter = ValueFilter { source, name, value ->
//        if (value is ObjectId) {
//            return@ValueFilter value.toString();
//        }
//        value;
//    }
//
////    var sc = SerializeConfig();
////    sc.put(ObjectId::class.java, ObjectIdSerializer());
//    return JSON.toJSONString(this, filter)
//}
//
//inline fun <reified T> String.FromJson(): T {
//    if (T::class.java == String::class.java) {
//        return this as T
//    }
//    return JSON.parseObject(this.RemoveComment().Remove("\r\n", "\n"), T::class.java);
//}
//
//fun <T> String.FromJson(collectionClass: Class<T>): T {
//    if (collectionClass == String::class.java) {
//        return this as T
//    }
//    return JSON.parseObject(this.RemoveComment().Remove("\r\n", "\n"), collectionClass);
//}