package nbcp.myoql.db.mongo.table

import java.io.*
import nbcp.base.db.*
import nbcp.base.comm.*
import nbcp.base.extend.*
import nbcp.base.enums.*
import nbcp.base.utils.*
import nbcp.myoql.db.*
import nbcp.myoql.db.comm.*
import nbcp.myoql.db.enums.*
import nbcp.myoql.db.mongo.*
import nbcp.myoql.db.mongo.base.*
import nbcp.myoql.db.mongo.component.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.*


@nbcp.base.db.annotation.Cn(value = """请求数据""")
class BaseRequestDataMeta(private val parentPropertyName: String) : MongoColumnName() {
    constructor(value: MongoColumnName) : this(value.toString()) {}

    @nbcp.base.db.annotation.Cn(value = """访问地址""")
    val url = mongoColumnJoin(this.parentPropertyName, "url")

    @nbcp.base.db.annotation.Cn(value = """访问方法""")
    val method = mongoColumnJoin(this.parentPropertyName, "method")

    @nbcp.base.db.annotation.Cn(value = """调用链Id""")
    val traceId = mongoColumnJoin(this.parentPropertyName, "traceId")

    @nbcp.base.db.annotation.Cn(value = """请求体""")
    val body = mongoColumnJoin(this.parentPropertyName, "body")

    @nbcp.base.db.annotation.Cn(value = """请求头""")
    val header = MoerMetaMap(this.parentPropertyName, "header") /*:map*/

    @nbcp.base.db.annotation.Cn(value = """客户端Ip""")
    val clientIP = mongoColumnJoin(this.parentPropertyName, "clientIP")
    override fun toString(): String {
        return mongoColumnJoin(this.parentPropertyName).toString()
    }
}

