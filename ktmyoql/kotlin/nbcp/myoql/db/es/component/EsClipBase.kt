package nbcp.myoql.db.es.component

import nbcp.base.extend.AsInt
import nbcp.base.extend.AsString
import nbcp.base.extend.HasValue
import nbcp.base.extend.scopes
import nbcp.base.utils.SpringUtil
import nbcp.myoql.db.db
import nbcp.myoql.db.es.EsIndexDataSource
import nbcp.myoql.db.es.RestClientScope
import org.elasticsearch.client.RestHighLevelClient
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
    val esTemplate: RestHighLevelClient
        get() {
            var isRead = this is EsBaseQueryClip || this is EsAggregateClip<*, *>;

            var config = SpringUtil.getBean<EsIndexDataSource>();
            var dataSourceName = config.getDataSourceName(this.collectionName, isRead)
            if (dataSourceName.HasValue) {
                var uri = SpringUtil.context.environment.getProperty("app.es.${dataSourceName}.ds.uri").AsString()
                var prefix = SpringUtil.context.environment.getProperty("app.es.${dataSourceName}.ds.prefix").AsString()
                var timeout = SpringUtil.context.environment.getProperty("app.es.${dataSourceName}.ds.timeout").AsInt()

                return db.es.getRestClient(uri, prefix, timeout);
            }

            var ds =
                db.es.esEvents.getDataSource(this.collectionName, isRead) ?: scopes.getLatest<RestClientScope>()?.value
            if (ds != null) {
                return ds;
            }


            return SpringUtil.getBean<RestHighLevelClient>()
        }
}

interface IEsWhereable {

}
