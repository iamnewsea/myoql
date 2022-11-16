package nbcp.base.db

import nbcp.base.db.annotation.Cn

open class NameUrl() : BaseUrlModel() {
    @Cn("名称")
    var name: String = ""

    constructor(name: String, url: String) : this() {
        this.name = name;
        this.url = url;
    }
}