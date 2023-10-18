package nbcp.myoql.db.comm


import nbcp.myoql.db.enums.DatabaseEnum
import java.lang.annotation.Inherited
import kotlin.reflect.KClass


/**
 * 生成元数据的组
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class MetaDataGroup(val dbType: DatabaseEnum, val value: String)

/**
 * 标记实体删除之前是否保存到垃圾箱
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
annotation class RemoveToSysDustbin()

/**
 * 逻辑删除
 * @param value: 表示逻辑删除的字段． true表示逻辑删除！
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
annotation class LogicalDelete(val value: String = "isDeleted")

/**
 * 可以按 groupBy 字段，自动设置排序的步长
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Repeatable
@Inherited
annotation class SortNumber(val field: String, val groupBy: String, val step: Int = 1)

/**
 * 变表
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
annotation class VarTable(val value: String)


/**
 * 变库
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
annotation class VarDatabase(val value: String)


/**
 * 树状表
 */
//@Target(AnnotationTarget.CLASS)
//@Retention(AnnotationRetention.RUNTIME)
//annotation class TreeTable(val parentField: String)

/**
 * 记录当更新某些字段时,记录到日志表
 * @param value: 要记录的字段名
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
annotation class DbEntityLogHistory(vararg val value: String)

/**
 * 重复性
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@Inherited
annotation class DbEntityFieldRefs(vararg val value: DbEntityFieldRef)

/**
 * 在引用表上定义 引用字段 与其它主表是映射关系。当其它主表的name字段更新后，级联更新引用的name字段
 * 例子： 对user表标注：
 * DbEntityFieldRef("corp","SysCorporation")
 * 当 SysCorporation.name 发生变化后， 该表自动更新。
 *
 * 如果引用数据是在数组内，则可以使用 DbEntityFieldRef("corp","SysCorporation")
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Repeatable
@Inherited
@java.lang.annotation.Repeatable(DbEntityFieldRefs::class)
annotation class DbEntityFieldRef(
    val field:String ,
    val refEntityClass: KClass<*>,
    val idField: String = "id",
    val refIdField: String = "id"
)


///**
// * 定义数据索引
// */
//@Target(AnnotationTarget.CLASS)
//@Retention(AnnotationRetention.RUNTIME)
//@Repeatable
//@Inherited
//annotation class DbEntityIndexes(vararg val value: DbEntityIndex)
//
//@Target(AnnotationTarget.CLASS)
//@Retention(AnnotationRetention.RUNTIME)
//@Repeatable
//@Inherited
//annotation class DbEntityIndex(val unique: Boolean, vararg val column: String)


//--------------------
@Inherited
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class DbDefines(vararg val value: DbDefine)

/**
 * 字段定义，用于 Es实体 生成 Mapping
 */
@Repeatable
@Inherited
@java.lang.annotation.Repeatable(DbDefines::class)
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class DbDefine(val fieldName: String, val define: String)


/**
 * ik分词器字段定义
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
annotation class IkFieldDefine(vararg val fieldNames: String)
//--------------------

