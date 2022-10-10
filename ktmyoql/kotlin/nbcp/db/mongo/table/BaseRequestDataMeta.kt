package nbcp.db.mongo.table

import nbcp.db.*
import nbcp.db.mongo.*
import nbcp.utils.*
import nbcp.comm.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.*

/**
 * 请求数据
 */
@nbcp.db.Cn(value = """请求数据""")
class BaseRequestDataMeta(private val parentPropertyName: String) : MongoColumnName() {
    constructor(value: MongoColumnName) : this(value.toString()) {}

    /**
     * 访问地址
     */
    @nbcp.db.Cn(value = """访问地址""")
    val url = mongoColumnJoin(this.parentPropertyName, "url")

    /**
     * 访问方法
     */
    @nbcp.db.Cn(value = """访问方法""")
    val method = mongoColumnJoin(this.parentPropertyName, "method")

    /**
     * 调用链Id
     */
    @nbcp.db.Cn(value = """调用链Id""")
    val traceId = mongoColumnJoin(this.parentPropertyName, "traceId")

    /**
     * 请求体
     */
    @nbcp.db.Cn(value = """请求体""")
    val body = mongoColumnJoin(this.parentPropertyName, "body")

    /**
     * 请求头
     */
    @nbcp.db.Cn(value = """请求头""")
    val header = MoerMetaMap(this.parentPropertyName, "header") /*:map*/

    /**
     * 客户端Ip
     */
    @nbcp.db.Cn(value = """客户端Ip""")
    val clientIP = mongoColumnJoin(this.parentPropertyName, "clientIP")
    override fun toString(): String {
        return mongoColumnJoin(this.parentPropertyName).toString()
    }
}

