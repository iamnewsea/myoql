package nbcp.db.sql.table

import nbcp.db.*
import nbcp.db.sql.*
import nbcp.db.sql.entity.*
import nbcp.db.mysql.*
import nbcp.db.mysql.entity.*
import nbcp.comm.*
import nbcp.utils.*
import org.springframework.stereotype.Component

//generate auto @2021-11-11 13:46:55


@Component("sql.SqlBase")
@MetaDataGroup("SqlBase")
class SqlBaseGroup : IDataGroup{
    override fun getEntities():Set<BaseMetaData> = setOf(s_annex,s_city,s_dustbin,s_log)

    val s_annex get() = s_annex_table();
    val s_city get() = s_city_table();
    val s_dustbin get() = s_dustbin_table();
    val s_log get() = s_log_table();


    
    class s_annex_table(datasource:String="")
        :SqlBaseMetaTable<nbcp.db.sql.entity.s_annex>(nbcp.db.sql.entity.s_annex::class.java,"s_annex") {
        val name = SqlColumnName(DbType.String, this.getAliaTableName(),"name")
        val tags = SqlColumnName(DbType.String, this.getAliaTableName(),"tags")
        val ext = SqlColumnName(DbType.String, this.getAliaTableName(),"ext")
        val size = SqlColumnName(DbType.Int, this.getAliaTableName(),"size")
        val imgWidth = SqlColumnName(DbType.Int, this.getAliaTableName(),"imgWidth")
        val imgHeight = SqlColumnName(DbType.Int, this.getAliaTableName(),"imgHeight")
        val url = SqlColumnName(DbType.String, this.getAliaTableName(),"url")
        val creator_id = SqlColumnName(DbType.String, this.getAliaTableName(),"creator_id")
        val creator_name = SqlColumnName(DbType.String, this.getAliaTableName(),"creator_name")
        val group = SqlColumnName(DbType.String, this.getAliaTableName(),"group")
        val corpId = SqlColumnName(DbType.String, this.getAliaTableName(),"corpId")
        val errorMsg = SqlColumnName(DbType.String, this.getAliaTableName(),"errorMsg")
        @ConverterValueToDb(nbcp.db.sql.AutoIdConverter::class)
        val id = SqlColumnName(DbType.String, this.getAliaTableName(),"id")
        val createAt = SqlColumnName(DbType.DateTime, this.getAliaTableName(),"createAt")
    
        override fun getSpreadColumns(): Array<String> { return arrayOf<String>("creator")}
        override fun getConvertValueColumns(): Array<String> { return arrayOf<String>("id")}
        override fun getAutoIncrementKey(): String { return ""}
        override fun getUks(): Array<Array<String>>{ return arrayOf( arrayOf("id")  )}
        override fun getFks(): Array<FkDefine>{ return arrayOf()}
    
    
        fun queryById (id: String): SqlQueryClip<s_annex_table, nbcp.db.sql.entity.s_annex> {
            return this.query().where{ it.id match id }
        }
    
        fun deleteById (id: String): SqlDeleteClip<s_annex_table> {
            return this.delete().where{ it.id match id }
        }
    
        fun updateById (id: String): SqlUpdateClip<s_annex_table> {
            return this.update().where{ it.id match id }
        }
    
    }
    
    class s_city_table(datasource:String="")
        :SqlBaseMetaTable<nbcp.db.sql.entity.s_city>(nbcp.db.sql.entity.s_city::class.java,"s_city") {
        val code = SqlColumnName(DbType.Int, this.getAliaTableName(),"code")
        val shortName = SqlColumnName(DbType.String, this.getAliaTableName(),"shortName")
        val name = SqlColumnName(DbType.String, this.getAliaTableName(),"name")
        val level = SqlColumnName(DbType.Int, this.getAliaTableName(),"level")
        val lng = SqlColumnName(DbType.Float, this.getAliaTableName(),"lng")
        val lat = SqlColumnName(DbType.Float, this.getAliaTableName(),"lat")
        val pinyin = SqlColumnName(DbType.String, this.getAliaTableName(),"pinyin")
        val telCode = SqlColumnName(DbType.String, this.getAliaTableName(),"telCode")
        val postCode = SqlColumnName(DbType.String, this.getAliaTableName(),"postCode")
        val pcode = SqlColumnName(DbType.Int, this.getAliaTableName(),"pcode")
    
        override fun getSpreadColumns(): Array<String> { return arrayOf<String>()}
        override fun getConvertValueColumns(): Array<String> { return arrayOf<String>()}
        override fun getAutoIncrementKey(): String { return ""}
        override fun getUks(): Array<Array<String>>{ return arrayOf( arrayOf("code")  )}
        override fun getFks(): Array<FkDefine>{ return arrayOf()}
    
    
        fun queryByCode (code: Int): SqlQueryClip<s_city_table, nbcp.db.sql.entity.s_city> {
            return this.query().where{ it.code match code }
        }
    
        fun deleteByCode (code: Int): SqlDeleteClip<s_city_table> {
            return this.delete().where{ it.code match code }
        }
    
        fun updateByCode (code: Int): SqlUpdateClip<s_city_table> {
            return this.update().where{ it.code match code }
        }
    
    }
    
    class s_dustbin_table(datasource:String="")
        :SqlBaseMetaTable<nbcp.db.sql.entity.s_dustbin>(nbcp.db.sql.entity.s_dustbin::class.java,"s_dustbin") {
        val table = SqlColumnName(DbType.String, this.getAliaTableName(),"table")
        val remark = SqlColumnName(DbType.String, this.getAliaTableName(),"remark")
        val creator_id = SqlColumnName(DbType.String, this.getAliaTableName(),"creator_id")
        val creator_name = SqlColumnName(DbType.String, this.getAliaTableName(),"creator_name")
        val data = SqlColumnName(DbType.String, this.getAliaTableName(),"data")
        @ConverterValueToDb(nbcp.db.sql.AutoIdConverter::class)
        val id = SqlColumnName(DbType.Long, this.getAliaTableName(),"id")
        val createAt = SqlColumnName(DbType.DateTime, this.getAliaTableName(),"createAt")
    
        override fun getSpreadColumns(): Array<String> { return arrayOf<String>("creator")}
        override fun getConvertValueColumns(): Array<String> { return arrayOf<String>("id")}
        override fun getAutoIncrementKey(): String { return ""}
        override fun getUks(): Array<Array<String>>{ return arrayOf( arrayOf("id")  )}
        override fun getFks(): Array<FkDefine>{ return arrayOf()}
    
    
        fun queryById (id: Long): SqlQueryClip<s_dustbin_table, nbcp.db.sql.entity.s_dustbin> {
            return this.query().where{ it.id match id }
        }
    
        fun deleteById (id: Long): SqlDeleteClip<s_dustbin_table> {
            return this.delete().where{ it.id match id }
        }
    
        fun updateById (id: Long): SqlUpdateClip<s_dustbin_table> {
            return this.update().where{ it.id match id }
        }
    
    }
    
    class s_log_table(datasource:String="")
        :SqlBaseMetaTable<nbcp.db.sql.entity.s_log>(nbcp.db.sql.entity.s_log::class.java,"s_log") {
        val module = SqlColumnName(DbType.String, this.getAliaTableName(),"module")
        val type = SqlColumnName(DbType.String, this.getAliaTableName(),"type")
        val tags = SqlColumnName(DbType.String, this.getAliaTableName(),"tags")
        val msg = SqlColumnName(DbType.String, this.getAliaTableName(),"msg")
        val request = SqlColumnName(DbType.String, this.getAliaTableName(),"request")
        val data = SqlColumnName(DbType.String, this.getAliaTableName(),"data")
        val response = SqlColumnName(DbType.String, this.getAliaTableName(),"response")
        val creatorId = SqlColumnName(DbType.String, this.getAliaTableName(),"creatorId")
        @ConverterValueToDb(nbcp.db.sql.AutoIdConverter::class)
        val id = SqlColumnName(DbType.Long, this.getAliaTableName(),"id")
        val createAt = SqlColumnName(DbType.DateTime, this.getAliaTableName(),"createAt")
    
        override fun getSpreadColumns(): Array<String> { return arrayOf<String>()}
        override fun getConvertValueColumns(): Array<String> { return arrayOf<String>("id")}
        override fun getAutoIncrementKey(): String { return ""}
        override fun getUks(): Array<Array<String>>{ return arrayOf( arrayOf("id")  )}
        override fun getFks(): Array<FkDefine>{ return arrayOf()}
    
    
        fun queryById (id: Long): SqlQueryClip<s_log_table, nbcp.db.sql.entity.s_log> {
            return this.query().where{ it.id match id }
        }
    
        fun deleteById (id: Long): SqlDeleteClip<s_log_table> {
            return this.delete().where{ it.id match id }
        }
    
        fun updateById (id: Long): SqlUpdateClip<s_log_table> {
            return this.update().where{ it.id match id }
        }
    
    }
}

fun SqlUpdateClip<SqlBaseGroup.s_annex_table>.set_sAnnex_creator(creator:nbcp.db.IdName):SqlUpdateClip<SqlBaseGroup.s_annex_table>{
    return this.set{ it.creator_id to creator.id }
		.set{ it.creator_name to creator.name }
}


fun SqlUpdateClip<SqlBaseGroup.s_dustbin_table>.set_sDustbin_creator(creator:nbcp.db.IdName):SqlUpdateClip<SqlBaseGroup.s_dustbin_table>{
    return this.set{ it.creator_id to creator.id }
		.set{ it.creator_name to creator.name }
}

