package nbcp.db

import nbcp.base.extend.*
import nbcp.base.utils.SpringUtil

/**
 * Created by yuxh on 2018/11/13
 */

open class IdName(var id: String = "", var name: String = "") : java.io.Serializable {}

open class CodeName(var code: String = "", var name: String = "") : java.io.Serializable {}

open class IdCodeName(var id: String = "", code: String = "", name: String = "") : CodeName(id, name) {}

open class KeyValueString(var key: String = "", var value: String = "") : java.io.Serializable {}


/**
 * 表示Mongo数据库里 Id，Url 的附件实体引用。
 */
open class IdUrl(var id: String = "", var url: String = "") : java.io.Serializable {}

open class IdNameUrl(id: String = "", var name: String = "", url: String = "") : IdUrl()


open class LoginUserModel(id: String = "", var loginName: String = "", name: String = "", var token: String = "") : IdName(id, name)

