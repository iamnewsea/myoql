package nbcp.comm

import org.slf4j.LoggerFactory
import nbcp.base.extend.*
import java.io.Serializable
import java.lang.reflect.ParameterizedType
import java.util.ArrayList


/**
 * Created by jin on 2017/3/16.
 */

/**
 * 普通的返回对象。
 */

    open class JsonResult(var msg: String = "", var cause: String = "") {}

class ApiResult<T>(msg: String = "", cause: String = "") : JsonResult(msg, cause) {
    var data: T? = null

    companion object {
        fun <T> of(data: T?): ApiResult<T> {
            var ret = ApiResult<T>();
            ret.data = data;
            return ret;
        }
    }
}


/**
 * 查询对象
 */

open class ListQueryModel {
    var skip: Int = 0;
    var take: Int = -1;
}

/**
 * 列表返回对象
 */
class ListResult<T>(msg: String = "",
                    var total: Int = -1,
                    var data: List<T> = listOf(),
                    var value: Any? = null
) : JsonResult(msg) {
    companion object {
        fun<T> of(data:List<T>):ListResult<T>{
            var ret = ListResult<T>();
            ret.data = data;
            ret.total = data.size;
            return ret;
        }
    }
}

//class AppListResult<T>(msg: String = "",
//                       var over: Boolean = false, //表示数据已结束。
//                       var data: MutableList<T> = mutableListOf(),
//                       var value: String = ""//返回除 data 额外的信息
//) : JsonResult(msg) {
//}