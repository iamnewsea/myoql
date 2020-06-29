package nbcp.db

import java.time.LocalDate
import java.time.LocalDateTime


open class BaseEntity {
    var id: String = "";
    @Cn("创建时间")
    var createAt: LocalDateTime = LocalDateTime.now()
    @Cn("更新时间")
    var updateAt: LocalDateTime? = null
}


enum class UserSexEnum(var remark: String) {
    Male("男"),
    Female("女")
}

/**
 * 身份证内容
 */
@Cn("身份证信息")
open class IdentityCardData(
        @Cn("头像")
        var photo: IdUrl = IdUrl(),
        @Cn("姓名")
        var name: String = "",
        @Cn("身份证号")
        var number: String = "",
        @Cn("性别")
        var sex: UserSexEnum? = null,
        @Cn("生日")
        var birthday: LocalDate? = null,
        @Cn("身份证地址")
        var location: String = ""   //身份证地址
)

/**
 * 营业执照内容
 */
@Cn("营业执照信息")
open class BusinessLicenseData(
        @Cn("统一社会信用代码")
        var code: String = "",
        @Cn("企业名称")
        var name: String = "",
        @Cn("法人")
        var legalPerson: String = "",   //法人
        @Cn("类型")
        var type: String = "",          //类型
        @Cn("经营范围")
        var businessScope: String = "",     //经营范围
        @Cn("注册资本")
        var registeredCapital: String = "",     //注册资本
        @Cn("成立日期")
        var buildAt: LocalDateTime? = null,     //成立日期
        @Cn("营业期限")
        var businessTerm: String = "",  //营业期限
        @Cn("住所")
        var location: String = "",      //住所
        @Cn("登记机关")
        var registeOrganization: String = "",   //登记机关
        @Cn("注册时间")
        var registeAt: String = ""      //注册时间
)


open class CityCodeName(var code: Int = 0, var name: String = "") : java.io.Serializable {
}
