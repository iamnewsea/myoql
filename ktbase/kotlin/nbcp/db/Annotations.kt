package nbcp.db

import java.lang.annotation.Inherited


/**
 * 指定数据库中表或列的名字
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class DbName(val value: String)

/**
 * 中文化名称
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Cn(val value: String)


///**
// * 之后移除，使用 DbEntityIndex注解
// */
//@Target(AnnotationTarget.FIELD)
//@Retention(AnnotationRetention.RUNTIME)
//annotation class DbKey()

///**
// * 之后移除，使用 DbEntityIndex注解
// */
//@Repeatable
//@Target(AnnotationTarget.CLASS)
//@Retention(AnnotationRetention.RUNTIME)
//annotation class DbUks(vararg val value: String)

/**
 * 实体的组
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class DbEntityGroup(val value: String)

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
annotation class DbEntityIndexes(vararg val value: DbEntityIndex)

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@java.lang.annotation.Repeatable(DbEntityIndexes::class)
@Repeatable
annotation class DbEntityIndex(vararg val value: String, val unique: Boolean = false)
