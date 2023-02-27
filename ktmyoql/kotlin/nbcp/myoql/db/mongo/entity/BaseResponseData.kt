package nbcp.myoql.db.mongo.entity

import nbcp.base.comm.StringMap
import nbcp.base.db.annotation.Cn
import java.io.Serializable

@Cn("回发数据")
open class BaseResponseData : Serializable {
    @Cn("状态码")
    var status = 0;

    @Cn("响应体")
    var body = "";

    @Cn("响应头")
    var header = StringMap();

    @Cn("结果")
    var result = "";
}