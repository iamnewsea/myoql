package nbcp.db.sql

import nbcp.comm.*
import nbcp.db.ITreeData
import java.io.Serializable

class MyOqlTreeData<M : SqlBaseMetaTable<T>, T : Serializable>(
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
        var clazz = baseQuery.mainEntity.tableClass;
        return list.map { it.ConvertType(clazz) as T }
    }

    fun toTreeJson(
        childrenFieldName: String = "children"
    ): List<JsonMap> {
        return getChildren(pidValue, childrenFieldName);
    }

    private fun getChildren(pidValue: Serializable, childrenFieldName: String = "children"): List<JsonMap> {
        var level0s = list.filter { it.get(pidColumn.getAliasName()) == pidValue }

        level0s.forEach {
            it.set(childrenFieldName, getChildren(it.get(idColumnName) as Serializable))
        }
        return level0s;
    }

    private fun loadSubsFromDb(pidValue: List<Serializable>): MutableList<JsonMap> {
        var ret = baseQuery.CloneObject()
        ret.where { pidColumn match_in pidValue.toTypedArray() }
        return ret.toMapList()
    }
}