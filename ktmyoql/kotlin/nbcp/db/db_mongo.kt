package nbcp.db

import nbcp.comm.*
import nbcp.utils.*
import nbcp.db.mongo.*
import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.core.convert.support.GenericConversionService
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper
import org.springframework.data.mongodb.core.convert.MappingMongoConverter
import org.springframework.data.mongodb.core.mapping.MongoMappingContext
import org.springframework.data.mongodb.core.query.Criteria
import java.lang.Exception
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.LinkedHashMap


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

    fun hasOrClip(where: Map<String, Any?>): Boolean {
        return where.any { it.key == "\$or" }
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


    fun getCriteriaFromDocument(document: Map<String, Any?>): Criteria {
        val c = Criteria()

        val _criteria = c.javaClass.getDeclaredField("criteria")
        _criteria.isAccessible = true
        var v = _criteria.get(c) as MutableMap<String, Any?>
        for ((key, value) in document) {
            v[key] = value
        }

        val _criteriaChain = c.javaClass.getDeclaredField("criteriaChain")
        _criteriaChain.isAccessible = true
        var v2 = _criteriaChain.get(c) as MutableList<Criteria>
        v2.add(c)

        return c;
    }

    fun getMergedMongoCriteria(where: Map<String, Any?>): Criteria {
        return getMergedMongoCriteria(getCriteriaFromDocument(where))
    }

    fun getMergedMongoCriteria(vararg where: Criteria): Criteria {
        if (where.size == 0) return Criteria();
        if (where.size == 1) return where[0];
        return Criteria().andOperator(*where);
    }

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

    /**
     * @sample
     * $project: {
     *   item: 1,
     *   discount:
     *     {
     *       $cond: { if: { $gte: [ "$qty", 250 ] }, then: 30, else: 20 }
     *     }
     * }
     */
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

    /**
     * @sample
     * {
     *     $project: {
     *      tags: {
     *         $filter: {
     *            input: "$tags",
     *            as: "item",
     *            cond:  {
     *                $eq: ["$$item.score" ,  1  ]
     *            }
     *         }
     *      }
     *   }
     * }
     * @param condExpression: 不是 db.mongo.cond 方法结构。 如果使用 Criteria.toExpression 注意要少一个 $
     */
    fun filter(input: String, alias: String, condExpression: Map<String, Any?>): MongoExpression {
        var map = MongoExpression();
        map.put("input", input);
        map.put("as", alias);
        map.put("cond", condExpression);

        return op(PipeLineOperatorEnum.filter, map);
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


    /**
     * 处理 ObjectId列，同时把列改为 _id 或 ._id
     */
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
                    keyColumn = MongoColumnName(keyString.Slice(0, -3) + "._id")
                    keyIsId = true;
                } else if (keyString.endsWith("._id")) {
                    keyIsId = true;
                }
                if (keyIsId) {
                    ret.changed = true;
                    ret.key = keyColumn;
                    ret.value = value;

                    var valueString = value.toString();
                    if (value is String && ObjectId.isValid(value)) {
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


    fun getEntityColumnName(key: String): String {
        if (key == "_id") return "id"
        if (key.endsWith("._id")) return key.Slice(0, -4) + ".id"
        return key;
    }


    fun getMongoColumnName(key: String): String {
        if (key == "id") return "_id"
        if (key.endsWith(".id")) return key.Slice(0, -3) + "._id"
        return key;
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

                //如果同时存在 id , _id ,则： 仅保留 _id

                if (m_json.containsKey("_id")) {
                    m_json.remove("id")

                    var _idValue = m_json.get("_id")

                    if (_idValue is String && ObjectId.isValid(_idValue)) {
                        m_json.put("_id", ObjectId(_idValue));
                    }
                } else if (m_json.containsKey("id")) {
                    var idValue = m_json.get("id")

                    m_json.remove("id")
                    if (idValue is String && ObjectId.isValid(idValue)) {
                        m_json.put("_id", ObjectId(idValue));
                    } else {
                        m_json.put("_id", idValue);
                    }
                }

                /**
                 * Json可能是多个展开式，如：  tenant.id
                 */
                m_json.keys.toTypedArray().forEach { key ->
                    if (key.endsWith("._id")) {
                        var idKey = key.Slice(0, -4) + ".id"
                        m_json.remove(idKey);

                        var _idValue = m_json.get(key);
                        if (_idValue is String && ObjectId.isValid(_idValue)) {
                            m_json.put(key, ObjectId(_idValue));
                        }
                    } else if (key.endsWith(".id")) {
                        var _idKey = key.Slice(0, -3) + "._id"

                        var idValue = m_json.get(key);
                        m_json.remove(key)

                        if (idValue is String && ObjectId.isValid(idValue)) {
                            m_json.put(_idKey, ObjectId(idValue));
                        } else {
                            m_json.put(_idKey, idValue)
                        }
                    }
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