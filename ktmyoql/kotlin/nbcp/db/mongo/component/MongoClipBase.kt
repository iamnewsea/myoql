package nbcp.db.mongo


import nbcp.comm.HasValue
import nbcp.comm.scopes
import org.springframework.data.mongodb.core.MongoTemplate
import nbcp.utils.*
import nbcp.db.db
import org.springframework.data.mongodb.core.query.Criteria
import java.io.Serializable
import java.lang.RuntimeException
import java.time.Duration

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

            //配置是定海神针。
            SpringUtil.getBean<MongoCollectionDataSource>().getDataSourceName(this.collectionName, isRead)
                    .apply {
                        if (this.HasValue) {
                            var uri = SpringUtil.context.environment.getProperty("app.mongo.${this}.ds.uri")
                            if (uri.isNullOrEmpty()) {
                                throw RuntimeException("Mongo数据源配置项为空:app.mongo.uris.${this}");
                            }
                            return db.mongo.getMongoTemplateByUri(uri) ?: throw RuntimeException("创建Mongo连接失败");
                        }
                    }

            //定义动态数据源
            db.mongo.mongoEvents.getDataSource(this.collectionName, isRead)
                    .apply {
                        if (this != null) {
                            return this;
                        }
                    }


            scopes.getLatest<MongoTemplateScope>()?.value
                    .apply {
                        if (this != null) {
                            return this;
                        }
                    }

            //最后不分读写
            return SpringUtil.getBean<MongoTemplate>()
        }


    val actualTableName by lazy {
        db.mongo.mongoEvents.getActualTableName(collectionName);
    }



    /**
     * 执行的语句
     */
    var script: String = ""
        get() = field
        protected set;

    /**
     * 影响行数.
     * 对于更新来说，是匹配行数
     * 对于删除来说，是删除的行数
     */
    var affectRowCount: Int = 0
        get() = field
        protected set(value) {
            field = value;
            db.affectRowCount = value;
        }

    var executeTime: Duration = Duration.ZERO
        get() = field
        protected set(value) {
            field = value;
            db.executeTime = value;
        }


}

interface IMongoWhere {
    val  whereData: MongoWhereClip
}
