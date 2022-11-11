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
 * 营业执照信息
 */
@nbcp.base.db.Cn(value = """营业执照信息""")
class BusinessLicenseDataMeta(private val parentPropertyName: String) : MongoColumnName() {
    constructor(value: MongoColumnName) : this(value.toString()) {}

    /**
     * 企业名称
     */
    @nbcp.base.db.Cn(value = """企业名称""")
    val name = mongoColumnJoin(this.parentPropertyName, "name")

    /**
     * 统一社会信用代码
     */
    @nbcp.base.db.Cn(value = """统一社会信用代码""")
    val code = mongoColumnJoin(this.parentPropertyName, "code")

    /**
     * 法人
     */
    @nbcp.base.db.Cn(value = """法人""")
    val legalPerson = mongoColumnJoin(this.parentPropertyName, "legalPerson")

    /**
     * 类型
     */
    @nbcp.base.db.Cn(value = """类型""")
    val type = mongoColumnJoin(this.parentPropertyName, "type")

    /**
     * 经营范围
     */
    @nbcp.base.db.Cn(value = """经营范围""")
    val businessScope = mongoColumnJoin(this.parentPropertyName, "businessScope")

    /**
     * 注册资本
     */
    @nbcp.base.db.Cn(value = """注册资本""")
    val registeredCapital = mongoColumnJoin(this.parentPropertyName, "registeredCapital")

    /**
     * 成立日期
     */
    @nbcp.base.db.Cn(value = """成立日期""")
    val buildAt = mongoColumnJoin(this.parentPropertyName, "buildAt")

    /**
     * 营业期限
     */
    @nbcp.base.db.Cn(value = """营业期限""")
    val businessTerm = mongoColumnJoin(this.parentPropertyName, "businessTerm")

    /**
     * 住所
     */
    @nbcp.base.db.Cn(value = """住所""")
    val location = mongoColumnJoin(this.parentPropertyName, "location")

    /**
     * 登记机关
     */
    @nbcp.base.db.Cn(value = """登记机关""")
    val registeOrganization = mongoColumnJoin(this.parentPropertyName, "registeOrganization")

    /**
     * 注册时间
     */
    @nbcp.base.db.Cn(value = """注册时间""")
    val registeAt = mongoColumnJoin(this.parentPropertyName, "registeAt")
    override fun toString(): String {
        return mongoColumnJoin(this.parentPropertyName).toString()
    }
}

