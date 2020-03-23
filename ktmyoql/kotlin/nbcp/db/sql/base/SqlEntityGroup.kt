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
 * 如果不指定 MysqlPk ，且 表包含 @Id , 则 Pk = @id
 * 如果没有Pk，则生成实体报错。
 */
@Repeatable
@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class SqlUks(vararg val ukColumns: String) //多个逗号隔开,多组。

/**
 * 实体字段上定义自增列，一个实体只能有一个自增列
 */
@Target(AnnotationTarget.TYPE, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class SqlAutoIncrementKey()

/**
 * 实体上定义的隔离分区键 ,它的维度要适中，起到隔离一批数据的作用。 (目前仅一组有效.),如 @SqlUks("city_id")
 */
@Repeatable
@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class SqlRks(vararg val rkColumns: String)//多个逗号隔开

/**
 * 实体字段上定义的外键关系，如： @SqlFk("s_user", "id")
 */
@Repeatable
@Target(AnnotationTarget.TYPE, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class SqlFk(val refTable: String, val refTableColumn: String)


/**
 * 插入，或更新某个字段前，进行数据转换。
 * 使用方式，如在实体字段上定义 @ConverterValueToDb(TrimUppercaseConverter::class)
 */
@Repeatable
@Target(AnnotationTarget.TYPE, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class ConverterValueToDb(val converter: KClass<out IConverter>)


interface  IConverter{
    fun convert(value:String):String
}
