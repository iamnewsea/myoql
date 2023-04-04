package nbcp.myoql.code.generator

import nbcp.base.extend.IsIn
import nbcp.base.utils.StringUtil


fun String.removeQuoteContent(): String {
    return Regex("""\([^)]*\)""").replace(this, "")
}


fun compareDbName(a: String, b: String): Int {

    var a_sects = StringUtil.getKebabCase(a).split("-")
    var b_sects = StringUtil.getKebabCase(b).split("-")

    if (a == b) {
        return 0;
    }

    //id在最前
    if (a == "id") {
        return -1;
    }
    if (b == "id") {
        return 1
    }

    if (a.IsIn("create_at", "create_by", "update_at", "update_by", "is_deleted") &&
            b.IsIn("create_at", "create_by", "update_at", "update_by", "is_deleted")) {
        return a.compareTo(b);
    }

    if (a.IsIn("create_at", "create_by", "update_at", "update_by", "is_deleted")) {
        return 1
    }

    if (b.IsIn("create_at", "create_by", "update_at", "update_by", "is_deleted")) {
        return -1
    }

    // rel 的表放到最后。

    if (a_sects.last() == "rel" && b_sects.last() != "rel") {
        return 1;
    }

    if (a_sects.last() != "rel" && b_sects.last() == "rel") {
        return -1;
    }

    //短的在前。
    if (a_sects.size != b_sects.size) {
        return a_sects.size - b_sects.size
    }

    //如果相同， 比较每个部分。
    for (i in 0 until a_sects.size) {
        var pa = a_sects[i];
        var pb = b_sects[i];

        var r = pa.compareTo(pb)
        if (r == 0) {
            continue;
        }

        return r;
    }


    return a.compareTo(b)
}