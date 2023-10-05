package nbcp.myoql.db.es.base

import nbcp.base.comm.MyString
import nbcp.myoql.db.es.component.WhereData

/**
 * es 列
 */
open class EsColumnName @JvmOverloads constructor(_es_value: String = "") : MyString(_es_value) {

    infix fun term(target: Any?): WhereData {
        return WhereData.esEquals(this.toString(),target)
    }


    infix fun match(target: Any?): WhereData {
        return WhereData.esMatch(this.toString(),target)
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