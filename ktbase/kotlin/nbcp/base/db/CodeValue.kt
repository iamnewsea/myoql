package nbcp.base.db

import nbcp.base.db.annotation.Cn
import nbcp.base.extend.*;
import java.io.Serializable

/**
 * 只能用于 code 不变的情况。
 */
open class CodeValue @JvmOverloads constructor(@Cn("编码") var code: String = "", @Cn("值") var value: String = "") :
    Serializable {
    override fun toString(): String {
        return this.ToJson().Slice(1, -1)
    }
}