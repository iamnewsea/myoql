package nbcp.myoql.db.mongo.table

import nbcp.myoql.db.mongo.base.MongoColumnName


class SerializableMeta(private val parentPropertyName: String) : MongoColumnName() {
    constructor(value: MongoColumnName) : this(value.toString()) {}

    override fun toString(): String {
        return mongoColumnJoin(this.parentPropertyName).toString()
    }
}

