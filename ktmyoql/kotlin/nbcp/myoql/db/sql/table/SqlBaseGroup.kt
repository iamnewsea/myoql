package nbcp.myoql.db.sql.table

import nbcp.myoql.db.comm.*
import nbcp.myoql.db.enums.*
import nbcp.myoql.db.sql.base.*
import nbcp.myoql.db.sql.enums.*
import nbcp.myoql.db.sql.define.*
import nbcp.myoql.db.sql.component.*
import org.springframework.stereotype.*



@Component("sql.SqlBase")
@MetaDataGroup(DatabaseEnum.SQL, "SqlBase")
class SqlBaseGroup : IDataGroup{
    override fun getEntities():Set<BaseMetaData<out Any>> = setOf(s_annex,s_city,s_dustbin,s_log)

    val s_annex get() = s_annex_table();
    val s_city get() = s_city_table();
    val s_dustbin get() = s_dustbin_table();
    val s_log get() = s_log_table();


    /**
     * 附件
     */
    @nbcp.base.db.annotation.DbEntityIndex(unique = true, cacheable = false, value = arrayOf("""id"""))
    @nbcp.myoql.db.sql.annotation.ConverterValueToDb(value = nbcp.myoql.db.sql.define.AutoIdConverter::class, field = """id""")
    @nbcp.base.db.annotation.DbEntityGroup(value = """SqlBase""")
    @nbcp.base.db.annotation.Cn(value = """附件""")
    class s_annex_table(collectionName: String = "", datasource:String="")
        :SqlBaseMetaTable<nbcp.myoql.db.sql.entity.s_annex>(nbcp.myoql.db.sql.entity.s_annex::class.java, "s_annex") {
        val name = SqlColumnName(DbType.STRING, this.getAliaTableName(),"name")
        val tags = SqlColumnName(DbType.JSON, this.getAliaTableName(),"tags")
        val ext = SqlColumnName(DbType.STRING, this.getAliaTableName(),"ext")
        val size = SqlColumnName(DbType.INT, this.getAliaTableName(),"size")
        val imgWidth = SqlColumnName(DbType.INT, this.getAliaTableName(),"imgWidth")
        val imgHeight = SqlColumnName(DbType.INT, this.getAliaTableName(),"imgHeight")
        val url = SqlColumnName(DbType.STRING, this.getAliaTableName(),"url")
        val creator_id = SqlColumnName(DbType.STRING, this.getAliaTableName(),"creator_id")
        val creator_name = SqlColumnName(DbType.STRING, this.getAliaTableName(),"creator_name")
        val group = SqlColumnName(DbType.STRING, this.getAliaTableName(),"group")
        val corpId = SqlColumnName(DbType.STRING, this.getAliaTableName(),"corpId")
        val errorMsg = SqlColumnName(DbType.STRING, this.getAliaTableName(),"errorMsg")
        val id = SqlColumnName(DbType.STRING, this.getAliaTableName(),"id")
        val createAt = SqlColumnName(DbType.DATE_TIME, this.getAliaTableName(),"createAt")
        val updateAt = SqlColumnName(DbType.DATE_TIME, this.getAliaTableName(),"updateAt")

        override fun getSpreadColumns(): Array<SqlSpreadColumnData> { return arrayOf<SqlSpreadColumnData>(SqlSpreadColumnData("creator","_"))}

        override fun getAutoIncrementKey(): String { return ""}
        override fun getUks(): Array<Array<String>>{ return arrayOf( arrayOf("id")  )}
        override fun getFks(): Array<FkDefine>{ return arrayOf()}


        fun queryById (id: String): SqlQueryClip<s_annex_table, nbcp.myoql.db.sql.entity.s_annex> {
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
    @nbcp.base.db.annotation.DbEntityGroup(value = """SqlBase""")
    @nbcp.base.db.annotation.DbEntityIndex(unique = true, cacheable = false, value = arrayOf("""code"""))
    @nbcp.base.db.annotation.Cn(value = """城市""")
    class s_city_table(collectionName: String = "", datasource:String="")
        :SqlBaseMetaTable<nbcp.myoql.db.sql.entity.s_city>(nbcp.myoql.db.sql.entity.s_city::class.java, "s_city") {
        val code = SqlColumnName(DbType.INT, this.getAliaTableName(),"code")
        val shortName = SqlColumnName(DbType.STRING, this.getAliaTableName(),"shortName")
        val name = SqlColumnName(DbType.STRING, this.getAliaTableName(),"name")
        val level = SqlColumnName(DbType.INT, this.getAliaTableName(),"level")
        val lng = SqlColumnName(DbType.FLOAT, this.getAliaTableName(),"lng")
        val lat = SqlColumnName(DbType.FLOAT, this.getAliaTableName(),"lat")
        val pinyin = SqlColumnName(DbType.STRING, this.getAliaTableName(),"pinyin")
        val telCode = SqlColumnName(DbType.STRING, this.getAliaTableName(),"telCode")
        val postCode = SqlColumnName(DbType.STRING, this.getAliaTableName(),"postCode")
        val pcode = SqlColumnName(DbType.INT, this.getAliaTableName(),"pcode")

        override fun getSpreadColumns(): Array<SqlSpreadColumnData> { return arrayOf<SqlSpreadColumnData>()}

        override fun getAutoIncrementKey(): String { return ""}
        override fun getUks(): Array<Array<String>>{ return arrayOf( arrayOf("code")  )}
        override fun getFks(): Array<FkDefine>{ return arrayOf()}


        fun queryByCode (code: Int): SqlQueryClip<s_city_table, nbcp.myoql.db.sql.entity.s_city> {
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
    @nbcp.base.db.annotation.DbEntityIndex(unique = true, cacheable = false, value = arrayOf("""id"""))
    @nbcp.myoql.db.sql.annotation.ConverterValueToDb(value = nbcp.myoql.db.sql.define.AutoIdConverter::class, field = """id""")
    @nbcp.base.db.annotation.DbEntityGroup(value = """SqlBase""")
    @nbcp.base.db.annotation.Cn(value = """数据垃圾箱""")
    class s_dustbin_table(collectionName: String = "", datasource:String="")
        :SqlBaseMetaTable<nbcp.myoql.db.sql.entity.s_dustbin>(nbcp.myoql.db.sql.entity.s_dustbin::class.java, "s_dustbin") {
        val table = SqlColumnName(DbType.STRING, this.getAliaTableName(),"table")
        val remark = SqlColumnName(DbType.STRING, this.getAliaTableName(),"remark")
        val creator_id = SqlColumnName(DbType.STRING, this.getAliaTableName(),"creator_id")
        val creator_name = SqlColumnName(DbType.STRING, this.getAliaTableName(),"creator_name")
        val data = SqlColumnName(DbType.STRING, this.getAliaTableName(),"data")
        val id = SqlColumnName(DbType.STRING, this.getAliaTableName(),"id")
        val createAt = SqlColumnName(DbType.DATE_TIME, this.getAliaTableName(),"createAt")
        val updateAt = SqlColumnName(DbType.DATE_TIME, this.getAliaTableName(),"updateAt")

        override fun getSpreadColumns(): Array<SqlSpreadColumnData> { return arrayOf<SqlSpreadColumnData>(SqlSpreadColumnData("creator","_"))}

        override fun getAutoIncrementKey(): String { return ""}
        override fun getUks(): Array<Array<String>>{ return arrayOf( arrayOf("id")  )}
        override fun getFks(): Array<FkDefine>{ return arrayOf()}


        fun queryById (id: String): SqlQueryClip<s_dustbin_table, nbcp.myoql.db.sql.entity.s_dustbin> {
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
    @nbcp.base.db.annotation.DbEntityIndex(unique = true, cacheable = false, value = arrayOf("""id"""))
    @nbcp.myoql.db.sql.annotation.ConverterValueToDb(value = nbcp.myoql.db.sql.define.AutoIdConverter::class, field = """id""")
    @nbcp.base.db.annotation.DbEntityGroup(value = """SqlBase""")
    @nbcp.base.db.annotation.Cn(value = """日志""")
    class s_log_table(collectionName: String = "", datasource:String="")
        :SqlBaseMetaTable<nbcp.myoql.db.sql.entity.s_log>(nbcp.myoql.db.sql.entity.s_log::class.java, "s_log") {
        val module = SqlColumnName(DbType.STRING, this.getAliaTableName(),"module")
        val type = SqlColumnName(DbType.STRING, this.getAliaTableName(),"type")
        val tags = SqlColumnName(DbType.JSON, this.getAliaTableName(),"tags")
        val msg = SqlColumnName(DbType.STRING, this.getAliaTableName(),"msg")
        val request = SqlColumnName(DbType.STRING, this.getAliaTableName(),"request")
        val data = SqlColumnName(DbType.STRING, this.getAliaTableName(),"data")
        val response = SqlColumnName(DbType.STRING, this.getAliaTableName(),"response")
        val creator_id = SqlColumnName(DbType.STRING, this.getAliaTableName(),"creator_id")
        val creator_name = SqlColumnName(DbType.STRING, this.getAliaTableName(),"creator_name")
        val id = SqlColumnName(DbType.STRING, this.getAliaTableName(),"id")
        val createAt = SqlColumnName(DbType.DATE_TIME, this.getAliaTableName(),"createAt")
        val updateAt = SqlColumnName(DbType.DATE_TIME, this.getAliaTableName(),"updateAt")

        override fun getSpreadColumns(): Array<SqlSpreadColumnData> { return arrayOf<SqlSpreadColumnData>(SqlSpreadColumnData("creator","_"))}

        override fun getAutoIncrementKey(): String { return ""}
        override fun getUks(): Array<Array<String>>{ return arrayOf( arrayOf("id")  )}
        override fun getFks(): Array<FkDefine>{ return arrayOf()}


        fun queryById (id: String): SqlQueryClip<s_log_table, nbcp.myoql.db.sql.entity.s_log> {
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

fun SqlUpdateClip<SqlBaseGroup.s_annex_table>.set_sAnnex_creator(creator:nbcp.base.db.IdName):SqlUpdateClip<SqlBaseGroup.s_annex_table>{
    return this.set{ it.creator_id to creator.id }
		.set{ it.creator_name to creator.name }
}


fun SqlUpdateClip<SqlBaseGroup.s_dustbin_table>.set_sDustbin_creator(creator:nbcp.base.db.IdName):SqlUpdateClip<SqlBaseGroup.s_dustbin_table>{
    return this.set{ it.creator_id to creator.id }
		.set{ it.creator_name to creator.name }
}


fun SqlUpdateClip<SqlBaseGroup.s_log_table>.set_sLog_creator(creator:nbcp.base.db.IdName):SqlUpdateClip<SqlBaseGroup.s_log_table>{
    return this.set{ it.creator_id to creator.id }
		.set{ it.creator_name to creator.name }
}

