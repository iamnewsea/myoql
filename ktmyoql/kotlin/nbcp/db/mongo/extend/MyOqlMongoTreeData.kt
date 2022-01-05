package nbcp.db.mongo

import nbcp.comm.*
import nbcp.db.db
import org.bson.Document
import java.io.Serializable

/**
 * 每个级别会查询一次。
 */
class MyOqlMongoTreeData<M : MongoBaseMetaCollection<T>, T : Any>(
    var baseQuery: MongoQueryClip<M, T>,
    var idColumnName: String,
    var pidColumn: MongoColumnName,
    var pidValue: Any,
    var childrenFieldName: String = "children"
) {
    var list = mutableListOf<Document>();


//    fun toList(): List<T> {
//        var clazz = baseQuery.moerEntity.entityClass;
//        return list.map { it.ConvertType(clazz) as T }
//    }

    fun toTreeJson(): List<Document> {
        var pids = listOf(pidValue)

        while (true) {
            var entitys = loadSubsFromDb(pids);
            if (entitys.any() == false) {
                break;
            }

            pids = entitys.map { it.getValueByWbsPath(idColumnName) as Serializable }
            list.addAll(entitys);
        }

        return getChildren(pidValue, childrenFieldName);
    }

    private fun getChildren(pidValue: Any, childrenFieldName: String = "children"): List<Document> {
        var level0s =
            list.filter { it.getValueByWbsPath(db.mongo.getEntityColumnName(pidColumn.toString())) == pidValue }

        level0s.forEach {
            it.set(childrenFieldName, getChildren(it.getValue(idColumnName) as Any))
        }
        return level0s;
    }

    private fun loadSubsFromDb(pidValue: List<Any>): List<Document> {
        var ret = baseQuery.CloneObject()
        ret.where { pidColumn match_in pidValue.toTypedArray() }
        return ret.toMapList()
    }
}