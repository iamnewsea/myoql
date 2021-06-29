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
open class JsonResult @JvmOverloads constructor(var msg: String = "", var code: Int? = null) : Serializable {
    var cause: String? = null
}

open class ApiResult<T> @JvmOverloads constructor(msg: String = "", code: Int? = null) : JsonResult(msg, code) {
    var data: T? = null
    var value: Any? = null

    companion object {
        @JvmStatic
        fun <T> of(data: T?): ApiResult<T> {
            var ret = ApiResult<T>();
            ret.data = data;
            return ret;
        }
    }

    /** 设置额外value的值。
     * @param value value的值
     */
    fun setValue(value: Any): ApiResult<T> {
        this.value = value;
        return this;
    }
}


class ParameterInvalidException @JvmOverloads constructor(msg: String, cause: String) :
    RuntimeException(msg.AsString("参数非法"))

class NoDataException @JvmOverloads constructor(msg: String, cause: String = "") :
    RuntimeException(msg.AsString("找不到数据"))

class ExecuteDbException @JvmOverloads constructor(msg: String, cause: String = "") :
    RuntimeException(msg.AsString("操作数据库失败"))

class ServerException @JvmOverloads constructor(msg: String, cause: String = "") :
    RuntimeException(msg.AsString("服务器异常"))

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
open class ListResult<T>() : JsonResult() {
    constructor(msg: String, code: Int? = null) : this() {
        this.msg = msg;
        this.code = code;
    }

    var total: Int = -1
    var data: List<T> = listOf()
    var value: Any? = null

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
     * @param value value的值
     */
    fun setValue(value: Serializable): ListResult<T> {
        this.value = value;
        return this;
    }
}