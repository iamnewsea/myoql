package nbcp.db.sql

import kotlin.reflect.KClass


//@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
//@Retention(AnnotationRetention.RUNTIME)
//annotation class SqlEntityGroup(val group: String)

//尽量不使用, 尽量使用 sql 数据库的列名.
//@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS, AnnotationTarget.FIELD)
//@Retention(AnnotationRetention.RUNTIME)
//annotation class SqlDbName(val name: String)


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
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class SqlAutoIncrementKey()

/**
 * 实体上定义的隔离分区键 ,它的维度要适中，起到隔离一批数据的作用。 (目前仅一组有效.),如 @DbUks("city_id")
 */
@Repeatable
@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class SqlRks(vararg val rkColumns: String)//多个逗号隔开

/**
 * 实体字段上定义的外键关系，如： @SqlFk("s_user", "id")
 */
@Repeatable
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class SqlFk(val refTable: String, val refTableColumn: String)


/**
 * 插入，或更新某个字段前，进行数据转换。
 * 使用方式，如在实体字段上定义 @ConverterValueToDb(TrimUppercaseConverter::class)
 */
@Repeatable
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class ConverterValueToDb(val converter: KClass<out IConverter>)


interface  IConverter{
    fun convert(value:String):String
}
