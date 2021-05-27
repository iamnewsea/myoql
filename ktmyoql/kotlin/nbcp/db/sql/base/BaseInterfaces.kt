package nbcp.db.sql

import nbcp.db.Cn
import nbcp.db.DbKey
import org.springframework.data.annotation.Id
import java.time.LocalDateTime

/**
 * 为了扩展性， id = 程序生成的唯一Id
 */
interface ISqlDbEntity : java.io.Serializable {
}


abstract class AutoNumberSqlDbEntity : ISqlDbEntity {
    @DbKey
    @ConverterValueToDb(AutoNumberConverter::class)
    var id: Long = 0

    @Cn("创建时间")
    var createAt: LocalDateTime = LocalDateTime.now()
}

abstract class AutoIdSqlDbEntity : ISqlDbEntity {
    @DbKey
    @ConverterValueToDb(AutoIdConverter::class)
    var id: String = ""

    @Cn("创建时间")
    var createAt: LocalDateTime = LocalDateTime.now()
}
