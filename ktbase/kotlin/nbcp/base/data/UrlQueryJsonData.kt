package nbcp.base.data

import nbcp.base.comm.StringMap
import nbcp.base.extend.HasValue

data class UrlQueryJsonData @JvmOverloads constructor(
    var path: String = "",
    var queryJson: StringMap = StringMap(),
    var hash: String = ""
) {
    fun toUrl(): String {
        var ret = path;
        if (queryJson.any()) {
            ret += ("?" + queryJson.map { it.key + "=" + it.value }.joinToString("&"))
        }

        if (hash.HasValue) {
            ret += ("#" + hash)
        }
        return ret;
    }

    companion object {

        @JvmStatic
        fun of(url: String): UrlQueryJsonData {
            var ret = UrlQueryJsonData();
            if (url.isEmpty()) return ret;

            var sect = url.split("?");
            ret.path = sect[0];
            if (sect.size < 2) return ret;

            if (sect[1].isEmpty()) return ret;

            var queryHash = sect[1].split("#");

            if (queryHash.size > 1) {
                ret.hash = queryHash.last();
            }

            queryHash[0].split("&").forEach {
                var items = it.split("=");
                if (items.size == 1) {
                    ret.queryJson.put(items[0], "")
                } else if (items.size == 2) {
                    ret.queryJson.put(items[0], items[1])
                } else {
                    throw RuntimeException("${items}非法，无法转换为键值对")
                }
            }

            return ret;
        }
    }
}