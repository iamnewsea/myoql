
package nbcp.db.mongo.table

import org.slf4j.LoggerFactory
import nbcp.base.extend.*
import nbcp.base.utils.*
import nbcp.db.mongo.*
import nbcp.db.*

//generate auto @2020-01-07 10:48:37

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


class BaseGroup : IDataGroup{
    override fun getEntities():Set<BaseDbEntity> = setOf(sysAnnex,sysCity,sysDustbin,sysLog)

    val sysAnnex=SysAnnexEntity();
    fun sysAnnex(collectionName:String)=SysAnnexEntity(collectionName);
    val sysCity=SysCityEntity();
    fun sysCity(collectionName:String)=SysCityEntity(collectionName);
    val sysDustbin=SysDustbinEntity();
    fun sysDustbin(collectionName:String)=SysDustbinEntity(collectionName);
    val sysLog=SysLogEntity();
    fun sysLog(collectionName:String)=SysLogEntity(collectionName);


    class SysAnnexEntity(collectionName:String="sysAnnex"):MongoBaseEntity<SysAnnex>(SysAnnex::class.java,collectionName) {
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
    
    class SysCityEntity(collectionName:String="sysCity"):MongoBaseEntity<SysCity>(SysCity::class.java,collectionName) {
        val code=MongoColumnName("code")
        val pcode=MongoColumnName("pcode")
        val name=MongoColumnName("name")
        val fullName=MongoColumnName("fullName")
        val level=MongoColumnName("level")
        val lng=MongoColumnName("lng")
        val lat=MongoColumnName("lat")
        val pinyin=MongoColumnName("pinyin")
        val telCode=MongoColumnName("telCode")
        val postCode=MongoColumnName("postCode")
        val id=MongoColumnName("_id")
    }
    
    class SysDustbinEntity(collectionName:String="sysDustbin"):MongoBaseEntity<SysDustbin>(SysDustbin::class.java,collectionName) {
        val table=MongoColumnName("table")
        val remark=MongoColumnName("remark")
        val creator=IdNameMeta("creator")
        val data=ObjectMeta("data")
        val createAt=MongoColumnName("createAt")
        val id=MongoColumnName("_id")
    }
    
    class SysLogEntity(collectionName:String="sysLog"):MongoBaseEntity<SysLog>(SysLog::class.java,collectionName) {
        val msg=MongoColumnName("msg")
        val creatAt=MongoColumnName("creatAt")
        val createBy=MongoColumnName("createBy")
        val type=MongoColumnName("type")
        val clientIp=MongoColumnName("clientIp")
        val module=MongoColumnName("module")
        val remark=MongoColumnName("remark")
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


