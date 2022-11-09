package nbcp.myoql.db.sql.event;

import nbcp.base.extend.ToJson
import nbcp.base.utils.CodeUtil
import nbcp.myoql.db.comm.EventResult
import nbcp.myoql.db.comm.RemoveToSysDustbin
import nbcp.myoql.db.db
import nbcp.myoql.db.sql.component.RawQuerySqlClip
import nbcp.myoql.db.sql.component.SqlDeleteClip
import nbcp.myoql.db.sql.component.doInsert
import nbcp.myoql.db.sql.entity.s_dustbin
import nbcp.myoql.db.sql.extend.fromTableName
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