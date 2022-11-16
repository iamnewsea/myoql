package nbcp.base.db

import nbcp.base.db.annotation.Cn
import nbcp.base.extend.Slice
import nbcp.base.extend.ToJson
import java.io.Serializable

open class JsonKeyValuePair<T> @JvmOverloads constructor(@Cn("键") var key: String = "", @Cn("值") var value: T? = null) :
    Serializable {
    override fun toString(): String {
        return this.ToJson().Slice(1, -1)
    }
}