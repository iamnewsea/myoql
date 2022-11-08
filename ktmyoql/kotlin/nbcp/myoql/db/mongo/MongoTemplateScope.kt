package nbcp.myoql.db.mongo

import nbcp.base.scope.IScopeData
import org.springframework.data.mongodb.core.MongoTemplate

data class MongoTemplateScope(val value: MongoTemplate): IScopeData