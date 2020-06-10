package nbcp.db

import nbcp.comm.*
import nbcp.utils.*

/**
 * Created by yuxh on 2018/11/13
 */

open class IdValue(var id: String = "", var value: String = "") : java.io.Serializable {}

open class IdName(var id: String = "", var name: String = "") : java.io.Serializable {}

open class CodeName(var code: String = "", var name: String = "") : java.io.Serializable {}

open class CodeValue(var code: String = "", var value: String = "") : java.io.Serializable {}

open class IdCodeName(var id: String = "", code: String = "", name: String = "") : CodeName(code, name) {}

open class KeyValueString(var key: String = "", var value: String = "") : java.io.Serializable {}


open class IntCodeName(var code: Int, var name: String) : java.io.Serializable {
    constructor() : this(0, "") {
    }
}

open class IdNameUrl(id: String = "", var name: String = "", url: String = "") : IdUrl(id, url)

/**
 * 登录用户数据
 */
open class LoginUserModel(
        id: String = "",
        var loginName: String = "",
        name: String = "",
        var token: String = "",
        var organization: IdName = IdName(),
        var roles: List<String> = listOf() //角色
) : IdName(id, name) {
    companion object {

    }

    fun AsIdName(): IdName {
        return IdName(this.id, this.name)
    }
}

