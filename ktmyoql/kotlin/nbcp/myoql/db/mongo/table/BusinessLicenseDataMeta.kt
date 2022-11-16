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


@nbcp.base.db.annotation.Cn(value = """营业执照信息""")
class BusinessLicenseDataMeta(private val parentPropertyName: String) : MongoColumnName() {
    constructor(value: MongoColumnName) : this(value.toString()) {}

    @nbcp.base.db.annotation.Cn(value = """企业名称""")
    val name = mongoColumnJoin(this.parentPropertyName, "name")

    @nbcp.base.db.annotation.Cn(value = """统一社会信用代码""")
    val code = mongoColumnJoin(this.parentPropertyName, "code")

    @nbcp.base.db.annotation.Cn(value = """法人""")
    val legalPerson = mongoColumnJoin(this.parentPropertyName, "legalPerson")

    @nbcp.base.db.annotation.Cn(value = """类型""")
    val type = mongoColumnJoin(this.parentPropertyName, "type")

    @nbcp.base.db.annotation.Cn(value = """经营范围""")
    val businessScope = mongoColumnJoin(this.parentPropertyName, "businessScope")

    @nbcp.base.db.annotation.Cn(value = """注册资本""")
    val registeredCapital = mongoColumnJoin(this.parentPropertyName, "registeredCapital")

    @nbcp.base.db.annotation.Cn(value = """成立日期""")
    val buildAt = mongoColumnJoin(this.parentPropertyName, "buildAt")

    @nbcp.base.db.annotation.Cn(value = """营业期限""")
    val businessTerm = mongoColumnJoin(this.parentPropertyName, "businessTerm")

    @nbcp.base.db.annotation.Cn(value = """住所""")
    val location = mongoColumnJoin(this.parentPropertyName, "location")

    @nbcp.base.db.annotation.Cn(value = """登记机关""")
    val registeOrganization = mongoColumnJoin(this.parentPropertyName, "registeOrganization")

    @nbcp.base.db.annotation.Cn(value = """注册时间""")
    val registeAt = mongoColumnJoin(this.parentPropertyName, "registeAt")
    override fun toString(): String {
        return mongoColumnJoin(this.parentPropertyName).toString()
    }
}

