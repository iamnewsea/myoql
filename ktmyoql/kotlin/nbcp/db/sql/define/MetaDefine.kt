package nbcp.db.sql

import java.util.*

/**
 * Created by yuxh on 2018/6/12
 */

/**
 * 外键定义
 */
data class FkDefine @JvmOverloads constructor(
        var table: String = "",
        var column: String = "",
        var refTable: String = "",
        var refColumn: String = ""
)

/**
 * 表示表的一个唯一键项.
 */
data class UnionKeyDefine @JvmOverloads constructor(
        var table: String = "",
        var unionKeys:  Set<String> = setOf()
)

/**
 * 表示表的一个隔离维度项
 */
data class RegionKeyDefine @JvmOverloads constructor(
        var table: String = "",
        var regionKeys: Set<String> = setOf()
)