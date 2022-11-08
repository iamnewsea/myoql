package nbcp.myoql.db.mongo


import nbcp.myoql.db.mongo.component.MongoBaseMetaCollection
import org.slf4j.LoggerFactory
import java.io.Serializable

/**
 * Created by udi on 17-4-17.
 */


/**
 * MongoInsert
 */
class MongoInsertClip<M : MongoBaseMetaCollection<out Any>>(var moerEntity: M) :
    MongoBaseInsertClip(moerEntity.tableName) {

    companion object {
        private var logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    fun add(entity: Serializable): MongoInsertClip<M> {
        super.addEntity(entity)
        return this;
    }
}