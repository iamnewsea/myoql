package nbcp.base.db

import nbcp.base.extend.Slice
import nbcp.base.extend.ToJson
import java.io.Serializable

open class KeyValueString @JvmOverloads constructor(@Cn("键") var key: String = "", @Cn("值") var value: String = "") :
    Serializable {
    override fun toString(): String {
        return this.ToJson().Slice(1, -1)
    }
}