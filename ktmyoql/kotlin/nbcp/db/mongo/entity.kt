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
@Cn("用户信息")
open class BasicUser(
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

        @Cn("居住城市")
        var liveCity: CityCodeName = CityCodeName(),
        @Cn("常住地址")
        var liveLocation: String = "",  //常住地
        @Cn("工作城市")
        var workCity: CityCodeName = CityCodeName(),
        @Cn("工作地址")
        var workLocation: String = ""  //工作地
) : BaseEntity(), IMongoDocument {

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
@Cn("用户登录信息")
open class BasicUserLoginInfo(
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

        @Cn("授权码")
        var authorizeCode: String = "", //授权码
        @Cn("令牌")
        var token: String = "",         //常用数据，也会放到主表。
        @Cn("刷新令牌")
        var freshToken: String = "",
        @Cn("授权码创建时间")
        var authorizeCodeCreateAt: LocalDateTime = LocalDateTime.now(),
        @Cn("授权应用")
        var grantApps: MutableList<IdName> = mutableListOf(),   //授权的App

        @Cn("是否已锁定")
        var isLocked: Boolean = false,
        @Cn("锁定详情")
        var lockedRemark: String = ""
) : BaseEntity(), IMongoDocument


@Document
@DbEntityGroup("MongoBase")
@RemoveToSysDustbin
@Cn("组织信息")
open class SysOrganization(
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
        var isLocked: Boolean = true,
        @Cn("锁定详情")
        var lockedRemark: String = ""
) : BaseEntity(), IMongoDocument

/**
 * 应用中心
 */
@Document
@DbEntityGroup("MongoBase")
@Cn("系统应用")
open class SysApplication(
        @Cn("键")
        var key: String = "",                    // 应用Id，CodeUtil.getCode()
        @Cn("应用名称")
        var name: String = "",                  //应用名称
        @Cn("备注")
        var remark: String = "",
        @Cn("安全域名")
        var hostDomainName: String = "",            // 安全域名，http 或 https 开头。
        @Cn("应用密钥")
        var secret: String = "",                    //应用密钥，Api用。
        @Cn("更新回调地址")
        var userUpdateHookCallbackUrl: String = "",     //用户更新后，通知App的回调。
        @Cn("授权范围")
        var authorizeRange: MutableList<String> = mutableListOf(),  //需要授权的信息
        @Cn("所属组织")
        var org: IdName = IdName(),             //所属组织信息
        @Cn("就否已锁定")
        var isLocked: Boolean = false,
        @Cn("锁定详情")
        var loadRemark: String = ""
) : BaseEntity(), IMongoDocument


//系统附件表
@Document
@DbEntityGroup("MongoBase")
@Cn("系统附件")
open class SysAnnex(
        @Cn("文件名")
        var name: String = "",          //显示的名字,友好的名称
        @Cn("标签")
        var tags: List<String> = listOf(),
        @Cn("扩展名")
        var ext: String = "",           //后缀名。
        @Cn("大小")
        var size: Int = 0,              //大小
        @Cn("校验码")
        var checkCode: String = "",     //Md5,Sha1
        @Cn("图像宽度")
        var imgWidth: Int = 0,          // 图像宽度值。
        @Cn("图像高度")
        var imgHeight: Int = 0,         // 图像高度值。

        @Cn("时长")
        var videoTime: Int = 0,          //视频时长,秒
        @Cn("封面")
        var videoLogoUrl: String = "",   //视频封面链接 ，第一巾帧链接

        @Cn("短路径")
        var url: String = "",           //下载的路径。没有 host

        @Cn("创建者")
        var creator: IdName = IdName(), //创建者
        @Cn("所属企业")
        var corpId: String = "",        //创建所属企业,如果是 admin,则 id = "admin" , name = "admin 即可
        @Cn("错误消息")
        var errorMsg: String = ""      //文件处理时的错误消息
) : BaseEntity(), IMongoDocument {
}


@Document
@DbEntityGroup("MongoBase")
@Cn("城市令牌")
data class SysCity(
        @DbKey
        @Cn("城市编码")
        var code: Int = 0,
        @Cn("城市短名称")
        var shortName: String = "",    // 城市控件使用 simpleName
        @Cn("城市全称")
        var name: String = "",          //界面常用全称
        @Cn("级别")
        var level: Int = 0,
        @Cn("经度")
        var lng: Float = 0F, //经度
        @Cn("纬度")
        var lat: Float = 0F, //纬度
        @Cn("拼音")
        var pinyin: String = "",
        @Cn("电话码")
        var telCode: String = "",
        @Cn("邮编")
        var postCode: String = "",
        @Cn("父级码")
        var pcode: Int = 0
) : BaseEntity(), IMongoDocument

@Document
@DbEntityGroup("MongoBase")
@Cn("系统日志")
open class SysLog(
        @Cn("模块")
        var module: String = "", //模块
        @Cn("类型")
        var type: String = "",  //类型
        @Cn("实体")
        var key: String = "",   //实体标志, 查询用： module + key
        @Cn("消息")
        var msg: String = "",   //消息
        @Cn("数据")
        var data: Any? = null,
        @Cn("详情")
        var remark: String = "",
        @Cn("客户Ip")
        var clientIp: String = "",
        @Cn("创建时间")
        var creatAt: LocalDateTime = LocalDateTime.now(),
        @Cn("创建者Id")
        var creatorId: String = ""
) : BaseEntity(), IMongoDocument {
}

//存放删除的数据。
@Document
@DbEntityGroup("MongoBase")
@Cn("数据垃圾箱")
open class SysDustbin(
        @Cn("表名")
        var table: String = "",
        @Cn("备注")
        var remark: String = "",
        @Cn("创建者")
        var creator: IdName = IdName(),
        @Cn("数据")
        var data: Any = Object()
) : BaseEntity(), IMongoDocument



