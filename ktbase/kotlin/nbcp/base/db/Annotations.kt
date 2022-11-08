package nbcp.base.db

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


/**
 * 插入时填充
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
annotation class InsertFill

/**
 * 更新时填充
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
annotation class UpdateFill


/**
 * 实体的组
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
annotation class DbEntityGroup(val value: String)

/**
 * 数据表索引，注解可以继承
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
annotation class DbEntityIndexes(vararg val value: DbEntityIndex)

/**
 * 数据表索引，注解可以继承
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@java.lang.annotation.Repeatable(DbEntityIndexes::class)
@Repeatable
@Inherited
annotation class DbEntityIndex(vararg val value: String, val unique: Boolean = false, val cacheable: Boolean = false)
