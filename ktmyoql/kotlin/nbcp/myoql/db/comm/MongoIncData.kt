package nbcp.myoql.db.comm

import nbcp.myoql.db.mongo.base.MongoColumnName


data class DbIncData(
        var column: String,
        var incValue: Number = 1)


infix fun MongoColumnName.op_inc(value: Number): DbIncData {
    return DbIncData(this.toString(), value)
}
