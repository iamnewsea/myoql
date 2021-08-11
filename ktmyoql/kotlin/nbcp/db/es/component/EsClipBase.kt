package nbcp.db.es

import nbcp.comm.GetLatest
import nbcp.comm.HasValue
import nbcp.comm.scopes
import nbcp.utils.*
import nbcp.db.db
import nbcp.db.es.tool.EsIndexDataSource
import nbcp.db.mongo.MongoAggregateClip
import nbcp.db.mongo.MongoBaseQueryClip
import nbcp.db.sql.SqlTableDataSource
import org.elasticsearch.client.RestClient
import org.springframework.data.mongodb.core.MongoTemplate
import java.io.Serializable

/**
 * Created by udi on 17-4-24.
 */


//collectionClazz 是集合类型。
open class EsClipBase(var collectionName: String) : Serializable {

    /**
     * 动态数据源：
     * 1. 配置文件
     * 2. 继承了 IDataSource 的 Bean
     * 3. 当前作用域
     * 4. 使用默认
     */
    val esTemplate: RestClient by lazy {
        var isRead = this is EsBaseQueryClip || this is EsAggregateClip<*, *>;

        var config = SpringUtil.getBean<EsIndexDataSource>();
        var dataSourceName = config.getDataSourceName(this.collectionName, isRead)
        if (dataSourceName.HasValue) {
            return@lazy SpringUtil.getBeanByName<RestClient>(dataSourceName)
        }

        var ds = db.es.esEvents.getDataSource(this.collectionName, isRead) ?: scopes.GetLatest<RestClient>()
        if (ds != null) {
            return@lazy ds;
        }


        return@lazy SpringUtil.getBean<RestClient>()
    }
}

interface IEsWhereable {

}
