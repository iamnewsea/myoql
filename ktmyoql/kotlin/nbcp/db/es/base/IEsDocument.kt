package nbcp.db.es

import java.time.LocalDateTime

/**
 * es 实体基类
 */
abstract class IEsDocument : java.io.Serializable {
    /**
     * 使用mapping "_id":{"path":"id"}，指定主键是 id
     */
    var id: String = "";
    var createAt: LocalDateTime = LocalDateTime.now()
    var updateAt: LocalDateTime? = null
}
