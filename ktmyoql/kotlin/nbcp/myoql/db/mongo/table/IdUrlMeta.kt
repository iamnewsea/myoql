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


class IdUrlMeta(private val parentPropertyName: String) : MongoColumnName() {
    constructor(value: MongoColumnName) : this(value.toString()) {}

    val id = mongoColumnJoin(this.parentPropertyName, "_id")

    /**
     * 网络资源地址
     */
    @nbcp.base.db.Cn(value = """网络资源地址""")
    val url = mongoColumnJoin(this.parentPropertyName, "url")
    override fun toString(): String {
        return mongoColumnJoin(this.parentPropertyName).toString()
    }
}

