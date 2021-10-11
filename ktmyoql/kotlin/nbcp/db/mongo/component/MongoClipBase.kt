package nbcp.db.mongo


import nbcp.comm.HasValue
import nbcp.comm.scopes
import org.springframework.data.mongodb.core.MongoTemplate
import nbcp.utils.*
import nbcp.db.db
import org.springframework.data.mongodb.core.query.Criteria
import java.io.Serializable
import java.lang.RuntimeException

/**
 * Created by udi on 17-4-24.
 */


//collectionClazz 是集合类型。
open class MongoClipBase(var collectionName: String) : Serializable {

    /**
     * 动态数据源：
     * 1. 配置文件
     * 2. 继承了 IDataSource 的 Bean
     * 3. 当前作用域
     * 4. 使用默认
     */
    val mongoTemplate: MongoTemplate
        get() {
            var isRead = this is MongoBaseQueryClip || this is MongoAggregateClip<*, *>;

            var config = SpringUtil.getBean<MongoCollectionDataSource>();
            var dataSourceName = config.getDataSourceName(this.collectionName, isRead)

            if (dataSourceName.HasValue) {
                var uri = SpringUtil.context.environment.getProperty("app.mongo.ds.${dataSourceName}-ds")
                return db.mongo.getMongoTemplateByUri(uri) ?: throw RuntimeException("创建Mongo连接失败");
            }

            var ds =
                db.mongo.mongoEvents.getDataSource(this.collectionName, isRead)
                    ?: scopes.GetLatest<MongoTemplateScope>()?.value
            if (ds != null) {
                return ds;
            }

            //最后不分读写
            return SpringUtil.getBean<MongoTemplate>()
        }

    fun getMongoCriteria(vararg where: Criteria): Criteria {
        if (where.size == 0) return Criteria();
        if (where.size == 1) return where[0];
        return Criteria().andOperator(*where);
    }
}

interface IMongoWhereable {

}
