package nbcp.myoql.db.mongo.entity

import nbcp.base.comm.StringMap
import nbcp.base.db.annotation.Cn
import java.io.Serializable

@Cn("请求数据")
open class BaseRequestData : Serializable {
    @Cn("访问地址")
    var url = "";

    @Cn("访问方法")
    var method = "";

    @Cn("调用链Id")
    var traceId = "";

    @Cn("请求体")
    var body = "";

    @Cn("请求头")
    var header = StringMap();

    @Cn("客户端Ip")
    var clientIP = "";
}