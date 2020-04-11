package nbcp.db.es

import nbcp.comm.JsonMap


class SearchBodyClip {
    var take = -1;
    var skip = 0L;
    var sortObject: JsonMap = JsonMap()


    //    private var whereJs: String = "";
    val selectColumns = mutableSetOf<String>();
    //    private var selectDbObjects = mutableSetOf<String>();
    val unSelectColumns = mutableSetOf<String>()

    override fun toString(): String {
        return super.toString()
    }

    fun isEmpty(): Boolean {
        return false;
    }
}