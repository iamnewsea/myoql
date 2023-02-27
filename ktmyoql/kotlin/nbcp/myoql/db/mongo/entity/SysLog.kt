package nbcp.myoql.db.mongo.entity

import nbcp.base.db.IdName
import nbcp.base.db.annotation.Cn
import nbcp.base.db.annotation.DbEntityGroup
import org.springframework.data.mongodb.core.mapping.Document
import java.io.Serializable
import java.time.LocalDateTime

@Document
@DbEntityGroup("MongoBase")
@Cn("系统日志")
open class SysLog @JvmOverloads constructor(
        var id: String = "",
        @Cn("模块")
    var module: String = "", //模块,多级模块用.分隔
        @Cn("类型")
    var level: String = "",  //类型， error,warn,info
        @Cn("标签")
    var tags: MutableList<String> = mutableListOf(),   //实体标志, 查询用： module + key
        @Cn("消息")
    var msg: String = "",   //消息

        @Cn("请求数据")
    var request: BaseRequestData = BaseRequestData(), //请求数据
        @Cn("程序数据")
    var data: Any? = null,    //程序处理数据
        @Cn("回发数据")
    var response: BaseResponseData = BaseResponseData(),  //回发数据

        @Cn("创建者Id")
    var creator: IdName = IdName(),
        @Cn("创建时间")
    var createAt: LocalDateTime = LocalDateTime.now()
) : Serializable {
}