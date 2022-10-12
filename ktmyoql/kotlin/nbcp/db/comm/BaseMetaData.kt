package nbcp.db

import nbcp.comm.HasValue
import nbcp.db.mongo.MongoEntityCollector
import nbcp.utils.MyUtil
import java.io.Serializable


abstract class BaseMetaData<T> @JvmOverloads constructor(

    val entityClass: Class<T>,
    var tableName: String = "",
    var databaseId: String = ""
) : Serializable {
    init {
        if (this.tableName.isEmpty()) {
            this.tableName = entityClass.simpleName;
        }
    }

    val actualTableName by lazy {
        db.mongo.mongoEvents.getActualTableName(tableName);
    }
}