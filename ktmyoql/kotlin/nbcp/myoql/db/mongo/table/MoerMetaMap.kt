package nbcp.myoql.db.mongo.table

import nbcp.base.extend.HasValue
import nbcp.myoql.db.mongo.base.MongoColumnName


fun mongoColumnJoin(vararg args: String): MongoColumnName {
    return MongoColumnName(args.toList().filter { it.HasValue }.joinToString("."))
}


data class MoerMetaMap(val parentPropertyName: String) {
    constructor(vararg args: String): this(args.toList().filter { it.HasValue }.joinToString(".")) {
    }
    
    fun keys(keys: String): String {
        return this.parentPropertyName + "." + keys
    }
}


