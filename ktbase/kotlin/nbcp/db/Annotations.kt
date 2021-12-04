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

/**
 * 变表，表名中包含变量
 * @param value: 表示变表的变量名。无存储意义。
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class VarTable(val value: String)

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
annotation class DbUks(vararg val value: String)


@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class DbDefines(vararg val value: DbDefine)

/**
 * 字段定义，用于 Es实体 生成 Mapping
 */
@Repeatable
@Inherited
@Target(AnnotationTarget.CLASS, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class DbDefine(val fieldName: String, val define: String)