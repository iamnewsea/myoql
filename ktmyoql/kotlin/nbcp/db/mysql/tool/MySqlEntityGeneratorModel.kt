package nbcp.db.mysql.tool

import nbcp.comm.HasValue
import nbcp.db.sql.DbType


class EntityDbItemFieldData {
    // name 小驼峰形式。
    var fieldName = ""
    var sqlType = ""
    var remark = ""
    var name = ""
    var dbType = DbType.Other
    var commentString = ""

    val comment: String
        get() {
            return Regex("""\s*\(\s*(auto_id)|(auto_number)\s*\)\s*""").replace(commentString, "")
        }
    val kotlinType: String
        get() {
            return this.dbType.toKotlinType()
        }

    val javaType: String
        get() {
            return this.dbType.javaType.typeName
        }

    val kotlinDefaultValue: String
        get() {
            return this.dbType.toKotlinDefaultValue()
        }

    val javaDefaultValue: String
        get() {
            if (this.dbType == DbType.Byte) {
                return "new byte[0]"
            }
            return this.dbType.toKotlinDefaultValue()
        }

    var autoId: Boolean = false
        get() {
            return commentString.contains(Regex("\bauto_id\b", RegexOption.IGNORE_CASE))
        }

    var autoNumber: Boolean = false
        get() {
            return commentString.contains(Regex("\bauto_number\b", RegexOption.IGNORE_CASE))
        }

    /*下面四个属性表示该表的单键主键 或 唯一键*/
    var autoInc: Boolean = false
}

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
            groups_value = name.split("_");
            if (groups_value.size > 1) {
                return groups_value.first()
            }

            return ""
        }

    var uks = arrayOf<String>()

    var columns = mutableListOf<EntityDbItemFieldData>()
}
