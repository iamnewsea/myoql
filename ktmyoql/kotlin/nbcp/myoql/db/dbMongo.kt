package nbcp.myoql.db

import nbcp.base.comm.*
import nbcp.base.data.UrlQueryJsonData
import nbcp.base.db.*
import nbcp.base.enums.*
import nbcp.base.extend.*
import nbcp.base.utils.*
import nbcp.myoql.db.comm.IDataGroup
import nbcp.myoql.db.enums.*
import nbcp.myoql.db.mongo.*
import nbcp.myoql.db.mongo.base.Date2LocalDateTimeConverter
import nbcp.myoql.db.mongo.base.MongoColumnName
import nbcp.myoql.db.mongo.component.MongoBaseMetaCollection
import nbcp.myoql.db.mongo.enums.PipeLineAccumulatorOperatorEnum
import nbcp.myoql.db.mongo.enums.PipeLineOperatorEnum
import nbcp.myoql.db.mongo.extend.MongoColumnTranslateResult
import nbcp.myoql.db.mongo.extend.toExpression
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
import java.time.LocalDate
import java.time.LocalDateTime


/**
 * 请使用 db.mongo
 */
object dbMongo {

    //所有的组。
    @JvmStatic
    val groups = mutableSetOf<IDataGroup>()

    //     val sqlEvents = SpringUtil.getBean<SqlEventConfig>();
    @JvmStatic
    val mongoEvents by lazy {
        return@lazy SpringUtil.getBean<MongoEntityCollector>();
    }

    @JvmStatic
    fun hasOrClip(where: MongoWhereClip): Boolean {
        return where.any { map -> map.any { it.key == "\$or" } }
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


    @JvmStatic
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

    @JvmStatic
    fun getMergedMongoCriteria(where: MongoWhereClip): Criteria {
        return getMergedMongoCriteria(*where.map { getCriteriaFromDocument(it) }.toTypedArray())
    }

    @JvmStatic
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
     * 变成更标准的连接字符串。动态添加authSource，uuidRepresentation，maxIdleTimeMS，connectTimeoutMS
     */
    @JvmStatic
    fun getMongoStandardUri(mongoURI: String): UrlQueryJsonData {
        /**
         * https://docs.mongodb.com/manual/reference/connection-string/#std-label-connections-connection-options
         */
        var uri = mongoURI;
        var urlJson = UrlUtil.parseUrlQueryJson(uri);

        if (urlJson.queryJson.get("uuidRepresentation").AsString().isEmpty()) {
            urlJson.queryJson.put("uuidRepresentation", "STANDARD")
        }

        if (uri.startsWith("mongodb://") && uri.contains('@')) {
            var userName = "";

            /*
            * 如果使用 root 用户连接，连接字符串须要额外添加  ?authSource=admin
            * mongodb://root:1234.5678@192.168.5.211:26757/cms?authSource=admin
            */
            try {
                userName = uri.split('@')
                    .first()
                    .split("mongodb://")
                    .last()
                    .split(':')
                    .first()
            } finally {
            }

            if (userName == "root" && !urlJson.queryJson.containsKey("authSource")) {
                urlJson.queryJson.put("authSource", "admin")
            }
        }

        var maxIdleTimeMS = urlJson.queryJson.getStringValue("maxIdleTimeMS", ignoreCase = true)
        if (maxIdleTimeMS.isNullOrEmpty()) {
            urlJson.queryJson.put("maxIdleTimeMS", "30000")
        }

        var connectTimeoutMS = urlJson.queryJson.getStringValue("connectTimeoutMS", ignoreCase = true)
        if (connectTimeoutMS.isNullOrEmpty()) {
            urlJson.queryJson.put("connectTimeoutMS", "10000")
        }

        return urlJson
    }

    /**
     * 获取 MongoTemplate ,将会有一个连接的线程在等待，所以要避免 usingScope 而不释放。
     * @param uri: 格式 mongodb://dev:123@mongo:27017/cms ，用户名密码出现特殊字符，需要替换：@ -> %40 , : -> %3A
     */
    @JvmStatic
    fun getMongoTemplateByUri(mongoURI: String): MongoTemplate? {
        if (mongoURI.isEmpty()) return null;


        var uri = getMongoStandardUri(mongoURI).toUrl()

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
    @JvmStatic
    fun cond(ifExpression: Criteria, trueExpression: String, falseExpression: String): MongoExpression {
        return op(PipeLineOperatorEnum.COND, arrayOf(ifExpression.toExpression(), trueExpression, falseExpression))
    }

    @JvmStatic
    fun cond(
        ifExpression: Criteria,
        trueExpression: MongoExpression,
        falseExpression: MongoExpression
    ): MongoExpression {
        return op(PipeLineOperatorEnum.COND, arrayOf(ifExpression.toExpression(), trueExpression, falseExpression))
    }

    @JvmStatic
    fun cond(ifExpression: Criteria, trueExpression: String, falseExpression: MongoExpression): MongoExpression {
        return op(PipeLineOperatorEnum.COND, arrayOf(ifExpression.toExpression(), trueExpression, falseExpression))
    }

    @JvmStatic
    fun cond(ifExpression: Criteria, trueExpression: MongoExpression, falseExpression: String): MongoExpression {
        return op(PipeLineOperatorEnum.COND, arrayOf(ifExpression.toExpression(), trueExpression, falseExpression))
    }

    @JvmStatic
    fun cond(
        ifExpression: MongoExpression,
        trueExpression: MongoExpression,
        falseExpression: MongoExpression
    ): MongoExpression {
        return op(PipeLineOperatorEnum.COND, arrayOf(ifExpression, trueExpression, falseExpression))
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
    @JvmStatic
    fun filter(input: String, alias: String, condExpression: Map<String, Any?>): MongoExpression {
        var map = MongoExpression();
        map.put("input", input);
        map.put("as", alias);
        map.put("cond", condExpression);

        return op(PipeLineOperatorEnum.FILTER, map);
    }

    @JvmStatic
    fun op(operator: PipeLineOperatorEnum, rawValue: String): MongoExpression {
        return MongoExpression("$" + operator.toString() to rawValue)
    }

    @JvmStatic
    fun op(operator: PipeLineOperatorEnum, rawValue: MongoExpression): MongoExpression {
        return MongoExpression("$" + operator.toString() to rawValue)
    }

    @JvmStatic
    fun op(operator: PipeLineOperatorEnum, rawValue: Array<*>): MongoExpression {
        return MongoExpression("$" + operator.toString() to rawValue)
    }

    @JvmStatic
    fun op(operator: PipeLineOperatorEnum, rawValue: Map<String, Any?>): MongoExpression {
        return MongoExpression("$" + operator.toString() to rawValue)
    }

    /**
     * 聚合
     */
    @JvmStatic
    fun accumulate(operator: PipeLineAccumulatorOperatorEnum, rawValue: String): MongoExpression {
        return MongoExpression("$" + operator.toString() to rawValue)
    }

    @JvmStatic
    fun accumulate(operator: PipeLineAccumulatorOperatorEnum, rawValue: Int): MongoExpression {
        return MongoExpression("$" + operator.toString() to rawValue)
    }

    @JvmStatic
    fun accumulate(operator: PipeLineAccumulatorOperatorEnum, rawValue: Double): MongoExpression {
        return MongoExpression("$" + operator.toString() to rawValue)
    }

    /**
     * cond 中应用 ifNull
     */
    @JvmStatic
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

        return MongoExpression("$" + PipeLineOperatorEnum.IF_NULL.toString() to expression)
    }


    /**
     * 处理 ObjectId列，同时把列改为 _id 或 ._id
     */
    @JvmStatic
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

    @JvmStatic
    fun getEntityColumnName(key: String): String {
        if (key == "_id") return "id"
        if (key.endsWith("._id")) return key.Slice(0, -4) + ".id"
        return key;
    }

    @JvmStatic
    fun getMongoColumnName(key: String): String {
        if (key == "id") return "_id"
        if (key.endsWith(".id")) return key.Slice(0, -3) + "._id"
        return key;
    }

    /**
     * 查询到对象后，转实体。
     */
    @JvmStatic
    fun <T> proc_mongo_doc_to_entity(
        it: Document,
        type: Class<T>,
        lastKey: String,
        mapFunc: ((Document) -> Unit)? = null
    ): T? {
        MongoDocument2EntityUtil.procDocumentJson(it);
        var lastKey = lastKey;

        if (mapFunc != null) {
            mapFunc(it);
        }
        if (type.IsSimpleType()) {
            if (lastKey.isEmpty()) {
                lastKey = it.keys.last()
            }

            val value = ReflectUtil.getValueByWbsPath(it, *lastKey.split(".").toTypedArray());
            if (value != null) {
                return value.ConvertType(type) as T
            } else {
                return null;
            }
        } else {
            if (Document::class.java.isAssignableFrom(type)) {
                return it as T;
            } else {
                return it.ConvertJson(type) as T;
            }
        }
    }


    /**
     * 把 Document 推送到数据库，需要转换 id
     */
    @JvmStatic
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
    @JvmStatic
    fun dynamicEntity(collectionName: String): MongoDynamicMetaEntity {
        return MongoDynamicMetaEntity(collectionName);
    }
}

class MongoDynamicEntity : JsonMap() {}
class MongoDynamicMetaEntity(collectionName: String, databaseId: String = "") :
    MongoBaseMetaCollection<MongoDynamicEntity>(MongoDynamicEntity::class.java, collectionName, databaseId) {
}