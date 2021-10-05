package nbcp.db.sql

import nbcp.comm.*
import nbcp.db.ITreeData
import java.io.Serializable

class MyOqlTreeData<M : SqlBaseMetaTable<T>, T : Serializable>(
    var metaTable: M,
    var pidValue: Serializable,
    var idColumn: SqlColumnName,
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

            pids = entitys.map { it.get(idColumn.name) as Serializable }
            list.addAll(entitys);
        }
    }

    fun toList(): List<T> {
        var clazz = metaTable.tableClass;
        return list.map { it.ConvertType(clazz) as T }
    }

    fun toTree(
        childrenFieldName: String = "children"
    ): List<JsonMap> {
        return getChildren(pidValue.toString(), childrenFieldName);
    }

    private fun getChildren(pidValue: String, childrenFieldName: String = "children"): List<JsonMap> {
        var level0s = list.filter { it.getStringValue(pidColumn.name) == pidValue }

        level0s.forEach {
            it.set(childrenFieldName, getChildren(it.getStringValue(idColumn.name).AsString()))
        }
        return level0s;
    }

    private fun loadSubsFromDb(pidValue: List<Serializable>): MutableList<JsonMap> {
        var ret = SqlQueryClip(metaTable);
        ret.where { pidColumn match_in pidValue.toTypedArray() }
        return ret.toMapList()
    }
}