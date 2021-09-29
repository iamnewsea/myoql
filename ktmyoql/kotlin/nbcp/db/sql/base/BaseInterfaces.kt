package nbcp.db.sql

import nbcp.db.Cn
import nbcp.db.DbKey
import org.springframework.data.annotation.Id
import java.time.LocalDateTime

/**
 * 仅留接口
 */
interface ISqlDbEntity : java.io.Serializable {
}


abstract class AutoNumberSqlDbEntity : java.io.Serializable {
    @DbKey
    @ConverterValueToDb(AutoIdConverter::class)
    var id: Long = 0

    @Cn("创建时间")
    var createAt: LocalDateTime = LocalDateTime.now()
}

abstract class AutoIdSqlDbEntity : java.io.Serializable {
    @DbKey
    @ConverterValueToDb(AutoIdConverter::class)
    var id: String = ""

    @Cn("创建时间")
    var createAt: LocalDateTime = LocalDateTime.now()
}
