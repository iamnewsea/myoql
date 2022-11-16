package nbcp.base.db.annotation

/**
 * 指定数据库中表或列的名字
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class DbName(val value: String)