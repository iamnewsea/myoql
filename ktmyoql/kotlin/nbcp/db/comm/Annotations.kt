package nbcp.db

import org.springframework.core.annotation.AliasFor
import org.springframework.stereotype.Component
import java.lang.annotation.Documented
import java.lang.annotation.Inherited
import kotlin.reflect.KClass


/**
 * 生成元数据的组
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class MetaDataGroup(val value: String)

/**
 * 标记实体删除之前是否保存到垃圾箱
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class RemoveToSysDustbin()

/**
 * 变表
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class VarTable(val value: String)


/**
 * 变库
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class VarDatabase(val value: String)

/**
 * 记录当更新某些字段时,记录到日志表
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class DbEntityLogHistory(vararg val value: String)

/**
 * 重复性
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class DbEntityFieldRefs(vararg val value: DbEntityFieldRef)

/**
 * 在引用表上定义 引用字段 与其它主表是映射关系。当其它主表的name字段更新后，级联更新引用的name字段
 * 例子： 对user表标注：
 * DbEntityFieldRef("corp.id","corp.name","SysCorporation","id","name")
 * 或，如果fieldName相同，可省略后面的name
 * DbEntityFieldRef("corp.id","corp.name","SysCorporation")
 * 当 SysCorporation.name 发生变化后， 该表自动更新。
 *
 * 如果引用数据是在数组内，则可以使用 DbEntityFieldRef("corp.id","corp.$.name","SysCorporation","id","name")
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
//@java.lang.annotation.Repeatable(DbEntityFieldRefs::class)
@Repeatable
@Inherited
annotation class DbEntityFieldRef(
        val idField: String,
        val nameField: String,
        val refEntityClass: KClass<*>,
        val refIdField: String = "",
        val refNameField: String = ""
)



