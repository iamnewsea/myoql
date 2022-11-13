package nbcp.base.db

open class IdCodeName @JvmOverloads constructor(var id: String = "", code: String = "", name: String = "") :
    CodeName(code, name) {
}