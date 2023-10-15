package nbcp.base.db

import nbcp.base.db.annotation.Cn
import nbcp.base.extend.*;
import java.io.Serializable

open class IntCodeName @JvmOverloads constructor(@Cn("编码") var code: Int = 0, @Cn("名称") var name: String = "") :
    Serializable {
    override fun toString(): String {
        return this.ToJson().Slice(1, -1)
    }
}