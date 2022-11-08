package nbcp.myoql.db.sql.table

import nbcp.base.db.Cn
import nbcp.base.db.DbEntityGroup
import nbcp.base.db.DbEntityIndex
import nbcp.myoql.db.sql.base.ConverterValueToDb
import nbcp.myoql.db.sql.base.SqlBaseMetaTable
import nbcp.myoql.db.sql.base.SqlColumnName
import nbcp.myoql.db.sql.define.AutoIdConverter
import nbcp.myoql.db.sql.define.FkDefine
import nbcp.myoql.db.sql.enums.DbType
import org.springframework.stereotype.*
import nbcp.base.db.*;
import nbcp.myoql.db.enums.*
import nbcp.myoql.db.comm.*
import nbcp.myoql.db.sql.base.SqlSpreadColumnData
import nbcp.myoql.db.sql.component.*
import nbcp.myoql.db.sql.s_annex
import nbcp.myoql.db.sql.s_city
import nbcp.myoql.db.sql.s_dustbin
import nbcp.myoql.db.sql.s_log


@Component("sql.SqlBase")
@MetaDataGroup(DatabaseEnum.Sql, "SqlBase")
class SqlBaseGroup : IDataGroup{
    override fun getEntities():Set<BaseMetaData<out Any>> = setOf(s_annex,s_city,s_dustbin,s_log)

    val s_annex get() = s_annex_table();
    val s_city get() = s_city_table();
    val s_dustbin get() = s_dustbin_table();
    val s_log get() = s_log_table();


    /**
     * 附件
     */
    @DbEntityIndex(unique = true, cacheable = false, value = arrayOf("""id"""))
    @ConverterValueToDb(value = AutoIdConverter::class, field = """id""")
    @DbEntityGroup(value = """SqlBase""")
    @Cn(value = """附件""")
    class s_annex_table(collectionName: String = "", datasource:String="")
        : SqlBaseMetaTable<s_annex>(s_annex::class.java, "s_annex") {
        val name = SqlColumnName(DbType.String, this.getAliaTableName(),"name")
        val tags = SqlColumnName(DbType.Json, this.getAliaTableName(),"tags")
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
        val id = SqlColumnName(DbType.String, this.getAliaTableName(),"id")
        val createAt = SqlColumnName(DbType.DateTime, this.getAliaTableName(),"createAt")
        val updateAt = SqlColumnName(DbType.DateTime, this.getAliaTableName(),"updateAt")

        override fun getSpreadColumns(): Array<SqlSpreadColumnData> { return arrayOf<SqlSpreadColumnData>(SqlSpreadColumnData("creator","_"))}

        override fun getAutoIncrementKey(): String { return ""}
        override fun getUks(): Array<Array<String>>{ return arrayOf( arrayOf("id")  )}
        override fun getFks(): Array<FkDefine>{ return arrayOf()}


        fun queryById (id: String): SqlQueryClip<s_annex_table, s_annex> {
            return this.query().where{ it.id match id }
        }

        fun deleteById (id: String): SqlDeleteClip<s_annex_table> {
            return this.delete().where{ it.id match id }
        }

        fun updateById (id: String): SqlUpdateClip<s_annex_table> {
            return this.update().where{ it.id match id }
        }

    }
    /**
     * 城市
     */
    @DbEntityGroup(value = """SqlBase""")
    @DbEntityIndex(unique = true, cacheable = false, value = arrayOf("""code"""))
    @Cn(value = """城市""")
    class s_city_table(collectionName: String = "", datasource:String="")
        : SqlBaseMetaTable<s_city>(s_city::class.java, "s_city") {
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

        override fun getSpreadColumns(): Array<SqlSpreadColumnData> { return arrayOf<SqlSpreadColumnData>()}

        override fun getAutoIncrementKey(): String { return ""}
        override fun getUks(): Array<Array<String>>{ return arrayOf( arrayOf("code")  )}
        override fun getFks(): Array<FkDefine>{ return arrayOf()}


        fun queryByCode (code: Int): SqlQueryClip<s_city_table, s_city> {
            return this.query().where{ it.code match code }
        }

        fun deleteByCode (code: Int): SqlDeleteClip<s_city_table> {
            return this.delete().where{ it.code match code }
        }

        fun updateByCode (code: Int): SqlUpdateClip<s_city_table> {
            return this.update().where{ it.code match code }
        }

    }
    /**
     * 数据垃圾箱
     */
    @DbEntityIndex(unique = true, cacheable = false, value = arrayOf("""id"""))
    @ConverterValueToDb(value = AutoIdConverter::class, field = """id""")
    @DbEntityGroup(value = """SqlBase""")
    @Cn(value = """数据垃圾箱""")
    class s_dustbin_table(collectionName: String = "", datasource:String="")
        : SqlBaseMetaTable<s_dustbin>(s_dustbin::class.java, "s_dustbin") {
        val table = SqlColumnName(DbType.String, this.getAliaTableName(),"table")
        val remark = SqlColumnName(DbType.String, this.getAliaTableName(),"remark")
        val creator_id = SqlColumnName(DbType.String, this.getAliaTableName(),"creator_id")
        val creator_name = SqlColumnName(DbType.String, this.getAliaTableName(),"creator_name")
        val data = SqlColumnName(DbType.String, this.getAliaTableName(),"data")
        val id = SqlColumnName(DbType.String, this.getAliaTableName(),"id")
        val createAt = SqlColumnName(DbType.DateTime, this.getAliaTableName(),"createAt")
        val updateAt = SqlColumnName(DbType.DateTime, this.getAliaTableName(),"updateAt")

        override fun getSpreadColumns(): Array<SqlSpreadColumnData> { return arrayOf<SqlSpreadColumnData>(SqlSpreadColumnData("creator","_"))}

        override fun getAutoIncrementKey(): String { return ""}
        override fun getUks(): Array<Array<String>>{ return arrayOf( arrayOf("id")  )}
        override fun getFks(): Array<FkDefine>{ return arrayOf()}


        fun queryById (id: String): SqlQueryClip<s_dustbin_table, s_dustbin> {
            return this.query().where{ it.id match id }
        }

        fun deleteById (id: String): SqlDeleteClip<s_dustbin_table> {
            return this.delete().where{ it.id match id }
        }

        fun updateById (id: String): SqlUpdateClip<s_dustbin_table> {
            return this.update().where{ it.id match id }
        }

    }
    /**
     * 日志
     */
    @DbEntityIndex(unique = true, cacheable = false, value = arrayOf("""id"""))
    @ConverterValueToDb(value = AutoIdConverter::class, field = """id""")
    @DbEntityGroup(value = """SqlBase""")
    @Cn(value = """日志""")
    class s_log_table(collectionName: String = "", datasource:String="")
        : SqlBaseMetaTable<s_log>(s_log::class.java, "s_log") {
        val module = SqlColumnName(DbType.String, this.getAliaTableName(),"module")
        val type = SqlColumnName(DbType.String, this.getAliaTableName(),"type")
        val tags = SqlColumnName(DbType.Json, this.getAliaTableName(),"tags")
        val msg = SqlColumnName(DbType.String, this.getAliaTableName(),"msg")
        val request = SqlColumnName(DbType.String, this.getAliaTableName(),"request")
        val data = SqlColumnName(DbType.String, this.getAliaTableName(),"data")
        val response = SqlColumnName(DbType.String, this.getAliaTableName(),"response")
        val creator_id = SqlColumnName(DbType.String, this.getAliaTableName(),"creator_id")
        val creator_name = SqlColumnName(DbType.String, this.getAliaTableName(),"creator_name")
        val id = SqlColumnName(DbType.String, this.getAliaTableName(),"id")
        val createAt = SqlColumnName(DbType.DateTime, this.getAliaTableName(),"createAt")
        val updateAt = SqlColumnName(DbType.DateTime, this.getAliaTableName(),"updateAt")

        override fun getSpreadColumns(): Array<SqlSpreadColumnData> { return arrayOf<SqlSpreadColumnData>(SqlSpreadColumnData("creator","_"))}

        override fun getAutoIncrementKey(): String { return ""}
        override fun getUks(): Array<Array<String>>{ return arrayOf( arrayOf("id")  )}
        override fun getFks(): Array<FkDefine>{ return arrayOf()}


        fun queryById (id: String): SqlQueryClip<s_log_table, s_log> {
            return this.query().where{ it.id match id }
        }

        fun deleteById (id: String): SqlDeleteClip<s_log_table> {
            return this.delete().where{ it.id match id }
        }

        fun updateById (id: String): SqlUpdateClip<s_log_table> {
            return this.update().where{ it.id match id }
        }

    }
}

fun SqlUpdateClip<SqlBaseGroup.s_annex_table>.set_sAnnex_creator(creator: IdName):SqlUpdateClip<SqlBaseGroup.s_annex_table>{
    return this.set{ it.creator_id to creator.id }
		.set{ it.creator_name to creator.name }
}


fun SqlUpdateClip<SqlBaseGroup.s_dustbin_table>.set_sDustbin_creator(creator: IdName):SqlUpdateClip<SqlBaseGroup.s_dustbin_table>{
    return this.set{ it.creator_id to creator.id }
		.set{ it.creator_name to creator.name }
}


fun SqlUpdateClip<SqlBaseGroup.s_log_table>.set_sLog_creator(creator: IdName):SqlUpdateClip<SqlBaseGroup.s_log_table>{
    return this.set{ it.creator_id to creator.id }
		.set{ it.creator_name to creator.name }
}

