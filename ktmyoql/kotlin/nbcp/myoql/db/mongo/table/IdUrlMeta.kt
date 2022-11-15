package nbcp.myoql.db.mongo.table

import nbcp.myoql.db.mongo.base.MongoColumnName


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

