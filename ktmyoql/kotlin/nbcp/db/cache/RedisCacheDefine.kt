package nbcp.db.cache

import nbcp.comm.*
import java.lang.annotation.Inherited

/**
 *
 */
@Inherited
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class RedisCacheDefine(
    /**
     * 分组的字段
     */
    vararg val value: String = arrayOf()
)