package nbcp.db.es

import nbcp.scope.IScopeData
import org.elasticsearch.client.RestClient

data class RestClientScope(val value:RestClient):IScopeData;