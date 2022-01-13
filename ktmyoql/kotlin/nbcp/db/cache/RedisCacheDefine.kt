package nbcp.db.cache

import nbcp.comm.*
import java.lang.annotation.Inherited

/**
 * 使用注解在指定单表上启用 RedisCache, id是默认分组字段
 */
//@Inherited
//@Target(AnnotationTarget.CLASS)
//@Retention(AnnotationRetention.RUNTIME)
//annotation class RedisCacheDefine(
//    /**
//     * 分组的字段,mongo默认包含id
//     */
//    vararg val value: String
//)