package nbcp.db

import com.mongodb.DBObject
import com.mongodb.MongoClient
import com.mongodb.MongoCredential
import com.mongodb.ServerAddress
import nbcp.base.extend.AsInt
import nbcp.base.extend.IsStringType
import nbcp.base.extend.Slice
import nbcp.base.utils.RecursionUtil
import nbcp.base.utils.SpringUtil
import nbcp.db.mongo.MongoEventConfig
import nbcp.db.sql.SqlBaseTable
import org.bson.Document
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.MongoDbFactory
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.SimpleMongoDbFactory
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

    private val execAffectRows: ThreadLocal<Int> = ThreadLocal.withInitial { return@withInitial -1 }

    @JvmStatic
    var affectRowCount: Int
        get() {
            return execAffectRows.get()
        }
        set(value) {
            execAffectRows.set(value);
        }


    private val execLastAutoId: ThreadLocal<Int> = ThreadLocal.withInitial { return@withInitial -1; }

    @JvmStatic
    var lastAutoId: Int
        get() {
            return execLastAutoId.get()
        }
        set(value) {
            execLastAutoId.set(value);
        }

//    fun getMongoCriteria(vararg where: Criteria): Criteria {
//        if (where.size == 1) return where[0];
//        if (where.size == 0) return Criteria();
//        return Criteria().andOperator(*where);
//    }

    fun change_id2Id(value: MutableMap<String, Any>, remove_id: Boolean = true) {
        var keys = value.keys.toTypedArray();
        var needReplace = keys.contains("_id") && !keys.contains("id")
        for (k in keys) {
            var v = value.get(k);
            if (needReplace && (k == "_id")) {
                value.set("id", v?.toString() ?: "");
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
                change_id2Id(v as MutableMap<String, Any>, remove_id);
            } else if (v is DBObject) {
                change_id2Id(v, remove_id);
            }
        }
    }

    fun change_id2Id(value: DBObject, remove_id: Boolean = true) {
        var keys = value.keySet().toTypedArray();
        var needReplace = keys.contains("_id") && !keys.contains("id")
        for (k in keys) {
            var v = value.get(k);
            if (needReplace && (k == "_id")) {
                value.put("id", v?.toString() ?: "");
                if (remove_id) {
                    value.removeField("_id")
                }
                needReplace = false;
                continue;
            }
            if (v == null) {
                continue;
            }
            if (v is MutableMap<*, *>) {
                change_id2Id(v as MutableMap<String, Any>, remove_id);
            } else if (v is DBObject) {
                change_id2Id(v, remove_id);
            }
        }
    }

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

//    fun getMongoTemplate(
//            host: String,
//            port: Int,
//            databaseName: String,
//            userName: String,
//            password: String): MongoTemplate {
//
//        //ServerAddress(host,port)两个参数分别为 IP地址 端口号
//        //ServerAddress(host,port)两个参数分别为 IP地址 端口号
//        val serverAddress = ServerAddress(host, port)
//        val addrs: MutableList<ServerAddress> = ArrayList<ServerAddress>()
//        addrs.add(serverAddress)
//
//        //MongoCredential.createScramSha1Credential(username,source,password)三个参数分别为 用户名 数据库名称 密码
//        //MongoCredential.createScramSha1Credential(username,source,password)三个参数分别为 用户名 数据库名称 密码
//        val credential: MongoCredential = MongoCredential.createScramSha1Credential(userName, databaseName, password.toCharArray())
//        val credentials: MutableList<MongoCredential> = ArrayList<MongoCredential>()
//        credentials.add(credential)
//
//        //通过连接认证获取MongoDB连接
//        //通过连接认证获取MongoDB连接
//        val mongoClient = MongoClient(addrs, credentials)
//        val mongoDbFactory: MongoDbFactory = SimpleMongoDbFactory(mongoClient, databaseName)
//        //SimpleMongoClientDbFactory("mongodb://dev:123@52borui.cn:27017/edu_report_10")
//
//        return MongoTemplate(mongoDbFactory)
//    }
}
