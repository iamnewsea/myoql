package nbcp.db.sql

import nbcp.scope.IScopeData
import javax.sql.DataSource

data class DataSourceScope(val value:DataSource):IScopeData