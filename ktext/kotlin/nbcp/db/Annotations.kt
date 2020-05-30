package nbcp.db


/**
 * 指定数据库中表的名字
 */
@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class DbName(val name: String)

/**
 * 实体字段上定义主键列，如实体的多个字段定义Key，认为是组合主键。
 */
@Target(AnnotationTarget.TYPE, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Key()


/**
 * 实体的组
 */
@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class DbEntityGroup(val group: String)

