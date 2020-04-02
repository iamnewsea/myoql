package nbcp.db


import nbcp.base.extend.*
import nbcp.base.utils.RecursionUtil
import nbcp.base.utils.SpringUtil
import nbcp.comm.*
import nbcp.db.mongo.*
import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.core.convert.support.GenericConversionService
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.SimpleMongoClientDbFactory
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper
import org.springframework.data.mongodb.core.convert.MappingMongoConverter
import org.springframework.data.mongodb.core.mapping.MongoMappingContext
import org.springframework.data.mongodb.core.query.Criteria

/**
 * 请使用 db.mongo
 */
object db_mongo {

    //所有的组。
    val groups = mutableSetOf<IDataGroup>()

    //     val sqlEvents = SpringUtil.getBean<SqlEventConfig>();
    val mongoEvents by lazy {
        return@lazy SpringUtil.getBean<MongoEntityEvent>();
    }

    private var dynamicMongoMap = StringMap();
//    private var dynamicMongoTemplate = StringTypedMap<MongoTemplate>();

    /**
     * 指派集合到数据库
     */
    fun bindCollection2Database(collectionName: String, connectionUri: String) {
        this.dynamicMongoMap.set(collectionName, connectionUri)
    }

    fun unbindCollection(collectionName: String) {
        this.dynamicMongoMap.remove(collectionName)
    }

    /**
     * 根据集合定义，获取 MongoTemplate
     */
    fun getMongoTemplateByCollectionName(collectionName: String): MongoTemplate? {
        var uri = dynamicMongoMap.get(collectionName);
        if (uri == null) return null;

        return getMongoTemplateByUri(uri)
    }

    fun getMongoTemplateByUri(uri: String): MongoTemplate? {
        if (uri.isEmpty()) return null;


        var dbFactory = SimpleMongoClientDbFactory(uri);
        val converter = MappingMongoConverter(DefaultDbRefResolver(dbFactory), MongoMappingContext())
        converter.setTypeMapper(DefaultMongoTypeMapper(null));
        (converter.conversionService as GenericConversionService).addConverter(Date2LocalDateTimeConverter())

        return MongoTemplate(dbFactory, converter);
    }
    //----------------mongo expression-------------

//    fun times(rawValue: String):PipeLineExpression{
//        return db.mongo.op(PipeLineOperatorEnum.multiply , arrayOf())
//    }

    fun cond(ifExpression: Criteria, trueExpression: String, falseExpression: String): MongoExpression {
        return op(PipeLineOperatorEnum.cond, arrayOf(ifExpression.toExpression(), trueExpression, falseExpression))
    }

    fun cond(ifExpression: Criteria, trueExpression: MongoExpression, falseExpression: MongoExpression): MongoExpression {
        return op(PipeLineOperatorEnum.cond, arrayOf(ifExpression.toExpression(), trueExpression, falseExpression))
    }

    fun cond(ifExpression: Criteria, trueExpression: String, falseExpression: MongoExpression): MongoExpression {
        return op(PipeLineOperatorEnum.cond, arrayOf(ifExpression.toExpression(), trueExpression, falseExpression))
    }

    fun cond(ifExpression: Criteria, trueExpression: MongoExpression, falseExpression: String): MongoExpression {
        return op(PipeLineOperatorEnum.cond, arrayOf(ifExpression.toExpression(), trueExpression, falseExpression))
    }

    fun cond(ifExpression: MongoExpression, trueExpression: MongoExpression, falseExpression: MongoExpression): MongoExpression {
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
     * 把 _id 转换为 id
     */
    fun procResultData_id2Id(value: Collection<*>, remove_id: Boolean = true) {
        value.forEach { v ->
            if (v == null) {
                return@forEach
            }

            if (v is MutableMap<*, *>) {
                db.mongo.procResultData_id2Id(v, remove_id);
            } else if (v is Collection<*>) {
                procResultData_id2Id(v, remove_id);
            } else if (v is Array<*>) {
                db.mongo.procResultData_id2Id(v, remove_id);
            }
        }
    }


    /**
     * 把 _id 转换为 id
     */
    fun procResultData_id2Id(value: Array<*>, remove_id: Boolean = true) {
        value.forEach { v ->
            if (v == null) {
                return@forEach
            }

            if (v is MutableMap<*, *>) {
                procResultData_id2Id(v, remove_id);
            } else if (v is Collection<*>) {
                procResultData_id2Id(v, remove_id);
            } else if (v is Array<*>) {
                procResultData_id2Id(v, remove_id);
            }
        }
    }

    fun procResultData_id2Id(value: MutableMap<*, *>, remove_id: Boolean = true) {
        var keys = value.keys.toTypedArray();
        var needReplace = keys.contains("_id") && !keys.contains("id")
        for (k in keys) {
            var v = value.get(k);
            if (needReplace && (k == "_id")) {
                (value as MutableMap<Any, Any?>).set("id", v?.toString() ?: "");
                if (remove_id) {
                    value.remove("_id")
                }
                needReplace = false;
                continue;
            }
            if (v == null) {
                continue;
            }
            if (v is MutableMap<*, *>) {
                procResultData_id2Id(v, remove_id);
            } else if (v is Collection<*>) {
                procResultData_id2Id(v, remove_id);
            }
        }
    }


    /**
     * 把 Document 推送到数据库，需要转换 id
     */
    fun procSetDocumentData(value: Any): Any {
        RecursionUtil.recursionJson(value, { json, type ->
            if (json is MutableMap<*, *>) {
                if (json.contains("id")) {
                    var value = json.get("id");

                    if (value is String && ObjectId.isValid(value)) {
                        (json as MutableMap<String, Any?>).put("_id", ObjectId(value));
                        json.remove("id")
                        return@recursionJson true;
                    }

                    (json as MutableMap<String, Any?>).put("_id", value)
                    json.remove("id")
                }
            }
            return@recursionJson true;
        });

        return value;
    }


    /**
     *value 可能会是： Document{{answerRole=Patriarch}}
     */
    fun procResultDocumentJsonData(value: Document) {
        fun test(item: Any?): Boolean {
            if (item == null) return false;
            var type = item::class.java;
            if (type.IsStringType() == false) return false;
            var v_string_value = item.toString()
            if (v_string_value.startsWith("Document{{") && v_string_value.endsWith("}}")) {
                return true;
            }
            return false;
        }

        fun proc(item: Any): Any {
            var v_string_value = item.toString()
            if (v_string_value.startsWith("Document{{") && v_string_value.endsWith("}}")) {
                //Document{{answerRole=Patriarch}}
                //目前只发现一个键值对形式的。
                var ary = v_string_value.Slice(10, -2).split("=")
                var json = Document();
                json.set(ary[0], ary[1]);
                return json;
            }
            return item;
        }

        RecursionUtil.recursionJson(value, { json, type ->
            if (type.isArray) {
                (json as Array<Any?>).forEachIndexed { index, it ->
                    if (it == null || !test(it)) {
                        return@forEachIndexed
                    }

                    json.set(index, proc(it));
                    return@forEachIndexed
                }
            } else if (json is MutableList<*>) {
                json.forEachIndexed { index, it ->
                    if (it == null || !test(it)) {
                        return@forEachIndexed
                    }
                    (json as MutableList<Any>)[index] = proc(it);
                }
            } else if (json is Map<*, *>) {
                json.keys.toTypedArray().forEachIndexed { index, it ->
                    if (it == null || !test(it)) {
                        return@forEachIndexed
                    }
                    (json as MutableMap<Any, Any>).set(it, proc(it));

                    return@forEachIndexed
                }
            } else {
                println("不识别的类型：" + json::class.java.name)
            }


            return@recursionJson true
        })
    }
}