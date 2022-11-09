package nbcp.base.comm

import nbcp.base.extend.AsString

/**
 * Created by jin on 2017/3/16.
 */

class DataInvalidateException @JvmOverloads constructor(msg: String = "") :
    RuntimeException(msg.AsString("数据非法"))

class ParameterInvalidException @JvmOverloads constructor(msg: String = "") :
    RuntimeException(msg.AsString("参数非法"))

class NoDataException @JvmOverloads constructor(msg: String = "") :
    RuntimeException(msg.AsString("找不到数据"))

class ExecuteDbException @JvmOverloads constructor(msg: String = "") :
    RuntimeException(msg.AsString("操作数据库失败"))

class ServerException @JvmOverloads constructor(msg: String = "") :
    RuntimeException(msg.AsString("服务器异常"))
