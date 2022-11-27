package nbcp.myoql.code.generator.tool

import nbcp.base.extend.AllFields
import nbcp.base.extend.AsString
import nbcp.base.extend.kotlinTypeName
import nbcp.base.utils.CnAnnotationUtil
import nbcp.base.utils.MyUtil
import nbcp.myoql.code.generator.tool.freemarker.*
import nbcp.myoql.code.generator.tool.freemarker.*
import nbcp.myoql.code.generator.tool.freemarker.*
import java.lang.reflect.Field

class CrudCodeTemplateData(
    var group: String,
    var entityClass: Class<*>,
    var tableName: String,
    var idKey: String
) : BaseFreemarkerModel() {

    val url: String
        get() {
            return "/${MyUtil.getKebabCase(group)}/${MyUtil.getKebabCase(entityClass.simpleName)}";
        }

    val entity: String
        get() {
            return entityClass.simpleName;
        }

    val fields: List<Field>
        get() {
            return entityClass.AllFields
                .sortedBy {
                    if (it.name == "id") return@sortedBy -9
                    if (it.name == "name") return@sortedBy -8
                    if (it.name == "code") return@sortedBy -7

                    return@sortedBy 0
                }
        }

    val title: String
        get() {
            return CnAnnotationUtil.getCnValue(entityClass).AsString(tableName);
        }

    val status_enum_class: String
        get() {
            var statusField = fields.firstOrNull { it.name == "status" }
            if (statusField != null) {
                return statusField.type.kotlinTypeName;
            }

            return "";
        }

    val kotlin_type = FreemarkerGetKotlinType()
    val hasField = FreemarkerHasField()
    val has_dustbin = FreemarkerHasDustbin()

}