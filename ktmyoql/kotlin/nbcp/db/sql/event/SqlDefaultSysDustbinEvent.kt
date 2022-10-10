package nbcp.db.sql.event;

import nbcp.db.sql.*;
import nbcp.comm.ToJson
import nbcp.utils.*
import nbcp.db.*
import nbcp.db.sql.entity.s_dustbin
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