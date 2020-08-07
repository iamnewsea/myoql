package nbcp.db.sql.table

import nbcp.db.*
import nbcp.db.sql.*
import nbcp.db.sql.entity.*
import nbcp.db.mysql.*
import nbcp.db.mysql.entity.*
import nbcp.comm.*
import nbcp.utils.*
import org.springframework.stereotype.Component

//generate auto @2020-08-07 16:11:21


@Component("sql.SqlBase")
@MetaDataGroup("SqlBase")
class SqlBaseGroup : IDataGroup{
    override fun getEntities():Set<BaseMetaData> = setOf(s_annex,s_dustbin,s_log)

    val s_annex get()= s_annex_table();
    val s_dustbin get()= s_dustbin_table();
    val s_log get()= s_log_table();


    
    class s_annex_table(datasource:String="")
        :SqlBaseMetaTable<nbcp.db.sql.entity.s_annex>(nbcp.db.sql.entity.s_annex::class.java,"s_annex") {
        @ConverterValueToDb(nbcp.db.sql.TrimLowercaseConverter::class)
        val id=SqlColumnName(DbType.String,this.getAliaTableName(),"id")
        val name=SqlColumnName(DbType.String,this.getAliaTableName(),"name")
        val tags=SqlColumnName(DbType.String,this.getAliaTableName(),"tags")
        val ext=SqlColumnName(DbType.String,this.getAliaTableName(),"ext")
        val size=SqlColumnName(DbType.Int,this.getAliaTableName(),"size")
        val checkCode=SqlColumnName(DbType.String,this.getAliaTableName(),"checkCode")
        val imgWidth=SqlColumnName(DbType.Int,this.getAliaTableName(),"imgWidth")
        val imgHeight=SqlColumnName(DbType.Int,this.getAliaTableName(),"imgHeight")
        val url=SqlColumnName(DbType.String,this.getAliaTableName(),"url")
        val creator_id=SqlColumnName(DbType.String,this.getAliaTableName(),"creator_id")
        val creator_name=SqlColumnName(DbType.String,this.getAliaTableName(),"creator_name")
        val corpId=SqlColumnName(DbType.String,this.getAliaTableName(),"corpId")
        val errorMsg=SqlColumnName(DbType.String,this.getAliaTableName(),"errorMsg")
        val createAt=SqlColumnName(DbType.DateTime,this.getAliaTableName(),"createAt")
    
        override fun getSpreadColumns(): Array<String> { return arrayOf<String>("creator")}
        override fun getConvertValueColumns(): Array<String> { return arrayOf<String>("id")}
        override fun getColumns(): SqlColumnNames { return SqlColumnNames(id,name,tags,ext,size,checkCode,imgWidth,imgHeight,url,creator_id,creator_name,corpId,errorMsg,createAt)}
        override fun getAutoIncrementKey(): String { return ""}
        override fun getUks(): Array<Array<String>>{ return arrayOf( arrayOf("id")  )}
        override fun getRks(): Array<Array<String>>{ return arrayOf( arrayOf("corpId")  )}
        override fun getFks(): Array<FkDefine>{ return arrayOf()}
    
    
        fun queryById (id: String): SqlQueryClip<s_annex_table, nbcp.db.sql.entity.s_annex> {
            return this.query().where{ it.id match id }
        }
    
        fun deleteById (id: String): SqlDeleteClip<s_annex_table,nbcp.db.sql.entity.s_annex> {
            return this.delete().where{ it.id match id }
        }
    
        fun updateById (id: String): SqlUpdateClip<s_annex_table,nbcp.db.sql.entity.s_annex> {
            return this.update().where{ it.id match id }
        }
    
    }
    
    class s_dustbin_table(datasource:String="")
        :SqlBaseMetaTable<nbcp.db.sql.entity.s_dustbin>(nbcp.db.sql.entity.s_dustbin::class.java,"s_dustbin") {
        val id=SqlColumnName(DbType.String,this.getAliaTableName(),"id")
        val table=SqlColumnName(DbType.String,this.getAliaTableName(),"table")
        val remark=SqlColumnName(DbType.String,this.getAliaTableName(),"remark")
        val creator_id=SqlColumnName(DbType.String,this.getAliaTableName(),"creator_id")
        val creator_name=SqlColumnName(DbType.String,this.getAliaTableName(),"creator_name")
        val data=SqlColumnName(DbType.String,this.getAliaTableName(),"data")
        val createAt=SqlColumnName(DbType.DateTime,this.getAliaTableName(),"createAt")
    
        override fun getSpreadColumns(): Array<String> { return arrayOf<String>("creator")}
        override fun getConvertValueColumns(): Array<String> { return arrayOf<String>()}
        override fun getColumns(): SqlColumnNames { return SqlColumnNames(id,table,remark,creator_id,creator_name,data,createAt)}
        override fun getAutoIncrementKey(): String { return ""}
        override fun getUks(): Array<Array<String>>{ return arrayOf( arrayOf("id")  )}
        override fun getRks(): Array<Array<String>>{ return arrayOf( )}
        override fun getFks(): Array<FkDefine>{ return arrayOf()}
    
    
        fun queryById (id: String): SqlQueryClip<s_dustbin_table, nbcp.db.sql.entity.s_dustbin> {
            return this.query().where{ it.id match id }
        }
    
        fun deleteById (id: String): SqlDeleteClip<s_dustbin_table,nbcp.db.sql.entity.s_dustbin> {
            return this.delete().where{ it.id match id }
        }
    
        fun updateById (id: String): SqlUpdateClip<s_dustbin_table,nbcp.db.sql.entity.s_dustbin> {
            return this.update().where{ it.id match id }
        }
    
    }
    
    class s_log_table(datasource:String="")
        :SqlBaseMetaTable<nbcp.db.sql.entity.s_log>(nbcp.db.sql.entity.s_log::class.java,"s_log") {
        @ConverterValueToDb(nbcp.db.sql.TrimLowercaseConverter::class)
        val id=SqlColumnName(DbType.String,this.getAliaTableName(),"id")
        val module=SqlColumnName(DbType.String,this.getAliaTableName(),"module")
        val type=SqlColumnName(DbType.String,this.getAliaTableName(),"type")
        val key=SqlColumnName(DbType.String,this.getAliaTableName(),"key")
        val msg=SqlColumnName(DbType.String,this.getAliaTableName(),"msg")
        val data=SqlColumnName(DbType.String,this.getAliaTableName(),"data")
        val remark=SqlColumnName(DbType.String,this.getAliaTableName(),"remark")
        val clientIp=SqlColumnName(DbType.String,this.getAliaTableName(),"clientIp")
        val creatAt=SqlColumnName(DbType.DateTime,this.getAliaTableName(),"creatAt")
        val creatorId=SqlColumnName(DbType.String,this.getAliaTableName(),"creatorId")
    
        override fun getSpreadColumns(): Array<String> { return arrayOf<String>("")}
        override fun getConvertValueColumns(): Array<String> { return arrayOf<String>("id")}
        override fun getColumns(): SqlColumnNames { return SqlColumnNames(id,module,type,key,msg,data,remark,clientIp,creatAt,creatorId)}
        override fun getAutoIncrementKey(): String { return ""}
        override fun getUks(): Array<Array<String>>{ return arrayOf( arrayOf("id")  )}
        override fun getRks(): Array<Array<String>>{ return arrayOf( )}
        override fun getFks(): Array<FkDefine>{ return arrayOf()}
    
    
        fun queryById (id: String): SqlQueryClip<s_log_table, nbcp.db.sql.entity.s_log> {
            return this.query().where{ it.id match id }
        }
    
        fun deleteById (id: String): SqlDeleteClip<s_log_table,nbcp.db.sql.entity.s_log> {
            return this.delete().where{ it.id match id }
        }
    
        fun updateById (id: String): SqlUpdateClip<s_log_table,nbcp.db.sql.entity.s_log> {
            return this.update().where{ it.id match id }
        }
    
    }
}
