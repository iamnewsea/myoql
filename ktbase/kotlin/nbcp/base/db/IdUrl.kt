package nbcp.base.db

open class IdUrl() : BaseUrlModel() {
    var id: String = ""

    constructor(id: String, url: String) : this() {
        this.id = id;
        this.url = url;
    }
}