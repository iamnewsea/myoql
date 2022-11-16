package nbcp.base.db.annotation

/**
 * 中文化名称
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Cn(val value: String)