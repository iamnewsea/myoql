package nbcp.myoql.db.es.base

import nbcp.base.comm.JsonMap
import nbcp.base.comm.MyString
import nbcp.myoql.db.db
import nbcp.myoql.db.es.component.WhereData

/**
 * es 列
 */
open class EsColumnName @JvmOverloads constructor(_es_value: String = "") : MyString(_es_value) {

    infix fun term(target: Any?): WhereData {
        var target = db.es.proc_es_value(target);

        return WhereData("term" to JsonMap(
            this.toString() to target
        )
        )
    }


    infix fun match(target: Any?): WhereData {
        var target = db.es.proc_es_value(target);

        return WhereData("match" to JsonMap(
            this.toString() to target
        )
        )
    }










//    val asc: EsOrderBy
//        get() = EsOrderBy(true, this)
//
//    val desc: EsOrderBy
//        get() = EsOrderBy(false, this)

    operator fun plus(value: String): EsColumnName {
        return EsColumnName(this.toString() + value)
    }

    operator fun plus(value: EsColumnName): EsColumnName {
        return EsColumnName(this.toString() + value.toString())
    }

    fun slice(startIndex: IntRange): EsColumnName {
        return EsColumnName(this.toString().slice(startIndex))
    }

    infix fun and (other: EsColumnName) : EsColumns {
        val ret= EsColumns()
        ret.add(this);
        ret.add(other);
        return ret;
    }
}