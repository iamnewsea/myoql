package nbcp.myoql.db.sql.entity

import nbcp.base.db.IdName
import nbcp.base.db.annotation.Cn
import nbcp.base.db.annotation.DbEntityGroup
import nbcp.myoql.db.sql.annotation.SqlColumnType
import nbcp.myoql.db.sql.annotation.SqlSpreadColumn
import nbcp.myoql.db.sql.base.AutoIdSqlBaseEntity
import java.time.LocalDateTime

@DbEntityGroup("SqlBase")
@Cn("数据版本初始化")
open class s_flyway @JvmOverloads constructor(
        var version: Int = 0,
        @Cn("备注")
        var remark: String = "",
        @Cn("执行的类")
        var execClass: String = "",
        @Cn("执行开始时间")
        var startAt: LocalDateTime = LocalDateTime.now(),
        @Cn("执行结束时间")
        var finishAt: LocalDateTime? = null,
        @Cn("是否成功")
        var isSuccess: Boolean = false
) : AutoIdSqlBaseEntity()