package nbcp.db.sql


import nbcp.db.sql.*
import java.io.Serializable


data class JoinTableData<M : SqlBaseTable<out T>, T : IBaseDbEntity>
(
        val joinType:String,
        val joinTable:M,
        val onWhere: WhereData,
        val select:SqlColumnNames
):Serializable