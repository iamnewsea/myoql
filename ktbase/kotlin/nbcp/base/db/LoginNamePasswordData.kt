package nbcp.base.db

import nbcp.base.extend.Slice
import nbcp.base.extend.ToJson
import java.io.Serializable

open class LoginNamePasswordData(var loginName: String = "", var password: String = "") : Serializable {
    override fun toString(): String {
        return this.ToJson().Slice(1, -1)
    }
}