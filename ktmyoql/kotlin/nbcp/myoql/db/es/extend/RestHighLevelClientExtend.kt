package nbcp.myoql.db.es.extend

import nbcp.base.comm.JsonMap
import nbcp.base.comm.const
import nbcp.base.extend.*
import nbcp.myoql.db.es.base.EsResultMsg
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest
import org.elasticsearch.client.ElasticsearchClient
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient

/**
 * Created by udi on 17-7-10.
 */


/**
 * 获取所有索引的别名。
 */
fun RestClient.getAllAlias(): Set<String> {
    var client = RestHighLevelClient(RestClient.builder(*this.nodes.toTypedArray()));
    var request = GetAliasesRequest();
    return client.indices().getAlias(request, RequestOptions.DEFAULT)
        .aliases
        .values
        .map { it.map { it.alias() } }
        .Unwind()
        .toSet();
}
