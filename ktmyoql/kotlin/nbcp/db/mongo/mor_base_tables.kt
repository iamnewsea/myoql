package nbcp.db.mongo.table

import nbcp.db.*
import nbcp.db.mongo.*
import nbcp.utils.*
import nbcp.comm.*
import nbcp.db.mongo.entity.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component


//generate auto @2021-12-05 02:33:48

class MenuDefineMeta(private val _pname: String) : MongoColumnName() {
    constructor(_val: MongoColumnName) : this(_val.toString()) {}


    val id = join(this._pname, "_id")

    /**
    * 菜单名称
    */
    @Cn("菜单名称")
    val name = join(this._pname, "name")

    /**
    * 菜单链接
    */
    @Cn("菜单链接")
    val url = join(this._pname, "url")

    /**
    * class
    */
    @Cn("class")
    val css = join(this._pname, "css")

    /**
    * 资源编码
    */
    @Cn("资源编码")
    val code = join(this._pname, "code")

    /**
    * 排序
    */
    @Cn("排序")
    val sort = join(this._pname, "sort")

    /**
    * 子菜单
    */
    @Cn("子菜单")
    val menus = join(this._pname, "menus") /*:递归类*/
    override fun toString(): String {
        return join(this._pname).toString()
    }
}

class IdUrlMeta(private val _pname: String) : MongoColumnName() {
    constructor(_val: MongoColumnName) : this(_val.toString()) {}


    val id = join(this._pname, "_id")

    /**
    * 网络资源地址
    */
    @Cn("网络资源地址")
    val url = join(this._pname, "url")
    override fun toString(): String {
        return join(this._pname).toString()
    }
}

/**
 * 身份证信息
 */
@Cn("身份证信息")
class IdentityCardDataMeta(private val _pname: String) : MongoColumnName() {
    constructor(_val: MongoColumnName) : this(_val.toString()) {}


    /**
    * 姓名
    */
    @Cn("姓名")
    val name = join(this._pname, "name")

    /**
    * 头像
    */
    @Cn("头像")
    val photo = IdUrlMeta(join(this._pname,"photo"))

    /**
    * 身份证号
    */
    @Cn("身份证号")
    val number = join(this._pname, "number")

    /**
    * 性别
    */
    @Cn("性别")
    val sex = join(this._pname, "sex")

    /**
    * 生日
    */
    @Cn("生日")
    val birthday = join(this._pname, "birthday")

    /**
    * 身份证地址
    */
    @Cn("身份证地址")
    val location = join(this._pname, "location")
    override fun toString(): String {
        return join(this._pname).toString()
    }
}

class IdNameMeta(private val _pname: String) : MongoColumnName() {
    constructor(_val: MongoColumnName) : this(_val.toString()) {}


    val id = join(this._pname, "_id")

    /**
    * 名称
    */
    @Cn("名称")
    val name = join(this._pname, "name")
    override fun toString(): String {
        return join(this._pname).toString()
    }
}

class SerializableMeta(private val _pname: String) : MongoColumnName() {
    constructor(_val: MongoColumnName) : this(_val.toString()) {}


    override fun toString(): String {
        return join(this._pname).toString()
    }
}

/**
 * 请求数据
 */
@Cn("请求数据")
class BaseRequestDataMeta(private val _pname: String) : MongoColumnName() {
    constructor(_val: MongoColumnName) : this(_val.toString()) {}


    /**
    * 访问地址
    */
    @Cn("访问地址")
    val url = join(this._pname, "url")

    /**
    * 访问方法
    */
    @Cn("访问方法")
    val method = join(this._pname, "method")

    /**
    * 调用链Id
    */
    @Cn("调用链Id")
    val traceId = join(this._pname, "traceId")

    /**
    * 请求体
    */
    @Cn("请求体")
    val body = join(this._pname, "body")

    /**
    * 请求头
    */
    @Cn("请求头")
    val header = join_map(this._pname, "header")/*:map*/

    /**
    * 客户端Ip
    */
    @Cn("客户端Ip")
    val clientIP = join(this._pname, "clientIP")
    override fun toString(): String {
        return join(this._pname).toString()
    }
}

class ObjectMeta(private val _pname: String) : MongoColumnName() {
    constructor(_val: MongoColumnName) : this(_val.toString()) {}


    override fun toString(): String {
        return join(this._pname).toString()
    }
}

/**
 * 回发数据
 */
@Cn("回发数据")
class BaseResponseDataMeta(private val _pname: String) : MongoColumnName() {
    constructor(_val: MongoColumnName) : this(_val.toString()) {}


    /**
    * 状态码
    */
    @Cn("状态码")
    val status = join(this._pname, "status")

    /**
    * 响应体
    */
    @Cn("响应体")
    val body = join(this._pname, "body")

    /**
    * 响应头
    */
    @Cn("响应头")
    val header = join_map(this._pname, "header")/*:map*/

    /**
    * 结果
    */
    @Cn("结果")
    val result = join(this._pname, "result")
    override fun toString(): String {
        return join(this._pname).toString()
    }
}

class CityCodeNameMeta(private val _pname: String) : MongoColumnName() {
    constructor(_val: MongoColumnName) : this(_val.toString()) {}


    val name = join(this._pname, "name")

    val code = join(this._pname, "code")
    override fun toString(): String {
        return join(this._pname).toString()
    }
}

/**
 * 营业执照信息
 */
@Cn("营业执照信息")
class BusinessLicenseDataMeta(private val _pname: String) : MongoColumnName() {
    constructor(_val: MongoColumnName) : this(_val.toString()) {}


    /**
    * 企业名称
    */
    @Cn("企业名称")
    val name = join(this._pname, "name")

    /**
    * 统一社会信用代码
    */
    @Cn("统一社会信用代码")
    val code = join(this._pname, "code")

    /**
    * 法人
    */
    @Cn("法人")
    val legalPerson = join(this._pname, "legalPerson")

    /**
    * 类型
    */
    @Cn("类型")
    val type = join(this._pname, "type")

    /**
    * 经营范围
    */
    @Cn("经营范围")
    val businessScope = join(this._pname, "businessScope")

    /**
    * 注册资本
    */
    @Cn("注册资本")
    val registeredCapital = join(this._pname, "registeredCapital")

    /**
    * 成立日期
    */
    @Cn("成立日期")
    val buildAt = join(this._pname, "buildAt")

    /**
    * 营业期限
    */
    @Cn("营业期限")
    val businessTerm = join(this._pname, "businessTerm")

    /**
    * 住所
    */
    @Cn("住所")
    val location = join(this._pname, "location")

    /**
    * 登记机关
    */
    @Cn("登记机关")
    val registeOrganization = join(this._pname, "registeOrganization")

    /**
    * 注册时间
    */
    @Cn("注册时间")
    val registeAt = join(this._pname, "registeAt")
    override fun toString(): String {
        return join(this._pname).toString()
    }
}


@Component("mongo.MongoBase")
@MetaDataGroup("MongoBase")
class MongoBaseGroup : IDataGroup{
    override fun getEntities():Set<BaseMetaData> = setOf(appMenu, basicUser, basicUserLoginInfo, flywayVersion, sysAnnex, sysCity, sysDictionary, sysDustbin, sysLog, sysOrganization)

    /**
    * 菜单 (变表)
    */
    private val appMenu get() = AppMenuEntity();
    /**
    * 菜单 (变表)
    */
    fun appMenu(owner: String) = AppMenuEntity("appMenu-${owner}");
    /**
    * 用户信息
    */
    val basicUser get() = BasicUserEntity();
    /**
    * 用户登录信息
    */
    val basicUserLoginInfo get() = BasicUserLoginInfoEntity();
    /**
    * 数据版本
    */
    val flywayVersion get() = FlywayVersionEntity();
    /**
    * 系统附件
    */
    val sysAnnex get() = SysAnnexEntity();
    /**
    * 城市令牌
    */
    val sysCity get() = SysCityEntity();
    /**
    * 字典
    */
    val sysDictionary get() = SysDictionaryEntity();
    /**
    * 数据垃圾箱
    */
    val sysDustbin get() = SysDustbinEntity();
    /**
    * 系统日志
    */
    val sysLog get() = SysLogEntity();
    /**
    * 组织信息
    */
    val sysOrganization get() = SysOrganizationEntity();


    /**
    * 菜单 (变表)
    */
    @Cn("菜单")
    @VarTable("owner")
    class AppMenuEntity(collectionName: String ="")
    :MongoBaseMetaCollection<nbcp.db.mongo.entity.AppMenu>(nbcp.db.mongo.entity.AppMenu::class.java,collectionName.AsString("appMenu")) {
    val id = MongoColumnName("_id")
    /**
    * 菜单名称
    */
    @Cn("菜单名称")
    val name = MongoColumnName("name")
    /**
    * 所有者
    */
    @Cn("所有者")
    val owner = MongoColumnName("owner")
    /**
    * 创建时间
    */
    @Cn("创建时间")
    val createAt = MongoColumnName("createAt")
    /**
    * 更新时间
    */
    @Cn("更新时间")
    val updateAt = MongoColumnName("updateAt")
    /**
    * 菜单链接
    */
    @Cn("菜单链接")
    val url = MongoColumnName("url")
    /**
    * class
    */
    @Cn("class")
    val css = MongoColumnName("css")
    /**
    * 资源编码
    */
    @Cn("资源编码")
    val code = MongoColumnName("code")
    /**
    * 排序
    */
    @Cn("排序")
    val sort = MongoColumnName("sort")
    /**
    * 子菜单
    */
    @Cn("子菜单")
    val menus = MenuDefineMeta("menus")

    }

    /**
    * 用户信息
    */
    @Cn("用户信息")
    class BasicUserEntity(collectionName: String ="")
    :MongoBaseMetaCollection<nbcp.db.mongo.entity.BasicUser>(nbcp.db.mongo.entity.BasicUser::class.java,collectionName.AsString("basicUser")) {
    val id = MongoColumnName("_id")
    /**
    * 昵称
    */
    @Cn("昵称")
    val name = MongoColumnName("name")
    /**
    * 登录名
    */
    @Cn("登录名")
    val loginName = MongoColumnName("loginName")
    /**
    * 手机号
    */
    @Cn("手机号")
    val mobile = MongoColumnName("mobile")
    /**
    * 电子邮件
    */
    @Cn("电子邮件")
    val email = MongoColumnName("email")
    /**
    * 头像
    */
    @Cn("头像")
    val logo = IdUrlMeta("logo")
    /**
    * 备注
    */
    @Cn("备注")
    val remark = MongoColumnName("remark")
    /**
    * 身份证
    */
    @Cn("身份证")
    val identityCard = IdentityCardDataMeta("identityCard")
    /**
    * 创建时间
    */
    @Cn("创建时间")
    val createAt = MongoColumnName("createAt")
    /**
    * 更新时间
    */
    @Cn("更新时间")
    val updateAt = MongoColumnName("updateAt")

    fun queryByLoginName (loginName: String) : MongoQueryClip<BasicUserEntity, nbcp.db.mongo.entity.BasicUser> {
    return this.query().where{ it.loginName match loginName }
    }

    fun deleteByLoginName (loginName: String) : MongoDeleteClip<BasicUserEntity> {
    return this.delete().where{ it.loginName match loginName }
    }

    fun updateByLoginName (loginName: String) : MongoUpdateClip<BasicUserEntity> {
    return this.update().where{ it.loginName match loginName }
    }


    fun queryByMobile (mobile: String) : MongoQueryClip<BasicUserEntity, nbcp.db.mongo.entity.BasicUser> {
    return this.query().where{ it.mobile match mobile }
    }

    fun deleteByMobile (mobile: String) : MongoDeleteClip<BasicUserEntity> {
    return this.delete().where{ it.mobile match mobile }
    }

    fun updateByMobile (mobile: String) : MongoUpdateClip<BasicUserEntity> {
    return this.update().where{ it.mobile match mobile }
    }


    fun queryByEmail (email: String) : MongoQueryClip<BasicUserEntity, nbcp.db.mongo.entity.BasicUser> {
    return this.query().where{ it.email match email }
    }

    fun deleteByEmail (email: String) : MongoDeleteClip<BasicUserEntity> {
    return this.delete().where{ it.email match email }
    }

    fun updateByEmail (email: String) : MongoUpdateClip<BasicUserEntity> {
    return this.update().where{ it.email match email }
    }

    }

    /**
    * 用户登录信息
    */
    @Cn("用户登录信息")
    class BasicUserLoginInfoEntity(collectionName: String ="")
    :MongoBaseMetaCollection<nbcp.db.mongo.entity.BasicUserLoginInfo>(nbcp.db.mongo.entity.BasicUserLoginInfo::class.java,collectionName.AsString("basicUserLoginInfo")) {
    val id = MongoColumnName("_id")
    /**
    * 用户唯一Id
    */
    @Cn("用户唯一Id")
    val userId = MongoColumnName("userId")
    /**
    * 登录名
    */
    @Cn("登录名")
    val loginName = MongoColumnName("loginName")
    /**
    * 登录手机
    */
    @Cn("登录手机")
    val mobile = MongoColumnName("mobile")
    /**
    * 登录邮箱
    */
    @Cn("登录邮箱")
    val email = MongoColumnName("email")
    /**
    * 密码
    */
    @Cn("密码")
    val password = MongoColumnName("password")
    /**
    * 最后登录时间
    */
    @Cn("最后登录时间")
    val lastLoginAt = MongoColumnName("lastLoginAt")
    /**
    * 是否已锁定
    */
    @Cn("是否已锁定")
    val isLocked = MongoColumnName("isLocked")
    /**
    * 锁定详情
    */
    @Cn("锁定详情")
    val lockedRemark = MongoColumnName("lockedRemark")
    /**
    * 创建时间
    */
    @Cn("创建时间")
    val createAt = MongoColumnName("createAt")
    /**
    * 更新时间
    */
    @Cn("更新时间")
    val updateAt = MongoColumnName("updateAt")

    fun queryByUserId (userId: String) : MongoQueryClip<BasicUserLoginInfoEntity, nbcp.db.mongo.entity.BasicUserLoginInfo> {
    return this.query().where{ it.userId match userId }
    }

    fun deleteByUserId (userId: String) : MongoDeleteClip<BasicUserLoginInfoEntity> {
    return this.delete().where{ it.userId match userId }
    }

    fun updateByUserId (userId: String) : MongoUpdateClip<BasicUserLoginInfoEntity> {
    return this.update().where{ it.userId match userId }
    }


    fun queryByLoginName (loginName: String) : MongoQueryClip<BasicUserLoginInfoEntity, nbcp.db.mongo.entity.BasicUserLoginInfo> {
    return this.query().where{ it.loginName match loginName }
    }

    fun deleteByLoginName (loginName: String) : MongoDeleteClip<BasicUserLoginInfoEntity> {
    return this.delete().where{ it.loginName match loginName }
    }

    fun updateByLoginName (loginName: String) : MongoUpdateClip<BasicUserLoginInfoEntity> {
    return this.update().where{ it.loginName match loginName }
    }


    fun queryByMobile (mobile: String) : MongoQueryClip<BasicUserLoginInfoEntity, nbcp.db.mongo.entity.BasicUserLoginInfo> {
    return this.query().where{ it.mobile match mobile }
    }

    fun deleteByMobile (mobile: String) : MongoDeleteClip<BasicUserLoginInfoEntity> {
    return this.delete().where{ it.mobile match mobile }
    }

    fun updateByMobile (mobile: String) : MongoUpdateClip<BasicUserLoginInfoEntity> {
    return this.update().where{ it.mobile match mobile }
    }


    fun queryByEmail (email: String) : MongoQueryClip<BasicUserLoginInfoEntity, nbcp.db.mongo.entity.BasicUserLoginInfo> {
    return this.query().where{ it.email match email }
    }

    fun deleteByEmail (email: String) : MongoDeleteClip<BasicUserLoginInfoEntity> {
    return this.delete().where{ it.email match email }
    }

    fun updateByEmail (email: String) : MongoUpdateClip<BasicUserLoginInfoEntity> {
    return this.update().where{ it.email match email }
    }

    }

    /**
    * 数据版本
    */
    @Cn("数据版本")
    class FlywayVersionEntity(collectionName: String ="")
    :MongoBaseMetaCollection<nbcp.db.mongo.entity.FlywayVersion>(nbcp.db.mongo.entity.FlywayVersion::class.java,collectionName.AsString("flywayVersion")) {
    val id = MongoColumnName("_id")
    /**
    * 版本
    */
    @Cn("版本")
    val version = MongoColumnName("version")
    /**
    * 备注
    */
    @Cn("备注")
    val remark = MongoColumnName("remark")
    /**
    * 执行的类
    */
    @Cn("执行的类")
    val execClass = MongoColumnName("execClass")
    /**
    * 执行开始时间
    */
    @Cn("执行开始时间")
    val startAt = MongoColumnName("startAt")
    /**
    * 执行结束时间
    */
    @Cn("执行结束时间")
    val finishAt = MongoColumnName("finishAt")
    /**
    * 是否成功
    */
    @Cn("是否成功")
    val isSuccess = MongoColumnName("isSuccess")
    /**
    * 创建时间
    */
    @Cn("创建时间")
    val createAt = MongoColumnName("createAt")
    /**
    * 更新时间
    */
    @Cn("更新时间")
    val updateAt = MongoColumnName("updateAt")

    }

    /**
    * 系统附件
    */
    @Cn("系统附件")
    class SysAnnexEntity(collectionName: String ="")
    :MongoBaseMetaCollection<nbcp.db.mongo.entity.SysAnnex>(nbcp.db.mongo.entity.SysAnnex::class.java,collectionName.AsString("sysAnnex")) {
    val id = MongoColumnName("_id")
    /**
    * 文件名
    */
    @Cn("文件名")
    val name = MongoColumnName("name")
    /**
    * 标签
    */
    @Cn("标签")
    val tags = MongoColumnName("tags")
    /**
    * 扩展名
    */
    @Cn("扩展名")
    val ext = MongoColumnName("ext")
    /**
    * 大小
    */
    @Cn("大小")
    val size = MongoColumnName("size")
    /**
    * 图像宽度
    */
    @Cn("图像宽度")
    val imgWidth = MongoColumnName("imgWidth")
    /**
    * 图像高度
    */
    @Cn("图像高度")
    val imgHeight = MongoColumnName("imgHeight")
    /**
    * 时长
    */
    @Cn("时长")
    val videoTime = MongoColumnName("videoTime")
    /**
    * 视频封面地址
    */
    @Cn("视频封面地址")
    val videoLogoUrl = MongoColumnName("videoLogoUrl")
    /**
    * 网络路径
    */
    @Cn("网络路径")
    val url = MongoColumnName("url")
    /**
    * 创建者
    */
    @Cn("创建者")
    val creator = IdNameMeta("creator")
    /**
    * 组
    */
    @Cn("组")
    val group = MongoColumnName("group")
    /**
    * 所属企业
    */
    @Cn("所属企业")
    val corpId = MongoColumnName("corpId")
    /**
    * 错误消息
    */
    @Cn("错误消息")
    val errorMsg = MongoColumnName("errorMsg")
    /**
    * 创建时间
    */
    @Cn("创建时间")
    val createAt = MongoColumnName("createAt")
    /**
    * 更新时间
    */
    @Cn("更新时间")
    val updateAt = MongoColumnName("updateAt")

    }

    /**
    * 城市令牌
    */
    @Cn("城市令牌")
    class SysCityEntity(collectionName: String ="")
    :MongoBaseMetaCollection<nbcp.db.mongo.entity.SysCity>(nbcp.db.mongo.entity.SysCity::class.java,collectionName.AsString("sysCity")) {
    val id = MongoColumnName("_id")
    /**
    * 城市全称
    */
    @Cn("城市全称")
    val name = MongoColumnName("name")
    /**
    * 城市编码
    */
    @Cn("城市编码")
    val code = MongoColumnName("code")
    /**
    * 城市短名称
    */
    @Cn("城市短名称")
    val shortName = MongoColumnName("shortName")
    /**
    * 级别
    */
    @Cn("级别")
    val level = MongoColumnName("level")
    /**
    * 经度
    */
    @Cn("经度")
    val lng = MongoColumnName("lng")
    /**
    * 纬度
    */
    @Cn("纬度")
    val lat = MongoColumnName("lat")
    /**
    * 拼音
    */
    @Cn("拼音")
    val pinyin = MongoColumnName("pinyin")
    /**
    * 电话码
    */
    @Cn("电话码")
    val telCode = MongoColumnName("telCode")
    /**
    * 邮编
    */
    @Cn("邮编")
    val postCode = MongoColumnName("postCode")
    /**
    * 父级码
    */
    @Cn("父级码")
    val pcode = MongoColumnName("pcode")
    /**
    * 创建时间
    */
    @Cn("创建时间")
    val createAt = MongoColumnName("createAt")
    /**
    * 更新时间
    */
    @Cn("更新时间")
    val updateAt = MongoColumnName("updateAt")

    fun queryByCode (code: Int) : MongoQueryClip<SysCityEntity, nbcp.db.mongo.entity.SysCity> {
    return this.query().where{ it.code match code }
    }

    fun deleteByCode (code: Int) : MongoDeleteClip<SysCityEntity> {
    return this.delete().where{ it.code match code }
    }

    fun updateByCode (code: Int) : MongoUpdateClip<SysCityEntity> {
    return this.update().where{ it.code match code }
    }

    }

    /**
    * 字典
    */
    @Cn("字典")
    class SysDictionaryEntity(collectionName: String ="")
    :MongoBaseMetaCollection<nbcp.db.mongo.entity.SysDictionary>(nbcp.db.mongo.entity.SysDictionary::class.java,collectionName.AsString("sysDictionary")) {
    val id = MongoColumnName("_id")
    /**
    * 所有者
    */
    @Cn("所有者")
    val owner = MongoColumnName("owner")
    /**
    * 值
    */
    @Cn("值")
    val key = MongoColumnName("key")
    /**
    * 值
    */
    @Cn("值")
    val label = MongoColumnName("label")
    /**
    * 排序
    */
    @Cn("排序")
    val sort = MongoColumnName("sort")
    /**
    * 创建时间
    */
    @Cn("创建时间")
    val createAt = MongoColumnName("createAt")
    /**
    * 更新时间
    */
    @Cn("更新时间")
    val updateAt = MongoColumnName("updateAt")

    }

    /**
    * 数据垃圾箱
    */
    @Cn("数据垃圾箱")
    class SysDustbinEntity(collectionName: String ="")
    :MongoBaseMetaCollection<nbcp.db.mongo.entity.SysDustbin>(nbcp.db.mongo.entity.SysDustbin::class.java,collectionName.AsString("sysDustbin")) {
    val id = MongoColumnName("_id")
    /**
    * 表名
    */
    @Cn("表名")
    val table = MongoColumnName("table")
    /**
    * 备注
    */
    @Cn("备注")
    val remark = MongoColumnName("remark")
    /**
    * 创建者
    */
    @Cn("创建者")
    val creator = IdNameMeta("creator")
    /**
    * 数据
    */
    @Cn("数据")
    val data = SerializableMeta("data")
    /**
    * 创建时间
    */
    @Cn("创建时间")
    val createAt = MongoColumnName("createAt")
    /**
    * 更新时间
    */
    @Cn("更新时间")
    val updateAt = MongoColumnName("updateAt")

    }

    /**
    * 系统日志
    */
    @Cn("系统日志")
    class SysLogEntity(collectionName: String ="")
    :MongoBaseMetaCollection<nbcp.db.mongo.entity.SysLog>(nbcp.db.mongo.entity.SysLog::class.java,collectionName.AsString("sysLog")) {
    val id = MongoColumnName("_id")
    /**
    * 模块
    */
    @Cn("模块")
    val module = MongoColumnName("module")
    /**
    * 类型
    */
    @Cn("类型")
    val level = MongoColumnName("level")
    /**
    * 标签
    */
    @Cn("标签")
    val tags = MongoColumnName("tags")
    /**
    * 消息
    */
    @Cn("消息")
    val msg = MongoColumnName("msg")
    /**
    * 请求数据
    */
    @Cn("请求数据")
    val request = BaseRequestDataMeta("request")
    /**
    * 程序数据
    */
    @Cn("程序数据")
    val data = ObjectMeta("data")
    /**
    * 回发数据
    */
    @Cn("回发数据")
    val response = BaseResponseDataMeta("response")
    /**
    * 创建者Id
    */
    @Cn("创建者Id")
    val creator = IdNameMeta("creator")
    /**
    * 创建时间
    */
    @Cn("创建时间")
    val createAt = MongoColumnName("createAt")

    }

    /**
    * 组织信息
    */
    @Cn("组织信息")
    class SysOrganizationEntity(collectionName: String ="")
    :MongoBaseMetaCollection<nbcp.db.mongo.entity.SysOrganization>(nbcp.db.mongo.entity.SysOrganization::class.java,collectionName.AsString("sysOrganization")) {
    val id = MongoColumnName("_id")
    /**
    * 组织名称
    */
    @Cn("组织名称")
    val name = MongoColumnName("name")
    /**
    * 网站地址
    */
    @Cn("网站地址")
    val siteUrl = MongoColumnName("siteUrl")
    /**
    * 网站备案号
    */
    @Cn("网站备案号")
    val siteNumber = MongoColumnName("siteNumber")
    /**
    * 所在城市
    */
    @Cn("所在城市")
    val city = CityCodeNameMeta("city")
    /**
    * 营业执照
    */
    @Cn("营业执照")
    val businessLicense = BusinessLicenseDataMeta("businessLicense")
    /**
    * 徽标
    */
    @Cn("徽标")
    val logo = IdUrlMeta("logo")
    /**
    * 是否已锁定
    */
    @Cn("是否已锁定")
    val isLocked = MongoColumnName("isLocked")
    /**
    * 锁定详情
    */
    @Cn("锁定详情")
    val lockedRemark = MongoColumnName("lockedRemark")
    /**
    * 创建时间
    */
    @Cn("创建时间")
    val createAt = MongoColumnName("createAt")
    /**
    * 更新时间
    */
    @Cn("更新时间")
    val updateAt = MongoColumnName("updateAt")

    }

}



private fun join(vararg args: String) : MongoColumnName{
    return MongoColumnName( args.toList().filter{it.HasValue}.joinToString (".") )
}

private fun join_map(vararg args: String) : moer_map{
    return moer_map(args.toList().filter{it.HasValue}.joinToString ("."))
}

data class moer_map(val _pname: String)
{
    fun keys(keys: String): String{
        return this._pname + "." + keys
    }
}


