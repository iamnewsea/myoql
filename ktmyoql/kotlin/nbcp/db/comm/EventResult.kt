package nbcp.db

import nbcp.db.mongo.event.*

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
        var extData: Any? = null,

        var dataSource: String = "",

        var tableName: String = ""
)


class QueryEventResult(var event: IMongoEntityQuery, var result: EventResult)
class InsertEventResult(var event: IMongoEntityInsert, var result: EventResult)
class DeleteEventResult(var event: IMongoEntityDelete, var result: EventResult)
class UpdateEventResult(var event: IMongoEntityUpdate, var result: EventResult)
class AggregateEventResult(var event: IMongoEntityAggregate, var result: EventResult)
