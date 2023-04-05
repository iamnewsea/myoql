package nbcp.myoql.code.generator.tool.freemarker

import nbcp.base.extend.*
import nbcp.myoql.code.generator.db.mysql.EntityDbItemFieldData
import java.lang.reflect.Field

class FreemarkerColumnStyle : BaseMethodModelFreemarker() {
    override fun exec(list: MutableList<Any?>): Any {
        var paramValue = getFreemarkerParameter(list[2]).AsString().trim();
        if (paramValue.isEmpty()) {
            return paramValue
        }
        var key = getFreemarkerParameter(list[1]).AsString().trim();
        var field = getFreemarkerParameter(list[0]) as EntityDbItemFieldData;
        if (field.isPrimary == "主键") {
            return "**" + paramValue + "**"
        }

        if (field.fieldName.IsIn("create_at", "create_by", "update_at", "update_by", "is_deleted")) {
            return "*" + paramValue + "*"
        }

        if (field.isPrimary.HasValue && key.IsIn("nam", "index")) {
            return "**" + paramValue + "**"
        }
        return paramValue;
    }
}