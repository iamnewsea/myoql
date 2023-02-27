package nbcp.myoql.db.mongo.entity

import nbcp.base.db.annotation.Cn
import nbcp.base.db.annotation.DbEntityGroup
import nbcp.myoql.db.BaseEntity
import nbcp.myoql.db.comm.SortNumber
import org.springframework.data.mongodb.core.mapping.Document

@Document
@DbEntityGroup("MongoBase")
@Cn("字典")
@SortNumber("sort", "", 10)
data class SysDictionary(
    @Cn("所有者")
    var owner: String = "",
    @Cn("组")
    var group: String = "",

    @Cn("键")
    var key: String = "",
    @Cn("值")
    var value: String = "",

    @Cn("备注")
    var remark: String = "",
    @Cn("排序")
    var sort: Float = 0F,
) : BaseEntity()