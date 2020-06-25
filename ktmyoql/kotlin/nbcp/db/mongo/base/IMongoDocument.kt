package nbcp.db.mongo

import nbcp.db.Cn
import java.time.LocalDateTime

/**
 * Mongo 实体基类
 */
interface IMongoDocument : java.io.Serializable {
    var id: String  ;

    var createAt: LocalDateTime
    var updateAt: LocalDateTime?
}
