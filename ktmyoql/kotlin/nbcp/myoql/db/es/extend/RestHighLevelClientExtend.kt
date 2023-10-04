package nbcp.myoql.db.es.extend

import nbcp.base.comm.JsonMap
import nbcp.base.comm.const
import nbcp.base.extend.*
import nbcp.myoql.db.es.base.EsResultMsg
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.Response
import org.elasticsearch.client.RestHighLevelClient

/**
 * Created by udi on 17-7-10.
 */


/**
 * 获取所有索引的别名。
 */
fun RestHighLevelClient.getAllIndexAlias(): Set<String> {
    var request = GetAliasesRequest();
    return this.indices().getAlias(request, RequestOptions.DEFAULT)
        .aliases
        .values
        .map { it.map { it.alias() } }
        .Unwind()
        .toSet();
}
