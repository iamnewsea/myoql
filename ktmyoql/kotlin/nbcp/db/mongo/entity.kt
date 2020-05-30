package nbcp.db.mongo.entity

import nbcp.db.*
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import nbcp.db.mongo.*

//--------------------------------------------------------
/**
 * 用户信息
 */
@Document
@DbEntityGroup("MongoBase")
@RemoveToSysDustbin
open class BasicUser(
        var name: String = "",      //这里的名称=自定义昵称
        var loginName: String = "",
        var mobile: String = "",
        var email: String = "",
        var logo: IdUrl = IdUrl(), //头像.
        var remark: String = "",

        var identityCard: IdentityCardData = IdentityCardData(),

        var liveCity: IntCodeName = IntCodeName(),
        var liveLocation: String = "",  //常住地
        var workCity: IntCodeName = IntCodeName(),
        var workLocation: String = ""  //工作地
) : BaseEntity(), IMongoDocument  {

//    var name: String
//        get() = this.identityCard.name
//        set(value) {
//            this.identityCard.name = value
//        }
}

/**
 * 登录信息
 */
@Document
@DbEntityGroup("MongoBase")
open class BasicUserLoginInfo(
        var userId: String = "",    //用户Id,唯一
        var loginName: String = "",
        var mobile: String = "",    //认证后更新
        var email: String = "",     //认证后更新

        var password: String = "",  // Md5Util.getBase64Md5(pwd)
        var lastLoginAt: LocalDateTime = LocalDateTime.now(),

        var authorizeCode: String = "", //授权码
        var token: String = "",         //常用数据，也会放到主表。
        var freshToken: String = "",
        var authorizeCodeCreateAt: LocalDateTime = LocalDateTime.now(),
        var grantApps: MutableList<IdName> = mutableListOf(),   //授权的App


        var isLocked: Boolean = false,
        var lockedRemark: String = ""
) :BaseEntity(), IMongoDocument


@Document
@DbEntityGroup("MongoBase")
@RemoveToSysDustbin
open class SysOrganization(
        var name: String = "",          //这里的名称=自定义昵称
        var siteUrl: String = "",       //网站
        var siteNumber: String = "",    //网站备案号

        var city: IntCodeName = IntCodeName(),
        var businessLicense: BusinessLicenseData = BusinessLicenseData(),
        var logo: IdUrl = IdUrl(),
        var isLocked: Boolean = true,
        var lockedRemark: String = ""
) : BaseEntity(), IMongoDocument

/**
 * 应用中心
 */
@Document
@DbEntityGroup("MongoBase")
open class SysApplication(
        var key: String = "",                    // 应用Id，CodeUtil.getCode()
        var name: String = "",                  //应用名称
        var remark: String = "",
        var hostDomainName: String = "",            // 安全域名，http 或 https 开头。
        var secret: String = "",                    //应用密钥，Api用。
        var userUpdateHookCallbackUrl: String = "",     //用户更新后，通知App的回调。
        var authorizeRange: MutableList<String> = mutableListOf(),  //需要授权的信息
        var org: IdName = IdName(),             //所属组织信息
        var isLocked: Boolean = false,
        var loadRemark: String = ""
) : BaseEntity(), IMongoDocument


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

        var creator: IdName = IdName(), //创建者
        var corpId: String = "",    //创建所属企业,如果是 admin,则 id = "admin" , name = "admin 即可
        var errorMsg: String = ""      //文件处理时的错误消息
) : BaseEntity(), IMongoDocument   {
}


@Document
@DbEntityGroup("MongoBase")
data class SysCity(
        @Key
        var code: Int = 0,
        var name: String = "",
        var fullName: String = "",
        var level: Int = 0,
        var lng: Float = 0F, //经度
        var lat: Float = 0F, //纬度
        var pinyin: String = "",
        var telCode: String = "",
        var postCode: String = "",
        var pcode: Int = 0
) : BaseEntity(), IMongoDocument

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
        var creatorId: String = ""
) : BaseEntity(), IMongoDocument   {
}

//存放删除的数据。
@Document
@DbEntityGroup("MongoBase")
open class SysDustbin(
        var table: String = "",
        var remark: String = "",
        var creator: IdName = IdName(),
        var data: Any = Object()
) : BaseEntity(), IMongoDocument



