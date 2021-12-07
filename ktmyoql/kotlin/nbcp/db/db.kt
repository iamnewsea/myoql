package nbcp.db

import nbcp.comm.*
import nbcp.db.cache.*
import nbcp.utils.*
import nbcp.db.mongo.table.MongoBaseGroup
import nbcp.db.redis.RedisBaseGroup
import nbcp.db.sql.table.SqlBaseGroup
import java.time.Duration
import nbcp.scope.*
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
    val sql = db_sql;

    @JvmStatic
    val mongo = db_mongo;

    @JvmStatic
    val es = db_es;

    @JvmStatic
    val redis = DbRedis;

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
            if (scopes.getLatest(MyOqlOrmScope.IgnoreAffectRow) != null) {
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
            if (scopes.getLatest(MyOqlOrmScope.IgnoreExecuteTime) != null) {
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
    val mor_base
        get() = MongoBaseGroup()

    @JvmStatic
    val rer_base
        get() = RedisBaseGroup()

    @JvmStatic
    val sql_base
        get() = SqlBaseGroup()


//-------------------

    @JvmStatic
    fun usingRedisCache(cacheForSelect: FromRedisCache, sql: String, variableMap: JsonMap): FromRedisCacheData {
        val spelExecutor = CacheKeySpelExecutor(variableMap);
        return FromRedisCacheData(
            spelExecutor.getVariableValue(cacheForSelect.getTableName()),
            cacheForSelect.getJoinTableNames(),
            spelExecutor.getVariableValue(cacheForSelect.groupKey),
            spelExecutor.getVariableValue(cacheForSelect.groupValue),
            spelExecutor.getVariableValue(cacheForSelect.sql.AsString(sql)),
            cacheForSelect.cacheSeconds
        )
    }


    /**
     * 简单模式
     */
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
        return usingRedisCache(tableClass.simpleName, arrayOf(), groupKey, groupValue, sql)
            .getJson(
                tableClass,
                consumer
            )
    }


    @JvmStatic
    fun usingRedisCache(
        /**
         * 如果 table 为空，则使用 table = tableClass.name
         */
        tableClass: KClass<*> = Boolean::class,
        /**
         * 缓存关联表
         */
        joinTables: Array<String> = arrayOf(),
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
        cacheTime: Duration = Duration.ofHours(1),
    ): FromRedisCacheData {
        return usingRedisCache(tableClass.java.simpleName, joinTables, groupKey, groupValue, sql, cacheTime)
    }

    @JvmStatic
    @JvmOverloads
    fun usingRedisCache(
        table: String = "",
        /**
         * 缓存关联表
         */
        joinTables: Array<String> = arrayOf(),
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
        cacheTime: Duration = Duration.ofHours(1),
    ): FromRedisCacheData {
        return FromRedisCacheData(table, joinTables, groupKey, groupValue, sql, cacheTime.seconds.AsInt());
    }


    @JvmStatic
    fun brokeRedisCache(tableClass: KClass<*>, groupKey: String, groupValue: String) {
        return BrokeRedisCacheData(tableClass.java.simpleName, groupKey, groupValue).brokeCache()
    }

    @JvmStatic
    fun brokeRedisCache(table: String, groupKey: String, groupValue: String) {
        return BrokeRedisCacheData(table, groupKey, groupValue).brokeCache()
    }


    @JvmStatic
    fun brokeRedisCache(cacheForBroke: BrokeRedisCache, variableMap: JsonMap) {
        val spelExecutor = CacheKeySpelExecutor(variableMap);
        var broke = BrokeRedisCacheData(
            spelExecutor.getVariableValue(cacheForBroke.getTableName()),
            spelExecutor.getVariableValue(cacheForBroke.groupKey),
            spelExecutor.getVariableValue(cacheForBroke.groupValue)
        );

        broke.brokeCache();
    }
}
