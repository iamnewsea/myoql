package nbcp.db.sql

import java.util.*

/**
 * Created by yuxh on 2018/6/12
 */


data class FkDefine(
        var table: String = "",
        var column: String = "",
        var refTable: String = "",
        var refColumn: String = ""
)

//表示表的一个唯一键项.
data class UnionKeyDefine(
        var table: String = "",
        var unionKeys:  Set<String> = setOf()
)

//表示表的一个隔离维度项
data class RegionKeyDefine(
        var table: String = "",
        var regionKeys: Set<String> = setOf()
)