package nbcp.base.db

import nbcp.base.db.annotation.Cn
import nbcp.base.extend.Slice
import nbcp.base.extend.ToJson
import java.io.Serializable

open class IdName @JvmOverloads constructor(var id: String = "", @Cn("名称") var name: String = "") :
    Serializable {
    override fun toString(): String {
        return this.ToJson().Slice(1, -1)
    }
}