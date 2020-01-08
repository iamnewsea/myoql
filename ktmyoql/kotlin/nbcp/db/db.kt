package nbcp.db

import com.mongodb.DBObject
import com.mongodb.MongoClient
import com.mongodb.MongoCredential
import com.mongodb.ServerAddress
import nbcp.base.extend.AsInt
import nbcp.base.extend.IsStringType
import nbcp.base.extend.Slice
import nbcp.base.extend.ToJson
import nbcp.base.utils.RecursionUtil
import nbcp.base.utils.SpringUtil
import nbcp.comm.JsonMap
import nbcp.comm.StringMap
import nbcp.comm.StringTypedMap
import nbcp.db.mongo.Date2LocalDateTimeConverter
import nbcp.db.mongo.MongoEventConfig
import nbcp.db.mongo.PipeLineOperatorEnum
import nbcp.db.sql.SqlBaseTable
import org.bson.Document
import org.slf4j.LoggerFactory
import org.springframework.core.convert.support.GenericConversionService
import org.springframework.data.mongodb.MongoDbFactory
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.SimpleMongoClientDbFactory
import org.springframework.data.mongodb.core.SimpleMongoDbFactory
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper
import org.springframework.data.mongodb.core.convert.MappingMongoConverter
import org.springframework.data.mongodb.core.mapping.MongoMappingContext
import org.springframework.data.mongodb.core.query.Criteria
import java.util.ArrayList
import kotlin.concurrent.getOrSet

enum class DatabaseEnum {
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
        return@lazy SpringUtil.getBean<MongoEventConfig>();
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

//    fun getMongoCriteria(vararg where: Criteria): Criteria {
//        if (where.size == 1) return where[0];
//        if (where.size == 0) return Criteria();
//        return Criteria().andOperator(*where);
//    }

    fun change_id2Id(value: Collection<*>, remove_id: Boolean = true) {
        value.forEach { v ->
            if (v == null) {
                return@forEach
            }

            if (v is MutableMap<*, *>) {
                change_id2Id(v, remove_id);
            } else if (v is Collection<*>) {
                change_id2Id(v, remove_id);
            }
        }
    }

    fun change_id2Id(value: MutableMap<*, *>, remove_id: Boolean = true) {
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
                change_id2Id(v, remove_id);
            } else if (v is Collection<*>) {
                change_id2Id(v, remove_id);
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
     *value 可能会是： Document{{answerRole=Patriarch}}
     */
    fun proc_document_json(value: Document) {
        RecursionUtil.recursionJson(value, { k, v, p ->
            if (v == null) return@recursionJson true
            var v_type = v::class.java;
            if (v_type.IsStringType() == false) return@recursionJson true

            var v_string_value = v.toString()
            if (v_string_value.startsWith("Document{{") && v_string_value.endsWith("}}")) {
                if (p is Document) {
                    //Document{{answerRole=Patriarch}}
                    var ary = v_string_value.Slice(10, -2).split("=")
                    var json = Document();
                    json.set(ary[0], ary[1]);
                    p.set(k, json);
                }
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
