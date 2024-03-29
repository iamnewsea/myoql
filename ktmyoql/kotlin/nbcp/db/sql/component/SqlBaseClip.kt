package nbcp.db.sql

import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.JdbcTemplate
import nbcp.comm.*

import nbcp.utils.*
import nbcp.db.*
import nbcp.db.cache.ProxyCache4SqlService
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl
import java.io.Serializable
import java.time.LocalDateTime
import nbcp.db.mysql.MysqlConfig

/**
 * ORM解决80%的问题即可. 对于 自连接,复杂的查询, 直接写Sql吧.
 *
 * 1. 读写分离
 * https://www.jianshu.com/p/f728e8c131a9
 * 配置： spring.datasource 是主库的数据库连接。
 * 额外增加： spring.datasource.slave 表示是从库连接。参数继承 spring.datasource
 * 额外增加： spring.datasource.slave.hikari 表示是从库连接池。参数继承 spring.datasource.hikari
 * 这样的好处是，当没有配置spring.datasource.slave，可当单库使用。
 *
 * 2. 切数据源
 *
 * usingScope(db.getJdbcTemplate("primary")){
 *
 * }
 *
 * 3. 事务使用方式：
transTemplate.execute(new TransactionCallback<Object>() {
@Override
public Object doInTransaction(TransactionStatus transactionStatus) {
// DML执行
jdbcTemplate.update("Delete from actor_new where actor_id=?", 11);

// 回滚
transactionStatus.setRollbackOnly();
return null;
}
});
 */
abstract class SqlBaseClip(var tableName: String) : Serializable {
    init {
        db.affectRowCount = -1
        db.lastAutoId = -1
    }

    companion object {
//        private val defaultJdbcTemplate by lazy {
//            return@lazy SpringUtil.getBean<JdbcTemplate>()
//        }

//        val jsonMapMapper = SpringUtil.getBean<JsonMapRowMapper>()

//        private val jdbcMap = linkedMapOf<String, JdbcTemplate>()

        // orm bean 代理 RequestCache 及 Redis Cache
        @JvmStatic
        val cacheService by lazy {
            return@lazy SpringUtil.getBean<ProxyCache4SqlService>();
        }


//        fun getJdbcTemplateByDatasrouce(datasourceName: String): JdbcTemplate {
//            if (datasourceName.HasValue) {
//                var ret = jdbcMap.get(datasourceName)
//                if (ret == null) {
//                    jdbcMap.set(datasourceName, JdbcTemplate(SpringUtil.getBeanByName<DataSource>(datasourceName)))
//                    ret = jdbcMap.get(datasourceName)
//                }
//
//                return ret!!
//            } else return defaultJdbcTemplate
//        }
    }

    /**
     * 通过 usingScope 作用域 切换数据源。
     */
    val jdbcTemplate: JdbcTemplate
        get() {
            var ret = db.sql.getJdbcTemplateByTableName(tableName) ?: scopes.getLatestScope<JdbcTemplate>();
            if (ret != null) return ret;


            if (this is SqlBaseQueryClip) {
                if (MysqlConfig.hasSlave) {
                    return SpringUtil.getBeanByName<JdbcTemplate>("slaveJdbcTemplate")
                }
            }

            return SpringUtil.getBean<JdbcTemplate>()
        }


//    val transactionTemplate: TransactionTemplate
//        get() {
//            if (datasourceName.HasValue) {
//                var dataSource = SpringUtil.getBeanByName<DataSource>(datasourceName)
//                var tx = DataSourceTransactionManager(dataSource)
//                return TransactionTemplate(tx)
//            }
//
//            return TransactionTemplate(DataSourceTransactionManager(SpringUtil.getBean<DataSource>()))
//        }


    abstract fun toSql(): SingleSqlData
}


abstract class SqlBaseQueryClip(tableName: String) : SqlBaseClip(tableName) {
    protected var skip = 0;
    protected var take = -1;
    protected var distinct = false;

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    /**
     * 请使用 ConvertJson 方法
     */
    @Deprecated("请使用 ConvertJson 方法")
    fun <T3 : Any> mapToEntity(retJson: Map<String, Any?>, entityFunc: () -> T3): T3 {
        var entity = entityFunc()

        if (retJson.keys.any() == false) {
            return entity
        }

        var clazz = entity::class.java

        //只取第一列的值. JsonMap 也必须仅有一列.
        if (clazz.IsSimpleType()) {
            if (retJson.keys.size != 1) {
                throw RuntimeException("查询单列数据时返回了多列数据!")
            }
            var ret2 = retJson.values.firstOrNull()
            if (ret2 == null) {
                return entity
            }
            return ret2.ConvertType(clazz) as T3
        }

        if (Map::class.java.isAssignableFrom(clazz)) {

            //types[0] 必须是 String
            var valueType = (clazz.genericSuperclass as ParameterizedTypeImpl).GetActualClass(1);

            var entMap = entity as MutableMap<String, Any?>

            if (valueType == String::class.java) {
                retJson.forEach {
                    entMap.put(it.key, it.value.AsString())
                }
            } else if (Number::class.java.isAssignableFrom(valueType)) {
                retJson.forEach {
                    entMap.put(it.key, it.value?.ConvertType(valueType))
                }
            } else {
                retJson.forEach {
                    entMap.put(it.key, it.value)
                }
            }
            entMap.putAll(retJson)

            return entity
        }

        clazz.AllFields.forEach {
            if (retJson.containsKey(it.name) == false) {
                return@forEach
            }

            var value = retJson[it.name]?.ConvertType(it.type)

            if (value == null) {
                return@forEach
            }

            it.isAccessible = true
            it.set(entity, value);
        }

        return entity
    }

    fun toMapList(): MutableList<JsonMap> {
        return toMapList(toSql());
    }

    /**
     * 返回第一列值的集合。
     */
    fun toScalarList(): List<Any?> {
        var ret = mutableListOf<Any?>()
        toMapList().forEach {
            ret.add(it.values.firstOrNull())
        }
        return ret;
    }

    protected fun toMapList(sql: SingleSqlData): MutableList<JsonMap> {
        db.affectRowCount = -1

        var settings = db.sql.sqlEvents.onSelecting(this)
        if (settings.any { it.second != null && it.second!!.result == false }) {
            db.affectRowCount = 0;
            return mutableListOf();
        }

        var retJsons = mutableListOf<MutableMap<String, Any?>>()

        var cacheKey = cacheService.getCacheKey(sql)

        //先从SessionCache中找。

        var cacheJson = cacheService.getCacheJson(cacheKey)


        if (cacheJson.isNullOrEmpty()) {
            var executeData = sql.toExecuteSqlAndParameters()

//            logger.info(executeData.executeSql +"  [" + executeData.parameters.map { it.value.AsString() }.joinToString(",") +"]");
            var startAt = LocalDateTime.now();

            var error = false;
            try {
                retJsons = jdbcTemplate.queryForList(executeData.executeSql, *executeData.executeParameters).toMutableList()
                db.executeTime = LocalDateTime.now() - startAt

                if (retJsons.size > 0) {
                    //setCache
                    cacheService.setCacheJson(cacheKey, retJsons.ToJson())
                }

            } catch (e: Exception) {
                error = true;
                throw e;
            } finally {
                logger.InfoError(error) {
                    var msg_log = mutableListOf("" +
                            "[select] ${executeData.executeSql}",
                            "[参数] ${executeData.executeParameters.map { it.AsString() }.joinToString(",")}"
                    )

                    if (logger.debug) {
                        msg_log.add("[result] ${retJsons.ToJson()}")
                    } else {
                        msg_log.add("[result.size] ${retJsons.size}")
                    }

                    msg_log.add("[耗时] ${db.executeTime}")
                    return@InfoError msg_log.joinToString(line_break)
                }
            }
        } else {
            //logger.info("sql query from cache: " + cacheKey.toString())
            retJsons = cacheJson.FromJsonWithDefaultValue<MutableList<MutableMap<String, Any?>>>()
        }

        db.affectRowCount = retJsons.size

        settings.forEach {
            it.first.select(this, it.second,retJsons);
        }

        if (retJsons.size == 0) {
            return mutableListOf();
        }

        var retJson = retJsons.first()

        if (retJson.keys.any() == false) {
            return mutableListOf()
        }

        return retJsons.map { JsonMap(it) }.toMutableList()
    }

    open fun toMap(): JsonMap? {
        this.take = 1;
        return toMapList().firstOrNull()
    }

    /**
     * 返回第一列的值
     */
    fun toScalar(): Any? {
        var ret = toMap()
        if (ret == null) return null;
        return ret.values.firstOrNull()
    }

    /**
     * 判断是否存在，判断第一条记录是否为空
     */
    open fun exists(): Boolean {
        var ret = toMap()
        if (ret == null) return false;
        return ret.isNotEmpty();
    }
}

abstract class SqlBaseExecuteClip(tableName: String) : SqlBaseClip(tableName) {
    abstract fun exec(): Int
}


