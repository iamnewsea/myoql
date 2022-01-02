package nbcp.db.mongo

import nbcp.comm.*
import org.bson.Document
import java.io.Serializable

/**
 * 每个级别会查询一次。
 */
class MyOqlMongoTreeData<M : MongoBaseMetaCollection<T>, T : Serializable>(
    var baseQuery: MongoQueryClip<M,T>,
    var pidValue: Serializable,
    var idColumnName: String,
    var pidColumn: MongoColumnName
) {

    var list = mutableListOf<Document>();

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
        var clazz = baseQuery.moerEntity.entityClass;
        return list.map { it.ConvertType(clazz) as T }
    }

    fun toTreeJson(
        childrenFieldName: String = "children"
    ): List<Document> {
        return getChildren(pidValue, childrenFieldName);
    }

    private fun getChildren(pidValue: Serializable, childrenFieldName: String = "children"): List<Document> {
        var level0s = list.filter { it.get(pidColumn.toString()) == pidValue }

        level0s.forEach {
            it.set(childrenFieldName, getChildren(it.get(idColumnName) as Serializable))
        }
        return level0s;
    }

    private fun loadSubsFromDb(pidValue: List<Serializable>): List<Document> {
        var ret = baseQuery.CloneObject()
        ret.where { pidColumn match_in pidValue.toTypedArray() }
        return ret.toMapList()
    }
}