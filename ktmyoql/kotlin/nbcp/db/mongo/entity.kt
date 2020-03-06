package nbcp.db.mongo.entity

import nbcp.db.DbEntityGroup
import nbcp.db.IdName
import nbcp.db.IdUrl
import nbcp.db.MongoEntitySysDustbin
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import nbcp.db.mongo.*
import java.time.LocalDate

data class PrivateSecretDataModel(
        var name: String = "", //登记别名
        var key: String = "",
        var secret: String = "",
        var type: String = "", //加密方式
        var createAt: LocalDateTime = LocalDateTime.now()
)

//--------------------------------------------------------

//系统附件表
@Document
@DbEntityGroup("MongoBase")
open class SysAnnex(
        var name: String = "",          //显示的名字,友好的名称
        var tags: List<String> = listOf(),
        var ext: String = "",           //后缀名。
        var size: Int = 0,              //大小
        var checkCode: String = "",     //Md5,Sha1
        var imgWidth: Int = 0,          // 图像宽度值。
        var imgHeight: Int = 0,         // 图像宽度值。
        var url: String = "",           //下载的路径。没有 host

        var createBy: IdName = IdName(), //创建者
        var corpId: String = "",    //创建所属企业,如果是 admin,则 id = "admin" , name = "admin 即可
        var errorMsg: String = "",      //文件处理时的错误消息
        var createAt: LocalDateTime = LocalDateTime.now()
) : IMongoDocument() {
}


@Document
@DbEntityGroup("MongoBase")
open class SysLog(
        var module: String = "", //模块
        var type: String = "",  //类型
        var key: String = "",   //实体标志, 查询用： module + key
        var msg: String = "",   //消息
        var data: Any? = null,
        var remark: String = "",
        var clientIp: String = "",
        var creatAt: LocalDateTime = LocalDateTime.now(),
        var createBy: String = ""
) : IMongoDocument() {
}

//存放删除的数据。
@Document
@DbEntityGroup("MongoBase")
open class SysDustbin(
        var table: String = "",
        var remark: String = "",
        var creator: IdName = IdName(),
        var data: Any = Object(),
        var createAt: LocalDateTime = LocalDateTime.now()
) : IMongoDocument()

/**
 * SSO用户
 */
@Document
@DbEntityGroup("MongoBase")
@MongoEntitySysDustbin
open class SysUser(
        var loginName: String = "",
        var name: String = "",
        var logo: IdUrl = IdUrl(), //头像.

        var mobile: String = "",
        var email: String = "",

        var idCard: UserIdCardData = UserIdCardData(),

        var liveLocation: String = "",  //常住地
        var corpName: String = "",
        var job: String = "",
        var workLocation: String = "",  //工作地

        var token: String = "",    //验证用户使用.实际保存的是 JsessionId
        var createAt: LocalDateTime = LocalDateTime.now(),
        var updateAt: LocalDateTime = LocalDateTime.now()
) : IMongoDocument()

/**
 * 用户密码
 */
@Document
@DbEntityGroup("MongoBase")
data class SysLoginUser(
        var loginName: String = "",
        var mobile: String = "",    //认证后更新
        var email: String = "",     //认证后更新
        var password: String = "",  // Md5Util.getBase64Md5(pwd)
        var lastLoginAt: LocalDateTime = LocalDateTime.now(),
//        var errorLoginTimes: Byte = 0,  //保存在Redis中
        var isLocked: Boolean = false,
        var lockedRemark: String = ""
) : IMongoDocument()


enum class SysApplicationAuthorizeTypeEnum {
    Name,
    Mobile,
    Logo,
    Email,
    IdCard,   //包括常住地
    CorpInfo, //包括公司及职位
}

/**
 * 应用的私密信息
 */
data class SysApplicationSecretInfoData(
        var secret: String = "",        // CodeUtil.getCode()
        var authorizeCode: String = "", //授权码
//        var privateSecrets: List<PrivateSecretDataModel> = listOf(), //私钥，客户端特殊加密用
        var token: String = "",
        var freshToken: String = "",
        var codeCreateAt: LocalDateTime = LocalDateTime.now(),
        var userUpdateHookCallbackUrl: String = "",   // 用户更新回调Url，除去安全域名开头的部分，以/开始。
        var lockedRemark: String = ""
)

/**
 * 应用中心
 */
@Document
@DbEntityGroup("MongoBase")
data class SysApplication(
        var name: String = "",
        var key: String = "",           // 应用Id，CodeUtil.getCode()
        var slogan: String = "",                    // 广告语， 每次登录的时候显示
        var logo: IdUrl = IdUrl(),      //应用Logo
        var siteUrl: String = "",         //展示信息，应用主站
        var remark: String = "",
        var hostDomainName: String = "",            // 安全域名，http 或 https 开头。
        var secretInfo: SysApplicationSecretInfoData = SysApplicationSecretInfoData(),
        var authorizeRange: List<SysApplicationAuthorizeTypeEnum> = listOf(),  //需要授权的信息
        var createAt: LocalDateTime = LocalDateTime.now(),
        var isLocked: Boolean = false
) : IMongoDocument()

