package nbcp.db.mongo.table

import nbcp.db.*
import nbcp.db.mongo.*
import nbcp.utils.*
import nbcp.comm.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component


//generate auto @2022-02-15 14:16:36


class SerializableMeta(private val _pname: String) : MongoColumnName() {
    constructor(_val: MongoColumnName) : this(_val.toString()) {}

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
 * 营业执照信息
 */
@nbcp.db.Cn(value = """营业执照信息"""")
class BusinessLicenseDataMeta(private val _pname: String) : MongoColumnName() {
    constructor(_val: MongoColumnName) : this(_val.toString()) {}

    /**
     * 企业名称
     */
    @nbcp.db.Cn(value = """企业名称"""")
    val name = join(this._pname, "name")

    /**
     * 统一社会信用代码
     */
    @nbcp.db.Cn(value = """统一社会信用代码"""")
    val code = join(this._pname, "code")

    /**
     * 法人
     */
    @nbcp.db.Cn(value = """法人"""")
    val legalPerson = join(this._pname, "legalPerson")

    /**
     * 类型
     */
    @nbcp.db.Cn(value = """类型"""")
    val type = join(this._pname, "type")

    /**
     * 经营范围
     */
    @nbcp.db.Cn(value = """经营范围"""")
    val businessScope = join(this._pname, "businessScope")

    /**
     * 注册资本
     */
    @nbcp.db.Cn(value = """注册资本"""")
    val registeredCapital = join(this._pname, "registeredCapital")

    /**
     * 成立日期
     */
    @nbcp.db.Cn(value = """成立日期"""")
    val buildAt = join(this._pname, "buildAt")

    /**
     * 营业期限
     */
    @nbcp.db.Cn(value = """营业期限"""")
    val businessTerm = join(this._pname, "businessTerm")

    /**
     * 住所
     */
    @nbcp.db.Cn(value = """住所"""")
    val location = join(this._pname, "location")

    /**
     * 登记机关
     */
    @nbcp.db.Cn(value = """登记机关"""")
    val registeOrganization = join(this._pname, "registeOrganization")

    /**
     * 注册时间
     */
    @nbcp.db.Cn(value = """注册时间"""")
    val registeAt = join(this._pname, "registeAt")
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


class IdNameMeta(private val _pname: String) : MongoColumnName() {
    constructor(_val: MongoColumnName) : this(_val.toString()) {}

    val id = join(this._pname, "_id")

    /**
     * 名称
     */
    @nbcp.db.Cn(value = """名称"""")
    val name = join(this._pname, "name")
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
    @nbcp.db.Cn(value = """网络资源地址"""")
    val url = join(this._pname, "url")
    override fun toString(): String {
        return join(this._pname).toString()
    }
}

/**
 * 身份证信息
 */
@nbcp.db.Cn(value = """身份证信息"""")
class IdentityCardDataMeta(private val _pname: String) : MongoColumnName() {
    constructor(_val: MongoColumnName) : this(_val.toString()) {}

    /**
     * 姓名
     */
    @nbcp.db.Cn(value = """姓名"""")
    val name = join(this._pname, "name")

    /**
     * 头像
     */
    @nbcp.db.Cn(value = """头像"""")
    val photo = IdUrlMeta(join(this._pname, "photo"))

    /**
     * 身份证号
     */
    @nbcp.db.Cn(value = """身份证号"""")
    val number = join(this._pname, "number")

    /**
     * 性别
     */
    @nbcp.db.Cn(value = """性别"""")
    val sex = join(this._pname, "sex")

    /**
     * 生日
     */
    @nbcp.db.Cn(value = """生日"""")
    val birthday = join(this._pname, "birthday")

    /**
     * 身份证地址
     */
    @nbcp.db.Cn(value = """身份证地址"""")
    val location = join(this._pname, "location")
    override fun toString(): String {
        return join(this._pname).toString()
    }
}

/**
 * 请求数据
 */
@nbcp.db.Cn(value = """请求数据"""")
class BaseRequestDataMeta(private val _pname: String) : MongoColumnName() {
    constructor(_val: MongoColumnName) : this(_val.toString()) {}

    /**
     * 访问地址
     */
    @nbcp.db.Cn(value = """访问地址"""")
    val url = join(this._pname, "url")

    /**
     * 访问方法
     */
    @nbcp.db.Cn(value = """访问方法"""")
    val method = join(this._pname, "method")

    /**
     * 调用链Id
     */
    @nbcp.db.Cn(value = """调用链Id"""")
    val traceId = join(this._pname, "traceId")

    /**
     * 请求体
     */
    @nbcp.db.Cn(value = """请求体"""")
    val body = join(this._pname, "body")

    /**
     * 请求头
     */
    @nbcp.db.Cn(value = """请求头"""")
    val header = join_map(this._pname, "header")/*:map*/

    /**
     * 客户端Ip
     */
    @nbcp.db.Cn(value = """客户端Ip"""")
    val clientIP = join(this._pname, "clientIP")
    override fun toString(): String {
        return join(this._pname).toString()
    }
}

/**
 * 回发数据
 */
@nbcp.db.Cn(value = """回发数据"""")
class BaseResponseDataMeta(private val _pname: String) : MongoColumnName() {
    constructor(_val: MongoColumnName) : this(_val.toString()) {}

    /**
     * 状态码
     */
    @nbcp.db.Cn(value = """状态码"""")
    val status = join(this._pname, "status")

    /**
     * 响应体
     */
    @nbcp.db.Cn(value = """响应体"""")
    val body = join(this._pname, "body")

    /**
     * 响应头
     */
    @nbcp.db.Cn(value = """响应头"""")
    val header = join_map(this._pname, "header")/*:map*/

    /**
     * 结果
     */
    @nbcp.db.Cn(value = """结果"""")
    val result = join(this._pname, "result")
    override fun toString(): String {
        return join(this._pname).toString()
    }
}


@Component("mongo.MongoBase")
@MetaDataGroup(DatabaseEnum.Mongo, "MongoBase")
class MongoBaseGroup : IDataGroup {
    override fun getEntities(): Set<BaseMetaData> = setOf(basicUser, basicUserLoginInfo, sysAnnex, sysCity, sysDictionary, sysDustbin, sysFlywayVersion, sysLastSortNumber, sysLog, sysOrganization)


    /**
     * 用户信息
     */
    val basicUser get() = BasicUserEntity();

    /**
     * 用户登录信息
     */
    val basicUserLoginInfo get() = BasicUserLoginInfoEntity();

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
     * 数据版本
     */
    val sysFlywayVersion get() = SysFlywayVersionEntity();

    /**
     * 排序记录号
     */
    val sysLastSortNumber get() = SysLastSortNumberEntity();

    /**
     * 系统日志
     */
    val sysLog get() = SysLogEntity();

    /**
     * 组织信息
     */
    val sysOrganization get() = SysOrganizationEntity();


    /**
     * 用户信息
     */
    @org.springframework.data.mongodb.core.mapping.Document(language = """"""", collation = """"""", collection = """"""", value = """"""")
    @nbcp.db.DbEntityGroup(value = """MongoBase"""")
    @nbcp.db.RemoveToSysDustbin
    @nbcp.db.Cn(value = """用户信息"""")
    class BasicUserEntity(collectionName: String = "", databaseId: String = "")
        : MongoBaseMetaCollection<nbcp.db.mongo.entity.BasicUser>(nbcp.db.mongo.entity.BasicUser::class.java, collectionName.AsString("basicUser"), databaseId) {

        val id = MongoColumnName("_id")

        /**
         * 昵称
         */
        @nbcp.db.Cn(value = """昵称"""") 
        val name = MongoColumnName("name")

        /**
         * 登录名
         */
        @nbcp.db.Cn(value = """登录名"""") 
        val loginName = MongoColumnName("loginName")

        /**
         * 手机号
         */
        @nbcp.db.Cn(value = """手机号"""") 
        val mobile = MongoColumnName("mobile")

        /**
         * 电子邮件
         */
        @nbcp.db.Cn(value = """电子邮件"""") 
        val email = MongoColumnName("email")

        /**
         * 头像
         */
        @nbcp.db.Cn(value = """头像"""") 
        val logo = IdUrlMeta("logo")

        /**
         * 备注
         */
        @nbcp.db.Cn(value = """备注"""") 
        val remark = MongoColumnName("remark")

        /**
         * 身份证
         */
        @nbcp.db.Cn(value = """身份证"""") 
        val identityCard = IdentityCardDataMeta("identityCard")

        /**
         * 创建时间
         */
        @nbcp.db.Cn(value = """创建时间"""") 
        val createAt = MongoColumnName("createAt")

        /**
         * 更新时间
         */
        @nbcp.db.Cn(value = """更新时间"""") 
        val updateAt = MongoColumnName("updateAt")

    }

    /**
     * 用户登录信息
     */
    @org.springframework.data.mongodb.core.mapping.Document(language = """"""", collation = """"""", collection = """"""", value = """"""")
    @nbcp.db.DbEntityGroup(value = """MongoBase"""")
    @nbcp.db.Cn(value = """用户登录信息"""")
    @nbcp.db.DbEntityIndex(cacheable = false, unique = true, value = arrayOf("""userId""""))
    class BasicUserLoginInfoEntity(collectionName: String = "", databaseId: String = "")
        : MongoBaseMetaCollection<nbcp.db.mongo.entity.BasicUserLoginInfo>(nbcp.db.mongo.entity.BasicUserLoginInfo::class.java, collectionName.AsString("basicUserLoginInfo"), databaseId) {

        val id = MongoColumnName("_id")

        /**
         * 用户唯一Id
         */
        @nbcp.db.Cn(value = """用户唯一Id"""") 
        val userId = MongoColumnName("userId")

        /**
         * 登录名
         */
        @nbcp.db.Cn(value = """登录名"""") 
        val loginName = MongoColumnName("loginName")

        /**
         * 登录手机
         */
        @nbcp.db.Cn(value = """登录手机"""") 
        val mobile = MongoColumnName("mobile")

        /**
         * 登录邮箱
         */
        @nbcp.db.Cn(value = """登录邮箱"""") 
        val email = MongoColumnName("email")

        /**
         * 密码
         */
        @nbcp.db.Cn(value = """密码"""") 
        val password = MongoColumnName("password")

        /**
         * 最后登录时间
         */
        @nbcp.db.Cn(value = """最后登录时间"""") 
        val lastLoginAt = MongoColumnName("lastLoginAt")

        /**
         * 是否已锁定
         */
        @nbcp.db.Cn(value = """是否已锁定"""") 
        val isLocked = MongoColumnName("isLocked")

        /**
         * 锁定详情
         */
        @nbcp.db.Cn(value = """锁定详情"""") 
        val lockedRemark = MongoColumnName("lockedRemark")

        /**
         * 创建时间
         */
        @nbcp.db.Cn(value = """创建时间"""") 
        val createAt = MongoColumnName("createAt")

        /**
         * 更新时间
         */
        @nbcp.db.Cn(value = """更新时间"""") 
        val updateAt = MongoColumnName("updateAt")

        fun queryByUserId(userId: String): MongoQueryClip<BasicUserLoginInfoEntity, nbcp.db.mongo.entity.BasicUserLoginInfo> {
            return this.query().where { it.userId match userId }
        }

        fun deleteByUserId(userId: String): MongoDeleteClip<BasicUserLoginInfoEntity> {
            return this.delete().where { it.userId match userId }
        }

        fun updateByUserId(userId: String): MongoUpdateClip<BasicUserLoginInfoEntity, nbcp.db.mongo.entity.BasicUserLoginInfo> {
            return this.update().where { it.userId match userId }
        }

    }

    /**
     * 系统附件
     */
    @org.springframework.data.mongodb.core.mapping.Document(language = """"""", collation = """"""", collection = """"""", value = """"""")
    @nbcp.db.DbEntityGroup(value = """MongoBase"""")
    @nbcp.db.Cn(value = """系统附件"""")
    class SysAnnexEntity(collectionName: String = "", databaseId: String = "")
        : MongoBaseMetaCollection<nbcp.db.mongo.entity.SysAnnex>(nbcp.db.mongo.entity.SysAnnex::class.java, collectionName.AsString("sysAnnex"), databaseId) {

        val id = MongoColumnName("_id")

        /**
         * 文件名
         */
        @nbcp.db.Cn(value = """文件名"""") 
        val name = MongoColumnName("name")

        /**
         * 标签
         */
        @nbcp.db.Cn(value = """标签"""") 
        val tags = MongoColumnName("tags")

        /**
         * 扩展名
         */
        @nbcp.db.Cn(value = """扩展名"""") 
        val ext = MongoColumnName("ext")

        /**
         * 大小
         */
        @nbcp.db.Cn(value = """大小"""") 
        val size = MongoColumnName("size")

        /**
         * 图像宽度
         */
        @nbcp.db.Cn(value = """图像宽度"""") 
        val imgWidth = MongoColumnName("imgWidth")

        /**
         * 图像高度
         */
        @nbcp.db.Cn(value = """图像高度"""") 
        val imgHeight = MongoColumnName("imgHeight")

        /**
         * 时长
         */
        @nbcp.db.Cn(value = """时长"""") 
        val videoTime = MongoColumnName("videoTime")

        /**
         * 视频封面地址
         */
        @nbcp.db.Cn(value = """视频封面地址"""") 
        val videoLogoUrl = MongoColumnName("videoLogoUrl")

        /**
         * 网络路径
         */
        @nbcp.db.Cn(value = """网络路径"""") 
        val url = MongoColumnName("url")

        /**
         * 创建者
         */
        @nbcp.db.Cn(value = """创建者"""") 
        val creator = IdNameMeta("creator")

        /**
         * 组
         */
        @nbcp.db.Cn(value = """组"""") 
        val group = MongoColumnName("group")

        /**
         * 所属企业
         */
        @nbcp.db.Cn(value = """所属企业"""") 
        val corpId = MongoColumnName("corpId")

        /**
         * 错误消息
         */
        @nbcp.db.Cn(value = """错误消息"""") 
        val errorMsg = MongoColumnName("errorMsg")

        /**
         * 创建时间
         */
        @nbcp.db.Cn(value = """创建时间"""") 
        val createAt = MongoColumnName("createAt")

        /**
         * 更新时间
         */
        @nbcp.db.Cn(value = """更新时间"""") 
        val updateAt = MongoColumnName("updateAt")

    }

    /**
     * 城市令牌
     */
    @org.springframework.data.mongodb.core.mapping.Document(language = """"""", collation = """"""", collection = """"""", value = """"""")
    @nbcp.db.DbEntityGroup(value = """MongoBase"""")
    @nbcp.db.Cn(value = """城市令牌"""")
    @nbcp.db.DbEntityIndex(cacheable = false, unique = true, value = arrayOf("""code""""))
    class SysCityEntity(collectionName: String = "", databaseId: String = "")
        : MongoBaseMetaCollection<nbcp.db.mongo.entity.SysCity>(nbcp.db.mongo.entity.SysCity::class.java, collectionName.AsString("sysCity"), databaseId) {

        val id = MongoColumnName("_id")

        /**
         * 城市全称
         */
        @nbcp.db.Cn(value = """城市全称"""") 
        val name = MongoColumnName("name")

        /**
         * 城市编码
         */
        @nbcp.db.Cn(value = """城市编码"""") 
        val code = MongoColumnName("code")

        /**
         * 城市短名称
         */
        @nbcp.db.Cn(value = """城市短名称"""") 
        val shortName = MongoColumnName("shortName")

        /**
         * 级别
         */
        @nbcp.db.Cn(value = """级别"""") 
        val level = MongoColumnName("level")

        /**
         * 经度
         */
        @nbcp.db.Cn(value = """经度"""") 
        val lng = MongoColumnName("lng")

        /**
         * 纬度
         */
        @nbcp.db.Cn(value = """纬度"""") 
        val lat = MongoColumnName("lat")

        /**
         * 拼音
         */
        @nbcp.db.Cn(value = """拼音"""") 
        val pinyin = MongoColumnName("pinyin")

        /**
         * 电话码
         */
        @nbcp.db.Cn(value = """电话码"""") 
        val telCode = MongoColumnName("telCode")

        /**
         * 邮编
         */
        @nbcp.db.Cn(value = """邮编"""") 
        val postCode = MongoColumnName("postCode")

        /**
         * 父级码
         */
        @nbcp.db.Cn(value = """父级码"""") 
        val pcode = MongoColumnName("pcode")

        /**
         * 创建时间
         */
        @nbcp.db.Cn(value = """创建时间"""") 
        val createAt = MongoColumnName("createAt")

        /**
         * 更新时间
         */
        @nbcp.db.Cn(value = """更新时间"""") 
        val updateAt = MongoColumnName("updateAt")

        fun queryByCode(code: Int): MongoQueryClip<SysCityEntity, nbcp.db.mongo.entity.SysCity> {
            return this.query().where { it.code match code }
        }

        fun deleteByCode(code: Int): MongoDeleteClip<SysCityEntity> {
            return this.delete().where { it.code match code }
        }

        fun updateByCode(code: Int): MongoUpdateClip<SysCityEntity, nbcp.db.mongo.entity.SysCity> {
            return this.update().where { it.code match code }
        }

    }

    /**
     * 字典
     */
    @org.springframework.data.mongodb.core.mapping.Document(language = """"""", collation = """"""", collection = """"""", value = """"""")
    @nbcp.db.DbEntityGroup(value = """MongoBase"""")
    @nbcp.db.Cn(value = """字典"""")
    @nbcp.db.SortNumber(step = 10, field = """sort"""", groupBy = """"""")
    class SysDictionaryEntity(collectionName: String = "", databaseId: String = "")
        : MongoBaseMetaCollection<nbcp.db.mongo.entity.SysDictionary>(nbcp.db.mongo.entity.SysDictionary::class.java, collectionName.AsString("sysDictionary"), databaseId) {

        val id = MongoColumnName("_id")

        /**
         * 所有者
         */
        @nbcp.db.Cn(value = """所有者"""") 
        val owner = MongoColumnName("owner")

        /**
         * 组
         */
        @nbcp.db.Cn(value = """组"""") 
        val group = MongoColumnName("group")

        /**
         * 键
         */
        @nbcp.db.Cn(value = """键"""") 
        val key = MongoColumnName("key")

        /**
         * 值
         */
        @nbcp.db.Cn(value = """值"""") 
        val value = MongoColumnName("value")

        /**
         * 中文
         */
        @nbcp.db.Cn(value = """中文"""") 
        val label = MongoColumnName("label")

        /**
         * 排序
         */
        @nbcp.db.Cn(value = """排序"""") 
        val sort = MongoColumnName("sort")

        /**
         * 创建时间
         */
        @nbcp.db.Cn(value = """创建时间"""") 
        val createAt = MongoColumnName("createAt")

        /**
         * 更新时间
         */
        @nbcp.db.Cn(value = """更新时间"""") 
        val updateAt = MongoColumnName("updateAt")

    }

    /**
     * 数据垃圾箱
     */
    @org.springframework.data.mongodb.core.mapping.Document(language = """"""", collation = """"""", collection = """"""", value = """"""")
    @nbcp.db.DbEntityGroup(value = """MongoBase"""")
    @nbcp.db.Cn(value = """数据垃圾箱"""")
    class SysDustbinEntity(collectionName: String = "", databaseId: String = "")
        : MongoBaseMetaCollection<nbcp.db.mongo.entity.SysDustbin>(nbcp.db.mongo.entity.SysDustbin::class.java, collectionName.AsString("sysDustbin"), databaseId) {

        val id = MongoColumnName("_id")

        /**
         * 表名
         */
        @nbcp.db.Cn(value = """表名"""") 
        val table = MongoColumnName("table")

        /**
         * 备注
         */
        @nbcp.db.Cn(value = """备注"""") 
        val remark = MongoColumnName("remark")

        /**
         * 创建者
         */
        @nbcp.db.Cn(value = """创建者"""") 
        val creator = IdNameMeta("creator")

        /**
         * 数据
         */
        @nbcp.db.Cn(value = """数据"""") 
        val data = SerializableMeta("data")

        /**
         * 创建时间
         */
        @nbcp.db.Cn(value = """创建时间"""") 
        val createAt = MongoColumnName("createAt")

        /**
         * 更新时间
         */
        @nbcp.db.Cn(value = """更新时间"""") 
        val updateAt = MongoColumnName("updateAt")

    }

    /**
     * 数据版本
     */
    @org.springframework.data.mongodb.core.mapping.Document(language = """"""", collation = """"""", collection = """"""", value = """"""")
    @nbcp.db.DbEntityGroup(value = """MongoBase"""")
    @nbcp.db.Cn(value = """数据版本"""")
    class SysFlywayVersionEntity(collectionName: String = "", databaseId: String = "")
        : MongoBaseMetaCollection<nbcp.db.mongo.entity.SysFlywayVersion>(nbcp.db.mongo.entity.SysFlywayVersion::class.java, collectionName.AsString("sysFlywayVersion"), databaseId) {

        val id = MongoColumnName("_id")

        /**
         * 版本
         */
        @nbcp.db.Cn(value = """版本"""") 
        val version = MongoColumnName("version")

        /**
         * 备注
         */
        @nbcp.db.Cn(value = """备注"""") 
        val remark = MongoColumnName("remark")

        /**
         * 执行的类
         */
        @nbcp.db.Cn(value = """执行的类"""") 
        val execClass = MongoColumnName("execClass")

        /**
         * 执行开始时间
         */
        @nbcp.db.Cn(value = """执行开始时间"""") 
        val startAt = MongoColumnName("startAt")

        /**
         * 执行结束时间
         */
        @nbcp.db.Cn(value = """执行结束时间"""") 
        val finishAt = MongoColumnName("finishAt")

        /**
         * 是否成功
         */
        @nbcp.db.Cn(value = """是否成功"""") 
        val isSuccess = MongoColumnName("isSuccess")

        /**
         * 创建时间
         */
        @nbcp.db.Cn(value = """创建时间"""") 
        val createAt = MongoColumnName("createAt")

        /**
         * 更新时间
         */
        @nbcp.db.Cn(value = """更新时间"""") 
        val updateAt = MongoColumnName("updateAt")

    }

    /**
     * 排序记录号
     */
    @org.springframework.data.mongodb.core.mapping.Document(language = """"""", collation = """"""", collection = """"""", value = """"""")
    @nbcp.db.DbEntityGroup(value = """MongoBase"""")
    @nbcp.db.Cn(value = """排序记录号"""")
    class SysLastSortNumberEntity(collectionName: String = "", databaseId: String = "")
        : MongoBaseMetaCollection<nbcp.db.mongo.entity.SysLastSortNumber>(nbcp.db.mongo.entity.SysLastSortNumber::class.java, collectionName.AsString("sysLastSortNumber"), databaseId) {

        val id = MongoColumnName("_id")

        /**
         * 表名
         */
        @nbcp.db.Cn(value = """表名"""") 
        val table = MongoColumnName("table")

        /**
         * 组
         */
        @nbcp.db.Cn(value = """组"""") 
        val group = MongoColumnName("group")

        /**
         * 值
         */
        @nbcp.db.Cn(value = """值"""") 
        val value = MongoColumnName("value")

        /**
         * 创建时间
         */
        @nbcp.db.Cn(value = """创建时间"""") 
        val createAt = MongoColumnName("createAt")

        /**
         * 更新时间
         */
        @nbcp.db.Cn(value = """更新时间"""") 
        val updateAt = MongoColumnName("updateAt")

    }

    /**
     * 系统日志
     */
    @org.springframework.data.mongodb.core.mapping.Document(language = """"""", collation = """"""", collection = """"""", value = """"""")
    @nbcp.db.DbEntityGroup(value = """MongoBase"""")
    @nbcp.db.Cn(value = """系统日志"""")
    class SysLogEntity(collectionName: String = "", databaseId: String = "")
        : MongoBaseMetaCollection<nbcp.db.mongo.entity.SysLog>(nbcp.db.mongo.entity.SysLog::class.java, collectionName.AsString("sysLog"), databaseId) {

        val id = MongoColumnName("_id")

        /**
         * 模块
         */
        @nbcp.db.Cn(value = """模块"""") 
        val module = MongoColumnName("module")

        /**
         * 类型
         */
        @nbcp.db.Cn(value = """类型"""") 
        val level = MongoColumnName("level")

        /**
         * 标签
         */
        @nbcp.db.Cn(value = """标签"""") 
        val tags = MongoColumnName("tags")

        /**
         * 消息
         */
        @nbcp.db.Cn(value = """消息"""") 
        val msg = MongoColumnName("msg")

        /**
         * 请求数据
         */
        @nbcp.db.Cn(value = """请求数据"""") 
        val request = BaseRequestDataMeta("request")

        /**
         * 程序数据
         */
        @nbcp.db.Cn(value = """程序数据"""") 
        val data = ObjectMeta("data")

        /**
         * 回发数据
         */
        @nbcp.db.Cn(value = """回发数据"""") 
        val response = BaseResponseDataMeta("response")

        /**
         * 创建者Id
         */
        @nbcp.db.Cn(value = """创建者Id"""") 
        val creator = IdNameMeta("creator")

        /**
         * 创建时间
         */
        @nbcp.db.Cn(value = """创建时间"""") 
        val createAt = MongoColumnName("createAt")

    }

    /**
     * 组织信息
     */
    @org.springframework.data.mongodb.core.mapping.Document(language = """"""", collation = """"""", collection = """"""", value = """"""")
    @nbcp.db.DbEntityGroup(value = """MongoBase"""")
    @nbcp.db.RemoveToSysDustbin
    @nbcp.db.Cn(value = """组织信息"""")
    class SysOrganizationEntity(collectionName: String = "", databaseId: String = "")
        : MongoBaseMetaCollection<nbcp.db.mongo.entity.SysOrganization>(nbcp.db.mongo.entity.SysOrganization::class.java, collectionName.AsString("sysOrganization"), databaseId) {

        val id = MongoColumnName("_id")

        /**
         * 组织名称
         */
        @nbcp.db.Cn(value = """组织名称"""") 
        val name = MongoColumnName("name")

        /**
         * 网站地址
         */
        @nbcp.db.Cn(value = """网站地址"""") 
        val siteUrl = MongoColumnName("siteUrl")

        /**
         * 网站备案号
         */
        @nbcp.db.Cn(value = """网站备案号"""") 
        val siteNumber = MongoColumnName("siteNumber")

        /**
         * 所在城市
         */
        @nbcp.db.Cn(value = """所在城市"""") 
        val city = CityCodeNameMeta("city")

        /**
         * 营业执照
         */
        @nbcp.db.Cn(value = """营业执照"""") 
        val businessLicense = BusinessLicenseDataMeta("businessLicense")

        /**
         * 徽标
         */
        @nbcp.db.Cn(value = """徽标"""") 
        val logo = IdUrlMeta("logo")

        /**
         * 是否已锁定
         */
        @nbcp.db.Cn(value = """是否已锁定"""") 
        val isLocked = MongoColumnName("isLocked")

        /**
         * 锁定详情
         */
        @nbcp.db.Cn(value = """锁定详情"""") 
        val lockedRemark = MongoColumnName("lockedRemark")

        /**
         * 创建时间
         */
        @nbcp.db.Cn(value = """创建时间"""") 
        val createAt = MongoColumnName("createAt")

        /**
         * 更新时间
         */
        @nbcp.db.Cn(value = """更新时间"""") 
        val updateAt = MongoColumnName("updateAt")

    }

}



private fun join(vararg args: String): MongoColumnName {
    return MongoColumnName(args.toList().filter { it.HasValue }.joinToString("."))
}

private fun join_map(vararg args: String): moer_map {
    return moer_map(args.toList().filter { it.HasValue }.joinToString("."))
}

data class moer_map(val _pname: String) {
    fun keys(keys: String): String {
        return this._pname + "." + keys
    }
}


