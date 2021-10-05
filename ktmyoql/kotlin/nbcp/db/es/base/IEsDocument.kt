package nbcp.db.es

import java.time.LocalDateTime
import java.io.Serializable

/**
 * es 实体基类
 */
interface IEsDocument :  Serializable {
    /**
     * 最新版本不支持指定其它字段，只能是 _id , 进库出库注意转换。
     */
    var id: String  ;
    var createAt: LocalDateTime
    var updateAt: LocalDateTime?
}
