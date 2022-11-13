package nbcp.base.db

open class IdNamePath @JvmOverloads constructor(id: String = "", name: String = "", @Cn("路径") var path: String = "") :
    IdName(id, name) {
}