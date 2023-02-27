package nbcp.myoql.db.mongo.entity

import nbcp.base.db.*
import nbcp.base.db.annotation.*
import nbcp.myoql.db.BaseEntity
import org.springframework.data.mongodb.core.mapping.Document

//--------------------------------------------------------


@Document
@DbEntityGroup("MongoBase")
@Cn("排序记录号")
data class SysLastSortNumber(
    @Cn("表名")
    var table: String = "",
    @Cn("组")
    var group: String = "",
    @Cn("值")
    var value: Float = 0F,
) : BaseEntity()
