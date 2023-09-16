package nbcp.base.comm

import nbcp.base.extend.HasValue
import java.io.Serializable


/**
 * Created by jin on 2017/3/16.
 */

/**
 * 普通的返回对象。
 */
open class JsonResult : Serializable {
    var code: Int = 0;
    var msg: String = ""
        get() {
            return field
        }
        set(value) {
            if (value.isEmpty()) code = 0
            else if (value.HasValue && code == 0) code = -1
            field = value;
        }

    var cause: String? = null

    val hasError: Boolean
        get() {
            return this.code != 0;
        }

    companion object {
        @JvmStatic
        @JvmOverloads
        fun error(msg: String, code: Int = 0): JsonResult {
            var ret = JsonResult();
            ret.code = code;
            ret.msg = msg;

            //如果指定了code
            if (code != 0) {
                ret.code = code;
            }
            return ret;
        }
    }
}