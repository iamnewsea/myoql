package nbcp.db.mongo.table

import nbcp.db.*
import nbcp.db.mongo.*
import nbcp.utils.*
import nbcp.comm.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component


//generate auto @2022-08-10 14:32:54
/**
 * 回发数据
 */
@nbcp.db.Cn(value = """回发数据""")
class BaseResponseDataMeta(private val parentPropertyName: String) : MongoColumnName() {
    constructor(value: MongoColumnName) : this(value.toString()) {}

    /**
     * 状态码
     */
    @nbcp.db.Cn(value = """状态码""")
    val status = mongoColumnJoin(this.parentPropertyName, "status")

    /**
     * 响应体
     */
    @nbcp.db.Cn(value = """响应体""")
    val body = mongoColumnJoin(this.parentPropertyName, "body")

    /**
     * 响应头
     */
    @nbcp.db.Cn(value = """响应头""")
    val header = MoerMetaMap(this.parentPropertyName, "header") /*:map*/

    /**
     * 结果
     */
    @nbcp.db.Cn(value = """结果""")
    val result = mongoColumnJoin(this.parentPropertyName, "result")
    override fun toString(): String {
        return mongoColumnJoin(this.parentPropertyName).toString()
    }
}

