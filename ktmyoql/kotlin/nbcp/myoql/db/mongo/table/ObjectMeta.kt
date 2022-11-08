package nbcp.myoql.db.mongo.table

import nbcp.myoql.db.*
import nbcp.myoql.db.mongo.*
import nbcp.base.utils.*
import nbcp.base.comm.*
import nbcp.myoql.db.mongo.base.MongoColumnName
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.*


class ObjectMeta(private val parentPropertyName: String) : MongoColumnName() {
    constructor(value: MongoColumnName) : this(value.toString()) {}

    override fun toString(): String {
        return mongoColumnJoin(this.parentPropertyName).toString()
    }
}

