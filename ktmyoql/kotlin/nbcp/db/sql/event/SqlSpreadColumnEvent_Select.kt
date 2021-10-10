package nbcp.db.sql.event

import nbcp.db.sql.*;
import nbcp.comm.ToMap
import nbcp.db.*
import org.springframework.stereotype.Component
import java.io.Serializable
/**
 *
 */
@Component
class SqlSpreadColumnEvent_Select : ISqlEntitySelect {
    override fun beforeSelect(select: SqlBaseQueryClip): EventResult? {
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

    override fun select(select: SqlBaseQueryClip, eventData: EventResult?, result: List<MutableMap<String, Any?>>) {
        if (select is SqlQueryClip<*, *> == false) {
            return
        }
        var spreads = select.mainEntity.getSpreadColumns();
        spreads.forEach { spread ->
            result.forEach { row ->
                var spreadRowData = row.filter { it.key.startsWith(spread + "_") }
                    .ToMap({ it.key.substring(spread.length + 1) }, { it.value })
                row.put(spread, spreadRowData);
            }
        }
    }
}