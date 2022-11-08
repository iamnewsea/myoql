package nbcp.base.comm

import java.io.Serializable
import nbcp.base.comm.*
import nbcp.base.enums.*
import nbcp.base.extend.*
import nbcp.base.utils.*


/**
 * Created by jin on 2017/3/16.
 */

/**
 * 普通的返回对象。
 */
open class TypedMapResult() : Serializable {
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

    companion object {
        @JvmStatic
        @JvmOverloads
        fun error(msg: String, code: Int = 0): TypedMapResult {
            var ret = TypedMapResult();
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