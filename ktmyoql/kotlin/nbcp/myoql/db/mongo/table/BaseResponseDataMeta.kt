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

