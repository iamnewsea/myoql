package nbcp.myoql.db.mongo.table

import nbcp.myoql.db.mongo.base.MongoColumnName


class CityCodeNameMeta(private val parentPropertyName: String) : MongoColumnName() {
    constructor(value: MongoColumnName) : this(value.toString()) {}

    val name = mongoColumnJoin(this.parentPropertyName, "name")

    val code = mongoColumnJoin(this.parentPropertyName, "code")
    override fun toString(): String {
        return mongoColumnJoin(this.parentPropertyName).toString()
    }
}

