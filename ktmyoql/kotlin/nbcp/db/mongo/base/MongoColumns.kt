package nbcp.db.mongo

import nbcp.comm.MyString

/**
 * Mongo 多列
 */
class MongoColumns(vararg value: MongoColumnName) : ArrayList<MongoColumnName>() {
    init {
        this.addAll(value)
    }

    infix fun and(other: MongoColumnName): MongoColumns {
        this.add(other);
        return this;
    }
}