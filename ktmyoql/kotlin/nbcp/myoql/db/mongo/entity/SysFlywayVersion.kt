package nbcp.myoql.db.mongo.entity

import nbcp.base.db.annotation.Cn
import nbcp.base.db.annotation.DbEntityGroup
import nbcp.myoql.db.BaseEntity
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document
@DbEntityGroup("MongoBase")
@Cn("数据版本")
open class SysFlywayVersion @JvmOverloads constructor(
        @Cn("版本")
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
) : BaseEntity()