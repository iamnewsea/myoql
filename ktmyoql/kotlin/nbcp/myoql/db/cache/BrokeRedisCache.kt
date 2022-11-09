package nbcp.myoql.db.cache

import nbcp.base.comm.*;
import nbcp.base.db.*;
import nbcp.base.enums.*;
import nbcp.base.extend.*;
import nbcp.base.utils.*;
import nbcp.myoql.db.enums.*
import nbcp.myoql.db.*;
import nbcp.myoql.db.cache.*
import nbcp.myoql.db.comm.*
import nbcp.myoql.db.redis.scanKeys
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.StringRedisTemplate
import java.lang.annotation.Inherited
import kotlin.reflect.KClass

/**
 * Sql Update/Insert/Delete Cache
 */
@Inherited
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class BrokeRedisCache(
    /**
     * 如果 table 为空，则使用 table = tableClass.name
     */
    val tableClass: KClass<*> = Void::class,
    /**
     * 破坏表的隔离键，如: "cityCode"
     */
    val groupKey: String = "",
    /**
     * 破坏表的隔离键值，如: "010",如果使用参数变量,使用 # 开头
     */
    val groupValue: String = "",
    /**
     * 破坏表
     */
    val table: String = ""
) {
    companion object {
        internal val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
//
//        @JvmStatic
//        fun of(table: String, groupKey: String, groupValue: String): BrokeRedisCache {
//            return BrokeRedisCache(table = table, groupKey = groupKey, groupValue = groupValue)
//        }
//
//        @JvmStatic
//        fun brokeCache(table: String, groupKey: String, groupValue: String) {
//            return BrokeRedisCache.of(table, groupKey, groupValue).brokeCache()
//        }
    }
}

fun BrokeRedisCache.getTableName(): String {
    if (this.table.HasValue) return this.table;


    if (this.tableClass == Void::class) {
        throw RuntimeException("需要指定 主表!")
    }
    return this.tableClass.java.simpleName!!;
}

private val redisTemplate by lazy {
    return@lazy SpringUtil.getBean<StringRedisTemplate>()
}

fun BrokeRedisCache.resolveWithVariable(variableMap: Map<String,Any?>): BrokeRedisCache {
    val spelExecutor = CacheKeySpelExecutor(variableMap);
    return BrokeRedisCache(
        table = spelExecutor.getVariableValue(this.getTableName()),
        groupKey = spelExecutor.getVariableValue(this.groupKey),
        groupValue = spelExecutor.getVariableValue(this.groupValue)
    );
}

/**
 * 执行破坏缓存操作！
 */
fun BrokeRedisCache.brokeCache() {
    val cacheBroke = this;
    BrokeRedisCache.logger.Important("!执行破坏缓存! ${cacheBroke.ToJson()}")

    //场景： 对表全量删除
    if (cacheBroke.groupKey.isEmpty() || cacheBroke.groupValue.isEmpty()) {
        brokeJoinTable(redisTemplate, cacheBroke.table);

        //破坏主表
        val pattern = "sc:${cacheBroke.table}/*";
        redisTemplate.scanKeys(pattern) { key ->
            redisTemplate.delete(key)
            return@scanKeys true;
        }
        return;
    }

    //破坏连接表
    brokeJoinTable(redisTemplate, cacheBroke.table);

    //破坏没有隔离键的（如全量查询）,没有隔离键,分两种情况：
    //A 有连接表,没有隔离键的
    var pattern = "sc:${cacheBroke.table}/*/@*"
    redisTemplate.scanKeys(pattern) { key ->
        redisTemplate.delete(key)
        return@scanKeys true;
    }

    //B 没有连接表,没有隔离键的
    pattern = "sc:${cacheBroke.table}/@*"
    redisTemplate.scanKeys(pattern) { key ->
        redisTemplate.delete(key)
        return@scanKeys true;
    }

    //剩下的，都是有隔离键的了！

    //破坏其它维度的隔离键
    val notMatchGroup = cacheBroke.groupKey.map { "[^${it}]" }.joinToString("")
    pattern = "sc:${cacheBroke.table}/*\\?${notMatchGroup}[^=]*"
    redisTemplate.scanKeys(pattern) { key ->
        redisTemplate.delete(key)
        return@scanKeys true;
    }

    //破坏当前隔离键值。
    pattern = "sc:${cacheBroke.table}/*\\?${cacheBroke.groupKey}=${cacheBroke.groupValue}@*"
    redisTemplate.scanKeys(pattern) { key ->
        redisTemplate.delete(key)
        return@scanKeys true;
    }
}


private fun BrokeRedisCache.brokeJoinTable(redisTemplate: StringRedisTemplate, joinTableName: String) {
    var pattern = "*/${joinTableName}/*"

    redisTemplate.scanKeys(pattern) {
        redisTemplate.delete(it);
        return@scanKeys true;
    }
}