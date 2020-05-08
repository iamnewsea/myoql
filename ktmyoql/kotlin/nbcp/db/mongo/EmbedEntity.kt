package nbcp.db.mongo

import nbcp.db.IdUrl
import java.time.LocalDate
import java.time.LocalDateTime

enum class UserSexEnum(var remark: String) {
    Male("男"),
    Female("女")
}

/**
 * 身份证内容
 */
open class IdentityCardData(
    var photo: IdUrl = IdUrl(),
    var name: String = "",
    var number: String = "",
    var sex: UserSexEnum? = null,
    var birthday: LocalDate? = null,
    var location: String = ""   //身份证地址
)

/**
 * 营业执照内容
 */
open class BusinessLicenseData(
        var code: String = "",
        var name: String = "",
        var legalPerson: String = "",   //法人
        var type: String = "",          //类型
        var businessScope: String = "",     //经营范围
        var registeredCapital: String = "",     //注册资本
        var buildAt: LocalDateTime? = null,     //成立日期
        var businessTerm: String = "",  //营业期限
        var location: String = "",      //住所
        var registeOrganization: String = "",   //登记机关
        var registeAt: String = ""      //注册时间
)

