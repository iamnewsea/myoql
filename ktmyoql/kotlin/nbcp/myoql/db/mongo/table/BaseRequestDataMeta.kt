package nbcp.myoql.db.mongo.table

import nbcp.myoql.db.mongo.base.MongoColumnName

/**
 * 请求数据
 */
@nbcp.base.db.Cn(value = """请求数据""")
class BaseRequestDataMeta(private val parentPropertyName: String) : MongoColumnName() {
    constructor(value: MongoColumnName) : this(value.toString()) {}

    /**
     * 访问地址
     */
    @nbcp.base.db.Cn(value = """访问地址""")
    val url = mongoColumnJoin(this.parentPropertyName, "url")

    /**
     * 访问方法
     */
    @nbcp.base.db.Cn(value = """访问方法""")
    val method = mongoColumnJoin(this.parentPropertyName, "method")

    /**
     * 调用链Id
     */
    @nbcp.base.db.Cn(value = """调用链Id""")
    val traceId = mongoColumnJoin(this.parentPropertyName, "traceId")

    /**
     * 请求体
     */
    @nbcp.base.db.Cn(value = """请求体""")
    val body = mongoColumnJoin(this.parentPropertyName, "body")

    /**
     * 请求头
     */
    @nbcp.base.db.Cn(value = """请求头""")
    val header = MoerMetaMap(this.parentPropertyName, "header") /*:map*/

    /**
     * 客户端Ip
     */
    @nbcp.base.db.Cn(value = """客户端Ip""")
    val clientIP = mongoColumnJoin(this.parentPropertyName, "clientIP")
    override fun toString(): String {
        return mongoColumnJoin(this.parentPropertyName).toString()
    }
}

