package nbcp.base.comm


/**
 * Created by jin on 2017/3/16.
 */

open class ApiResult<T> : nbcp.base.comm.JsonResult() {
    var data: T? = null
    var value: Any? = null

    companion object {
        @JvmStatic
        @JvmOverloads
        fun <T> error(msg: String, code: Int = 0): nbcp.base.comm.ApiResult<T> {
            var ret = nbcp.base.comm.ApiResult<T>();
            ret.code = code;
            ret.msg = msg;
            return ret;
        }

        @JvmStatic
        fun <T> of(data: T?): nbcp.base.comm.ApiResult<T> {
            var ret = nbcp.base.comm.ApiResult<T>();
            ret.data = data;
            return ret;
        }
    }

    /** 设置额外value的值。
     * @param value value的值
     */
    fun withValue(value: Any?): nbcp.base.comm.ApiResult<T> {
        this.value = value;
        return this;
    }
}

