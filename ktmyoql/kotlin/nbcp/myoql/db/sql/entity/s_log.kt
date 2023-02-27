package nbcp.myoql.db.sql.entity

import nbcp.base.db.IdName
import nbcp.base.db.annotation.Cn
import nbcp.base.db.annotation.DbEntityGroup
import nbcp.myoql.db.sql.annotation.SqlColumnType
import nbcp.myoql.db.sql.annotation.SqlSpreadColumn
import nbcp.myoql.db.sql.base.AutoIdSqlBaseEntity

@DbEntityGroup("SqlBase")
@Cn("日志")
open class s_log @JvmOverloads constructor(
    @Cn("模块名称")
    var module: String = "", //模块

    @Cn("类型")
    var type: String = "",  //类型

    @Cn("标签")
    var tags: List<String> = listOf(),   //实体标志, 查询用： module + key

    @Cn("消息")
    @SqlColumnType("varchar(255)")
    var msg: String = "",   //消息

    @Cn("请求")
    @SqlColumnType("varchar(512)")
    var request: String = "",

    @Cn("数据")
    @SqlColumnType("varchar(512)")
    var data: String = "",

    @Cn("响应")
    @SqlColumnType("varchar(512)")
    var response: String = "",

    @Cn("创建者")
    @SqlSpreadColumn()
    var creator: IdName = IdName()
) : AutoIdSqlBaseEntity()