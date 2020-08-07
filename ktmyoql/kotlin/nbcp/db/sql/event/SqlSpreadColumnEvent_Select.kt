package nbcp.db.sql

import nbcp.comm.AllFields
import nbcp.comm.JsonMap
import nbcp.comm.ToJson
import nbcp.comm.ToMap
import nbcp.utils.*
import nbcp.db.*
import nbcp.db.sql.entity.s_dustbin
import org.springframework.stereotype.Component
import java.lang.reflect.Field
import kotlin.reflect.full.createInstance

/**
 * 处理删除数据后转移到垃圾箱功能
 */
@Component
class SqlSpreadColumnEvent_Select : ISqlEntitySelect {
    override fun beforeSelect(select: SqlBaseQueryClip): DbEntityEventResult? {
//        var select  = select as SqlQueryClip< SqlBaseMetaTable<out ISqlDbEntity>, ISqlDbEntity>
//        var spreads = select.mainEntity.getSpreadColumns();
//        if( spreads.any()) {
//            var select_columns = select.columns.map { it.name }.intersect(spreads.toList())
//            if( select_columns.any()){
//                select.select {  }
//            }
//        }
        return DbEntityEventResult(true)
    }

    override fun select(select: SqlBaseQueryClip, eventData: DbEntityEventResult?, result: List<MutableMap<String, Any?>>) {
        var select = select as SqlQueryClip<SqlBaseMetaTable<out ISqlDbEntity>, ISqlDbEntity>
        var spreads = select.mainEntity.getSpreadColumns();
        spreads.forEach { spread ->
            result.forEach { row ->
                var spreadRowData = row.filter { it.key.startsWith(spread + "_") }.ToMap({ it.key.substring(spread.length + 1) }, { it.value })
                row.put(spread, spreadRowData);
            }
        }
    }
}