package nbcp.myoql.db

import nbcp.base.extend.scopes
import nbcp.myoql.annotation.BrokeRedisCache
import nbcp.myoql.annotation.brokeCache
import nbcp.myoql.db.cache.*
import nbcp.myoql.db.enums.MyOqlDbScopeEnum
import nbcp.myoql.db.mongo.table.MongoBaseGroup
import nbcp.myoql.db.redis.RedisBaseGroup
import nbcp.myoql.db.sql.table.SqlBaseGroup
import java.time.Duration
import java.util.function.Supplier
import kotlin.reflect.KClass


/**
 * 数据库上下文，及操作类。
 */
object db {
//    private val logger by lazy {
//        return@lazy LoggerFactory.getLogger(this::class.java)
//    }

    @JvmStatic
    val sql = dbSql;

    @JvmStatic
    val mongo = dbMongo;

    @JvmStatic
    val es = dbEs;

    @JvmStatic
    val redis = nbcp.myoql.db.DbRedis;

//    private val _beforeExecuteDbData: ThreadLocal<Any?> = ThreadLocal.withInitial { return@withInitial null }
//
//    //执行前保存的数据,可能会执行后用到。
//    //删除数据之前，先查出来，执行成功后，放到垃圾箱。
//    @JvmStatic
//    var beforeExecuteDbData: Any?
//        get() {
//            return _beforeExecuteDbData.get()
//        }
//        set(value) {
//            _beforeExecuteDbData.set(value);
//        }


//    private val _lastCommand: ThreadLocal<String> = ThreadLocal.withInitial { return@withInitial "" }
//
//    //最后执行的命令
//    @JvmStatic
//    var lastCommand: String
//        get() {
//            return _lastCommand.get()
//        }
//        set(value) {
//            _lastCommand.set(value);
//        }


    private val _affectRowCount: ThreadLocal<Int> = ThreadLocal.withInitial { return@withInitial -1 }

    /**
     * 最后执行的影响行数，mongo,sql
     */
    @JvmStatic
    var affectRowCount: Int
        get() {
            return _affectRowCount.get()
        }
        set(value) {
            if (scopes.getLatest(MyOqlDbScopeEnum.IgnoreAffectRow) != null) {
                return;
            }
            _affectRowCount.set(value);
        }


    private val _lastAutoId: ThreadLocal<Int> = ThreadLocal.withInitial { return@withInitial -1; }

    /**
     * 对sql数据来说，记录最后一条插入数据的自增Id
     */
    @JvmStatic
    var lastAutoId: Int
        get() {
            return _lastAutoId.get()
        }
        set(value) {
            _lastAutoId.set(value);
        }

    private val _executeTime: ThreadLocal<Duration> = ThreadLocal.withInitial { return@withInitial Duration.ZERO; }

    /**
     * 记录最后一次操作的执行时间，单位毫秒
     */
    @JvmStatic
    var executeTime: Duration
        get() {
            return _executeTime.get()
        }
        set(value) {
            if (scopes.getLatest(MyOqlDbScopeEnum.IgnoreExecuteTime) != null) {
                return;
            }
            _executeTime.set(value);
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

    //    /**
//     * 填充 city.name
//     */
//    fun fillCityName(value: Any) {
//        RecursionUtil.recursionAny(value, { map, pkey ->
//            true
//        }, { list, pkey ->
//            true
//        }, { value, pkey ->
//            if (value is CityCodeName) {
//                if (value.code > 0 && value.name.isEmpty()) {
//                    value.name = db.rer_base.getCityNameByCode(value.code)
//                }
//            }
//            true
//        })
//    }
    @JvmStatic
    val morBase
        get() = MongoBaseGroup()

    @JvmStatic
    val rerBase
        get() = RedisBaseGroup()

    @JvmStatic
    val sqlBase
        get() = SqlBaseGroup()


//-------------------

//    @JvmStatic
//    fun usingRedisCache(cacheForSelect: FromRedisCache, sql: String, variableMap: JsonMap): FromRedisCache {
//        val spelExecutor = CacheKeySpelExecutor(variableMap);
//        return FromRedisCache(
//            cacheForSelect.tableClass,
//            cacheForSelect.joinTableClasses,
//            spelExecutor.getVariableValue(cacheForSelect.groupKey),
//            spelExecutor.getVariableValue(cacheForSelect.groupValue),
//            spelExecutor.getVariableValue(cacheForSelect.sql.AsString(sql)),
//            cacheForSelect.cacheSeconds,
//            spelExecutor.getVariableValue(cacheForSelect.getTableName()),
//            cacheForSelect.getJoinTableNames()
//        )
//    }

    @JvmStatic
    fun <T> getRedisCacheJson(
        tableClass: Class<T>,
        /**
         * 缓存表的隔离键, 如:"cityCode"
         */
        groupKey: String,
        /**
         * 缓存表的隔离值,如: "010"
         */
        groupValue: String,

        /**
         * 唯一值
         */
        sql: String,
        consumer: Supplier<T?>
    ): T? {
        return getRedisCacheJson(tableClass.simpleName, tableClass, groupKey, groupValue, sql, consumer)
    }

    /**
     * 简单模式
     */
    @JvmStatic
    fun <T> getRedisCacheJson(
        table: String,
        cacheType: Class<T>,
        /**
         * 缓存表的隔离键, 如:"cityCode"
         */
        groupKey: String,
        /**
         * 缓存表的隔离值,如: "010"
         */
        groupValue: String,

        /**
         * 唯一值
         */
        sql: String,
        consumer: Supplier<T?>
    ): T? {
        return FromRedisCache(table = table, groupKey = groupKey, groupValue = groupValue, sql = sql)
            .getJson(
                cacheType,
                consumer
            )
    }

    @JvmStatic
    fun <T> getRedisCacheList(
        tableClass: Class<T>,
        /**
         * 缓存表的隔离键, 如:"cityCode"
         */
        groupKey: String,
        /**
         * 缓存表的隔离值,如: "010"
         */
        groupValue: String,

        /**
         * 唯一值
         */
        sql: String,
        consumer: Supplier<List<T>?>
    ): List<T>? {
        return getRedisCacheList(tableClass.simpleName, tableClass, groupKey, groupValue, sql, consumer)
    }

    @JvmStatic
    fun <T> getRedisCacheList(
        table: String,
        cacheType: Class<T>,
        /**
         * 缓存表的隔离键, 如:"cityCode"
         */
        groupKey: String,
        /**
         * 缓存表的隔离值,如: "010"
         */
        groupValue: String,

        /**
         * 唯一值
         */
        sql: String,
        consumer: Supplier<List<T>?>
    ): List<T>? {
        return FromRedisCache(table = table, groupKey = groupKey, groupValue = groupValue, sql = sql)
            .getList(
                cacheType,
                consumer
            )
    }


    @JvmStatic
    fun brokeRedisCache(tableClass: KClass<*>, groupKey: String, groupValue: String) {
        return BrokeRedisCache(
            table = tableClass.java.simpleName,
            groupKey = groupKey,
            groupValue = groupValue
        ).brokeCache()
    }

    @JvmStatic
    fun brokeRedisCache(table: String, groupKey: String, groupValue: String) {
        return BrokeRedisCache(table = table, groupKey = groupKey, groupValue = groupValue).brokeCache()
    }
}
