package nbcp.base.exception

import nbcp.base.extend.AsString

/**
 * 函数参数非法
 */
open class ParameterInvalidException @JvmOverloads constructor(msg: String = "") :
    RuntimeException(msg.AsString("参数非法"))

