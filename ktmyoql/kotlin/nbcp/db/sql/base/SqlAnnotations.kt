package nbcp.db.sql

import java.lang.annotation.Inherited
import java.lang.reflect.Field
import kotlin.reflect.KClass

/**
 * 实体字段上定义自增列，一个实体只能有一个自增列
 *
 * 所以框架识别主键的顺序是：
 * 1. @SqlAutoIncrementKey
 * 2. DbEntityIndexes 中的唯一索引 中，键个数最少的。
 * 3. DbEntityIndex  唯一索引
 *
 * 如果没有 Pk，则生成实体报错。
 * 作用： insert 时会忽略该键
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
annotation class SqlAutoIncrementKey()


/**
 * Sql中的 varchar长度
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
annotation class DataLength(val value: Int)


/**
 * 定义Sql的数据类型
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
annotation class SqlColumnType(val value: String)

/**
 * 实体上定义的外键关系，如： @SqlFks(SqlFk("s_user", "id"))
 */
@Repeatable
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
annotation class SqlFk(val fieldName: String, val refTable: String, val refTableColumn: String)


/**
 * 实体字段上定义，表示该复杂对象字段在数据库默认上对应多个列。使用下划线展开对象的每个字段列。
 */
@Repeatable
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class SqlSpreadColumn(val value: String = "_")

///**
// * 数据表检查项，注解可以继承
// */
//@Target(AnnotationTarget.FIELD)
//@Retention(AnnotationRetention.RUNTIME)
//@Inherited
//annotation class DbEntityChecks(vararg val value: DbEntityCheck)
//
///**
// * 数据表检查项，注解可以继承, 形如：  CHECK ( atr_len < stock )
// */
//@Target(AnnotationTarget.FIELD)
//@Retention(AnnotationRetention.RUNTIME)
//@java.lang.annotation.Repeatable(DbEntityChecks::class)
//@Repeatable
//@Inherited
//annotation class DbEntityCheck(val expression: String)

