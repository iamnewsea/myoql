package nbcp.myoql.db.sql.component


import nbcp.myoql.db.sql.base.SqlBaseMetaTable
import nbcp.myoql.db.sql.base.SqlColumnNames
import java.io.Serializable


data class JoinTableData<M : SqlBaseMetaTable<out T>, T : Serializable>
(
        val joinType:String,
        val joinTable:M,
        val onWhere: WhereData,
        val select: SqlColumnNames
):Serializable