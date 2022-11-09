package nbcp.myoql.db.sql


import nbcp.base.extend.Slice
import nbcp.base.extend.Tokenizer
import nbcp.myoql.db.sql.define.*

class SqlTokenAnalysor() {
    var sqlwords = arrayOf("select", "update", "insert", "delete", "from", "join", "where", "group",
            "order", "having", "limit", "offset", "with", "call", "into", "values", "set", "union")

    fun analyse(sql: String): List<SqlBaseSect> {
        val list = mutableListOf<SqlBaseSect>()
        val expressions = sql.replace("\r\n", " ").replace("\n", " ").trim().Tokenizer()

        val keyIndexs = mutableListOf<Int>()
        expressions.forEachIndexed { index, item ->
            if (sqlwords.any { it.equals(item, true) }) {
                keyIndexs.add(index);
            }
        }

        var prevIndex = 0;
        keyIndexs.forEach { index ->
            if (index == 0) {
                prevIndex = 0;
            }

            if (prevIndex >= index) {
                return@forEach
            }

            list.add(getSect(expressions.Slice(prevIndex, index)))
            prevIndex = index;
        }

        if (prevIndex < expressions.size) {
            list.add(getSect(expressions.Slice(prevIndex, expressions.size)))
        }

        return list;
    }

    private fun getSect(sqls: Collection<String>): SqlBaseSect {
//        if( sqls.isEmpty()){
//        }
        val type = sqls.first().lowercase()
        val exps = sqls.Slice(1)
        when (type) {
            "select" -> return toSelect(exps);
            "update" -> return toUpdate(exps);
            "insert" -> return toInsert(exps);
            "delete" -> return toDelete(exps);
            "from" -> return toFrom(exps);
            "join" -> return toJoin(exps);
            "where" -> return toWhere(exps);
            "group" -> return toGroup(exps);
            "order" -> return toOrder(exps);
            "having" -> return toHaving(exps);
            "limit" -> return toLimit(exps);
            "offset" -> return toOffset(exps);
            "with" -> return toWith(exps);
            "call" -> return toCall(exps);
            "into" -> return toInto(exps);
            "values" -> return toValues(exps);
            "set" -> return toSet(exps);
            "union" -> return toUnion(exps);
        }
        throw RuntimeException("不识别的Sql！" + exps.joinToString(","))
    }

    private fun toSelect(sqls: Collection<String>): SqlBaseSect {
        val ret = SelectSqlSect(sqls.joinToString(" "));
        return ret
    }

    private fun toUpdate(sqls: Collection<String>): SqlBaseSect {
        val ret = UpdateSqlSect(sqls.joinToString(" "));
        return ret
    }

    private fun toInsert(sqls: Collection<String>): SqlBaseSect {
        val ret = InsertSqlSect(sqls.joinToString(" "));
        return ret
    }

    private fun toDelete(sqls: Collection<String>): SqlBaseSect {
        val ret = DeleteSqlSect(sqls.joinToString(" "));
        return ret
    }

    private fun toFrom(sqls: Collection<String>): SqlBaseSect {
        val ret = FromSqlSect(sqls.joinToString(" "));
        return ret
    }

    private fun toJoin(sqls: Collection<String>): SqlBaseSect {
        val ret = JoinSqlSect(sqls.joinToString(" "));
        return ret
    }

    private fun toWhere(sqls: Collection<String>): SqlBaseSect {
        val ret = WhereSqlSect(sqls.joinToString(" "));
        return ret
    }

    private fun toGroup(sqls: Collection<String>): SqlBaseSect {
        val ret = GroupBySqlSect(sqls.joinToString(" "));
        return ret
    }

    private fun toOrder(sqls: Collection<String>): SqlBaseSect {
        val ret = OrderBySqlSect(sqls.joinToString(" "));
        return ret
    }

    private fun toHaving(sqls: Collection<String>): SqlBaseSect {
        val ret = HavingSqlSect(sqls.joinToString(" "));
        return ret
    }

    private fun toLimit(sqls: Collection<String>): SqlBaseSect {
        val ret = LimitSqlSect(sqls.joinToString(" "));
        return ret
    }

    private fun toOffset(sqls: Collection<String>): SqlBaseSect {
        val ret = OffsetSqlSect(sqls.joinToString(" "));
        return ret
    }

    private fun toWith(sqls: Collection<String>): SqlBaseSect {
        val ret = WithSqlSect(sqls.joinToString(" "));
        return ret
    }

    private fun toCall(sqls: Collection<String>): SqlBaseSect {
        val ret = CallSqlSect(sqls.joinToString(" "));
        return ret
    }

    private fun toInto(sqls: Collection<String>): SqlBaseSect {
        val ret = IntoSqlSect(sqls.joinToString(" "));
        return ret
    }

    private fun toValues(sqls: Collection<String>): SqlBaseSect {
        val ret = ValuesSqlSect(sqls.joinToString(" "));
        return ret
    }

    private fun toSet(sqls: Collection<String>): SqlBaseSect {
        val ret = SetSqlSect(sqls.joinToString(" "));
        return ret
    }

    private fun toUnion(sqls: Collection<String>): SqlBaseSect {
        val ret = UnionSqlSect(sqls.joinToString(" "));
        return ret
    }

}