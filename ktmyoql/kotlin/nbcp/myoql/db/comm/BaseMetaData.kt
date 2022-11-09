package nbcp.myoql.db.comm

import nbcp.myoql.db.db
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