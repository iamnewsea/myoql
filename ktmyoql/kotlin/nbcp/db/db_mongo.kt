package nbcp.db


import com.mongodb.client.MongoDatabase
import nbcp.comm.*
import nbcp.utils.*
import nbcp.db.mongo.*
import org.bson.Document
import org.bson.UuidRepresentation
import org.bson.codecs.UuidCodecProvider
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistry
import org.bson.types.ObjectId
import org.springframework.core.convert.support.GenericConversionService
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper
import org.springframework.data.mongodb.core.convert.MappingMongoConverter
import org.springframework.data.mongodb.core.mapping.MongoMappingContext
import org.springframework.data.mongodb.core.query.Criteria
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 请使用 db.mongo
 */
object db_mongo {

    //所有的组。
    val groups = mutableSetOf<IDataGroup>()

    //     val sqlEvents = SpringUtil.getBean<SqlEventConfig>();
    val mongoEvents by lazy {
        return@lazy SpringUtil.getBean<MongoEntityCollector>();
    }

//    private var dynamicMongoMap = StringMap();
//    private var dynamicMongoTemplate = StringTypedMap<MongoTemplate>();

    /**
     * 指派集合到数据库
     */
//    fun bindCollection2Database(collectionName: String, connectionUri: String) {
//        this.dynamicMongoMap.set(collectionName, connectionUri)
//    }
//
//    fun unbindCollection(collectionName: String) {
//        this.dynamicMongoMap.remove(collectionName)
//    }

    /**
     * 根据集合定义，获取 MongoTemplate
     */
//    fun getMongoTemplateByCollectionName(collectionName: String): MongoTemplate? {
//        var uri = dynamicMongoMap.get(collectionName);
//        if (uri == null) return null;
//
//        return getMongoTemplateByUri(uri)
//    }

    /**
     * MongoTemplate不释放，所以要缓存。
     */
    private val mongo_template_map = linkedMapOf<String, MongoTemplate>()

    /**
     * 获取 MongoTemplate ,将会有一个连接的线程在等待，所以要避免 usingScope 而不释放。
     * @param uri: 格式 mongodb://dev:123@mongo:27017/cms ，用户名密码出现特殊字符，需要替换：@ -> %40 , : -> %3A
     */
    fun getMongoTemplateByUri(uri: String): MongoTemplate? {
        if (uri.isEmpty()) return null;

        /**
         * https://docs.mongodb.com/manual/reference/connection-string/#std-label-connections-connection-options
         */
        var uri = uri;
        var urlJson = JsUtil.parseUrlQueryJson(uri);
        var maxIdleTimeMS = urlJson.queryJson.getStringValue("maxIdleTimeMS", ignoreCase = true)
        urlJson.queryJson.put("uuidRepresentation", "STANDARD")
        if (maxIdleTimeMS.isNullOrEmpty()) {
            urlJson.queryJson.put("maxIdleTimeMS", "30000")
            uri = urlJson.toUrl();
        }

        var ret = mongo_template_map.get(uri);
        if (ret != null) {
            return ret;
        }
        val dbFactory = SimpleMongoClientDatabaseFactory(uri);
        val converter =
                MappingMongoConverter(DefaultDbRefResolver(dbFactory), SpringUtil.getBean<MongoMappingContext>())
        converter.setTypeMapper(DefaultMongoTypeMapper(null));
        (converter.conversionService as GenericConversionService).addConverter(Date2LocalDateTimeConverter())

        ret = MongoTemplate(dbFactory, converter);
        mongo_template_map.put(uri, ret);
        return ret;
    }
 
    fun cond(ifExpression: Criteria, trueExpression: String, falseExpression: String): MongoExpression {
        return op(PipeLineOperatorEnum.cond, arrayOf(ifExpression.toExpression(), trueExpression, falseExpression))
    }

    fun cond(
            ifExpression: Criteria,
            trueExpression: MongoExpression,
            falseExpression: MongoExpression
    ): MongoExpression {
        return op(PipeLineOperatorEnum.cond, arrayOf(ifExpression.toExpression(), trueExpression, falseExpression))
    }

    fun cond(ifExpression: Criteria, trueExpression: String, falseExpression: MongoExpression): MongoExpression {
        return op(PipeLineOperatorEnum.cond, arrayOf(ifExpression.toExpression(), trueExpression, falseExpression))
    }

    fun cond(ifExpression: Criteria, trueExpression: MongoExpression, falseExpression: String): MongoExpression {
        return op(PipeLineOperatorEnum.cond, arrayOf(ifExpression.toExpression(), trueExpression, falseExpression))
    }

    fun cond(
            ifExpression: MongoExpression,
            trueExpression: MongoExpression,
            falseExpression: MongoExpression
    ): MongoExpression {
        return op(PipeLineOperatorEnum.cond, arrayOf(ifExpression, trueExpression, falseExpression))
    }


    fun op(operator: PipeLineOperatorEnum, rawValue: String): MongoExpression {
        return MongoExpression("$" + operator.toString() to rawValue)
    }

    fun op(operator: PipeLineOperatorEnum, rawValue: MongoExpression): MongoExpression {
        return MongoExpression("$" + operator.toString() to rawValue)
    }

    fun op(operator: PipeLineOperatorEnum, rawValue: Array<*>): MongoExpression {
        return MongoExpression("$" + operator.toString() to rawValue)
    }


    fun op(operator: PipeLineOperatorEnum, rawValue: JsonMap): MongoExpression {
        return MongoExpression("$" + operator.toString() to rawValue)
    }

    /**
     * 聚合
     */
    fun accumulate(operator: PipeLineAccumulatorOperatorEnum, rawValue: String): MongoExpression {
        return MongoExpression("$" + operator.toString() to rawValue)
    }

    fun accumulate(operator: PipeLineAccumulatorOperatorEnum, rawValue: Int): MongoExpression {
        return MongoExpression("$" + operator.toString() to rawValue)
    }

    fun accumulate(operator: PipeLineAccumulatorOperatorEnum, rawValue: Double): MongoExpression {
        return MongoExpression("$" + operator.toString() to rawValue)
    }

    /**
     * cond 中应用 ifNull
     */
    fun ifNull(vararg expression: String): MongoExpression {

        /*
db.getCollection("adminRole").aggregate(
[
    {
        $project:
            {
                "id": 1,
                "createAt": 1, "updateAt": 1,
                "sort":
                    { $ifNull: ['$updateAt', "$createAt"] }
            }
    },
    {
        $sort: { sort: -1}
    }
]
)
 */

        return MongoExpression("$" + PipeLineOperatorEnum.ifNull.toString() to expression)
    }


    fun proc_mongo_key_value(key: MongoColumnName, value: Any?): Pair<String, Any?> {
        /**
         * 只有列 = id , _id , 以 .id , ._id 结尾时，才可能转化为 ObjectId
         */
        fun translateMongoKeyValue(key: MongoColumnName, value: Any?): MongoColumnTranslateResult {
            var ret = MongoColumnTranslateResult(key, value);
            if (value == null) return ret;

            var value_type = value::class.java
            if (value_type.isEnum) {
                ret.changed = true;
                ret.value = value.toString();
                return ret;
            } else if (value_type == LocalDateTime::class.java || value_type == LocalDate::class.java) {
                ret.changed = true;
                ret.value = value.AsLocalDateTime().AsDate()
                return ret;
            } else if (value_type.IsStringType) {
                var keyColumn = key;

                var keyString = key.toString();
                var keyIsId = false;
                if (keyString == "id") {
                    keyColumn = MongoColumnName("_id")
                    keyIsId = true;
                } else if (keyString == "_id") {
                    keyIsId = true;
                } else if (keyString.endsWith(".id")) {
                    keyColumn = MongoColumnName(keyString.slice(0..keyString.length - 4) + "._id")
                    keyIsId = true;
                } else if (keyString.endsWith("._id")) {
                    keyIsId = true;
                }
                if (keyIsId) {
                    var valueString = value.toString();
                    if (ObjectId.isValid(valueString)) {
                        ret.changed = true;
                        ret.key = keyColumn;
                        ret.value = ObjectId(valueString)
                        return ret;
                    }
                }
            } else if (value_type.isArray) {
                ret.value = (value as Array<*>).map {
                    var ret_sub = translateMongoKeyValue(key, it);

                    if (ret_sub.changed) {
                        ret.changed = true;
                        ret.key = ret_sub.key;
                    }
                    return@map ret_sub.value;
                }.toTypedArray()

                return ret;
            } else if (value_type.IsCollectionType) {

                ret.value = (value as Collection<*>).map {
                    var ret_sub = translateMongoKeyValue(key, it);

                    if (ret_sub.changed) {
                        ret.changed = true;
                        ret.key = ret_sub.key;
                    }
                    return@map ret_sub.value;
                }.toList()

                return ret;

            } else if (value_type is Pair<*, *>) {
                var pair_value = value as Pair<*, *>

                var ret_1 = translateMongoKeyValue(key, pair_value.first);
                var ret_2 = translateMongoKeyValue(key, pair_value.second);

                if (ret_1.changed == false && ret_2.changed == false) {
                    return ret;
                }

                ret.changed = true;
                ret.key = ret_1.key;
                ret.value = ret_1.value to ret_2.value;
                return ret;
            }


            return ret;
        }

        var ret = translateMongoKeyValue(key, value)
        if (ret.changed) {
            return ret.key.toString() to ret.value
        }

        return key.toString() to value;
    }


    /**
     * 查询到对象后，转实体。
     */
    fun <T> proc_mongo_doc_to_entity(
            it: Document,
            clazz: Class<T>,
            lastKey: String,
            mapFunc: ((Document) -> Unit)? = null
    ): T? {
        MongoDocument2EntityUtil.procDocumentJson(it);
        var lastKey = lastKey;

        if (mapFunc != null) {
            mapFunc(it);
        }
        if (clazz.IsSimpleType()) {
            if (lastKey.isEmpty()) {
                lastKey = it.keys.last()
            }

            val value = MyUtil.getValueByWbsPath(it, *lastKey.split(".").toTypedArray());
            if (value != null) {
                return value.ConvertType(clazz) as T
            } else {
                return null;
            }
        } else {
            if (Document::class.java.isAssignableFrom(clazz)) {
                return it as T;
            } else {
                return it.ConvertJson(clazz) as T;
            }
        }
    }


    /**
     * 把 Document 推送到数据库，需要转换 id
     */
    fun transformDocumentIdTo_id(value: Any): Any {
        RecursionUtil.recursionAny(value, { json ->
            if (json is MutableMap<*, *>) {
                var m_json = (json as MutableMap<String, Any?>);
                if (json.containsKey("id") && !json.containsKey("_id")) {
                    var idValue = json.get("id");

                    if (idValue is String && ObjectId.isValid(idValue)) {
                        m_json.put("_id", ObjectId(idValue));
                        m_json.remove("id")
                        return@recursionAny true;
                    }

                    m_json.put("_id", idValue)
                    m_json.remove("id")
                }
            }
            return@recursionAny true;
        });

        return value;
    }


    /**
     * 动态实体
     */
    fun dynamicEntity(collectionName: String): MongoDynamicMetaEntity {
        return MongoDynamicMetaEntity(collectionName);
    }
}

class MongoDynamicEntity : JsonMap() {}
class MongoDynamicMetaEntity(collectionName: String, databaseId: String = "") :
        MongoBaseMetaCollection<MongoDynamicEntity>(MongoDynamicEntity::class.java, collectionName, databaseId) {
}