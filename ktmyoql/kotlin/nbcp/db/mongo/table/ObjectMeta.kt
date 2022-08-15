package nbcp.db.mongo.table

import nbcp.db.*
import nbcp.db.mongo.*
import nbcp.utils.*
import nbcp.comm.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component


//generate auto @2022-08-10 14:32:54

class ObjectMeta(private val parentPropertyName: String) : MongoColumnName() {
    constructor(value: MongoColumnName) : this(value.toString()) {}

    override fun toString(): String {
        return mongoColumnJoin(this.parentPropertyName).toString()
    }
}
