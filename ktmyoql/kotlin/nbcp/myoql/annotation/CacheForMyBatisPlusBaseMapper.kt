package nbcp.myoql.annotation

import java.lang.annotation.Inherited
import kotlin.reflect.KClass


/**
 * @param value 主表名
 */
@Inherited
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class CacheForMyBatisPlusBaseMapper(
    val value: KClass<*>,
    val groupKey: String = "",
    val cacheSeconds: Int = 3600
)