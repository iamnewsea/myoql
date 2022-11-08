package nbcp.myoql.db.mongo.table

import nbcp.base.db.Cn
import nbcp.myoql.db.*
import nbcp.myoql.db.mongo.*
import nbcp.base.utils.*
import nbcp.base.comm.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.*
import nbcp.myoql.db.mongo.base.*

class IdUrlMeta(private val parentPropertyName: String) : MongoColumnName() {
    constructor(value: MongoColumnName) : this(value.toString()) {}

    val id = mongoColumnJoin(this.parentPropertyName, "_id")

    /**
     * 网络资源地址
     */
    @Cn(value = """网络资源地址""")
    val url = mongoColumnJoin(this.parentPropertyName, "url")
    override fun toString(): String {
        return mongoColumnJoin(this.parentPropertyName).toString()
    }
}

