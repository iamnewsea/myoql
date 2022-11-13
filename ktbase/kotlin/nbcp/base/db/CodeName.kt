package nbcp.base.db

import nbcp.base.extend.Slice
import nbcp.base.extend.ToJson
import java.io.Serializable

/**
 * 只能用于 code 不变的情况。
 */
open class CodeName @JvmOverloads constructor(@Cn("编码") var code: String = "", @Cn("名称") var name: String = "") :
    Serializable {
    override fun toString(): String {
        return this.ToJson().Slice(1, -1)
    }
}