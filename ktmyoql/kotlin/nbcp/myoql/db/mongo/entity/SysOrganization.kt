package nbcp.myoql.db.mongo.entity

import nbcp.base.db.IdUrl
import nbcp.base.db.annotation.Cn
import nbcp.base.db.annotation.DbEntityGroup
import nbcp.myoql.db.BaseEntity
import nbcp.myoql.db.BusinessLicenseData
import nbcp.myoql.db.CityCodeName
import nbcp.myoql.db.comm.RemoveToSysDustbin
import org.springframework.data.mongodb.core.mapping.Document

@Document
@DbEntityGroup("MongoBase")
@RemoveToSysDustbin
@Cn("组织信息")
open class SysOrganization @JvmOverloads constructor(
        @Cn("组织名称")
    var name: String = "",          //这里的名称=自定义昵称
        @Cn("网站地址")
    var siteUrl: String = "",       //网站
        @Cn("网站备案号")
    var siteNumber: String = "",    //网站备案号

        @Cn("所在城市")
    var city: CityCodeName = CityCodeName(),
        @Cn("营业执照")
    var businessLicense: BusinessLicenseData = BusinessLicenseData(),
        @Cn("徽标")
    var logo: IdUrl = IdUrl(),
        @Cn("是否已锁定")
    var isLocked: Boolean = false,
        @Cn("锁定详情")
    var lockedRemark: String = ""
) : BaseEntity()