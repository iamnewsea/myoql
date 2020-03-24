package nbcp.db.mongo


/**
 * mongo 元数据实体的基类， MongoBaseEntity的基类
 */
abstract class BaseDbEntity(val tableName: String) : java.io.Serializable {
}
