package nbcp.db.sql

import nbcp.db.*
import java.io.Serializable
import java.time.LocalDateTime

/**
 * 仅留接口
 */
interface ISqlDbEntity : Serializable {
}


//@DbEntityIndexes(DbEntityIndex("id", unique = true))
//abstract class AutoNumberSqlDbEntity : Serializable {
//
//    var id: Long = 0
//
//    @Cn("创建时间")
//    var createAt: LocalDateTime = LocalDateTime.now()
//}

@DbEntityIndex("id", unique = true)
@ConverterValueToDb("id", AutoIdConverter::class)
abstract class AutoIdSqlDbEntity : Serializable {
    var id: String = ""

    @Cn("创建时间")
    var createAt: LocalDateTime = LocalDateTime.now()
}
