package nbcp.db

import nbcp.comm.HasValue
import nbcp.db.mongo.MongoEntityCollector
import java.io.Serializable


abstract class BaseMetaData(var tableName: String) : Serializable{

    val actualTableName by lazy {
        db.mongo.mongoEvents.getActualTableName(tableName);
    }
}