package nbcp.db.mongo


import org.slf4j.LoggerFactory
import java.io.Serializable
/**
 * Created by udi on 17-4-17.
 */


/**
 * MongoInsert
 */
class MongoInsertClip<M : MongoBaseMetaCollection<E>, E : Serializable>(var moerEntity: M) : MongoBaseInsertClip(moerEntity.tableName)  {

    companion object {
        private var logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    fun add(entity: E): MongoInsertClip<M, E> {
        super.addEntity(entity)
        return this;
    }
}