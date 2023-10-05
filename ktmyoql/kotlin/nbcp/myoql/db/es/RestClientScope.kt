package nbcp.myoql.db.es

import nbcp.base.scope.IScopeData
import org.elasticsearch.client.RestClient

data class RestClientScope(val value: RestClient): IScopeData;