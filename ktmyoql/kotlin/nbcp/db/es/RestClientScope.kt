package nbcp.db.es

import nbcp.scope.IScopeData
import org.elasticsearch.client.RestHighLevelClient

data class RestClientScope(val value: RestHighLevelClient):IScopeData;