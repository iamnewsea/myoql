package nbcp.myoql.db.mongo.table

import nbcp.base.db.Cn
import nbcp.myoql.db.*
import nbcp.myoql.db.mongo.*
import nbcp.base.utils.*
import nbcp.base.comm.*
import nbcp.myoql.db.mongo.base.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.*

/**
 * 请求数据
 */
@Cn(value = """请求数据""")
class BaseRequestDataMeta(private val parentPropertyName: String) : MongoColumnName() {
    constructor(value: MongoColumnName) : this(value.toString()) {}

    /**
     * 访问地址
     */
    @Cn(value = """访问地址""")
    val url = mongoColumnJoin(this.parentPropertyName, "url")

    /**
     * 访问方法
     */
    @Cn(value = """访问方法""")
    val method = mongoColumnJoin(this.parentPropertyName, "method")

    /**
     * 调用链Id
     */
    @Cn(value = """调用链Id""")
    val traceId = mongoColumnJoin(this.parentPropertyName, "traceId")

    /**
     * 请求体
     */
    @Cn(value = """请求体""")
    val body = mongoColumnJoin(this.parentPropertyName, "body")

    /**
     * 请求头
     */
    @Cn(value = """请求头""")
    val header = MoerMetaMap(this.parentPropertyName, "header") /*:map*/

    /**
     * 客户端Ip
     */
    @Cn(value = """客户端Ip""")
    val clientIP = mongoColumnJoin(this.parentPropertyName, "clientIP")
    override fun toString(): String {
        return mongoColumnJoin(this.parentPropertyName).toString()
    }
}

