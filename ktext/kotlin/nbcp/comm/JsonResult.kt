package nbcp.comm

import org.slf4j.LoggerFactory
import nbcp.comm.*
import java.io.Serializable
import java.lang.RuntimeException
import java.lang.reflect.ParameterizedType
import java.util.ArrayList


/**
 * Created by jin on 2017/3/16.
 */

/**
 * 普通的返回对象。
 */
open class JsonResult @JvmOverloads constructor(var msg: String = "", var cause: String = "") : Serializable {}

open class ApiResult<T> @JvmOverloads constructor(msg: String = "", cause: String = "") : JsonResult(msg, cause) {
    var data: T? = null
    var value: Any? = null
    var valueRemark: String = ""

    companion object {
        @JvmStatic
        fun <T> of(data: T?): ApiResult<T> {
            var ret = ApiResult<T>();
            ret.data = data;
            return ret;
        }
    }

    /** 设置额外value的值。
     * @param valueRemark value值的含义
     * @param value value的值
     */
    fun setValue(valueRemark: String, value: Any): ApiResult<T> {
        this.valueRemark = valueRemark;
        this.value = value;
        return this;
    }
}


class ParameterInvalidException @JvmOverloads constructor(msg: String, cause: String) : RuntimeException(msg.AsString("参数非法"))

class NoDataException @JvmOverloads constructor(msg: String, cause: String = "") : RuntimeException(msg.AsString("找不到数据"))

class ExecuteDbException @JvmOverloads constructor(msg: String, cause: String = "") : RuntimeException(msg.AsString("操作数据库失败"))

class ServerException @JvmOverloads constructor(msg: String, cause: String = "") : RuntimeException(msg.AsString("服务器异常"))

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
open class ListResult<T>(
    msg: String = "",
    var total: Int = -1,
    var data: List<T> = listOf()
) : JsonResult(msg) {
    var value: Any? = null
    var valueRemark: String = ""

    companion object {
        @JvmStatic
        @JvmOverloads
        fun <T> of(data: List<T>, total: Int = -1): ListResult<T> {
            var ret = ListResult<T>();
            ret.data = data;
            if (total < 0) {
                ret.total = data.size;
            } else {
                ret.total = total;
            }
            return ret;
        }
    }

    /** 设置额外value的值。
     * @param valueRemark value值的含义
     * @param value value的值
     */
    fun setValue(valueRemark: String, value: Serializable): ListResult<T> {
        this.valueRemark = valueRemark;
        this.value = value;
        return this;
    }
}