package nbcp.db.sql

import nbcp.db.*
import java.io.Serializable
import java.time.LocalDateTime
/**
 * 仅留接口
 */
interface ISqlDbEntity : Serializable {
}


abstract class AutoNumberSqlDbEntity : Serializable {
    @DbKey
    var id: Long = 0

    @Cn("创建时间")
    var createAt: LocalDateTime = LocalDateTime.now()
}

abstract class AutoIdSqlDbEntity : Serializable {
    @DbKey
    var id: String = ""

    @Cn("创建时间")
    var createAt: LocalDateTime = LocalDateTime.now()
}
