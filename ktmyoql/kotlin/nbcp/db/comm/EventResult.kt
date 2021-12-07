package nbcp.db

import nbcp.db.mongo.event.IMongoEntityDelete
import nbcp.db.mongo.event.IMongoEntityUpdate

/**
 * 更新或删除事件执行的结果
 */
data class EventResult @JvmOverloads constructor(
    /**
     * 执行结果 ，返回 false 将停止后面的执行。
     */
    var result: Boolean = true,

    /**
     * 传递给后续操作的额外数据。
     */
    var extData: Any? = null
)


class DeleteEventResult(var event: IMongoEntityDelete, var chain:EventChain, var result:EventResult)
class UpdateEventResult(var event: IMongoEntityUpdate, var chain:EventChain, var result:EventResult)

