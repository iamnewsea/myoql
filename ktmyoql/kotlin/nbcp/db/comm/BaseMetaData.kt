package nbcp.db

import nbcp.comm.HasValue
import nbcp.db.mongo.MongoEntityCollector
import java.io.Serializable


abstract class BaseMetaData @JvmOverloads constructor(
        var defEntityName: String,
        var tableName: String = "",
        var databaseId: String = ""
) : Serializable {
    init {
        if (this.tableName.isEmpty()) {
            this.tableName = defEntityName;
        }
    }

    val actualTableName by lazy {
        db.mongo.mongoEvents.getActualTableName(tableName);
    }
}