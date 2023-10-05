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
import org.elasticsearch.rest.RestStatus

/**
 * Created by udi on 17-7-10.
 */


/**
 * 获取所有索引。如果有别名，返回别名。
 */
fun RestClient.getAllIndex(): Set<String> {
    var client = RestHighLevelClient(RestClient.builder(*this.nodes.toTypedArray()));
    var request = GetAliasesRequest();
    var response = client.indices().getAlias(request, RequestOptions.DEFAULT)
    if (response.status() != RestStatus.OK) {
        throw response.exception
    }

    return response.aliases.map {
        if (it.value.isNotEmpty()) {
            return@map it.value.map { it.alias() }
        }
        return@map listOf(it.key.toString())
    }
        .Unwind()
        .toSet()
}
