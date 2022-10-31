package nbcp.db.sql.event

import nbcp.comm.ConvertType
import nbcp.comm.FindField
import nbcp.db.sql.*;
import nbcp.comm.ToMap
import nbcp.db.*
import org.springframework.stereotype.Component

/**
 *
 */
@Component
class SqlDefaultQueryEvent : ISqlEntitySelect {
    override fun beforeSelect(select: SqlBaseQueryClip): EventResult {
//        var select  = select as SqlQueryClip< SqlBaseMetaTable<out Serializable>, Serializable>
//        var spreads = select.mainEntity.getSpreadColumns();
//        if( spreads.any()) {
//            var select_columns = select.columns.map { it.name }.intersect(spreads.toList())
//            if( select_columns.any()){
//                select.select {  }
//            }
//        }
        return EventResult(true)
    }

    override fun select(select: SqlBaseQueryClip, eventData: EventResult, result: List<MutableMap<String, Any?>>) {
        if (select is SqlQueryClip<*, *> == false) {
            return
        }

        val spreads = select.mainEntity.getSpreadColumns();
        spreads.forEach { spread ->
            val entField = select.mainEntity.entityClass.FindField(spread.column)
            if (entField == null) {
                return@forEach
            }

            result.forEach { row ->
                val spreadRowData = row.filter { it.key.startsWith(spread.getPrefixName()) }
                    .ToMap({ it.key.substring(spread.getPrefixName().length) }, { it.value })
                    .ConvertType(entField.type)
                row.put(spread.column, spreadRowData);
            }
        }
    }
}