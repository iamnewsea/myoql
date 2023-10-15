package nbcp.base.db

import nbcp.base.db.annotation.Cn
import nbcp.base.extend.*;
import java.io.Serializable

/**
 * 表示Mongo数据库里 Id，Url 的附件实体引用。
 * 保存到Mongo数据库的时候，使用 field Json，无Host。
 * 返回到Mvc前端的时候，使用 get method Json，带Host。
 */
abstract class BaseUrlModel() : Serializable {
    @Cn("网络资源地址")
    var url: String = ""
    override fun toString(): String {
        return this.ToJson().Slice(1, -1)
    }
}