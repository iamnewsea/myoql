package nbcp.myoql.db.mongo.entity

import nbcp.base.db.IdUrl
import nbcp.base.db.annotation.Cn
import nbcp.myoql.db.BaseEntity
import nbcp.myoql.db.IdentityCardData

/**
 * 用户信息
 */
@Cn("用户信息")
abstract class BasicUser @JvmOverloads constructor(
        @Cn("昵称")
    var name: String = "",      //这里的名称=自定义昵称
        @Cn("登录名")
    var loginName: String = "",
        @Cn("手机号")
    var mobile: String = "",
        @Cn("电子邮件")
    var email: String = "",
        @Cn("头像")
    var logo: IdUrl = IdUrl(), //头像.
        @Cn("备注")
    var remark: String = "",

        @Cn("身份证")
    var identityCard: IdentityCardData = IdentityCardData(),
) : BaseEntity() {
}