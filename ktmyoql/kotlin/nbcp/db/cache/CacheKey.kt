package nbcp.db

import nbcp.comm.*

/**
 * 缓存类型
 */
enum class CacheKeyTypeEnum {
    None,
    Normal,
    UnionKey,
    RegionKey,
    UnionReginKey
}

/**
 * 缓存Key
 */
data class CacheKey(
        var key: CacheKeyTypeEnum = CacheKeyTypeEnum.Normal,
        //md5 + chksum
        var md5: String = "",
        //主表。
        var tableName:String = "",
        var dependencies: Set<String> = setOf(),   //缓存依赖
        // 保留字段: 主表主键值. 当且仅当 dependencies.size == 1 时,且查询条件有主键时.多主键 ,用逗号分隔
        //在哪里定义表的主键 ???
        var whereJson: JsonMap = JsonMap() //形如: id=1&name=2&
) {
    companion object {
        fun empty():CacheKey{
            return CacheKey(CacheKeyTypeEnum.None)
        }
    }

    /**
     */
    fun getExpression(): String {
        var tables = dependencies.joinToString("-");
        if (key == CacheKeyTypeEnum.Normal) {
            return "sql-${tables}-${md5}"
        } else if (key == CacheKeyTypeEnum.UnionKey) {
            return "uk-${tables}-&" + whereJson.toSortedMap().map { it.key + "=" + it.value+ "&" }.joinToString("") + "${md5}"
        } else if (key == CacheKeyTypeEnum.RegionKey) {
            return "rk-${tables}-&" + whereJson.toSortedMap().map { it.key + "=" + it.value  + "&"}.joinToString("")+ "${md5}"
        } else if (key == CacheKeyTypeEnum.UnionReginKey) {
            return "urk-${tables}-&" + whereJson.toSortedMap().map { it.key + "=" + it.value + "&"}.joinToString("") + "${md5}"
        }
        return ""
    }


    override fun toString(): String {
        return getExpression()
    }
}
