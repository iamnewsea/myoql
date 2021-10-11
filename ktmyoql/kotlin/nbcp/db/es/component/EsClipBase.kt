package nbcp.db.es

import nbcp.comm.*
import nbcp.utils.*
import nbcp.db.db
import nbcp.db.es.tool.EsIndexDataSource
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
    val esTemplate: RestClient
        get() {
            var isRead = this is EsBaseQueryClip || this is EsAggregateClip<*, *>;

            var config = SpringUtil.getBean<EsIndexDataSource>();
            var dataSourceName = config.getDataSourceName(this.collectionName, isRead)
            if (dataSourceName.HasValue) {
                var uri = SpringUtil.context.environment.getProperty("app.es.ds.${dataSourceName}-ds.uri").AsString()
                var prefix = SpringUtil.context.environment.getProperty("app.es.ds.${dataSourceName}-ds.prefix").AsString()
                var timeout = SpringUtil.context.environment.getProperty("app.es.ds.${dataSourceName}-ds.timeout").AsInt()

                return db.es.getRestClient(uri, prefix, timeout);
            }

            var ds =
                db.es.esEvents.getDataSource(this.collectionName, isRead) ?: scopes.GetLatest<RestClientScope>()?.value
            if (ds != null) {
                return ds;
            }


            return SpringUtil.getBean<RestClient>()
        }
}

interface IEsWhereable {

}
