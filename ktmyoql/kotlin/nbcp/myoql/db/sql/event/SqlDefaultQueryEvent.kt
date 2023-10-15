package nbcp.myoql.db.sql.event

import nbcp.base.comm.*
import nbcp.base.db.*
import nbcp.base.enums.*
import nbcp.base.extend.*
import nbcp.base.extend.*
import nbcp.base.extend.*
import nbcp.base.utils.*
import nbcp.myoql.db.*
import nbcp.myoql.db.comm.*
import nbcp.myoql.db.enums.*
import nbcp.myoql.db.sql.component.SqlBaseQueryClip
import nbcp.myoql.db.sql.component.SqlQueryClip
import org.springframework.stereotype.Component
import java.lang.reflect.ParameterizedType

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


        //先转Json
        select.mainEntity.getJsonColumns()
            .forEach { column ->
                var key = column.getAliasName();
                var field = select.mainEntity.entityClass.FindField(key)
                if (field == null) {
                    return@forEach
                }

                var genericType = (field.genericType as ParameterizedType?)?.GetActualClass(0)
                result.forEach { row ->
                    var value = row.get(key).AsString();
                    if (value.HasValue) {
                        if (field.type.isArray) {
                            row.put(key, value.FromListJson(field.type.componentType).toTypedArray());
                        } else if (field.type.IsCollectionType) {
                            row.put(key, value.FromListJson(genericType!!));
                        } else {
                            row.put(key, value.FromJson(field.type));
                        }
                    }
                }
            }


        val spreads = select.mainEntity.getSpreadColumns();
        spreads.forEach { spread ->
            val entField = select.mainEntity.entityClass.FindField(spread.column)
            if (entField == null) {
                return@forEach
            }

            //SpreadColumn
            result.forEach { row ->
                val spreadRowData = row.filter { it.key.startsWith(spread.getPrefixName()) }
                    .ToMap({ it.key.substring(spread.getPrefixName().length) }, { it.value })
                    .ConvertType(entField.type)
                row.put(spread.column, spreadRowData);
            }
        }
    }
}