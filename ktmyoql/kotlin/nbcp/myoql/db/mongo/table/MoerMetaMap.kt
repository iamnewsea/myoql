package nbcp.myoql.db.mongo.table

import nbcp.myoql.db.*
import nbcp.myoql.db.mongo.*
import nbcp.base.utils.*
import nbcp.base.comm.*
import nbcp.base.extend.HasValue
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.*
import nbcp.myoql.db.mongo.base.*


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


