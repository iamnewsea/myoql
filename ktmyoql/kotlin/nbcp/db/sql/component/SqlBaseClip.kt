package nbcp.db.sql

import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import nbcp.comm.*
import nbcp.base.extend.*
import nbcp.base.line_break
import nbcp.base.utils.SpringUtil
import nbcp.db.*
import nbcp.db.sql.component.JsonMapRowMapper
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl
import java.io.Serializable
import javax.sql.DataSource
import nbcp.db.sql.*

/*
 ORM解决80%的问题即可. 对于 自连接,复杂的查询, 直接写Sql吧.
 */

abstract class SqlBaseClip(var datasourceName: String) : Serializable {
    init {
        db.affectRowCount = -1
        db.lastAutoId = -1
    }

    companion object {
        private val defaultJdbcTemplate: JdbcTemplate = SpringUtil.getBean<JdbcTemplate>()

        val jsonMapMapper = SpringUtil.getBean<JsonMapRowMapper>()

        private val jdbcMap = linkedMapOf<String, JdbcTemplate>()

        // orm bean 代理 RequestCache 及 Redis Cache
        val cacheService = SpringUtil.getBean<IProxyCache4Sql>();


        fun getJdbcTemplateByDatasrouce(datasourceName: String): JdbcTemplate {
            if (datasourceName.HasValue) {
                var ret = jdbcMap.get(datasourceName)
                if (ret == null) {
                    jdbcMap.set(datasourceName, JdbcTemplate(SpringUtil.getBeanByName<DataSource>(datasourceName)))
                    ret = jdbcMap.get(datasourceName)
                }

                return ret!!
            } else return defaultJdbcTemplate
        }
    }

    val jdbcTemplate: JdbcTemplate
        get() = getJdbcTemplateByDatasrouce(datasourceName)


    val transactionTemplate: TransactionTemplate
        get() {
            if (datasourceName.HasValue) {
                var dataSource = SpringUtil.getBeanByName<DataSource>(datasourceName)
                var tx = DataSourceTransactionManager(dataSource)
                return TransactionTemplate(tx)
            }

            return TransactionTemplate(DataSourceTransactionManager(SpringUtil.getBean<DataSource>()))
        }


    abstract fun toSql(): SingleSqlData
}


abstract class SqlBaseQueryClip(private var mainEntity: SqlBaseTable<*>? = null) : SqlBaseClip(mainEntity?.datasourceName
        ?: "") {
    protected var skip = 0;
    protected var take = -1;
    protected var distinct = false;

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

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
            var valueType = (clazz.genericSuperclass as ParameterizedTypeImpl).actualTypeArguments[1] as Class<*>

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

    fun toScalarList(): List<Any?> {
        var ret = mutableListOf<Any?>()
        toMapList().forEach {
            ret.add(it.values.firstOrNull())
        }
        return ret;
    }

    protected fun toMapList(sql: SingleSqlData): MutableList<JsonMap> {
        db.affectRowCount = -1
        var retJsons = mutableListOf<Map<String, Any?>>()

        var cacheKey = cacheService.getCacheKey(sql)

        //先从SessionCache中找。

        var cacheJson = cacheService.getCacheJson(cacheKey)


        if (cacheJson.isNullOrEmpty()) {
            var executeData = sql.toExecuteSqlAndParameters()

//            logger.info(executeData.executeSql +"  [" + executeData.parameters.map { it.value.AsString() }.joinToString(",") +"]");
            var startAt = System.currentTimeMillis();

            var error = false;
            try {
                retJsons = jdbcTemplate.queryForList(executeData.executeSql, *executeData.executeParameters).toMutableList()

                if (retJsons.size > 0) {
                    //setCache

                    cacheService.setCacheJson(cacheKey, retJsons.ToJson())
                }

            } catch (e: Exception) {
                error = true;
                throw e;
            } finally {
                logger.InfoError(error) {
                    var msg_log = mutableListOf("[sql] ${executeData.executeSql}", "[参数] ${executeData.executeParameters.map { it.AsString() }.joinToString(",")}")
                    msg_log.add("[耗时] ${System.currentTimeMillis() - startAt} ms")
                    return@InfoError msg_log.joinToString(line_break)
                }
            }
        } else {
            //logger.info("sql query from cache: " + cacheKey.toString())
            retJsons = cacheJson.FromJson<MutableList<Map<String, Any?>>>()
        }

        db.affectRowCount = retJsons.size

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

    fun toScalar(): Any? {
        var ret = toMap()
        if (ret == null) return null;
        return ret.values.firstOrNull()
    }

    open fun exists(): Boolean {
        var ret = toMap()
        if (ret == null) return false;
        return ret.isNotEmpty();
    }
}

abstract class SqlBaseExecuteClip(private var mainEntity: SqlBaseTable<*>? = null) : SqlBaseClip(mainEntity?.datasourceName
        ?: "") {
    abstract fun exec(): Int
}


