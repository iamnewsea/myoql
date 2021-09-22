package nbcp.db.mongo

import nbcp.scope.IScopeData
import org.springframework.data.mongodb.core.MongoTemplate

data class MongoTemplateScope(val value: MongoTemplate):IScopeData