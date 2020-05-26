package nbcp.db

import nbcp.comm.*
import nbcp.db.sql.*
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Service
import nbcp.utils.*
import org.springframework.context.annotation.DependsOn

@Service
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@DependsOn("springUtil")
class ProxyDataCache4Sql : IProxyCache4Sql {
    private val redisCache by lazy {
        return@lazy SpringUtil.getBeanByName<IDataCache4Sql>("redis")
    }


    override fun isEnable(): Boolean {
        return false; //(requestCache?.isEnable() ?: false) || (redisCache.isEnable() ?: false)
    }


    fun getFromTables(sql: String): Set<String> {
        return """\bfrom\b\s*([^\s]+\s*\.\s*)?([^\s]+)"""
                .toRegex(setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
                .findAll(sql, 0)
                .map {
                    //最后一个有值的
                    for (i in (it.groupValues.size - 1) downTo 1) {
                        var item = it.groupValues[i];
                        if (item.isEmpty()) {
                            continue
                        }

                        return@map item.toLowerCase();
                    }
                    return@map ""
                }
                .map {
                    //可能是   `table`
                    var quoted = (it.startsWith("`") && it.endsWith("`")) ||
                            (it.startsWith("\"") && it.endsWith("\"")) ||
                            (it.startsWith("[") && it.endsWith("]"))

                    if (quoted) {
                        return@map it.Slice(1, -1)
                    }

                    return@map it
                }
                .toSortedSet()
    }

    fun getJoinTables(sql: String): Set<String> {
        return """\bjoin\s+([^\s]+\s*\.\s*)?([^\s]+)"""
                .toRegex(setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
                .findAll(sql, 0)
                .map {
                    //最后一个有值的
                    for (i in (it.groupValues.size - 1) downTo 1) {
                        var item = it.groupValues[i];
                        if (item.isEmpty()) {
                            continue
                        }

                        return@map item.toLowerCase();
                    }
                    return@map ""
                }
                .map {
                    return@map getUnquoteName(it)
                }
                .toSortedSet()
    }

    fun getFromJoinTables(sql: String): Set<String> {
        var set = hashSetOf<String>()
        set.addAll(getFromTables(sql))
        set.addAll(getJoinTables(sql))
        return set;
    }

    fun getUnquoteName(it: String?): String {
        if (it == null) return "";

        if ((it.startsWith("`") && it.endsWith("`")) ||
                (it.startsWith("\"") && it.endsWith("\"")) ||
                (it.startsWith("[") && it.endsWith("]"))) {
            return it.Slice(1, -1)
        }
        return it;
    }

    fun columnBelongTable(column: String, tableAlias: String): Boolean {
        return true;
    }

    fun getColumnName(column: String): String {
        return column;
    }

    fun getWhereMap(where: WhereSqlSect, rks: Array<Array<String>>, tableAlias: String, parameters: JsonMap): ApiResult<StringMap> {
        var ret = ApiResult.of(StringMap())

        //如果有 or 则, or 前后都不可用. 用 ret.msg = "or" 表示.
        var cur_where: WhereSqlSect? = where;
        while (cur_where != null) {
            if (cur_where.linker == "or") {
                ret.msg = "or"
                return ret
            }
            cur_where = cur_where.next;
        }

        if (where.child != null) {
            return ret;
        }

        if (where.op == "=") {
            var left = where.column
            var right = where.value

            var flatrks = rks.toList().Unwind()
            var columnName = getColumnName(left)
            var value = "";
            if (columnBelongTable(left, tableAlias)) {

                if (flatrks.contains(columnName) == false) {
                    return ret
                }

                value = right;
            }


            if (value.HasValue) {
                if (value.startsWith("{") &&
                        value.endsWith("}")) {
                    ret.data = StringMap(columnName to parameters.get(value.Slice(1, -1)).AsString())
                } else {
                    ret.data = StringMap(columnName to value)
                }
                return ret
            }
        }

        if (where.next != null) {
            ret.data!!.putAll(getWhereMap(where.next!!, rks, tableAlias, parameters).data!!)
        }
        return ret
    }


    override fun getCacheKey(sql: SingleSqlData): CacheKey {
        var analysor = SqlTokenAnalysor().analyse(sql.expression);

        var from = analysor.firstOrNull { it.key == SqlKeyEnum.From } as FromSqlSect?;
        if (from == null) {
            return CacheKey.empty()
        }

        var tableName = getUnquoteName(from.tableName)

        var set = getFromJoinTables(sql.expression)
        var md5 = Md5Util.getBase64Md5(sql.expression + "\n" + sql.values.ToJson())

        var ret = CacheKey(CacheKeyTypeEnum.Normal, md5, tableName, set)

        //from 子查询 将忽略掉主键,隔离键 .

        var where = analysor.firstOrNull { it.key == SqlKeyEnum.Where } as WhereSqlSect?

        if (where == null) {
            return ret;
        }

        var dbEntity = db.sql.getSqlEntity?.invoke(tableName)
        if (dbEntity == null) {
            return ret
        }

        var dbTable = (dbEntity as SqlBaseMetaTable<*>)

        var rks = dbTable.getRks()
        var uks = dbTable.getUks()

        var tableAlias = getUnquoteName(from.alias.AsString(from.tableName))

        var rksValue = getWhereMap(where, rks, tableAlias, sql.values).data!!
        var uksValue = getWhereMap(where, uks, tableAlias, sql.values).data!!

        //如果键是全的.
        var rksValid = rks.any { it.intersect(rksValue.keys).size == it.size }
        var uksValid = uks.any { it.intersect(uksValue.keys).size == it.size }

        if (rksValid && uksValid) {
            ret.key = CacheKeyTypeEnum.UnionReginKey
            ret.whereJson.putAll(uksValue)
            ret.whereJson.putAll(rksValue)

        } else if (rksValid) {
            ret.key = CacheKeyTypeEnum.RegionKey
            ret.whereJson.putAll(rksValue)
        } else if (uksValid) {
            ret.key = CacheKeyTypeEnum.UnionKey
            ret.whereJson.putAll(uksValue)

        }

        return ret
    }

//    fun getQueryMainTable(sql: SingleSqlData): String {
//        var select = (CCJSqlParserManager().parse(StringReader(sql.expression)) as Select).selectBody as PlainSelect
//        return getUnquoteName((select.fromItem as Table).name)
//    }

    override fun getCacheJson(cacheKey: CacheKey): String {
        var ret = redisCache.getCacheJson(cacheKey) ?: "";
        if (ret.HasValue) return ret;

        return "";
    }

    override fun setCacheJson(cacheKey: CacheKey, cacheJson: String) {
        redisCache.setCacheJson(cacheKey, cacheJson);
    }


    //---------------------------------------

    fun getUrkInfo(dbTable: SqlBaseMetaTable<*>, tableAlias: String, where: WhereSqlSect, parameters: JsonMap): UrkInfo {
        var rks = dbTable.getRks()
        var uks = dbTable.getUks()

        var rksValue = getWhereMap(where, rks, tableAlias, parameters).data!!
        var uksValue = getWhereMap(where, uks, tableAlias, parameters).data!!

        //如果键是全的.
        var rksValid = rks.any { it.intersect(rksValue.keys).size == it.size }
        var uksValid = uks.any { it.intersect(uksValue.keys).size == it.size }

        return UrkInfo(rksValue, uksValue, rksValid, uksValid)
    }

    /** 1
     * 移除缓存,
     * 再启一个服务,遍历 brokeTables 清除缓存
     */
    fun getUpdateDeleteById_BrokeCache(tableName: String, idJson: StringMap): Set<String> {
//        rer.publish2brokeTables(tableName)

        var valuePattern = idJson.toSortedMap().map { "${it.key}=${it.value}" }.joinToString("&")
        if (valuePattern.HasValue) {
            valuePattern = "&" + valuePattern + "&*"
        }

        var ret = hashSetOf<String>()
        ret.add("rk*-${tableName}-*")
        ret.add("uk*-${tableName}-*" + valuePattern)
        ret.add("urk*-${tableName}-*" + valuePattern)
        ret.add("sql*-${tableName}-*")
        return ret;
    }

    //2
    fun getUpdateByRegion_BrokeCache(tableName: String, regionValue: StringMap): Set<String> {
        var valuePattern = regionValue.toSortedMap().map { "${it.key}=${it.value}" }.joinToString("&")
        if (valuePattern.HasValue) {
            valuePattern = "&" + valuePattern + "&*"
        }

        var ret = hashSetOf<String>()
        ret.add("uk*-${tableName}-*")
        ret.add("rk*-${tableName}-*" + valuePattern)
        ret.add("urk*-${tableName}-*" + valuePattern)
        ret.add("sql*-${tableName}-*")
        return ret;
    }

    //3
    fun getDeleteByRegion_BrokeCache(tableName: String, regionValue: StringMap): Set<String> {
        var valuePattern = regionValue.toSortedMap().map { "${it.key}=${it.value}" }.joinToString("&")

        if (valuePattern.HasValue) {
            valuePattern = "&" + valuePattern + "&*"
        }

        var ret = hashSetOf<String>()
        ret.add("uk*-${tableName}-*")
        ret.add("rk*-${tableName}-*" + valuePattern)
        ret.add("urk*-${tableName}-*" + valuePattern)
        ret.add("sql*-${tableName}-*")
        return ret;
    }

    //4
    fun getUpdateDeleteByReginUnion_BrokeCache(tableName: String, regionValue: StringMap, idValue: StringMap): Set<String> {
        var regionPattern = regionValue.toSortedMap().map { "${it.key}=${it.value}" }.joinToString("&")
        var idPattern = idValue.toSortedMap().map { "${it.key}=${it.value}" }.joinToString("&")

        if (regionPattern.HasValue) {
            regionPattern = "&" + regionPattern + "&*"
        }

        if (idPattern.HasValue) {
            idPattern = "&" + idPattern + "&*"
        }

        var ret = hashSetOf<String>()
        ret.add("rk*-${tableName}-*" + regionPattern)
        ret.add("uk*-${tableName}-*" + idPattern)
        ret.add("urk*-${tableName}-*" + idPattern)
        ret.add("sql*-${tableName}-*")
        return ret
    }

    //5
    fun getUpdateDelete_BrokeCache(tableName: String): Set<String> {
        var ret = hashSetOf<String>()
        ret.add("uk*-${tableName}-*")
        ret.add("rk*-${tableName}-*")
        ret.add("urk*-${tableName}-*")
        ret.add("sql*-${tableName}-*")
        return ret;
    }

    fun getUpdateTable(sql: String): Set<String> {
        return """\bfrom\b\s*([^\s]+\s*\.\s*)?([^\s]+)"""
                .toRegex(setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
                .findAll(sql, 0)
                .map {
                    //最后一个有值的
                    for (i in (it.groupValues.size - 1) downTo 1) {
                        var item = it.groupValues[i];
                        if (item.isEmpty()) {
                            continue
                        }

                        return@map item.toLowerCase();
                    }
                    return@map ""
                }
                .map {
                    //可能是   `table`
                    var quoted = (it.startsWith("`") && it.endsWith("`")) ||
                            (it.startsWith("\"") && it.endsWith("\"")) ||
                            (it.startsWith("[") && it.endsWith("]"))

                    if (quoted) {
                        return@map it.Slice(1, -1)
                    }

                    return@map it
                }
                .toSortedSet()
    }

    override fun updated4BrokeCache(sql: SingleSqlData) {

        //由于 update from join 会出错， 暂时先返回空。
        if (isEnable() == false) return;

        var analysor = SqlTokenAnalysor().analyse(sql.expression)
        var update = (analysor.first { it.key == SqlKeyEnum.Update } as UpdateSqlSect);
        var where = (analysor.first { it.key == SqlKeyEnum.Where } as WhereSqlSect);
        var tableName = update.tableName;


        var tableAlias = update.alias;


        var dbEntity = db.sql.getSqlEntity?.invoke(tableName)
        if (dbEntity == null) {
            return
        }

        var urkInfo = getUrkInfo(
                dbEntity as SqlBaseMetaTable<*>,
                getUnquoteName(tableAlias.AsString(tableName)),
                where,
                sql.values)


        var set = setOf<String>()
        if (urkInfo.rksValid && urkInfo.uksValid) {
            set = getUpdateDeleteByReginUnion_BrokeCache(tableName, urkInfo.rks, urkInfo.uks)
        } else if (urkInfo.rksValid) {
            set = getUpdateByRegion_BrokeCache(tableName, urkInfo.rks)
        } else if (urkInfo.uksValid) {
            set = getUpdateDeleteById_BrokeCache(tableName, urkInfo.uks)
        } else {
            set = getUpdateDelete_BrokeCache(tableName)
        }


        var cacheSeconds = this.redisCache.getCacheSeconds(tableName) ?: 0
        if (cacheSeconds > 0) {
            redisCache.brokeCache(tableName, set);
        }
    }

    override fun delete4BrokeCache(sql: SingleSqlData) {
        if (isEnable() == false) return;
        var analysor = SqlTokenAnalysor().analyse(sql.expression)
        var delete = (analysor.first { it.key == SqlKeyEnum.Delete } as DeleteSqlSect);
        var where = (analysor.first { it.key == SqlKeyEnum.Where } as WhereSqlSect);
        var tableName = delete.tableName;
        var tableAlias = delete.alias;

        var dbEntity = db.sql.getSqlEntity?.invoke(tableName)
        if (dbEntity == null) {
            return
        }

        var urkInfo = getUrkInfo(
                dbEntity as SqlBaseMetaTable<*>,
                tableAlias,
                where,
                sql.values)

        var set = setOf<String>()
        if (urkInfo.rksValid && urkInfo.uksValid) {
            set = getUpdateDeleteByReginUnion_BrokeCache(tableName, urkInfo.rks, urkInfo.uks)
        } else if (urkInfo.rksValid) {
            set = getDeleteByRegion_BrokeCache(tableName, urkInfo.rks)
        } else if (urkInfo.uksValid) {
            set = getUpdateDeleteById_BrokeCache(tableName, urkInfo.uks)
        } else {
            set = getUpdateDelete_BrokeCache(tableName)
        }


        var cacheSeconds = this.redisCache.getCacheSeconds(tableName) ?: 0
        if (cacheSeconds > 0) {
            redisCache.brokeCache(tableName, set);
        }
    }

    override fun insert4BrokeCache(sql: SingleSqlData) {
        if (isEnable() == false) return;

        var analysor = SqlTokenAnalysor().analyse(sql.expression)
        var insert = (analysor.first { it.key == SqlKeyEnum.Insert } as InsertSqlSect);
        var where = (analysor.first { it.key == SqlKeyEnum.Where } as WhereSqlSect);
        var tableName = insert.tableName;

        var dbEntity = db.sql.getSqlEntity?.invoke(tableName)
        if (dbEntity == null) {
            return
        }

        var dbTable = dbEntity as SqlBaseMetaTable<*>

        var rks = dbTable.getRks()

        var rksValue = JsonMap(* rks.toList().Unwind().map { it to sql.values.getOrDefault(it, null) }.filter { it.second != null }.toTypedArray())

        var set = hashSetOf<String>()
        rks.forEach { group ->
            var regionValue = rksValue.filterKeys { group.contains(it) }
            if (group.size == regionValue.size) {
                //该组合法.
                var valuePattern = regionValue.toSortedMap().map { "${it.key}=${it.value}" }.joinToString("&")

                if (valuePattern.HasValue) {
                    valuePattern = "&" + valuePattern + "&*"
                }
                set.add("rk*-${tableName}-*${valuePattern}")
            }
        }
        set.add("sql*-${tableName}-*")


        var cacheSeconds = this.redisCache.getCacheSeconds(tableName) ?: 0
        if (cacheSeconds > 0) {
            redisCache.brokeCache(tableName, set);
        }
    }

    override fun insertMany4BrokeCache(tableName: String) {
        if (isEnable() == false) return;
        var set = mutableSetOf<String>()
        set.add("rk*-${tableName}-*")
        set.add("sql*-${tableName}-*")


        var cacheSeconds = this.redisCache.getCacheSeconds(tableName) ?: 0
        if (cacheSeconds > 0) {
            redisCache.brokeCache(tableName, set);
        }
    }

    override fun insertSelect4BrokeCache(tableName: String) {
        if (isEnable() == false) return;
        var set = setOf("rk*-${tableName}-*", "sql*-${tableName}-*")

        var cacheSeconds = this.redisCache.getCacheSeconds(tableName) ?: 0
        if (cacheSeconds > 0) {
            redisCache.brokeCache(tableName, set);
        }
    }

    override fun clear(tableName: String) {
        redisCache.clear(tableName);
    }

}