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


//@Retention(AnnotationRetention.RUNTIME)
//@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
//annotation class DbEntitySetteds(val values: Array<DbEntitySetted>)

/**
 * 实体或字段更新后拦截,该注解需要和 IDbEntitySetted 同时使用
 * 用法：
 * @MongoSetted
 */
@Repeatable
@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Component
@Inherited
annotation class DbEntityUpdate(val name: String)


//@Retention(AnnotationRetention.RUNTIME)
//@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
//annotation class DbEntitySettings(val values: Array<DbEntitySetting>)

/**
 * 字段更新前拦截,该注解需要和 IDbEntitySetting 同时使用
 */
//@Repeatable
//@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
//@Retention(AnnotationRetention.RUNTIME)
//@Component
//@Inherited
//annotation class DbEntitySetting(val entityClass: KClass<*>)


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


//enum class DbEntityEventEnum{
//    Setting,
//    Setted,
//    Deleting,
//    Deleted,
////    Adding,
////    Added
//}

//---------------------------
//data class DbEntityUpdateData(
//        var entityClass: Class<*>, //moer class
//        var eventBean: IDbEntityUpdate
//)
//
//
//data class DbEntityDeleteData(
//        var entityClass: Class<*>, //moer class
//        var eventBean: IDbEntityDelete
//)

/**
 * 冗余字段的引用。如 user.corp.name 引用的是  corp.name
 */
data class DbEntityFieldRefData(
        //实体，如 user
        var entityClass: Class<*>, //moer class
        //实体的引用Id
        var idField: String,
        var nameField: String,
        var masterEntityClass: Class<*>,
        var masterIdField: String,
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

data class DbEntityEventResult(
        var result: Boolean = true,
        var extData: Any? = null
)

interface IDbEntityUpdate {
    fun beforeUpdate(update: MongoUpdateClip<*>): DbEntityEventResult?

    fun update(update: MongoUpdateClip<*>, eventData: DbEntityEventResult?)
}


interface IDbEntityDelete {
    fun beforeDelete(delete: MongoDeleteClip<*>): DbEntityEventResult?

    fun delete(delete: MongoDeleteClip<*>, eventData: DbEntityEventResult?)
}