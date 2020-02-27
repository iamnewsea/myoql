package nbcp.db

import nbcp.base.extend.*
import nbcp.base.utils.RecursionUtil
import nbcp.base.utils.SpringUtil
import nbcp.comm.StringMap
import nbcp.comm.StringTypedMap
import nbcp.db.mongo.*
import nbcp.db.mongo.MongoEntityEvent
import nbcp.db.sql.SqlBaseTable
import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.core.convert.support.GenericConversionService
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.SimpleMongoClientDbFactory
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper
import org.springframework.data.mongodb.core.convert.MappingMongoConverter
import org.springframework.data.mongodb.core.mapping.MongoMappingContext

enum class DatabaseEnum {
    Mongo,
    Redis,
    Hbase,

    Mysql,
    Oracle,
    Sqlite,
    Mssql
}

object db {
//    private val logger by lazy {
//        return@lazy LoggerFactory.getLogger(this::class.java)
//    }

    var currentDatabaseType: DatabaseEnum = DatabaseEnum.Mysql

    var getSqlEntity: ((tableName: String) -> SqlBaseTable<*>)? = null


    //     val sqlEvents = SpringUtil.getBean<SqlEventConfig>();
    val mongoEvents by lazy {
        return@lazy SpringUtil.getBean<MongoEntityEvent>();
    }

    fun getQuoteName(value: String): String {
        if (currentDatabaseType == DatabaseEnum.Mysql) {
            return "`${value}`"
        } else if (currentDatabaseType == DatabaseEnum.Mssql) {
            return "[${value}]"
        } else {
            return """"${value}""""
        }
    }

    private val _beforeExecuteDbData: ThreadLocal<Any?> = ThreadLocal.withInitial { return@withInitial null }

    //执行前保存的数据,可能会执行后用到。
    //删除数据之前，先查出来，执行成功后，放到垃圾箱。
    @JvmStatic
    var beforeExecuteDbData: Any?
        get() {
            return _beforeExecuteDbData.get()
        }
        set(value) {
            _beforeExecuteDbData.set(value);
        }


    private val _lastCommand: ThreadLocal<String> = ThreadLocal.withInitial { return@withInitial "" }

    //最后执行的命令
    @JvmStatic
    var lastCommand: String
        get() {
            return _lastCommand.get()
        }
        set(value) {
            _lastCommand.set(value);
        }


    private val _affectRowCount: ThreadLocal<Int> = ThreadLocal.withInitial { return@withInitial -1 }

    //最后执行的影响行数
    @JvmStatic
    var affectRowCount: Int
        get() {
            return _affectRowCount.get()
        }
        set(value) {
            if (scopes.getLatest<NoAffectRowCount>() != null) {
                return;
            }
            _affectRowCount.set(value);
        }


    private val _lastAutoId: ThreadLocal<Int> = ThreadLocal.withInitial { return@withInitial -1; }

    //对sql数据来说，记录最后一条插入数据的自增Id
    @JvmStatic
    var lastAutoId: Int
        get() {
            return _lastAutoId.get()
        }
        set(value) {
            _lastAutoId.set(value);
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
                procResultData_id2Id(v, remove_id);
            } else if (v is Collection<*>) {
                procResultData_id2Id(v, remove_id);
            } else if (v is Array<*>) {
                procResultData_id2Id(v, remove_id);
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

//    fun change_id2Id(value: DBObject, remove_id: Boolean = true) {
//        var keys = value.keySet().toTypedArray();
//        var needReplace = keys.contains("_id") && !keys.contains("id")
//        for (k in keys) {
//            var v = value.get(k);
//            if (needReplace && (k == "_id")) {
//                value.put("id", v?.toString() ?: "");
//                if (remove_id) {
//                    value.removeField("_id")
//                }
//                needReplace = false;
//                continue;
//            }
//            if (v == null) {
//                continue;
//            }
//            if (v is MutableMap<*, *>) {
//                change_id2Id(v, remove_id);
//            } else if (v is DBObject) {
//                change_id2Id(v, remove_id);
//            }
//        }
//    }

    /**
     * 把 Document 推送到数据库，需要转换 id
     */
    fun procSetDocumentData(value: Any): Any {
        RecursionUtil.recursionJson(value, { json,type ->
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
                (json as Array<Any>).forEachIndexed { index, it ->
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

    private var dynamicMongoMap = StringMap();
    fun setDynamicMongo(collectionName: String, connectionUri: String) {
        this.dynamicMongoMap.set(collectionName, connectionUri)
    }

    private var dynamicMongoTemplate = StringTypedMap<MongoTemplate>();
    fun getDynamicMongoTemplateByCollectionName(collectionName: String): MongoTemplate? {
        var uri = dynamicMongoMap.get(collectionName);
        if (uri == null) return null;

        return getDynamicMongoTemplateByUri(uri)
    }

    /**
     * 根据Uri获取 MongoTemplate，会缓存
     */
    fun getDynamicMongoTemplateByUri(uri: String): MongoTemplate? {
        return dynamicMongoTemplate.getOrPut(uri, {

            var dbFactory = SimpleMongoClientDbFactory(uri);
            val converter = MappingMongoConverter(DefaultDbRefResolver(dbFactory), MongoMappingContext())
            converter.setTypeMapper(DefaultMongoTypeMapper(null));
            (converter.conversionService as GenericConversionService).addConverter(Date2LocalDateTimeConverter())

            return@getOrPut MongoTemplate(dbFactory, converter);
        })
    }

    val mongo = db_mongo
}
