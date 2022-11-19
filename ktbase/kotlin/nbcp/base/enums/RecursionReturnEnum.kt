package nbcp.base.enums


/**
 * 递归的返回状态
 */
enum class RecursionReturnEnum(val value: Int) {
    NONE(0),
    GO(1),
    STOP_SUB(2),
    ABORT(6), // 6 = 2 + 4  , abort 包含了 StopSub
    REMOVE(8);
}