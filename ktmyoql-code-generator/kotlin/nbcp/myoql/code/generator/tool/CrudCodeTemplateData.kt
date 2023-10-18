package nbcp.myoql.code.generator.tool

import nbcp.base.extend.*
import nbcp.base.utils.CnAnnotationUtil
import nbcp.base.utils.MyUtil
import nbcp.base.utils.StringUtil
import nbcp.myoql.code.generator.tool.freemarker.*
import nbcp.myoql.code.generator.tool.freemarker.*
import nbcp.myoql.code.generator.tool.freemarker.*
import nbcp.myoql.db.BaseEntity
import java.lang.reflect.Field
import java.lang.reflect.ParameterizedType

class CrudCodeTemplateData(
    var group: String,
    var entityClass: Class<*>,
    var tableName: String,
    var idKey: String
) : BaseFreemarkerModel() {

    val url: String
        get() {
            return "/${StringUtil.getKebabCase(group)}/${StringUtil.getKebabCase(entityClass.simpleName)}";
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

    val enumTypes: Set<Class<*>>
        get(){
            return entityClass.findAllEnum()
        }

    val embedModelTypes: Set<Class<*>>
        get(){
            var list = mutableSetOf<Class<*>>();
            entityClass.AllFields
                .forEach {
                    if (it.IsCollectionType()) {
                        return@forEach
                    }


                    if (it.IsArrayType()) {
                        return@forEach
                    }

                    if( it.type.IsSimpleType()){
                        return@forEach
                    }

                    if( it.type is BaseEntity){
                        return@forEach
                    }

                    list.add(it.type)
                    return@forEach
                }


            return list;
        }

    val inputTableTypes: Set<Class<*>>
        get() {
            var list = mutableSetOf<Class<*>>();

            entityClass.AllFields
                .forEach {
                    if (it.IsCollectionType()) {
                        var comType = (it.genericType as ParameterizedType).GetActualClass(0)

                        if (!comType.IsSimpleType()) {
                            list.add(comType)
                        }

                        return@forEach
                    }


                    if (it.IsArrayType()) {
                        var comType = it.type.componentType;

                        if (!comType.IsSimpleType()) {
                            list.add(comType)
                        }
                        return@forEach
                    }

                    return@forEach
                }


            return list;
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