package nbcp.base.exception

import nbcp.base.extend.AsString

open class NoDbDataException @JvmOverloads constructor(msg: String = "") :
    RuntimeException(msg.AsString("找不到数据"))