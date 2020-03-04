
package nbcp.db.mongo.table

import org.slf4j.LoggerFactory
import nbcp.base.extend.*
import nbcp.base.utils.*
import nbcp.db.mongo.entity.*
import nbcp.db.mongo.*
import nbcp.db.*
import org.springframework.stereotype.Component

//generate auto @2020-03-04 23:38:11

class IdNameMeta (private val _pname:String):MongoColumnName() {
    constructor(_val:MongoColumnName):this(_val.toString()) {}

    val id=join(this._pname, "_id")
    val name=join(this._pname, "name")

    override fun toString(): String {
        return join(this._pname).toString()
    }
}

class PrivateSecretDataModelMeta (private val _pname:String):MongoColumnName() {
    constructor(_val:MongoColumnName):this(_val.toString()) {}

    val name=join(this._pname, "name")
    val key=join(this._pname, "key")
    val secret=join(this._pname, "secret")
    val type=join(this._pname, "type")
    val createAt=join(this._pname, "createAt")

    override fun toString(): String {
        return join(this._pname).toString()
    }
}

class IdUrlMeta (private val _pname:String):MongoColumnName() {
    constructor(_val:MongoColumnName):this(_val.toString()) {}

    val id=join(this._pname, "_id")
    val url=join(this._pname, "url")

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

class UserIdCardDataMeta (private val _pname:String):MongoColumnName() {
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


@Component("mongo.MongoBase")
@DataGroup("MongoBase")
class MongoBaseGroup : IDataGroup{
    override fun getEntities():Set<BaseDbEntity> = setOf(sysAnnex,sysApplication,sysDustbin,sysLog,sysLoginUser,sysUser)

    val sysAnnex=SysAnnexEntity();
    fun sysAnnex(collectionName:String)=SysAnnexEntity(collectionName);
    val sysApplication=SysApplicationEntity();
    fun sysApplication(collectionName:String)=SysApplicationEntity(collectionName);
    val sysDustbin=SysDustbinEntity();
    fun sysDustbin(collectionName:String)=SysDustbinEntity(collectionName);
    val sysLog=SysLogEntity();
    fun sysLog(collectionName:String)=SysLogEntity(collectionName);
    val sysLoginUser=SysLoginUserEntity();
    fun sysLoginUser(collectionName:String)=SysLoginUserEntity(collectionName);
    val sysUser=SysUserEntity();
    fun sysUser(collectionName:String)=SysUserEntity(collectionName);


    class SysAnnexEntity(collectionName:String=""):MongoBaseEntity<SysAnnex>(SysAnnex::class.java,collectionName.AsString("sysAnnex")) {
        val name=MongoColumnName("name")
        val tags=MongoColumnName("tags")
        val ext=MongoColumnName("ext")
        val size=MongoColumnName("size")
        val checkCode=MongoColumnName("checkCode")
        val imgWidth=MongoColumnName("imgWidth")
        val imgHeight=MongoColumnName("imgHeight")
        val url=MongoColumnName("url")
        val createBy=IdNameMeta("createBy")
        val corpId=MongoColumnName("corpId")
        val errorMsg=MongoColumnName("errorMsg")
        val createAt=MongoColumnName("createAt")
        val id=MongoColumnName("_id")
    }
    
    class SysApplicationEntity(collectionName:String=""):MongoBaseEntity<SysApplication>(SysApplication::class.java,collectionName.AsString("sysApplication")) {
        val name=MongoColumnName("name")
        val key=MongoColumnName("key")
        val secret=MongoColumnName("secret")
        val privateSecrets=PrivateSecretDataModelMeta("privateSecrets")
        val authorizeCode=MongoColumnName("authorizeCode")
        val token=MongoColumnName("token")
        val freshToken=MongoColumnName("freshToken")
        val slogan=MongoColumnName("slogan")
        val loginedCallbackUrl=MongoColumnName("loginedCallbackUrl")
        val userUpdateHookCallbackUrl=MongoColumnName("userUpdateHookCallbackUrl")
        val logo=IdUrlMeta("logo")
        val siteUrl=MongoColumnName("siteUrl")
        val remark=MongoColumnName("remark")
        val codeCreateAt=MongoColumnName("codeCreateAt")
        val createAt=MongoColumnName("createAt")
        val isLocked=MongoColumnName("isLocked")
        val lockedRemark=MongoColumnName("lockedRemark")
        val id=MongoColumnName("_id")
    }
    
    class SysDustbinEntity(collectionName:String=""):MongoBaseEntity<SysDustbin>(SysDustbin::class.java,collectionName.AsString("sysDustbin")) {
        val table=MongoColumnName("table")
        val remark=MongoColumnName("remark")
        val creator=IdNameMeta("creator")
        val data=ObjectMeta("data")
        val createAt=MongoColumnName("createAt")
        val id=MongoColumnName("_id")
    }
    
    class SysLogEntity(collectionName:String=""):MongoBaseEntity<SysLog>(SysLog::class.java,collectionName.AsString("sysLog")) {
        val module=MongoColumnName("module")
        val type=MongoColumnName("type")
        val key=MongoColumnName("key")
        val msg=MongoColumnName("msg")
        val data=ObjectMeta("data")
        val remark=MongoColumnName("remark")
        val clientIp=MongoColumnName("clientIp")
        val creatAt=MongoColumnName("creatAt")
        val createBy=MongoColumnName("createBy")
        val id=MongoColumnName("_id")
    }
    
    class SysLoginUserEntity(collectionName:String=""):MongoBaseEntity<SysLoginUser>(SysLoginUser::class.java,collectionName.AsString("sysLoginUser")) {
        val loginName=MongoColumnName("loginName")
        val password=MongoColumnName("password")
        val lastLoginAt=MongoColumnName("lastLoginAt")
        val errorLoginTimes=MongoColumnName("errorLoginTimes")
        val isLocked=MongoColumnName("isLocked")
        val lockedRemark=MongoColumnName("lockedRemark")
        val id=MongoColumnName("_id")
    }
    
    class SysUserEntity(collectionName:String=""):MongoBaseEntity<SysUser>(SysUser::class.java,collectionName.AsString("sysUser")) {
        val loginName=MongoColumnName("loginName")
        val logo=IdUrlMeta("logo")
        val mobile=MongoColumnName("mobile")
        val email=MongoColumnName("email")
        val idCard=UserIdCardDataMeta("idCard")
        val workLocation=MongoColumnName("workLocation")
        val liveLocation=MongoColumnName("liveLocation")
        val corpName=MongoColumnName("corpName")
        val job=MongoColumnName("job")
        val token=MongoColumnName("token")
        val createAt=MongoColumnName("createAt")
        val updateAt=MongoColumnName("updateAt")
        val id=MongoColumnName("_id")
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


