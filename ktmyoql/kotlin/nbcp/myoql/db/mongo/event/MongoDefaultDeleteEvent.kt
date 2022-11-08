package nbcp.myoql.db.mongo;

import nbcp.base.comm.*;
import nbcp.base.db.*;
import nbcp.base.enums.*;
import nbcp.base.extend.*;
import nbcp.base.utils.*;
import nbcp.myoql.db.enums.*
import nbcp.myoql.db.*;
import nbcp.myoql.db.comm.*
import nbcp.myoql.db.comm.EventResult
import nbcp.myoql.db.mongo.event.IMongoEntityDelete
import org.bson.Document
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.Serializable


/**
 * 同步处理，删除的数据转移到垃圾箱
 */
@Component
class MongoDefaultDeleteEvent : IMongoEntityDelete {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    override fun beforeDelete(delete: MongoDeleteClip<*>): EventResult {

        return EventResult(true, null)
    }

    override fun delete(delete: MongoDeleteClip<*>, eventData: EventResult) {
        //清缓存
        val cacheGroups = MongoEntityCollector.sysRedisCacheDefines.get(delete.defEntityName)
        if (cacheGroups == null) {
            return;
        }

        if (db.mongo.hasOrClip(delete.whereData)) {
            clearAllCache(delete.actualTableName);
            return;
        }

        cacheGroups.forEach { groupKeys ->
            val groupValue = groupKeys.map {
                return@map it to delete.whereData.findValueFromRootLevel(it).AsString()
            }.filter { it.second.HasValue }
                .toMap();

            if (groupValue.keys.size != groupKeys.size) {
                clearAllCache(delete.actualTableName);
                return@forEach ;
            }


            db.brokeRedisCache(
                table = delete.actualTableName,
                groupKey = groupValue.keys.joinToString(","),
                groupValue = groupValue.values.joinToString(",")
            )
        }
    }

    private fun clearAllCache(actualTableName: String) {
        db.brokeRedisCache(
            table = actualTableName,
            groupKey = "",
            groupValue = ""
        )
    }
}