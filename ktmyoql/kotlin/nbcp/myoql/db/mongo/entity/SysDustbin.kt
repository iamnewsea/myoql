package nbcp.myoql.db.mongo.entity

import nbcp.base.db.IdName
import nbcp.base.db.annotation.Cn
import nbcp.base.db.annotation.DbEntityGroup
import nbcp.myoql.db.BaseEntity
import org.springframework.data.mongodb.core.mapping.Document
import java.io.Serializable

//存放删除的数据。
@Document
@DbEntityGroup("MongoBase")
@Cn("数据垃圾箱")
open class SysDustbin @JvmOverloads constructor(
        @Cn("表名")
    var table: String = "",
        @Cn("备注")
    var remark: String = "",
        @Cn("创建者")
    var creator: IdName = IdName(),
        @Cn("数据")
    var data: Serializable? = null
) : BaseEntity()