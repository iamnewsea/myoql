package nbcp.base.db

open class IdNameUrl @JvmOverloads constructor(var id: String = "", name: String = "", url: String = "") :
    NameUrl(name, url) {
}