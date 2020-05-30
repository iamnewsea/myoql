package nbcp.db.mongo.table

import nbcp.db.*
import nbcp.db.mongo.*
import nbcp.utils.*
import nbcp.comm.*
import nbcp.db.mongo.entity.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component


//generate auto @2020-05-30 13:17:36

class IdUrlMeta (private val _pname:String):MongoColumnName() {
    constructor(_val:MongoColumnName):this(_val.toString()) {}

    val id=join(this._pname, "_id")
    val url=join(this._pname, "url")

    override fun toString(): String {
        return join(this._pname).toString()
    }
}

class IdentityCardDataMeta (private val _pname:String):MongoColumnName() {
    constructor(_val:MongoColumnName):this(_val.toString()) {}

    val photo=IdUrlMeta(join(this._pname,"photo"))
    val name=join(this._pname, "name")
    val number=join(this._pname, "number")
    val sex=join(this._pname, "sex")
    val birthday=join(this._pname, "birthday")
    val location=join(this._pname, "location")

    override fun toString(): String {
        return join(this._pname).toString()
    }
}

class IntCodeNameMeta (private val _pname:String):MongoColumnName() {
    constructor(_val:MongoColumnName):this(_val.toString()) {}

    val code=join(this._pname, "code")
    val name=join(this._pname, "name")

    override fun toString(): String {
        return join(this._pname).toString()
    }
}

class IdNameMeta (private val _pname:String):MongoColumnName() {
    constructor(_val:MongoColumnName):this(_val.toString()) {}

    val id=join(this._pname, "_id")
    val name=join(this._pname, "name")

    override fun toString(): String {
        return join(this._pname).toString()
    }
}

class ObjectMeta (private val _pname:String):MongoColumnName() {
    constructor(_val:MongoColumnName):this(_val.toString()) {}



    override fun toString(): String {
        return join(this._pname).toString()
    }
}

class BusinessLicenseDataMeta (private val _pname:String):MongoColumnName() {
    constructor(_val:MongoColumnName):this(_val.toString()) {}

    val code=join(this._pname, "code")
    val name=join(this._pname, "name")
    val legalPerson=join(this._pname, "legalPerson")
    val type=join(this._pname, "type")
    val businessScope=join(this._pname, "businessScope")
    val registeredCapital=join(this._pname, "registeredCapital")
    val buildAt=join(this._pname, "buildAt")
    val businessTerm=join(this._pname, "businessTerm")
    val location=join(this._pname, "location")
    val registeOrganization=join(this._pname, "registeOrganization")
    val registeAt=join(this._pname, "registeAt")

    override fun toString(): String {
        return join(this._pname).toString()
    }
}


@Component("mongo.MongoBase")
@MetaDataGroup("MongoBase")
object MongoBaseGroup : IDataGroup{
    override fun getEntities():Set<BaseMetaData> = setOf(basicUser,basicUserLoginInfo,sysAnnex,sysApplication,sysCity,sysDustbin,sysLog,sysOrganization)

    val basicUser=BasicUserEntity();
    fun basicUser(collectionName:String)=BasicUserEntity(collectionName);
    val basicUserLoginInfo=BasicUserLoginInfoEntity();
    fun basicUserLoginInfo(collectionName:String)=BasicUserLoginInfoEntity(collectionName);
    val sysAnnex=SysAnnexEntity();
    fun sysAnnex(collectionName:String)=SysAnnexEntity(collectionName);
    val sysApplication=SysApplicationEntity();
    fun sysApplication(collectionName:String)=SysApplicationEntity(collectionName);
    val sysCity=SysCityEntity();
    fun sysCity(collectionName:String)=SysCityEntity(collectionName);
    val sysDustbin=SysDustbinEntity();
    fun sysDustbin(collectionName:String)=SysDustbinEntity(collectionName);
    val sysLog=SysLogEntity();
    fun sysLog(collectionName:String)=SysLogEntity(collectionName);
    val sysOrganization=SysOrganizationEntity();
    fun sysOrganization(collectionName:String)=SysOrganizationEntity(collectionName);


    class BasicUserEntity(collectionName:String="")
        :MongoBaseMetaCollection<nbcp.db.mongo.entity.BasicUser>(nbcp.db.mongo.entity.BasicUser::class.java,collectionName.AsString("basicUser")) {
        val name=MongoColumnName("name")
        val loginName=MongoColumnName("loginName")
        val mobile=MongoColumnName("mobile")
        val email=MongoColumnName("email")
        val logo=IdUrlMeta("logo")
        val remark=MongoColumnName("remark")
        val identityCard=IdentityCardDataMeta("identityCard")
        val liveCity=IntCodeNameMeta("liveCity")
        val liveLocation=MongoColumnName("liveLocation")
        val workCity=IntCodeNameMeta("workCity")
        val workLocation=MongoColumnName("workLocation")
        val id=MongoColumnName("_id")
        val createAt=MongoColumnName("createAt")
        val updateAt=MongoColumnName("updateAt")
    
    }
    
    class BasicUserLoginInfoEntity(collectionName:String="")
        :MongoBaseMetaCollection<nbcp.db.mongo.entity.BasicUserLoginInfo>(nbcp.db.mongo.entity.BasicUserLoginInfo::class.java,collectionName.AsString("basicUserLoginInfo")) {
        val userId=MongoColumnName("userId")
        val loginName=MongoColumnName("loginName")
        val mobile=MongoColumnName("mobile")
        val email=MongoColumnName("email")
        val password=MongoColumnName("password")
        val lastLoginAt=MongoColumnName("lastLoginAt")
        val authorizeCode=MongoColumnName("authorizeCode")
        val token=MongoColumnName("token")
        val freshToken=MongoColumnName("freshToken")
        val authorizeCodeCreateAt=MongoColumnName("authorizeCodeCreateAt")
        val grantApps=IdNameMeta("grantApps")
        val isLocked=MongoColumnName("isLocked")
        val lockedRemark=MongoColumnName("lockedRemark")
        val id=MongoColumnName("_id")
        val createAt=MongoColumnName("createAt")
        val updateAt=MongoColumnName("updateAt")
    
    }
    
    class SysAnnexEntity(collectionName:String="")
        :MongoBaseMetaCollection<nbcp.db.mongo.entity.SysAnnex>(nbcp.db.mongo.entity.SysAnnex::class.java,collectionName.AsString("sysAnnex")) {
        val name=MongoColumnName("name")
        val tags=MongoColumnName("tags")
        val ext=MongoColumnName("ext")
        val size=MongoColumnName("size")
        val checkCode=MongoColumnName("checkCode")
        val imgWidth=MongoColumnName("imgWidth")
        val imgHeight=MongoColumnName("imgHeight")
        val url=MongoColumnName("url")
        val creator=IdNameMeta("creator")
        val corpId=MongoColumnName("corpId")
        val errorMsg=MongoColumnName("errorMsg")
        val id=MongoColumnName("_id")
        val createAt=MongoColumnName("createAt")
        val updateAt=MongoColumnName("updateAt")
    
    }
    
    class SysApplicationEntity(collectionName:String="")
        :MongoBaseMetaCollection<nbcp.db.mongo.entity.SysApplication>(nbcp.db.mongo.entity.SysApplication::class.java,collectionName.AsString("sysApplication")) {
        val key=MongoColumnName("key")
        val name=MongoColumnName("name")
        val remark=MongoColumnName("remark")
        val hostDomainName=MongoColumnName("hostDomainName")
        val secret=MongoColumnName("secret")
        val userUpdateHookCallbackUrl=MongoColumnName("userUpdateHookCallbackUrl")
        val authorizeRange=MongoColumnName("authorizeRange")
        val org=IdNameMeta("org")
        val isLocked=MongoColumnName("isLocked")
        val loadRemark=MongoColumnName("loadRemark")
        val id=MongoColumnName("_id")
        val createAt=MongoColumnName("createAt")
        val updateAt=MongoColumnName("updateAt")
    
    }
    
    class SysCityEntity(collectionName:String="")
        :MongoBaseMetaCollection<nbcp.db.mongo.entity.SysCity>(nbcp.db.mongo.entity.SysCity::class.java,collectionName.AsString("sysCity")) {
        val code=MongoColumnName("code")
        val name=MongoColumnName("name")
        val fullName=MongoColumnName("fullName")
        val level=MongoColumnName("level")
        val lng=MongoColumnName("lng")
        val lat=MongoColumnName("lat")
        val pinyin=MongoColumnName("pinyin")
        val telCode=MongoColumnName("telCode")
        val postCode=MongoColumnName("postCode")
        val pcode=MongoColumnName("pcode")
        val id=MongoColumnName("_id")
        val createAt=MongoColumnName("createAt")
        val updateAt=MongoColumnName("updateAt")
    
        fun queryByCode (code: Int ): MongoQueryClip<SysCityEntity, nbcp.db.mongo.entity.SysCity> {
            return this.query().where{ it.code match code }
        }
    
        fun deleteByCode (code: Int ): MongoDeleteClip<SysCityEntity> {
            return this.delete().where{ it.code match code }
        }
    
        fun updateByCode (code: Int ): MongoUpdateClip<SysCityEntity> {
            return this.update().where{ it.code match code }
        }
    
    }
    
    class SysDustbinEntity(collectionName:String="")
        :MongoBaseMetaCollection<nbcp.db.mongo.entity.SysDustbin>(nbcp.db.mongo.entity.SysDustbin::class.java,collectionName.AsString("sysDustbin")) {
        val table=MongoColumnName("table")
        val remark=MongoColumnName("remark")
        val creator=IdNameMeta("creator")
        val data=ObjectMeta("data")
        val id=MongoColumnName("_id")
        val createAt=MongoColumnName("createAt")
        val updateAt=MongoColumnName("updateAt")
    
    }
    
    class SysLogEntity(collectionName:String="")
        :MongoBaseMetaCollection<nbcp.db.mongo.entity.SysLog>(nbcp.db.mongo.entity.SysLog::class.java,collectionName.AsString("sysLog")) {
        val module=MongoColumnName("module")
        val type=MongoColumnName("type")
        val key=MongoColumnName("key")
        val msg=MongoColumnName("msg")
        val data=ObjectMeta("data")
        val remark=MongoColumnName("remark")
        val clientIp=MongoColumnName("clientIp")
        val creatAt=MongoColumnName("creatAt")
        val creatorId=MongoColumnName("creatorId")
        val id=MongoColumnName("_id")
        val createAt=MongoColumnName("createAt")
        val updateAt=MongoColumnName("updateAt")
    
    }
    
    class SysOrganizationEntity(collectionName:String="")
        :MongoBaseMetaCollection<nbcp.db.mongo.entity.SysOrganization>(nbcp.db.mongo.entity.SysOrganization::class.java,collectionName.AsString("sysOrganization")) {
        val name=MongoColumnName("name")
        val siteUrl=MongoColumnName("siteUrl")
        val siteNumber=MongoColumnName("siteNumber")
        val city=IntCodeNameMeta("city")
        val businessLicense=BusinessLicenseDataMeta("businessLicense")
        val logo=IdUrlMeta("logo")
        val isLocked=MongoColumnName("isLocked")
        val lockedRemark=MongoColumnName("lockedRemark")
        val id=MongoColumnName("_id")
        val createAt=MongoColumnName("createAt")
        val updateAt=MongoColumnName("updateAt")
    
    }
    
}



private fun join(vararg args:String): MongoColumnName{
    return MongoColumnName( args.toList().filter{it.HasValue}.joinToString (".") )
}

private fun join_map(vararg args:String):moer_map{
    return moer_map(args.toList().filter{it.HasValue}.joinToString ("."))
}

data class moer_map(val _pname:String)
{
    fun keys(keys:String):String{
        return this._pname + "." + keys
    }
}


