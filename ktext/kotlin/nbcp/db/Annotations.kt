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
annotation class DbKey()


/**
 * 实体的组
 */
@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class DbEntityGroup(val group: String)


/**
 * 定义唯一键，每一项表示唯一键，如果唯一键多个用逗号分隔
 */
@Repeatable
@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class DbUks(vararg val ukColumns: String)
