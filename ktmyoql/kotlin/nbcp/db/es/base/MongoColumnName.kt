package nbcp.db.es

import nbcp.comm.MyString

/**
 * es 列
 */
open class EsColumnName @JvmOverloads constructor(value: String = "") : MyString(value) {

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

    infix fun and (other: EsColumnName) :EsColumns{
        var ret= EsColumns()
        ret.add(this);
        ret.add(other);
        return ret;
    }
}