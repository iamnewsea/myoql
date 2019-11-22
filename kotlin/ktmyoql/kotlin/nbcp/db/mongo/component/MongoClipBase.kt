package nbcp.db.mongo

import com.mongodb.DBCollection
import org.springframework.data.mongodb.core.MongoTemplate
import nbcp.base.utils.SpringUtil
import nbcp.db.mongo.*
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
}

interface IMongoWhereable {

}



fun <T:MongoClipBase> T.useTemplate(template: MongoTemplate):T{
    this.mongoTemplate = template;
    return this;
}