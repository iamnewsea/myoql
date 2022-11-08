package nbcp.myoql.db.es

import nbcp.base.scope.IScopeData
import org.elasticsearch.client.RestHighLevelClient

data class RestClientScope(val value: RestHighLevelClient): IScopeData;