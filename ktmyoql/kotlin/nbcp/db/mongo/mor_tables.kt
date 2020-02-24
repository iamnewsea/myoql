
package nbcp.db.mongo.table

import org.slf4j.LoggerFactory
import nbcp.base.extend.*
import nbcp.base.utils.*
import nbcp.db.mongo.entity.*
import nbcp.db.mongo.*
import nbcp.db.*
import org.springframework.stereotype.Component

//generate auto @2020-02-24 11:14:50

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


@Component("mongo.base")
@DataGroup("base")
class BaseGroup : IDataGroup{
    override fun getEntities():Set<BaseDbEntity> = setOf(sysAnnex,sysDustbin,sysLog)

    val sysAnnex=SysAnnexEntity();
    fun sysAnnex(collectionName:String)=SysAnnexEntity(collectionName);
    val sysDustbin=SysDustbinEntity();
    fun sysDustbin(collectionName:String)=SysDustbinEntity(collectionName);
    val sysLog=SysLogEntity();
    fun sysLog(collectionName:String)=SysLogEntity(collectionName);


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


