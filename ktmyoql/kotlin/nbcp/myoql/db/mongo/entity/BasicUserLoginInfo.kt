package nbcp.myoql.db.mongo.entity

import nbcp.base.db.annotation.Cn
import nbcp.base.db.annotation.DbEntityIndex
import nbcp.myoql.db.BaseEntity
import java.time.LocalDateTime

/**
 * 登录信息
 */
@Cn("用户登录信息")
@DbEntityIndex("userId", unique = true)
abstract class BasicUserLoginInfo @JvmOverloads constructor(
        @Cn("用户唯一Id")
    var userId: String = "",    //用户Id,唯一
        @Cn("登录名")
    var loginName: String = "",
        @Cn("登录手机")
    var mobile: String = "",    //认证后更新
        @Cn("登录邮箱")
    var email: String = "",     //认证后更新

        @Cn("密码")
    var password: String = "",  // Md5Util.getBase64Md5(pwd)
        @Cn("最后登录时间")
    var lastLoginAt: LocalDateTime = LocalDateTime.now(),

        @Cn("是否已锁定")
    var isLocked: Boolean = false,
        @Cn("锁定详情")
    var lockedRemark: String = ""
) : BaseEntity()