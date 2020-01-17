package nbcp.db

import nbcp.db.mongo.MongoDeleteClip
import nbcp.db.mongo.MongoUpdateClip
import org.springframework.core.annotation.AliasFor
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.stereotype.Component
import java.lang.annotation.Inherited
import kotlin.reflect.KClass

/**
 *  Mongo实体的组
 */
@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class DbEntityGroup(val group: String)

/**
 * 生成元数据的组
 */
@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Component
annotation class DataGroup(val group: String)

/**
 * 标记Mongo实体删除之前是否保存到垃圾箱
 */
@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class MongoEntitySysDustbin()



/**
 * 标注 实体更新的 Bean，需要实现  IDbEntityUpdate
 */
@Repeatable
@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Component
@Inherited
annotation class DbEntityUpdate(val name: String)


/**
 * 标注 实体删除的 Bean，需要实现  IDbEntityDelete
 */
@Repeatable
@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Component
@Inherited
annotation class DbEntityDelete(val name: String)


/**
 * 重复性
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
annotation class DbEntityFieldRefs(val values: Array<DbEntityFieldRef>)

/**
 * 标记Mongo字段是另一个表字段的引用， 当另一个表字段更新后，更新该字段
 */
@java.lang.annotation.Repeatable(DbEntityFieldRefs::class)
@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Component
@Inherited
annotation class DbEntityFieldRef(val idField: String, val nameField: String, val masterEntityClass: String, val masterIdField: String, val masterNameField: String)


/**
 * 保存收集 DbEntityFieldRef 的Bean。
 * 冗余字段的引用。如 user.corp.name 引用的是  corp.name
 * 更新规则：
 * 如更新了引用实体，corp.id = 1 ,corp.name = 'a'
 * 则：
 * mor.定义的实体
 *  .where { it.corp.id match 1 }
 *  .set { it.corp.name to 'a' }
 *  .exec()
 *
 */
data class DbEntityFieldRefData(
        //实体，如 user
        var entityClass: Class<*>, //moer class
        //实体的引用Id， 如 "corp._id"
        var idField: String,
        //实体的冗余字段, 如： "corp.name"
        var nameField: String,
        // 引用的实体
        var masterEntityClass: Class<*>,
        //引用实体的Id字段， user.corp._id == corp._id
        var masterIdField: String,
        //冗余字段对应的引用实体字段， user.corp.name == corp.name
        var masterNameField: String
) {
    constructor(entityClass: Class<*>, annRef: DbEntityFieldRef) : this(
            entityClass, //moer class
            annRef.idField,
            annRef.nameField,
            Class.forName(annRef.masterEntityClass),
            annRef.masterIdField,
            annRef.masterNameField) {

    }
}

/**
 * 更新或删除事件执行的结果
 */
data class DbEntityEventResult(
        // 执行结果
        var result: Boolean = true,
        // 执行前，操作的额外数据。
        var extData: Any? = null
)

/**
 * 实体Update接口，标记 DbEntityUpdate 注解的类使用。
 */
interface IDbEntityUpdate {
    fun beforeUpdate(update: MongoUpdateClip<*>): DbEntityEventResult?

    fun update(update: MongoUpdateClip<*>, eventData: DbEntityEventResult?)
}

/**
 * 实体 Delete 接口，标记 DbEntityDelete 注解的类使用。
 */
interface IDbEntityDelete {
    fun beforeDelete(delete: MongoDeleteClip<*>): DbEntityEventResult?

    fun delete(delete: MongoDeleteClip<*>, eventData: DbEntityEventResult?)
}