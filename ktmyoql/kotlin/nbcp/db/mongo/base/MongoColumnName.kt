package nbcp.db.mongo

import nbcp.comm.AsString
import nbcp.comm.MyString
import nbcp.db.db
import org.springframework.data.mongodb.core.query.Criteria

/**
 * Mongo列
 */
open class MongoColumnName @JvmOverloads constructor(_mongo_value: String = "") : MyString(_mongo_value) {

//    val asc: MongoOrderBy
//        get() = MongoOrderBy(true, this)
//
//    val desc: MongoOrderBy
//        get() = MongoOrderBy(false, this)


    fun match(to: Any?): Criteria {
        val (key, toValue) = db.mongo.proc_mongo_key_value(this, to);
        return Criteria.where(key.AsString("\$eq")).`is`(toValue);// Pair<String, T>(this, to);
    }

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