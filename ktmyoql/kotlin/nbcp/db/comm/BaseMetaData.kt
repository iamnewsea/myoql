package nbcp.db

import nbcp.comm.HasValue
import nbcp.db.mongo.MongoEntityCollector
import java.io.Serializable


abstract class BaseMetaData @JvmOverloads constructor(
    var tableName: String,
    var databaseId: String = ""
) :
    Serializable {

    val actualTableName by lazy {
        db.mongo.mongoEvents.getActualTableName(tableName);
    }
}