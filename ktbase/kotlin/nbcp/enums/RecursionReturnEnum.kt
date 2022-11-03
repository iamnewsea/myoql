package nbcp.utils


/**
 * 递归的返回状态
 */
enum class RecursionReturnEnum private constructor(val value: Int) {
    None(0),
    Go(1),
    StopSub(2),
    Abord(6), // 6 = 2 + 4  , Abord 包含了 StopSub
    Remove(8);
}