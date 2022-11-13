package nbcp.base.db

import nbcp.base.extend.Slice
import nbcp.base.extend.ToJson
import java.io.Serializable

/**
 * Created by yuxh on 2018/11/13
 */

open class IdValue @JvmOverloads constructor(var id: String = "", var value: String = "") : Serializable {
    override fun toString(): String {
        return this.ToJson().Slice(1, -1)
    }
}