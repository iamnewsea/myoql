package nbcp.myoql.db.mongo.base

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