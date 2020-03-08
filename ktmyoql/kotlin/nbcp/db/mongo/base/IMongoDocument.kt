package nbcp.db.mongo

import java.time.LocalDateTime

/**
 * Mongo 实体基类
 */
abstract class IMongoDocument : java.io.Serializable {
    var id: String = "";
    var createAt: LocalDateTime = LocalDateTime.now()
    var updateAt: LocalDateTime? = null
}
