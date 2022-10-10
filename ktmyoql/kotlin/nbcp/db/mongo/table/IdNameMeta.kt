package nbcp.db.mongo.table

import nbcp.db.*
import nbcp.db.mongo.*
import nbcp.utils.*
import nbcp.comm.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.*


class IdNameMeta(private val parentPropertyName: String) : MongoColumnName() {
    constructor(value: MongoColumnName) : this(value.toString()) {}

    val id = mongoColumnJoin(this.parentPropertyName, "_id")

    /**
     * 名称
     */
    @nbcp.db.Cn(value = """名称""")
    val name = mongoColumnJoin(this.parentPropertyName, "name")
    override fun toString(): String {
        return mongoColumnJoin(this.parentPropertyName).toString()
    }
}

