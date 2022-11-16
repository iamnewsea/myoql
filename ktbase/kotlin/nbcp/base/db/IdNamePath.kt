package nbcp.base.db

import nbcp.base.db.annotation.Cn

open class IdNamePath @JvmOverloads constructor(id: String = "", name: String = "", @Cn("路径") var path: String = "") :
    IdName(id, name) {
}