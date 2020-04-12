package nbcp.db.mongo

import nbcp.comm.getLatestScope
import nbcp.comm.scopes
import org.springframework.data.mongodb.core.MongoTemplate
import nbcp.utils.*
import nbcp.db.db
import org.springframework.data.mongodb.core.query.Criteria
import java.io.Serializable

/**
 * Created by udi on 17-4-24.
 */



//collectionClazz 是集合类型。
open class MongoClipBase(var collectionName: String): Serializable {

    val mongoTemplate: MongoTemplate
        get() {
            return db.mongo.getMongoTemplateByCollectionName(collectionName) ?: scopes.getLatestScope<MongoTemplate>() ?: SpringUtil.getBean<MongoTemplate>()
        }

    fun getMongoCriteria(vararg where: Criteria): Criteria {
        if (where.size == 0) return Criteria();
        if (where.size == 1) return where[0];
        return Criteria().andOperator(*where);
    }
}

interface IMongoWhereable {

}
