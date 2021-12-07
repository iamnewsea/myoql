package nbcp.db

import nbcp.comm.HasValue
import nbcp.db.mongo.MongoEntityCollector
import java.io.Serializable


abstract class BaseMetaData @JvmOverloads constructor(
        var tableName: String,
        /**
         * 动态库使用
         */
        var databaseId: String = "") : Serializable {

}