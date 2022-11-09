package nbcp.myoql.db.mongo.table

import nbcp.myoql.db.mongo.base.MongoColumnName


class IdNameMeta(private val parentPropertyName: String) : MongoColumnName() {
    constructor(value: MongoColumnName) : this(value.toString()) {}

    val id = mongoColumnJoin(this.parentPropertyName, "_id")

    /**
     * 名称
     */
    @nbcp.base.db.Cn(value = """名称""")
    val name = mongoColumnJoin(this.parentPropertyName, "name")
    override fun toString(): String {
        return mongoColumnJoin(this.parentPropertyName).toString()
    }
}

