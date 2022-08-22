package nbcp.comm

import java.io.Serializable
import java.lang.RuntimeException


/**
 * Created by jin on 2017/3/16.
 */

/**
 * 查询对象
 */

/**
 * 列表返回对象
 */
open class ListResult<T>(
    //@Transient val clazz: Class<T>
) : JsonResult() {

    var total: Int = -1
    var data: List<out T> = listOf()
    var value: Any? = null

    companion object {
        @JvmStatic
        @JvmOverloads
        fun <T> error(msg: String, code: Int = 0): ListResult<T> {
            var ret = ListResult<T>();
            ret.code = code;
            ret.msg = msg;
            return ret;
        }

        @JvmStatic
        @JvmOverloads
        fun <T> of(data: Collection<T>, total: Int = -1): ListResult<T> {
            var ret = ListResult<T>();
            ret.data = data.toList();
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
    fun withValue(value: Any?): ListResult<T> {
        this.value = value;
        return this;
    }
}
