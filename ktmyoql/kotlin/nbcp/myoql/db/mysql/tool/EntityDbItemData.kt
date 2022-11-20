package nbcp.myoql.db.mysql.tool

import nbcp.base.comm.config
import nbcp.base.extend.HasValue
import nbcp.base.utils.MyUtil

class EntityDbItemData {
    var name = ""

    //原始的表注释
    var commentString = ""

    //去除 （）
    val comment: String
        get() {
            return Regex("""\s*\(\s*[\w-_]+\s*\)\s*""").replace(commentString, "")
        }

    val group: String
        get() {
            var groups_all_value = Regex("""\(\s*([\w-_]+)\s*\)""")
                .find(
                    commentString
                        .replace("（", "(")
                        .replace("）", "")
                )
                ?.groupValues ?: listOf()
            var group_value = ""
            if (groups_all_value.size > 0) {
                group_value = groups_all_value[1];
            }

            var groups_value = group_value.split(",").map { it.trim() }.filter { it.HasValue }

            if (groups_value.any()) {
                return groups_value.first();
            }
            groups_value = name.split("_", "-");
            if (groups_value.size > 1) {
                return groups_value.first()
            }

            return ""
        }

    var uks = arrayOf<String>()

    var columns = mutableListOf<EntityDbItemFieldData>()

    val className: String
        get() {
            var name2 = this.name
            if (name2[0].isDigit()) {
                name2 = "table_" + name2;
            }

            if (config.myoqlKeepDbName) {
                return MyUtil.splitWordParts(name2).joinToString("_")
            }

            return MyUtil.getBigCamelCase(name2)
        }
}