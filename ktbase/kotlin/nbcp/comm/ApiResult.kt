package nbcp.comm

import java.io.Serializable
import java.lang.RuntimeException


/**
 * Created by jin on 2017/3/16.
 */

open class ApiResult<T>  : JsonResult() {
    var data: T? = null
    var value: Any? = null

    companion object {
        @JvmStatic
        @JvmOverloads
        fun <T> error(msg: String, code: Int = 0): ApiResult<T> {
            var ret = ApiResult<T>();
            ret.code = code;
            ret.msg = msg;
            return ret;
        }

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
    fun withValue(value: Any?): ApiResult<T> {
        this.value = value;
        return this;
    }
}

