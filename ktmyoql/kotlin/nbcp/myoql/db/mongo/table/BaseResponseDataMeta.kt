package nbcp.myoql.db.mongo.table

import nbcp.base.db.Cn
import nbcp.myoql.db.*
import nbcp.myoql.db.mongo.*
import nbcp.base.utils.*
import nbcp.base.comm.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.*
import nbcp.myoql.db.mongo.base.*
/**
 * 回发数据
 */
@Cn(value = """回发数据""")
class BaseResponseDataMeta(private val parentPropertyName: String) : MongoColumnName() {
    constructor(value: MongoColumnName) : this(value.toString()) {}

    /**
     * 状态码
     */
    @Cn(value = """状态码""")
    val status = mongoColumnJoin(this.parentPropertyName, "status")

    /**
     * 响应体
     */
    @Cn(value = """响应体""")
    val body = mongoColumnJoin(this.parentPropertyName, "body")

    /**
     * 响应头
     */
    @Cn(value = """响应头""")
    val header = MoerMetaMap(this.parentPropertyName, "header") /*:map*/

    /**
     * 结果
     */
    @Cn(value = """结果""")
    val result = mongoColumnJoin(this.parentPropertyName, "result")
    override fun toString(): String {
        return mongoColumnJoin(this.parentPropertyName).toString()
    }
}

