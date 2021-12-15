package nbcp.db.mongo.entity

import nbcp.comm.StringMap
import nbcp.db.*
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import nbcp.db.mongo.*
import java.io.Serializable

//--------------------------------------------------------
/**
 * 用户信息
 */
@Document
@DbEntityGroup("MongoBase")
@RemoveToSysDustbin
@Cn("用户信息")
open class BasicUser @JvmOverloads constructor(
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

/**
 * 登录信息
 */
@Document
@DbEntityGroup("MongoBase")
@Cn("用户登录信息")
@DbEntityIndex("userId", unique = true)
open class BasicUserLoginInfo @JvmOverloads constructor(
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
        var isLocked: Boolean = true,
        @Cn("锁定详情")
        var lockedRemark: String = ""
) : BaseEntity()


//系统附件表
@Document
@DbEntityGroup("MongoBase")
@Cn("系统附件")
open class SysAnnex @JvmOverloads constructor(
        @Cn("文件名")
        var name: String = "",          //显示的名字,友好的名称
        @Cn("标签")
        var tags: List<String> = listOf(),
        @Cn("扩展名")
        var ext: String = "",           //后缀名。
        @Cn("大小")
        var size: Int = 0,              //大小

        @Cn("图像宽度")
        var imgWidth: Int = 0,          // 图像宽度值。
        @Cn("图像高度")
        var imgHeight: Int = 0,         // 图像高度值。

        @Cn("时长")
        var videoTime: Int = 0,          //视频时长,秒

        @Cn("视频封面地址")
        var videoLogoUrl: String = "",      //视频封面地址

        @Cn("网络路径")
        var url: String = "",           //下载的路径。没有 host

        @Cn("创建者")
        var creator: IdName = IdName(), //创建者

        @Cn("组")
        var group: String = "",          //所属的大分组，可能是不同的系统。

        @Cn("所属企业")
        var corpId: String = "",        //创建所属企业,如果是 admin,则 id = "admin" , name = "admin 即可

        @Cn("错误消息")
        var errorMsg: String = ""      //文件处理时的错误消息
) : BaseEntity() {
}


@Document
@DbEntityGroup("MongoBase")
@Cn("城市令牌")
@DbEntityIndex("code", unique = true)
open class SysCity @JvmOverloads constructor(
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
) : BaseEntity()

@Cn("请求数据")
open class BaseRequestData : Serializable {
    @Cn("访问地址")
    var url = "";

    @Cn("访问方法")
    var method = "";

    @Cn("调用链Id")
    var traceId = "";

    @Cn("请求体")
    var body = "";

    @Cn("请求头")
    var header = StringMap();

    @Cn("客户端Ip")
    var clientIP = "";
}

@Cn("回发数据")
open class BaseResponseData : Serializable {
    @Cn("状态码")
    var status = 0;

    @Cn("响应体")
    var body = "";

    @Cn("响应头")
    var header = StringMap();

    @Cn("结果")
    var result = "";
}

@Document
@DbEntityGroup("MongoBase")
@Cn("系统日志")
open class SysLog @JvmOverloads constructor(
        var id: String = "",
        @Cn("模块")
        var module: String = "", //模块,多级模块用.分隔
        @Cn("类型")
        var level: String = "",  //类型， error,warn,info
        @Cn("标签")
        var tags: MutableList<String> = mutableListOf(),   //实体标志, 查询用： module + key
        @Cn("消息")
        var msg: String = "",   //消息

        @Cn("请求数据")
        var request: BaseRequestData = BaseRequestData(), //请求数据
        @Cn("程序数据")
        var data: Any? = null,    //程序处理数据
        @Cn("回发数据")
        var response: BaseResponseData = BaseResponseData(),  //回发数据

        @Cn("创建者Id")
        var creator: IdName = IdName(),
        @Cn("创建时间")
        var createAt: LocalDateTime = LocalDateTime.now()
) : Serializable {
}

//存放删除的数据。
@Document
@DbEntityGroup("MongoBase")
@Cn("数据垃圾箱")
open class SysDustbin @JvmOverloads constructor(
        @Cn("表名")
        var table: String = "",
        @Cn("备注")
        var remark: String = "",
        @Cn("创建者")
        var creator: IdName = IdName(),
        @Cn("数据")
        var data: Serializable? = null
) : BaseEntity()


@Document
@DbEntityGroup("MongoBase")
@Cn("数据版本")
open class FlywayVersion @JvmOverloads constructor(
        @Cn("版本")
        var version: Int = 0,
        @Cn("备注")
        var remark: String = "",
        @Cn("执行的类")
        var execClass: String = "",
        @Cn("执行开始时间")
        var startAt: LocalDateTime = LocalDateTime.now(),
        @Cn("执行结束时间")
        var finishAt: LocalDateTime? = null,
        @Cn("是否成功")
        var isSuccess: Boolean = false
) : BaseEntity()


@Document
@DbEntityGroup("MongoBase")
@Cn("字典")
data class SysDictionary(
        @Cn("所有者")
        var owner: String = "",
        @Cn("值")
        var key: String = "",
        @Cn("值")
        var label: String = "",
        @Cn("排序")
        var sort: Float = 0F,
) : BaseEntity()
