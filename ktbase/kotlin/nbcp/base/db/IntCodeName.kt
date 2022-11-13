package nbcp.base.db

import nbcp.base.extend.Slice
import nbcp.base.extend.ToJson
import java.io.Serializable

open class IntCodeName @JvmOverloads constructor(@Cn("编码") var code: Int = 0, @Cn("名称") var name: String = "") :
    Serializable {
    override fun toString(): String {
        return this.ToJson().Slice(1, -1)
    }
}