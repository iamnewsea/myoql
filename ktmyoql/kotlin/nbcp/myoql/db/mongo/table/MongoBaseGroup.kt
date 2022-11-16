package nbcp.myoql.db.mongo.table

import java.io.*
import nbcp.base.db.*
import nbcp.base.comm.*
import nbcp.base.extend.*
import nbcp.base.enums.*
import nbcp.base.utils.*
import nbcp.myoql.db.*
import nbcp.myoql.db.comm.*
import nbcp.myoql.db.enums.*
import nbcp.myoql.db.mongo.*
import nbcp.myoql.db.mongo.base.*
import nbcp.myoql.db.mongo.component.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.*


@Component("mongo.MongoBase")
@MetaDataGroup(DatabaseEnum.Mongo, "MongoBase")
class MongoBaseGroup : IDataGroup {
    override fun getEntities(): Set<BaseMetaData<out Any>> = setOf(sysAnnex, sysCity, sysDictionary, sysDustbin, sysFlywayVersion, sysLastSortNumber, sysLog, sysOrganization)



    val sysAnnex get() = SysAnnexEntity();


    val sysCity get() = SysCityEntity();


    val sysDictionary get() = SysDictionaryEntity();


    val sysDustbin get() = SysDustbinEntity();


    val sysFlywayVersion get() = SysFlywayVersionEntity();


    val sysLastSortNumber get() = SysLastSortNumberEntity();


    val sysLog get() = SysLogEntity();


    val sysOrganization get() = SysOrganizationEntity();



    @org.springframework.data.mongodb.core.mapping.Document(value = """""", language = """""", collection = """""", collation = """""")
    @nbcp.base.db.annotation.DbEntityGroup(value = """MongoBase""")
    @nbcp.base.db.annotation.Cn(value = """系统附件""")
    class SysAnnexEntity(collectionName: String = "sysAnnex", databaseId: String = "")
        : MongoBaseMetaCollection<nbcp.myoql.db.mongo.entity.SysAnnex>(nbcp.myoql.db.mongo.entity.SysAnnex::class.java, collectionName, databaseId) {

        /**
         * 
         */ 
        val id = MongoColumnName("_id")

        @nbcp.base.db.annotation.Cn(value = """文件名""") 
        val name = MongoColumnName("name")

        @nbcp.base.db.annotation.Cn(value = """标签""") 
        val tags = MongoColumnName("tags")

        @nbcp.base.db.annotation.Cn(value = """扩展名""") 
        val ext = MongoColumnName("ext")

        @nbcp.base.db.annotation.Cn(value = """大小""") 
        val size = MongoColumnName("size")

        @nbcp.base.db.annotation.Cn(value = """图像宽度""") 
        val imgWidth = MongoColumnName("imgWidth")

        @nbcp.base.db.annotation.Cn(value = """图像高度""") 
        val imgHeight = MongoColumnName("imgHeight")

        @nbcp.base.db.annotation.Cn(value = """时长""") 
        val videoTime = MongoColumnName("videoTime")

        @nbcp.base.db.annotation.Cn(value = """视频封面地址""") 
        val videoLogoUrl = MongoColumnName("videoLogoUrl")

        @nbcp.base.db.annotation.Cn(value = """存储类型""") 
        val storageType = MongoColumnName("storageType")

        @nbcp.base.db.annotation.Cn(value = """下载地址""") 
        val url = MongoColumnName("url")

        @nbcp.base.db.annotation.Cn(value = """创建者""") 
        val creator = IdNameMeta("creator")

        @nbcp.base.db.annotation.Cn(value = """组""") 
        val group = MongoColumnName("group")

        @nbcp.base.db.annotation.Cn(value = """所属企业""") 
        val corpId = MongoColumnName("corpId")

        @nbcp.base.db.annotation.Cn(value = """错误消息""") 
        val errorMsg = MongoColumnName("errorMsg")

        @nbcp.base.db.annotation.Cn(value = """创建时间""") 
        val createAt = MongoColumnName("createAt")

        @nbcp.base.db.annotation.Cn(value = """更新时间""") 
        val updateAt = MongoColumnName("updateAt")

    }


    @org.springframework.data.mongodb.core.mapping.Document(value = """""", language = """""", collection = """""", collation = """""")
    @nbcp.base.db.annotation.DbEntityGroup(value = """MongoBase""")
    @nbcp.base.db.annotation.Cn(value = """城市令牌""")
    @nbcp.base.db.annotation.DbEntityIndex(value = arrayOf("""code"""), unique = true, cacheable = false)
    class SysCityEntity(collectionName: String = "sysCity", databaseId: String = "")
        : MongoBaseMetaCollection<nbcp.myoql.db.mongo.entity.SysCity>(nbcp.myoql.db.mongo.entity.SysCity::class.java, collectionName, databaseId) {

        /**
         * 
         */ 
        val id = MongoColumnName("_id")

        @nbcp.base.db.annotation.Cn(value = """城市全称""") 
        val name = MongoColumnName("name")

        @nbcp.base.db.annotation.Cn(value = """城市编码""") 
        val code = MongoColumnName("code")

        @nbcp.base.db.annotation.Cn(value = """城市短名称""") 
        val shortName = MongoColumnName("shortName")

        @nbcp.base.db.annotation.Cn(value = """级别""") 
        val level = MongoColumnName("level")

        @nbcp.base.db.annotation.Cn(value = """经度""") 
        val lng = MongoColumnName("lng")

        @nbcp.base.db.annotation.Cn(value = """纬度""") 
        val lat = MongoColumnName("lat")

        @nbcp.base.db.annotation.Cn(value = """拼音""") 
        val pinyin = MongoColumnName("pinyin")

        @nbcp.base.db.annotation.Cn(value = """电话码""") 
        val telCode = MongoColumnName("telCode")

        @nbcp.base.db.annotation.Cn(value = """邮编""") 
        val postCode = MongoColumnName("postCode")

        @nbcp.base.db.annotation.Cn(value = """父级码""") 
        val pcode = MongoColumnName("pcode")

        @nbcp.base.db.annotation.Cn(value = """创建时间""") 
        val createAt = MongoColumnName("createAt")

        @nbcp.base.db.annotation.Cn(value = """更新时间""") 
        val updateAt = MongoColumnName("updateAt")

        fun queryByCode(code: Int): MongoQueryClip<SysCityEntity, nbcp.myoql.db.mongo.entity.SysCity> {
            return this.query().where { it.code match code }
        }

        fun deleteByCode(code: Int): MongoDeleteClip<SysCityEntity> {
            return this.delete().where { it.code match code }
        }

        fun updateByCode(code: Int): MongoUpdateClip<SysCityEntity, nbcp.myoql.db.mongo.entity.SysCity> {
            return this.update().where { it.code match code }
        }

    }


    @org.springframework.data.mongodb.core.mapping.Document(value = """""", language = """""", collection = """""", collation = """""")
    @nbcp.base.db.annotation.DbEntityGroup(value = """MongoBase""")
    @nbcp.base.db.annotation.Cn(value = """字典""")
    @nbcp.myoql.db.comm.SortNumber(field = """sort""", step = 10, groupBy = """""")
    class SysDictionaryEntity(collectionName: String = "sysDictionary", databaseId: String = "")
        : MongoBaseMetaCollection<nbcp.myoql.db.mongo.entity.SysDictionary>(nbcp.myoql.db.mongo.entity.SysDictionary::class.java, collectionName, databaseId) {

        /**
         * 
         */ 
        val id = MongoColumnName("_id")

        @nbcp.base.db.annotation.Cn(value = """所有者""") 
        val owner = MongoColumnName("owner")

        @nbcp.base.db.annotation.Cn(value = """组""") 
        val group = MongoColumnName("group")

        @nbcp.base.db.annotation.Cn(value = """键""") 
        val key = MongoColumnName("key")

        @nbcp.base.db.annotation.Cn(value = """值""") 
        val value = MongoColumnName("value")

        @nbcp.base.db.annotation.Cn(value = """备注""") 
        val remark = MongoColumnName("remark")

        @nbcp.base.db.annotation.Cn(value = """排序""") 
        val sort = MongoColumnName("sort")

        @nbcp.base.db.annotation.Cn(value = """创建时间""") 
        val createAt = MongoColumnName("createAt")

        @nbcp.base.db.annotation.Cn(value = """更新时间""") 
        val updateAt = MongoColumnName("updateAt")

    }


    @org.springframework.data.mongodb.core.mapping.Document(value = """""", language = """""", collection = """""", collation = """""")
    @nbcp.base.db.annotation.DbEntityGroup(value = """MongoBase""")
    @nbcp.base.db.annotation.Cn(value = """数据垃圾箱""")
    class SysDustbinEntity(collectionName: String = "sysDustbin", databaseId: String = "")
        : MongoBaseMetaCollection<nbcp.myoql.db.mongo.entity.SysDustbin>(nbcp.myoql.db.mongo.entity.SysDustbin::class.java, collectionName, databaseId) {

        /**
         * 
         */ 
        val id = MongoColumnName("_id")

        @nbcp.base.db.annotation.Cn(value = """表名""") 
        val table = MongoColumnName("table")

        @nbcp.base.db.annotation.Cn(value = """备注""") 
        val remark = MongoColumnName("remark")

        @nbcp.base.db.annotation.Cn(value = """创建者""") 
        val creator = IdNameMeta("creator")

        @nbcp.base.db.annotation.Cn(value = """数据""") 
        val data = SerializableMeta("data")

        @nbcp.base.db.annotation.Cn(value = """创建时间""") 
        val createAt = MongoColumnName("createAt")

        @nbcp.base.db.annotation.Cn(value = """更新时间""") 
        val updateAt = MongoColumnName("updateAt")

    }


    @org.springframework.data.mongodb.core.mapping.Document(value = """""", language = """""", collection = """""", collation = """""")
    @nbcp.base.db.annotation.DbEntityGroup(value = """MongoBase""")
    @nbcp.base.db.annotation.Cn(value = """数据版本""")
    class SysFlywayVersionEntity(collectionName: String = "sysFlywayVersion", databaseId: String = "")
        : MongoBaseMetaCollection<nbcp.myoql.db.mongo.entity.SysFlywayVersion>(nbcp.myoql.db.mongo.entity.SysFlywayVersion::class.java, collectionName, databaseId) {

        /**
         * 
         */ 
        val id = MongoColumnName("_id")

        @nbcp.base.db.annotation.Cn(value = """版本""") 
        val version = MongoColumnName("version")

        @nbcp.base.db.annotation.Cn(value = """备注""") 
        val remark = MongoColumnName("remark")

        @nbcp.base.db.annotation.Cn(value = """执行的类""") 
        val execClass = MongoColumnName("execClass")

        @nbcp.base.db.annotation.Cn(value = """执行开始时间""") 
        val startAt = MongoColumnName("startAt")

        @nbcp.base.db.annotation.Cn(value = """执行结束时间""") 
        val finishAt = MongoColumnName("finishAt")

        @nbcp.base.db.annotation.Cn(value = """是否成功""") 
        val isSuccess = MongoColumnName("isSuccess")

        @nbcp.base.db.annotation.Cn(value = """创建时间""") 
        val createAt = MongoColumnName("createAt")

        @nbcp.base.db.annotation.Cn(value = """更新时间""") 
        val updateAt = MongoColumnName("updateAt")

    }


    @org.springframework.data.mongodb.core.mapping.Document(value = """""", language = """""", collection = """""", collation = """""")
    @nbcp.base.db.annotation.DbEntityGroup(value = """MongoBase""")
    @nbcp.base.db.annotation.Cn(value = """排序记录号""")
    class SysLastSortNumberEntity(collectionName: String = "sysLastSortNumber", databaseId: String = "")
        : MongoBaseMetaCollection<nbcp.myoql.db.mongo.entity.SysLastSortNumber>(nbcp.myoql.db.mongo.entity.SysLastSortNumber::class.java, collectionName, databaseId) {

        /**
         * 
         */ 
        val id = MongoColumnName("_id")

        @nbcp.base.db.annotation.Cn(value = """表名""") 
        val table = MongoColumnName("table")

        @nbcp.base.db.annotation.Cn(value = """组""") 
        val group = MongoColumnName("group")

        @nbcp.base.db.annotation.Cn(value = """值""") 
        val value = MongoColumnName("value")

        @nbcp.base.db.annotation.Cn(value = """创建时间""") 
        val createAt = MongoColumnName("createAt")

        @nbcp.base.db.annotation.Cn(value = """更新时间""") 
        val updateAt = MongoColumnName("updateAt")

    }


    @org.springframework.data.mongodb.core.mapping.Document(value = """""", language = """""", collection = """""", collation = """""")
    @nbcp.base.db.annotation.DbEntityGroup(value = """MongoBase""")
    @nbcp.base.db.annotation.Cn(value = """系统日志""")
    class SysLogEntity(collectionName: String = "sysLog", databaseId: String = "")
        : MongoBaseMetaCollection<nbcp.myoql.db.mongo.entity.SysLog>(nbcp.myoql.db.mongo.entity.SysLog::class.java, collectionName, databaseId) {

        /**
         * 
         */ 
        val id = MongoColumnName("_id")

        @nbcp.base.db.annotation.Cn(value = """模块""") 
        val module = MongoColumnName("module")

        @nbcp.base.db.annotation.Cn(value = """类型""") 
        val level = MongoColumnName("level")

        @nbcp.base.db.annotation.Cn(value = """标签""") 
        val tags = MongoColumnName("tags")

        @nbcp.base.db.annotation.Cn(value = """消息""") 
        val msg = MongoColumnName("msg")

        @nbcp.base.db.annotation.Cn(value = """请求数据""") 
        val request = BaseRequestDataMeta("request")

        @nbcp.base.db.annotation.Cn(value = """程序数据""") 
        val data = ObjectMeta("data")

        @nbcp.base.db.annotation.Cn(value = """回发数据""") 
        val response = BaseResponseDataMeta("response")

        @nbcp.base.db.annotation.Cn(value = """创建者Id""") 
        val creator = IdNameMeta("creator")

        @nbcp.base.db.annotation.Cn(value = """创建时间""") 
        val createAt = MongoColumnName("createAt")

    }


    @org.springframework.data.mongodb.core.mapping.Document(value = """""", language = """""", collection = """""", collation = """""")
    @nbcp.base.db.annotation.DbEntityGroup(value = """MongoBase""")
    @nbcp.myoql.db.comm.RemoveToSysDustbin
    @nbcp.base.db.annotation.Cn(value = """组织信息""")
    class SysOrganizationEntity(collectionName: String = "sysOrganization", databaseId: String = "")
        : MongoBaseMetaCollection<nbcp.myoql.db.mongo.entity.SysOrganization>(nbcp.myoql.db.mongo.entity.SysOrganization::class.java, collectionName, databaseId) {

        /**
         * 
         */ 
        val id = MongoColumnName("_id")

        @nbcp.base.db.annotation.Cn(value = """组织名称""") 
        val name = MongoColumnName("name")

        @nbcp.base.db.annotation.Cn(value = """网站地址""") 
        val siteUrl = MongoColumnName("siteUrl")

        @nbcp.base.db.annotation.Cn(value = """网站备案号""") 
        val siteNumber = MongoColumnName("siteNumber")

        @nbcp.base.db.annotation.Cn(value = """所在城市""") 
        val city = CityCodeNameMeta("city")

        @nbcp.base.db.annotation.Cn(value = """营业执照""") 
        val businessLicense = BusinessLicenseDataMeta("businessLicense")

        @nbcp.base.db.annotation.Cn(value = """徽标""") 
        val logo = IdUrlMeta("logo")

        @nbcp.base.db.annotation.Cn(value = """是否已锁定""") 
        val isLocked = MongoColumnName("isLocked")

        @nbcp.base.db.annotation.Cn(value = """锁定详情""") 
        val lockedRemark = MongoColumnName("lockedRemark")

        @nbcp.base.db.annotation.Cn(value = """创建时间""") 
        val createAt = MongoColumnName("createAt")

        @nbcp.base.db.annotation.Cn(value = """更新时间""") 
        val updateAt = MongoColumnName("updateAt")

    }

}
