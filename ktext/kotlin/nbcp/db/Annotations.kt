package nbcp.db

import org.springframework.core.annotation.AliasFor
import org.springframework.stereotype.Component
import java.lang.annotation.Inherited


/**
 * 指定数据库中表的名字
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class DbName(val value: String)


/**
 * 中文化名称
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Cn(val value: String)


/**
 * 实体字段上定义主键列，如实体的多个字段定义Key，认为是组合主键。
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class DbKey()


/**
 * 实体的组
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class DbEntityGroup(val value: String)


/**
 * 定义唯一键，每一项表示唯一键，如果唯一键多个用逗号分隔
 */
@Repeatable
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class DbUks(vararg val ukColumns: String)



@Target(AnnotationTarget.CLASS, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Defines(val values: Array<Define>)

/**
 * 字段定义，用于 Es实体 生成 Mapping
 */
@java.lang.annotation.Repeatable(Defines::class)
@Repeatable
@Inherited
@Target(AnnotationTarget.CLASS, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Define(val value: String, val key: String = "")