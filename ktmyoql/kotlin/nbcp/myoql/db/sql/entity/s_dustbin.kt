package nbcp.myoql.db.sql.entity

import nbcp.base.db.IdName
import nbcp.base.db.annotation.*
import nbcp.myoql.db.sql.annotation.SqlColumnType
import nbcp.myoql.db.sql.annotation.SqlSpreadColumn
import nbcp.myoql.db.sql.base.AutoIdSqlBaseEntity


/**
 * 删除数据 前，先把数据放在这里，再删除原数据！
 *
 */
@DbEntityGroup("SqlBase")
@Cn("数据垃圾箱")
open class s_dustbin @JvmOverloads constructor(
    @Cn("表名")
    var table: String = "",

    @Cn("备注")
    var remark: String = "",

    @Cn("操作者")
    @SqlSpreadColumn()
    var creator: IdName = IdName(),

    @Cn("数据")
    @SqlColumnType("varchar(255)")
    var data: String = "",  //保存 JSON 数据
) : AutoIdSqlBaseEntity()