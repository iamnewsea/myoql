package nbcp.db.mongo

import com.mongodb.DBCollection
import com.mongodb.client.MongoCollection
import nbcp.base.extend.Slice
import nbcp.base.extend.getLatest
import nbcp.base.extend.scopes
import org.springframework.data.mongodb.core.MongoTemplate
import nbcp.base.utils.SpringUtil
import nbcp.db.db
import nbcp.db.mongo.*
import org.bson.Document
import org.springframework.data.mongodb.core.query.Criteria
import java.io.Serializable

/**
 * Created by udi on 17-4-24.
 */



//collectionClazz 是集合类型。
open class MongoClipBase(var collectionName: String): Serializable {

    val mongoTemplate: MongoTemplate
        get() {
            return db.getDynamicMongoTemplateByCollectionName(collectionName) ?: scopes.getLatest<MongoTemplate>() ?: SpringUtil.getBean<MongoTemplate>()
        }


//    fun getCollection(): MongoCollection<Document> {
//        return mongoTemplate.mongoDbFactory.db.getCollection(this.collectionName)
//    }

    fun getMongoCriteria(vararg where: Criteria): Criteria {
        if (where.size == 0) return Criteria();
        if (where.size == 1) return where[0];
        return Criteria().andOperator(*where);
    }
}

interface IMongoWhereable {

}



//fun <T:MongoClipBase> T.useTemplate(template: MongoTemplate):T{
//    this.mongoTemplate = template;
//    return this;
//}