package nbcp.myoql.db.mongo.table

import nbcp.myoql.db.mongo.base.MongoColumnName

/**
 * 回发数据
 */
@nbcp.base.db.Cn(value = """回发数据""")
class BaseResponseDataMeta(private val parentPropertyName: String) : MongoColumnName() {
    constructor(value: MongoColumnName) : this(value.toString()) {}

    /**
     * 状态码
     */
    @nbcp.base.db.Cn(value = """状态码""")
    val status = mongoColumnJoin(this.parentPropertyName, "status")

    /**
     * 响应体
     */
    @nbcp.base.db.Cn(value = """响应体""")
    val body = mongoColumnJoin(this.parentPropertyName, "body")

    /**
     * 响应头
     */
    @nbcp.base.db.Cn(value = """响应头""")
    val header = MoerMetaMap(this.parentPropertyName, "header") /*:map*/

    /**
     * 结果
     */
    @nbcp.base.db.Cn(value = """结果""")
    val result = mongoColumnJoin(this.parentPropertyName, "result")
    override fun toString(): String {
        return mongoColumnJoin(this.parentPropertyName).toString()
    }
}

