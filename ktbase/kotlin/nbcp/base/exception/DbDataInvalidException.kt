package nbcp.base.exception

import nbcp.base.extend.AsString

/**
 * Created by jin on 2017/3/16.
 */

open class DbDataInvalidException @JvmOverloads constructor(msg: String = "") :
    RuntimeException(msg.AsString("数据非法"))