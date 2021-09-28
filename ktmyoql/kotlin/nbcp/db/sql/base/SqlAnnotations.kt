package nbcp.db.sql

import java.lang.reflect.Field
import kotlin.reflect.KClass

/**
 * 实体字段上定义自增列，一个实体只能有一个自增列
 *
 * 以下情况不需要定义 Uks：
 * 1. 实体表中字段定义了 @SqlAutoIncrementKey
 * 2. 实体表中字段定义了 @DbKey ,多个字段定义认为是组合主键。
 *
 * 所以框架识别主键的顺序是：
 * 1. @SqlAutoIncrementKey
 * 2. @DbKey
 * 3. @DbUks
 *
 * 如果没有 Pk，则生成实体报错。
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class SqlAutoIncrementKey()

/**
 * 实体上定义的隔离分区键 ,它的维度要适中，起到隔离一批数据的作用。 (目前仅一组有效.),如 @DbUks("city_id")
 */
//@Repeatable
//@Target(AnnotationTarget.CLASS)
//@Retention(AnnotationRetention.RUNTIME)
//annotation class SqlRks(vararg val rkColumns: String)//多个逗号隔开

/**
 * 实体字段上定义的外键关系，如： @SqlFk("s_user", "id")
 */
@Repeatable
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class SqlFk(val refTable: String, val refTableColumn: String)


/**
 * 实体字段上定义，表示该复杂对象字段在数据库默认上对应多个列。使用下划线展开对象的每个字段列。
 */
@Repeatable
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class SqlSpreadColumn()

/**
 * 插入，或更新某个字段前，进行数据转换。
 * 使用方式，如在实体字段上定义 @ConverterValueToDb(TrimUppercaseConverter::class)
 * TODO 之后改为 vararg
 */
@Repeatable
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class ConverterValueToDb(val value: KClass<out IConverter>)


interface IConverter {
    /**
     * @param field 实体字段
     */
    fun convert(field:Field , value: Any?): Any?
}
