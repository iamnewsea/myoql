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
 * 营业执照信息
 */
@Cn(value = """营业执照信息""")
class BusinessLicenseDataMeta(private val parentPropertyName: String) : MongoColumnName() {
    constructor(value: MongoColumnName) : this(value.toString()) {}

    /**
     * 企业名称
     */
    @Cn(value = """企业名称""")
    val name = mongoColumnJoin(this.parentPropertyName, "name")

    /**
     * 统一社会信用代码
     */
    @Cn(value = """统一社会信用代码""")
    val code = mongoColumnJoin(this.parentPropertyName, "code")

    /**
     * 法人
     */
    @Cn(value = """法人""")
    val legalPerson = mongoColumnJoin(this.parentPropertyName, "legalPerson")

    /**
     * 类型
     */
    @Cn(value = """类型""")
    val type = mongoColumnJoin(this.parentPropertyName, "type")

    /**
     * 经营范围
     */
    @Cn(value = """经营范围""")
    val businessScope = mongoColumnJoin(this.parentPropertyName, "businessScope")

    /**
     * 注册资本
     */
    @Cn(value = """注册资本""")
    val registeredCapital = mongoColumnJoin(this.parentPropertyName, "registeredCapital")

    /**
     * 成立日期
     */
    @Cn(value = """成立日期""")
    val buildAt = mongoColumnJoin(this.parentPropertyName, "buildAt")

    /**
     * 营业期限
     */
    @Cn(value = """营业期限""")
    val businessTerm = mongoColumnJoin(this.parentPropertyName, "businessTerm")

    /**
     * 住所
     */
    @Cn(value = """住所""")
    val location = mongoColumnJoin(this.parentPropertyName, "location")

    /**
     * 登记机关
     */
    @Cn(value = """登记机关""")
    val registeOrganization = mongoColumnJoin(this.parentPropertyName, "registeOrganization")

    /**
     * 注册时间
     */
    @Cn(value = """注册时间""")
    val registeAt = mongoColumnJoin(this.parentPropertyName, "registeAt")
    override fun toString(): String {
        return mongoColumnJoin(this.parentPropertyName).toString()
    }
}

