package nbcp.db.mongo

import nbcp.comm.MyString

/**
 * Mongo列
 */
open class MongoColumnName @JvmOverloads constructor(value: String = "") : MyString(value) {

//    val asc: MongoOrderBy
//        get() = MongoOrderBy(true, this)
//
//    val desc: MongoOrderBy
//        get() = MongoOrderBy(false, this)

    operator fun plus(value: String): MongoColumnName {
        return MongoColumnName(this.toString() + value)
    }

    operator fun plus(value: MongoColumnName): MongoColumnName {
        return MongoColumnName(this.toString() + value.toString())
    }

    fun slice(startIndex: IntRange): MongoColumnName {
        return MongoColumnName(this.toString().slice(startIndex))
    }

    infix fun and(other: MongoColumnName): MongoColumns {
        val ret = MongoColumns()
        ret.add(this);
        ret.add(other);
        return ret;
    }
}