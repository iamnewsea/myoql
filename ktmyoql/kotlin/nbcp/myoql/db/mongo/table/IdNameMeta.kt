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

class IdNameMeta(private val parentPropertyName: String) : MongoColumnName() {
    constructor(value: MongoColumnName) : this(value.toString()) {}

    val id = mongoColumnJoin(this.parentPropertyName, "_id")

    /**
     * 名称
     */
    @Cn(value = """名称""")
    val name = mongoColumnJoin(this.parentPropertyName, "name")
    override fun toString(): String {
        return mongoColumnJoin(this.parentPropertyName).toString()
    }
}

