package nbcp.myoql.db.sql.extend

import nbcp.base.comm.*;
import nbcp.base.db.*;
import nbcp.base.enums.*;
import nbcp.base.extend.*;
import nbcp.base.utils.*;
import nbcp.myoql.db.enums.*
import nbcp.myoql.db.*;
import nbcp.myoql.db.comm.*
import nbcp.myoql.db.sql.component.*
import nbcp.myoql.db.sql.extend.*
import nbcp.base.extend.ConvertType
import nbcp.myoql.db.sql.base.SqlBaseMetaTable
import nbcp.myoql.db.sql.base.SqlColumnName
import nbcp.myoql.db.sql.component.SqlQueryClip
import java.io.Serializable

/**
 * 每个级别会查询一次。
 */
class MyOqlSqlTreeData<M : SqlBaseMetaTable<T>, T : Serializable>(
    var baseQuery: SqlQueryClip<M, T>,
    var pidValue: Serializable,
    var idColumnName: String,
    var pidColumn: SqlColumnName
) {

    var list = mutableListOf<JsonMap>();

    init {
        var pids = listOf(pidValue)

        while (true) {
            var entitys = loadSubsFromDb(pids);
            if (entitys.any() == false) {
                break;
            }

            pids = entitys.map { it.get(idColumnName) as Serializable }
            list.addAll(entitys);
        }
    }

    fun toList(): List<T> {
        var clazz = baseQuery.mainEntity.entityClass;
        return list.map { it.ConvertType(clazz) as T }
    }

    fun toTreeJson(
        childrenFieldName: String = "children"
    ): List<JsonMap> {
        return getChildren(pidValue, childrenFieldName);
    }

    private fun getChildren(pidValue: Serializable, childrenFieldName: String = "children"): List<JsonMap> {
        var level0s = list.filter { it.get(pidColumn.getAliasName()).AsString() == pidValue.AsString() }

        level0s.forEach {
            it.set(childrenFieldName, getChildren(it.get(idColumnName) as Serializable))
        }
        return level0s;
    }

    private fun loadSubsFromDb(pidValue: Collection<Serializable>): MutableList<JsonMap> {
        var ret = baseQuery.CloneObject()
        ret.where { pidColumn match_in pidValue.toTypedArray() }
        return ret.toMapList()
    }
}