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


@Target(AnnotationTarget.TYPE, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class SqlAutoIncrementKey() //多个逗号隔开

/**
 * 隔离分区键 ,(目前仅一组有效.)
 */
@Repeatable
@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class SqlRks(vararg val rkColumns: String)//多个逗号隔开


@Repeatable
@Target(AnnotationTarget.TYPE, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class SqlFk(val refTable: String, val refTableColumn: String)




@Repeatable
@Target(AnnotationTarget.TYPE, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class ConverterValueToDb(val converter: KClass<out IConverter>)


interface  IConverter{
    fun convert(value:String):String
}
