package nbcp.myoql.db.mysql.tool

import nbcp.base.extend.AllFields

class BaseEntityInfo(var entity: EntityDbItemData, var baseEntityClass: Array<out Class<*>>) {
    fun getBaseInterfaces(): List<Class<*>> {
        var list = mutableListOf<Class<*>>()
        baseEntityClass.forEach {
            if (it.isInterface == false) {
                return@forEach
            }

            if (itMatch(entity, it) == false) {
                return@forEach
            }

            list.add(it);
        }
        return list;
    }

    private fun itMatch(entity: EntityDbItemData, it: Class<*>): Boolean {
        return it.AllFields.all { field ->
            var fieldName = field.name;
            var column = entity.columns.find { it.fieldName == fieldName };
            if (column == null) {
                return@all false;
            }

            if (column.dbType.javaType != field.type) {
                return@all false;
            }

            return@all true
        }
    }

    fun getBaseClasses(): List<Class<*>> {
        var list = mutableListOf<Class<*>>()
        baseEntityClass.forEach {
            if (it.isInterface) {
                return@forEach
            }

            if (itMatch(entity, it) == false) {
                return@forEach
            }

            list.add(it);
        }
        return list;
    }


    fun getColumnsWithoutBaseClasses(): Set<String> {
        var set = mutableSetOf<String>()
        getBaseClasses().forEach { type ->
            set.addAll(type.AllFields.map { it.name })
        }

        return set;
    }

    fun getColumnsWithoutBaseInterfaces(): Set<String> {
        var set = mutableSetOf<String>()
        getBaseInterfaces().forEach { type ->
            set.addAll(type.AllFields.map { it.name })
        }

        return set;
    }

    fun getJpaStyleFields(): List<String> {
        var baseColumns = getColumnsWithoutBaseClasses();
        var baseColumnsInInterface = getColumnsWithoutBaseInterfaces();
        return this.entity.columns
                .filter { column ->
                    return@filter baseColumns.contains(column.fieldName)
                }
                .map { column ->
                    """/** ${column.comment} */
@Cn("${column.comment}")
@Column(name = "${column.name}")
@DbName(name = "${column.name}")
private ${if (baseColumnsInInterface.contains(column.fieldName)) "override" else ""}${column.javaType} ${column.fieldName} ;"""
                }
    }

    fun getBaseClasseString(): String {
        var list = getBaseClasses();
        if (list.any() == false) return "";
        return " extends ${list.joinToString(",")}"
    }

    fun getBaseInterfaceString(): String {
        var list = getBaseInterfaces();
        if (list.any() == false) return "";
        return " implements ${list.joinToString(",")}"
    }


}