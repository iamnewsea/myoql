package nbcp.db.es

import java.time.LocalDateTime

/**
 * es 实体基类
 */
abstract class IEsDocument : java.io.Serializable {
    /**
     * 最新版本不支持指定其它字段，只能是 _id , 进库出库注意转换。
     */
    var id: String = "";
    var createAt: LocalDateTime = LocalDateTime.now()
    var updateAt: LocalDateTime? = null
}
