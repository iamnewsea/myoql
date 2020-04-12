package nbcp.db.es

import nbcp.comm.*
import nbcp.db.*
import nbcp.db.es.*
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.time.LocalDateTime

open class EsBaseInsertClip(tableName: String) : EsClipBase(tableName), IEsWhereable {
    companion object {
        private val logger by lazy {
            return@lazy LoggerFactory.getLogger(this::class.java)
        }
    }

    var entities = mutableListOf<Any>()

    /**
     * 批量添加中的添加实体。
     */
    fun addEntity(entity: Any) {
        if (entity is IEsDocument) {
            if (entity.id.isEmpty()) {
                entity.id = ObjectId().toString()
            }
            entity.createAt = LocalDateTime.now()
        }
        this.entities.add(entity)
    }

    fun exec(): Int {
        db.affectRowCount = -1;
        var ret = 0;

        var settingResult = db.es.esEvents.onInserting(this)
        if (settingResult.any { it.second.result == false }) {
            return 0;
        }

        var startAt = LocalDateTime.now()
        try {
//            esTemplate.insertAll(entities)
            db.executeTime = LocalDateTime.now() - startAt

            using(OrmLogScope.IgnoreAffectRow) {
                using(OrmLogScope.IgnoreExecuteTime) {
                    settingResult.forEach {
                        it.first.insert(this, it.second)
                    }
                }
            }

            ret = entities.size;
            db.affectRowCount = entities.size
            return db.affectRowCount
        } catch (e: Exception) {
            ret = -1;
            throw e;
        } finally {
            logger.InfoError(ret < 0) {
                """[insert] ${this.collectionName}
${if (db.debug) "[enities.size] ${entities.size}" else "[entities] ${entities.ToJson()}"}
[result] ${ret}
[耗时] ${db.executeTime}
"""
            };
        }

        return ret;
    }
}