package nbcp.db

import nbcp.db.mongo.MongoColumnName


data class DbIncData(
        var column: String,
        var incValue: Number = 1)


infix fun MongoColumnName.op_inc(value: Number): DbIncData {
    return DbIncData(this.toString(), value)
}
