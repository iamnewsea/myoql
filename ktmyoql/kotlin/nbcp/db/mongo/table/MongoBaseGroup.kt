package nbcp.db.mongo.table

import nbcp.db.*
import nbcp.db.mongo.*
import nbcp.utils.*
import nbcp.comm.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component


//generate auto @2022-08-10 14:32:54

@Component("mongo.MongoBase")
@MetaDataGroup(DatabaseEnum.Mongo, "MongoBase")
class MongoBaseGroup : IDataGroup {
    override fun getEntities(): Set<BaseMetaData> = setOf(sysAnnex, sysCity, sysDictionary, sysDustbin, sysFlywayVersion, sysLastSortNumber, sysLog, sysOrganization)


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
     * 系统附件
     */
    @org.springframework.data.mongodb.core.mapping.Document(language = """""", collection = """""", collation = """""", value = """""")
    @nbcp.db.DbEntityGroup(value = """MongoBase""")
    @nbcp.db.Cn(value = """系统附件""")
    class SysAnnexEntity(collectionName: String = "", databaseId: String = "")
        : MongoBaseMetaCollection<nbcp.db.mongo.entity.SysAnnex>(nbcp.db.mongo.entity.SysAnnex::class.java, "sysAnnex", collectionName.AsString("sysAnnex"), databaseId) {

        val id = MongoColumnName("_id")

        /**
         * 文件名
         */
        @nbcp.db.Cn(value = """文件名""") 
        val name = MongoColumnName("name")

        /**
         * 标签
         */
        @nbcp.db.Cn(value = """标签""") 
        val tags = MongoColumnName("tags")

        /**
         * 扩展名
         */
        @nbcp.db.Cn(value = """扩展名""") 
        val ext = MongoColumnName("ext")

        /**
         * 大小
         */
        @nbcp.db.Cn(value = """大小""") 
        val size = MongoColumnName("size")

        /**
         * 图像宽度
         */
        @nbcp.db.Cn(value = """图像宽度""") 
        val imgWidth = MongoColumnName("imgWidth")

        /**
         * 图像高度
         */
        @nbcp.db.Cn(value = """图像高度""") 
        val imgHeight = MongoColumnName("imgHeight")

        /**
         * 时长
         */
        @nbcp.db.Cn(value = """时长""") 
        val videoTime = MongoColumnName("videoTime")

        /**
         * 视频封面地址
         */
        @nbcp.db.Cn(value = """视频封面地址""") 
        val videoLogoUrl = MongoColumnName("videoLogoUrl")

        /**
         * 存储类型
         */
        @nbcp.db.Cn(value = """存储类型""") 
        val storageType = MongoColumnName("storageType")

        /**
         * 下载地址
         */
        @nbcp.db.Cn(value = """下载地址""") 
        val url = MongoColumnName("url")

        /**
         * 创建者
         */
        @nbcp.db.Cn(value = """创建者""") 
        val creator = IdNameMeta("creator")

        /**
         * 组
         */
        @nbcp.db.Cn(value = """组""") 
        val group = MongoColumnName("group")

        /**
         * 所属企业
         */
        @nbcp.db.Cn(value = """所属企业""") 
        val corpId = MongoColumnName("corpId")

        /**
         * 错误消息
         */
        @nbcp.db.Cn(value = """错误消息""") 
        val errorMsg = MongoColumnName("errorMsg")

        /**
         * 创建时间
         */
        @nbcp.db.Cn(value = """创建时间""") 
        val createAt = MongoColumnName("createAt")

        /**
         * 更新时间
         */
        @nbcp.db.Cn(value = """更新时间""") 
        val updateAt = MongoColumnName("updateAt")

    }

    /**
     * 城市令牌
     */
    @org.springframework.data.mongodb.core.mapping.Document(language = """""", collection = """""", collation = """""", value = """""")
    @nbcp.db.DbEntityGroup(value = """MongoBase""")
    @nbcp.db.Cn(value = """城市令牌""")
    @nbcp.db.DbEntityIndex(cacheable = false, unique = true, value = arrayOf("""code"""))
    class SysCityEntity(collectionName: String = "", databaseId: String = "")
        : MongoBaseMetaCollection<nbcp.db.mongo.entity.SysCity>(nbcp.db.mongo.entity.SysCity::class.java, "sysCity", collectionName.AsString("sysCity"), databaseId) {

        val id = MongoColumnName("_id")

        /**
         * 城市全称
         */
        @nbcp.db.Cn(value = """城市全称""") 
        val name = MongoColumnName("name")

        /**
         * 城市编码
         */
        @nbcp.db.Cn(value = """城市编码""") 
        val code = MongoColumnName("code")

        /**
         * 城市短名称
         */
        @nbcp.db.Cn(value = """城市短名称""") 
        val shortName = MongoColumnName("shortName")

        /**
         * 级别
         */
        @nbcp.db.Cn(value = """级别""") 
        val level = MongoColumnName("level")

        /**
         * 经度
         */
        @nbcp.db.Cn(value = """经度""") 
        val lng = MongoColumnName("lng")

        /**
         * 纬度
         */
        @nbcp.db.Cn(value = """纬度""") 
        val lat = MongoColumnName("lat")

        /**
         * 拼音
         */
        @nbcp.db.Cn(value = """拼音""") 
        val pinyin = MongoColumnName("pinyin")

        /**
         * 电话码
         */
        @nbcp.db.Cn(value = """电话码""") 
        val telCode = MongoColumnName("telCode")

        /**
         * 邮编
         */
        @nbcp.db.Cn(value = """邮编""") 
        val postCode = MongoColumnName("postCode")

        /**
         * 父级码
         */
        @nbcp.db.Cn(value = """父级码""") 
        val pcode = MongoColumnName("pcode")

        /**
         * 创建时间
         */
        @nbcp.db.Cn(value = """创建时间""") 
        val createAt = MongoColumnName("createAt")

        /**
         * 更新时间
         */
        @nbcp.db.Cn(value = """更新时间""") 
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
    @org.springframework.data.mongodb.core.mapping.Document(language = """""", collection = """""", collation = """""", value = """""")
    @nbcp.db.DbEntityGroup(value = """MongoBase""")
    @nbcp.db.Cn(value = """字典""")
    @nbcp.db.SortNumber(step = 10, field = """sort""", groupBy = """""")
    class SysDictionaryEntity(collectionName: String = "", databaseId: String = "")
        : MongoBaseMetaCollection<nbcp.db.mongo.entity.SysDictionary>(nbcp.db.mongo.entity.SysDictionary::class.java, "sysDictionary", collectionName.AsString("sysDictionary"), databaseId) {

        val id = MongoColumnName("_id")

        /**
         * 所有者
         */
        @nbcp.db.Cn(value = """所有者""") 
        val owner = MongoColumnName("owner")

        /**
         * 组
         */
        @nbcp.db.Cn(value = """组""") 
        val group = MongoColumnName("group")

        /**
         * 键
         */
        @nbcp.db.Cn(value = """键""") 
        val key = MongoColumnName("key")

        /**
         * 值
         */
        @nbcp.db.Cn(value = """值""") 
        val value = MongoColumnName("value")

        /**
         * 备注
         */
        @nbcp.db.Cn(value = """备注""") 
        val remark = MongoColumnName("remark")

        /**
         * 排序
         */
        @nbcp.db.Cn(value = """排序""") 
        val sort = MongoColumnName("sort")

        /**
         * 创建时间
         */
        @nbcp.db.Cn(value = """创建时间""") 
        val createAt = MongoColumnName("createAt")

        /**
         * 更新时间
         */
        @nbcp.db.Cn(value = """更新时间""") 
        val updateAt = MongoColumnName("updateAt")

    }

    /**
     * 数据垃圾箱
     */
    @org.springframework.data.mongodb.core.mapping.Document(language = """""", collection = """""", collation = """""", value = """""")
    @nbcp.db.DbEntityGroup(value = """MongoBase""")
    @nbcp.db.Cn(value = """数据垃圾箱""")
    class SysDustbinEntity(collectionName: String = "", databaseId: String = "")
        : MongoBaseMetaCollection<nbcp.db.mongo.entity.SysDustbin>(nbcp.db.mongo.entity.SysDustbin::class.java, "sysDustbin", collectionName.AsString("sysDustbin"), databaseId) {

        val id = MongoColumnName("_id")

        /**
         * 表名
         */
        @nbcp.db.Cn(value = """表名""") 
        val table = MongoColumnName("table")

        /**
         * 备注
         */
        @nbcp.db.Cn(value = """备注""") 
        val remark = MongoColumnName("remark")

        /**
         * 创建者
         */
        @nbcp.db.Cn(value = """创建者""") 
        val creator = IdNameMeta("creator")

        /**
         * 数据
         */
        @nbcp.db.Cn(value = """数据""") 
        val data = SerializableMeta("data")

        /**
         * 创建时间
         */
        @nbcp.db.Cn(value = """创建时间""") 
        val createAt = MongoColumnName("createAt")

        /**
         * 更新时间
         */
        @nbcp.db.Cn(value = """更新时间""") 
        val updateAt = MongoColumnName("updateAt")

    }

    /**
     * 数据版本
     */
    @org.springframework.data.mongodb.core.mapping.Document(language = """""", collection = """""", collation = """""", value = """""")
    @nbcp.db.DbEntityGroup(value = """MongoBase""")
    @nbcp.db.Cn(value = """数据版本""")
    class SysFlywayVersionEntity(collectionName: String = "", databaseId: String = "")
        : MongoBaseMetaCollection<nbcp.db.mongo.entity.SysFlywayVersion>(nbcp.db.mongo.entity.SysFlywayVersion::class.java, "sysFlywayVersion", collectionName.AsString("sysFlywayVersion"), databaseId) {

        val id = MongoColumnName("_id")

        /**
         * 版本
         */
        @nbcp.db.Cn(value = """版本""") 
        val version = MongoColumnName("version")

        /**
         * 备注
         */
        @nbcp.db.Cn(value = """备注""") 
        val remark = MongoColumnName("remark")

        /**
         * 执行的类
         */
        @nbcp.db.Cn(value = """执行的类""") 
        val execClass = MongoColumnName("execClass")

        /**
         * 执行开始时间
         */
        @nbcp.db.Cn(value = """执行开始时间""") 
        val startAt = MongoColumnName("startAt")

        /**
         * 执行结束时间
         */
        @nbcp.db.Cn(value = """执行结束时间""") 
        val finishAt = MongoColumnName("finishAt")

        /**
         * 是否成功
         */
        @nbcp.db.Cn(value = """是否成功""") 
        val isSuccess = MongoColumnName("isSuccess")

        /**
         * 创建时间
         */
        @nbcp.db.Cn(value = """创建时间""") 
        val createAt = MongoColumnName("createAt")

        /**
         * 更新时间
         */
        @nbcp.db.Cn(value = """更新时间""") 
        val updateAt = MongoColumnName("updateAt")

    }

    /**
     * 排序记录号
     */
    @org.springframework.data.mongodb.core.mapping.Document(language = """""", collection = """""", collation = """""", value = """""")
    @nbcp.db.DbEntityGroup(value = """MongoBase""")
    @nbcp.db.Cn(value = """排序记录号""")
    class SysLastSortNumberEntity(collectionName: String = "", databaseId: String = "")
        : MongoBaseMetaCollection<nbcp.db.mongo.entity.SysLastSortNumber>(nbcp.db.mongo.entity.SysLastSortNumber::class.java, "sysLastSortNumber", collectionName.AsString("sysLastSortNumber"), databaseId) {

        val id = MongoColumnName("_id")

        /**
         * 表名
         */
        @nbcp.db.Cn(value = """表名""") 
        val table = MongoColumnName("table")

        /**
         * 组
         */
        @nbcp.db.Cn(value = """组""") 
        val group = MongoColumnName("group")

        /**
         * 值
         */
        @nbcp.db.Cn(value = """值""") 
        val value = MongoColumnName("value")

        /**
         * 创建时间
         */
        @nbcp.db.Cn(value = """创建时间""") 
        val createAt = MongoColumnName("createAt")

        /**
         * 更新时间
         */
        @nbcp.db.Cn(value = """更新时间""") 
        val updateAt = MongoColumnName("updateAt")

    }

    /**
     * 系统日志
     */
    @org.springframework.data.mongodb.core.mapping.Document(language = """""", collection = """""", collation = """""", value = """""")
    @nbcp.db.DbEntityGroup(value = """MongoBase""")
    @nbcp.db.Cn(value = """系统日志""")
    class SysLogEntity(collectionName: String = "", databaseId: String = "")
        : MongoBaseMetaCollection<nbcp.db.mongo.entity.SysLog>(nbcp.db.mongo.entity.SysLog::class.java, "sysLog", collectionName.AsString("sysLog"), databaseId) {

        val id = MongoColumnName("_id")

        /**
         * 模块
         */
        @nbcp.db.Cn(value = """模块""") 
        val module = MongoColumnName("module")

        /**
         * 类型
         */
        @nbcp.db.Cn(value = """类型""") 
        val level = MongoColumnName("level")

        /**
         * 标签
         */
        @nbcp.db.Cn(value = """标签""") 
        val tags = MongoColumnName("tags")

        /**
         * 消息
         */
        @nbcp.db.Cn(value = """消息""") 
        val msg = MongoColumnName("msg")

        /**
         * 请求数据
         */
        @nbcp.db.Cn(value = """请求数据""") 
        val request = BaseRequestDataMeta("request")

        /**
         * 程序数据
         */
        @nbcp.db.Cn(value = """程序数据""") 
        val data = ObjectMeta("data")

        /**
         * 回发数据
         */
        @nbcp.db.Cn(value = """回发数据""") 
        val response = BaseResponseDataMeta("response")

        /**
         * 创建者Id
         */
        @nbcp.db.Cn(value = """创建者Id""") 
        val creator = IdNameMeta("creator")

        /**
         * 创建时间
         */
        @nbcp.db.Cn(value = """创建时间""") 
        val createAt = MongoColumnName("createAt")

    }

    /**
     * 组织信息
     */
    @org.springframework.data.mongodb.core.mapping.Document(language = """""", collection = """""", collation = """""", value = """""")
    @nbcp.db.DbEntityGroup(value = """MongoBase""")
    @nbcp.db.RemoveToSysDustbin
    @nbcp.db.Cn(value = """组织信息""")
    class SysOrganizationEntity(collectionName: String = "", databaseId: String = "")
        : MongoBaseMetaCollection<nbcp.db.mongo.entity.SysOrganization>(nbcp.db.mongo.entity.SysOrganization::class.java, "sysOrganization", collectionName.AsString("sysOrganization"), databaseId) {

        val id = MongoColumnName("_id")

        /**
         * 组织名称
         */
        @nbcp.db.Cn(value = """组织名称""") 
        val name = MongoColumnName("name")

        /**
         * 网站地址
         */
        @nbcp.db.Cn(value = """网站地址""") 
        val siteUrl = MongoColumnName("siteUrl")

        /**
         * 网站备案号
         */
        @nbcp.db.Cn(value = """网站备案号""") 
        val siteNumber = MongoColumnName("siteNumber")

        /**
         * 所在城市
         */
        @nbcp.db.Cn(value = """所在城市""") 
        val city = CityCodeNameMeta("city")

        /**
         * 营业执照
         */
        @nbcp.db.Cn(value = """营业执照""") 
        val businessLicense = BusinessLicenseDataMeta("businessLicense")

        /**
         * 徽标
         */
        @nbcp.db.Cn(value = """徽标""") 
        val logo = IdUrlMeta("logo")

        /**
         * 是否已锁定
         */
        @nbcp.db.Cn(value = """是否已锁定""") 
        val isLocked = MongoColumnName("isLocked")

        /**
         * 锁定详情
         */
        @nbcp.db.Cn(value = """锁定详情""") 
        val lockedRemark = MongoColumnName("lockedRemark")

        /**
         * 创建时间
         */
        @nbcp.db.Cn(value = """创建时间""") 
        val createAt = MongoColumnName("createAt")

        /**
         * 更新时间
         */
        @nbcp.db.Cn(value = """更新时间""") 
        val updateAt = MongoColumnName("updateAt")

    }

}