package nbcp.myoql.db.mongo.table

import java.io.*
import nbcp.base.db.*
import nbcp.base.comm.*
import nbcp.base.extend.*
import nbcp.base.enums.*
import nbcp.base.utils.*
import nbcp.myoql.db.*
import nbcp.myoql.db.comm.*
import nbcp.myoql.db.enums.*
import nbcp.myoql.db.mongo.*
import nbcp.myoql.db.mongo.base.*
import nbcp.myoql.db.mongo.component.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.*



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


