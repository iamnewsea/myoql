package nbcp.base.db.annotation

import java.lang.annotation.Inherited

/**
 * 更新时填充
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
annotation class UpdateFill