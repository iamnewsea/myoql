package nbcp.myoql.db.sql.event;

import nbcp.base.extend.*;
import nbcp.base.utils.*;
import nbcp.myoql.db.*;
import nbcp.myoql.db.comm.*
import nbcp.myoql.db.sql.component.*
import nbcp.myoql.db.sql.extend.*
import nbcp.myoql.db.sql.s_dustbin
import org.springframework.stereotype.Component

/**
 * 处理删除数据后转移到垃圾箱功能
 */
@Component
class SqlDefaultSysDustbinEvent : ISqlEntityDelete {

    override fun beforeDelete(delete: SqlDeleteClip<*>): EventResult {
        var dust = delete.mainEntity.entityClass.getAnnotation(RemoveToSysDustbin::class.java)
        if (dust != null) {
            //找出数据
            var where = delete.whereDatas.toSingleData()
            where.expression = "select * from " + delete.mainEntity.fromTableName + " where " + where.expression
            var cursor = RawQuerySqlClip(where, delete.mainEntity.tableName).toMapList()
            return EventResult(true, cursor)
        }

        return EventResult(true);
    }

    override fun delete(delete: SqlDeleteClip<*>, eventData: EventResult) {
        val data = eventData.extData
        if (data == null) return

        val dustbin = s_dustbin()
        dustbin.id = CodeUtil.getCode()
        dustbin.table = delete.mainEntity.tableName
        dustbin.data = data.ToJson()

        db.sqlBase.s_dustbin.doInsert(dustbin)
    }
}