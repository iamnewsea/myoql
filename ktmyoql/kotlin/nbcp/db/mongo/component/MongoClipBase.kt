package nbcp.db.mongo

import com.mongodb.DBCollection
import nbcp.base.extend.Slice
import org.springframework.data.mongodb.core.MongoTemplate
import nbcp.base.utils.SpringUtil
import nbcp.db.mongo.*
import org.springframework.data.mongodb.core.query.Criteria
import java.io.Serializable

/**
 * Created by udi on 17-4-24.
 */



//collectionClazz 是集合类型。
open class MongoClipBase(var collectionName: String): Serializable {

    var mongoTemplate = SpringUtil.getBean<MongoTemplate>();


    fun getCollection(): DBCollection{
        return mongoTemplate.mongoDbFactory.legacyDb.getCollection(this.collectionName)
    }

    fun getMongoCriteria(vararg where: Criteria): Criteria {
        if (where.size == 0) return Criteria();
        if (where.size == 1) return where[0];
        var first = where.first();
        return first.andOperator(*where.Slice(1).toTypedArray())
    }
}

interface IMongoWhereable {

}



fun <T:MongoClipBase> T.useTemplate(template: MongoTemplate):T{
    this.mongoTemplate = template;
    return this;
}