package nbcp.db.cache

import nbcp.comm.*
import java.lang.annotation.Inherited
import java.util.*

/**
 * 使用注解在指定单表上启用 RedisCache
 * 用于补充 DbEntityIndex 之外的缓存项，如id
 *
 * @see nbcp.db.DbEntityIndex
 */
@Inherited
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class RedisCacheDefine(
    /**
     * 分组的字段,mongo默认包含id
     */
    vararg val value: String
)

class RedisCacheColumns(): LinkedList<String>() {
    constructor(ary:Array<out String>):this(){
        this.addAll(ary)
    }
}