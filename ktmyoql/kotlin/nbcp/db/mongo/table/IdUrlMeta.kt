package nbcp.db.mongo.table

import nbcp.db.*
import nbcp.db.mongo.*
import nbcp.utils.*
import nbcp.comm.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.*


class IdUrlMeta(private val parentPropertyName: String) : MongoColumnName() {
    constructor(value: MongoColumnName) : this(value.toString()) {}

    val id = mongoColumnJoin(this.parentPropertyName, "_id")

    /**
     * 网络资源地址
     */
    @nbcp.db.Cn(value = """网络资源地址""")
    val url = mongoColumnJoin(this.parentPropertyName, "url")
    override fun toString(): String {
        return mongoColumnJoin(this.parentPropertyName).toString()
    }
}

