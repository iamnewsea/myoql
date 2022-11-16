package nbcp.myoql.db.sql.base

import nbcp.base.db.annotation.*
import nbcp.base.db.annotation.*
import nbcp.myoql.db.sql.annotation.ConverterValueToDb
import nbcp.myoql.db.sql.define.AutoIdConverter
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
abstract class AutoIdSqlBaseEntity : Serializable {
    var id: String = ""

    @Cn("创建时间")
    var createAt: LocalDateTime = LocalDateTime.now()

    //最后更新时间
    @Cn("最后更新时间")
    var updateAt: LocalDateTime = LocalDateTime.now()
}
