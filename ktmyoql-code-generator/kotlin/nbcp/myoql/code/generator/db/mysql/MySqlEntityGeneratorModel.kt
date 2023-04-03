package nbcp.myoql.code.generator.db.mysql

import nbcp.base.comm.config
import nbcp.base.extend.AsBoolean
import nbcp.base.extend.AsBooleanWithNull
import nbcp.base.utils.MyUtil
import nbcp.base.utils.StringUtil
import nbcp.myoql.db.sql.enums.DbType


class EntityDbItemFieldData {
    // name 小驼峰形式。
    val fieldName: String
        get() {
            var name2 = this.name;
            if (name2[0].isDigit()) {
                name2 = "field_" + name2;
            }

            if (config.myoqlKeepDbName) {
                return StringUtil.splitWordParts(name2).joinToString("_");
            }
            return StringUtil.getSmallCamelCase(name2);
        }

    var sqlType = ""
    var remark = ""
    var name = ""
    var dbType = DbType.OTHER
    var commentString = ""
    var isPrimary = false

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
            if (this.dbType == DbType.BYTE) {
                return "new byte[0]"
            }
            return this.dbType.toKotlinDefaultValue()
        }

    var autoId: Boolean = false
        get() {
            var idIsAutoId = fieldName == "id" && config.getConfig("app.sql.id-is-autoId").AsBoolean();
            if (idIsAutoId) {
                return idIsAutoId;
            }
            return commentString.contains(Regex("\bauto_id\b", RegexOption.IGNORE_CASE))
        }

    var autoNumber: Boolean = false
        get() {
            var idIsAutoNumber = fieldName == "id" && config.getConfig("app.sql.id-is-autoNumber").AsBoolean();
            if (idIsAutoNumber) {
                return idIsAutoNumber;
            }
            return commentString.contains(Regex("\bauto_number\b", RegexOption.IGNORE_CASE))
        }

    /*下面四个属性表示该表的单键主键 或 唯一键*/
    var autoInc: Boolean = false
}

