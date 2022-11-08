package nbcp.myoql.db.sql

import nbcp.base.scope.IScopeData
import javax.sql.DataSource

data class DataSourceScope(val value:DataSource): IScopeData