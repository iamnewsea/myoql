package nbcp.db

import nbcp.comm.HasValue
import nbcp.db.mongo.MongoEntityCollector
import java.io.Serializable


abstract class BaseMetaData constructor(
        var tableName: String) : Serializable {
}