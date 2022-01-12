package nbcp.db.mongo;

import nbcp.comm.AsString
import nbcp.db.*
import nbcp.db.mongo.entity.*
import nbcp.db.mongo.event.*
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

    override fun beforeDelete(delete: MongoDeleteClip<*>, chain: EventChain): EventResult {

        return EventResult(true, null)
    }

    override fun delete(delete: MongoDeleteClip<*>, chain: EventChain, eventData: EventResult) {
        //清缓存
        var clearAll = false;
        val groupKeys = MongoEntityCollector.sysRedisCacheDefines.get(delete.collectionName) ?: arrayOf()
        groupKeys.union(listOf("id"))
                .toSet()
                .forEach { groupKey ->
                    if (clearAll) return@forEach

                    val groupValue = delete.whereData.get(groupKey)
                    if (groupValue != null) {
                        db.brokeRedisCache(
                                table = delete.actualTableName,
                                groupKey = groupKey,
                                groupValue = groupValue.toString()
                        )
                    } else {
                        clearAll = true;
                    }
                }

        if (clearAll) {
            db.brokeRedisCache(
                    table = delete.actualTableName,
                    groupKey = "",
                    groupValue = ""
            )
        }
    }
}