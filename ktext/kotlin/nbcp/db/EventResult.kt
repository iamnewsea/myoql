package nbcp.db

/**
 * 更新或删除事件执行的结果
 */
data class EventResult(
        /**
         * 执行结果 ，返回 false 将停止后面的执行。
         */
        var result: Boolean = true,
        /**
         * 传递给后续操作的额外数据。
         */
        var extData: Any? = null
)