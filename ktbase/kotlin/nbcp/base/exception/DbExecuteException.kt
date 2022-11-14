package nbcp.base.exception

import nbcp.base.extend.AsString

open class DbExecuteException @JvmOverloads constructor(msg: String = "") :
    RuntimeException(msg.AsString("操作数据库失败"))