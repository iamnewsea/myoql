package nbcp.db.mongo

import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.stereotype.Component
import java.lang.annotation.Inherited

/**
 *  Mongo实体的组
 */
@Repeatable
@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class MongoEntityGroup(val group: String)


/**
 * 标记Mongo实体删除之前是否保存到垃圾箱
 */
@Repeatable
@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class MongoEntitySysDustbin()


/**
 * 实体或字段更新后拦截
 * 用法：
 * @MongoSetted
 */
@Repeatable
@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Component
@Inherited
annotation class MongoSetted(val collectionName: String)

/**
 * 字段更新前拦截
 */
@Repeatable
@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Component
@Inherited
annotation class MongoSetting(val collectionName: String)


/**
 * 标记Mongo字段是另一个表字段的引用， 当另一个表字段更新后，更新该字段
 */
@Repeatable
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Component
@Inherited
annotation class MongoRefField(val masterCollectionName: String, val masterFieldName: String)
